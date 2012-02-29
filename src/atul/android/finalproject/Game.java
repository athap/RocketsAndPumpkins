package atul.android.finalproject;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
* Class Game
* 
* Brief This is the class that handles the game logic. It extends SurfaceView which provides
* 		a dedicated drawing surface. Surface is accessed via getHolder(). 
* 		It also implements SurfaceHolder.callBack interface. By using this the surface being held is 
* 		available only between calls to surfaceCreated and surfaceDestroyed.
* 
* 		Main purpose of this class is to provide a surface which can be rendered by a separate thread.
* 		This way we can update the rendering quickly and user gets a good look and feel
* 		
* 		The Surface held is destroyed when the rocket hits the target or crosses the boundaries
* 
*@note	This class also contains a private class AnimationThread, which handles the game engine 
* 
*/
public class Game extends SurfaceView implements SurfaceHolder.Callback
{
	/**
	 * Represent the default constructor. This constructor is called by the StartGame activity which receives
	 * game parameters set by the player. These parameters are passed here to set the positions of various
	 * game objects
	 * 
	 * @param context - Context is required by the SurfaceView super(context)
	 * @param angle - Launch angle
	 * @param velocity - Launch velocity
	 * @param targetX - X coordinate of the target location
	 * @param targetY - Y coordinate of the target location
	 */
	public Game(Context context, int angle, int velocity, int targetX, int targetY)
	{
		super(context);
		
		// This initializes the sound effects
		initializePlaylist();
		
		// set the value of angle
		m_angle = angle;
		
		// set the value of velocity
		m_velocity = velocity;
		
		// set the value of x co-ordinate
		m_targetX = targetX;
		
		// set the value of y co-ordinate
		m_targetY = targetY;
		
		/*
		* This is rather an important line. This line sets the Game class as a handler for events
		* happening on the actual surface  
		*/
		getHolder().addCallback(this);
		
		/*
		* Create the instance of the thread (game engine) and provide the holder object to it
		* so that it can access the canvas
		*/
		m_thread = new AnimationThread(getHolder(), this);		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) 
	{
		
	}

	/**
	* This is called immediately after the surface is first created. This sets up all the rendering
	* start up needs.
	* 
	* @param holder - Provides access to the surface
	* 
	*/
	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		// Set the rocket position
		setRocket(getHeight());
		
		// Set the target position 
		setTarget();
		
		//to be called after setTarget(). Sets the trigger area
		setTriggerArea();
		
		// Make the thread run condition true
		m_thread.setKeepWorking(true);
		
		// Start the thread
		m_thread.start();
	}

	/**
	*  
	*  This is called immediately before a surface is being destroyed. After returning 
	*  from this call, you should no longer try to access this surface. If you have a rendering thread 
	*  that directly accesses the surface, you must ensure that thread is no longer touching the Surface 
	*  before returning from this function.
	*  
	*  @param holder - Provides access to the surface
	*/
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		boolean retry = true;
		
		// Stops the currently playing sound
		m_musicPlayer.stop(null);
		
		m_thread.setKeepWorking(false);
		
		// Make sure that the rendering thread 
		while(retry)
		{
			try 
			{
				// join the thread so that m_thread join this thread. m_thread dies when this thread dies
				m_thread.join();
				retry = false;
				
			} 
			catch (Exception e) 
			{
				//System.out.println("ERROR IN DESTROY");
			}
		}
	}
	
	/**
	* This method updates the coordinates of the rocket (x, y) of rocket in each frame. Values 
	* depends on the projectile equation  
	*/
	public void update()
	{
			if(m_projectile == null)
				return;
			
			// Get the x coordinate of the projectile
			float x = m_projectile.getX();
			
			// Get the y coordinate of the projectile
			float y = m_projectile.getY();
			
			/*
			* keep track of the maximum height reached by the rocket. Calculations are
			* reversed because rocket starts from  the bottom of the screen where as android
			* calculates the height starting from the top of the screen
			*/
			if(y < m_maxHeight)
				m_maxHeight = y;
			
			/*
			* If condition - 	Check if the rocket has crossed the boundaries. If yes then set the 
			*					result and return
			* 
			* Else condition -  Check if the rocket has enter the trigger area. If yes then set the
			* 					result and return
			* 
			* Trigger area	 -	It is a rectangle surrounding the target. If the rocket enters the
			* 					rectangle, it is counted as a hit	
			*/
			if(outOfBound(x, y))
			{
				// Make the projectile game object null
				m_projectile = null;
				
				// Set the threads run condition to false
				m_thread.setKeepWorking(false);
				
				// Set the results in the parent activity
				((StartGameAct)getContext()).returnResult(x, y, m_maxHeight, false);
				return;
			} 
			else if(m_triggerArea.collider(x, y))
			{
				// Make the projectile game object null
				m_projectile = null;
				
				// Play the sound of blast
				playBlastSound();
				
				// Set the thread run condition to false
				m_thread.setKeepWorking(false);
				
				// Set the results in the parent activity
				((StartGameAct)getContext()).returnResult(x, y, m_maxHeight, true);
				return;
			}
			
			// Calculate the angle in radian
			double o = (Math.PI / 180) * m_angle;
	        
			/*
			* Here the y coordinate is calculates based on the projectile motion equation
			* 
			* Reference - http://en.wikipedia.org/wiki/Projectile_motion
			* 
			* Equation --> y = ( xtan(theta) ) - ( (x^2g) / 2v^2cos^2(theta) )
			* 
			*/
	        m_currentHeight = (float) ((x * Math.tan(o)) - 
	        							( (x * x * 9.8) / 
	        							(2 * m_velocity * m_velocity * Math.pow(Math.cos(o), 2))));
	        
	        // Increase the x co-ordinate
	        m_projectile.setX(x + 5);
	        
	        /*
	        *  Set the y coordinate. Multiplying the m_currentHeight by UNIT (density) so that the
	        *  the m_currentHeight scales properly on other devices.
	        *  
	        *  note - This calculation is not working properly on tablets
	        */
	        m_projectile.setY((getHeight() - (m_currentHeight * UNIT)));	        
	}
	
	/**
	* Here all the drawing is done. The rocket and target (pumpkin) are draw on the canvas by this
	* method
	* 
	* @param canvas - canvas on which we draw
	*/
	@Override
	protected void onDraw(Canvas canvas) 
	{
		canvas.drawColor(Color.BLACK);
		
		if(m_target != null)
			canvas.drawBitmap(m_target.getProjectile(), 
							  m_target.getX(), 
							  m_target.getY(), 
							  null);
				
		if(m_projectile != null)
			canvas.drawBitmap(m_projectile.getProjectile(), 
							  m_projectile.getX(), 
							  m_projectile.getY(), 
							  null);
				
		
	}
	
	// Plays the blast sound
	private void playBlastSound() 
	{
		m_musicPlayer.play(BLAST);
		
		try {
			 Thread.sleep(50);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	* Check if the rocket is out of screen
	* @param x - x coordinate of rocket
	* @param y - y coordinate of rocket
	* 
	* @return - True if the rocket is out of screen otherwise false
	*/
	private boolean outOfBound(float x, float y) 
	{
		if(x < 0 || x > getWidth() || y < 0 || y > getHeight())
			return true;
		
		return false;
	}
	
	/**
	 * Sets a rectangular trigger area around the target. If the rocket enters that trigger area
	 * it is counted as a hit
	 */
	private void setTriggerArea() 
	{
		// Set upper left x of rectangle
		float ulx = m_target.getX() - m_target.getImageWidth() - 5;
		
		// Set upper left y of rectangle
		float uly = m_target.getY() - (m_target.getImageHeight());
		
		// Set lower right x of rectangle
		float lrx = m_target.getX() + (m_target.getImageWidth() / 2);
		
		// Set lower right y of rectangle
		float lry = m_target.getY() + (m_target.getImageHeight());
		
		m_triggerArea = new TriggerArea(new PointF(ulx, uly), new PointF(lrx, lry));
	}
	
	/**
	 * sets the initial position of the rocket
	 * 
	 * @param height - height of the screen. This is the lowest point in the screen( e.g lower left corner)
	 */
	private void setRocket(float height)
	{
		// Create a new instance of Projectile class and set the image and other params
		m_projectile = new Projectile(BitmapFactory.decodeResource(getResources(), R.drawable.rocket));
		m_projectile.setX(0);
		m_projectile.setY(height);
		
		// set initial height to the lowest point in the screen
		m_maxHeight = height;
	}
	
	/**
	 * sets the initial position of the taget
	 */
	private void setTarget() 
	{
		// Create a new instance of Projectile class and set the image and other params
		if(m_target == null)
			m_target = new Projectile(BitmapFactory.decodeResource(getResources(), R.drawable.target));
		m_target.setX(m_targetX);
		m_target.setY(m_targetY);	
	}
	
	/**
	* Initialize the play list so that sound effects can be played
	*  
	*/
	private void initializePlaylist() 
    {
		// Get the instance of the music player.
		m_musicPlayer = MusicPlayer.getInstance();
		
		// Play the sound of rocket because immediately after this object 
		// is created, the rockets starts moving
		m_musicPlayer.play(ROCKET);
	}
	
	private final float UNIT = getResources().getDisplayMetrics().density;
		
	private final String ROCKET = "rocket";
	private final String BLAST = "blast";
	
	private AnimationThread m_thread = null;
	
	private Projectile m_projectile = null;
	private Projectile m_target = null;
	private MusicPlayer m_musicPlayer;
		
	private int m_velocity;
	private int m_angle;
	private int m_targetX;
	private int m_targetY;
	
	private float m_currentHeight = 0;
	private float m_maxHeight = 0;
	
	private TriggerArea m_triggerArea = null;	
		
}

/**
 * Class AnimationThread
 * 
 * Brief This class acts as the engine of the game. It extends the Thread class and it is resposible
 * 		 for updating and rendering game objects
 *
 */
class AnimationThread extends Thread
{
	/**
	* Default constructor
	* 
	* @param surfaceHolder - The surfaceholder object to access the surface view 
	* 						 to lock the canvas while updating it
	* 
	* @param game - Represents the Game class object
	*/
	public AnimationThread(SurfaceHolder surfaceHolder, Game game) 
	{
		m_surfaceHolder = surfaceHolder;
		m_game = game;
	}
	
	public void setKeepWorking(boolean work)
	{
		m_keepWorking = work;
	}
	

	@Override
	public void run() 
	{
		Canvas c;
		Log.v("inside run","m_kepworking" + m_keepWorking);
		while(m_keepWorking)
		{
			c = null;
			
			try 
			{
				Thread.sleep(50);
				
				c = m_surfaceHolder.lockCanvas(null);
		
				synchronized (m_surfaceHolder) 
				{
					// Update the position of the rocket
					m_game.update();
					
					// Draw the rocket at updated position 
					m_game.onDraw(c);
				}
			}
			catch(Exception e) 
			{
				//System.out.println("ERROR IN RUN");
			}
			
			finally
			{
				if(c != null)
					m_surfaceHolder.unlockCanvasAndPost(c);
			}		
			
		}
	}
	
	private SurfaceHolder m_surfaceHolder;
	
	private Game m_game;
	
	private boolean m_keepWorking = false;
}