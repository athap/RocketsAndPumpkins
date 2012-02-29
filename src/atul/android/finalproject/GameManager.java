package atul.android.finalproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
//import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.TextView;
//import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;


/**
* Class GameManager 
* 
* Brief This is the main activity of the application. This activity interacts with the player
* 		 by giving details(in audio) about the target. 
* 
*       Player can set the velocity or launch angle by scrolling up and down the screen.
*       To set the value double tap event is used. Once a value is setted by the double tap,
*       Activity communicates it to the player (using audio).
* 		
*       Player can reset the values by shaking the phone.
*       
*       Once the velocity and launch angle are setted, the rocket is launched by double tapping the
*       screen. Results are communicated back to the player.* 		  	 
* 
*
*/
public class GameManager extends Activity implements TextToSpeech.OnInitListener, 
														OnSeekBarChangeListener
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // set the content layout
        setContentView(R.layout.setup_layout);
        
        // should be called before generateTargetXY()
        getScreenHeightWidth();
        
        // Initialize the music player with all the sounds so that they can be played quickly
        setBackgroundMusic();
        
        // Generate the target location
		if(! generateTargetXY())
			return;
        
		
		// check for tts availability. If its unavailable then install it
        checkTts();
		
        // Get the tts object
        m_droid = new TextToSpeech(this, this);
        
        // Get the seekbar widget object
        m_seekbar = (SeekBar)findViewById(R.id.seekbar);
        
        // Get the text view which displays value of the seekbar
        m_reading = (TextView)findViewById(R.id.reading);
        
        // Get the current high score of the game
        m_currentScoreTv = (TextView)findViewById(R.id.current_score);
        
        // Get the shared prefs
        m_sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                
        m_highScoreTv = (TextView)findViewById(R.id.high_score);
        
        m_highScoreTv.setText(getString(R.string.maximum_score_text) + 
				String.valueOf(m_sharedPref.getInt(HIGH_SCORE, 0)));
        
        // Shows the scores in the screen
        loadScores();
        
        // Initializes all the listeners used by this activity. onSeekBarChange, GestureDetector, onTouch
        // doubleTap
        initializeListeners();
    }
	
    /**
    * Notifies if the progress leval is changed
    * 
    * @param seekBar  - The SeekBar whose progress has changed
    * @param progress - The current progress level
    * @param fromUser - True if the progress change was initiated by a user 
    */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
	{
		m_prevProgress = m_progress;
		
		m_progress = progress;
		
		// count == 0 represent that value of angle will be set
		// count == 1 represent that value of velocity will be set
		if(m_count == 0)
			m_reading.setText("Angle = " + String.valueOf(m_progress));
		else if(m_count == 1)
			m_reading.setText("Velocity = " + String.valueOf(m_progress));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		//stopReadingText();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) 
	{
		//speakText(getString(R.string.select_value_text));
	}
    
	/**
	* Called to signal the completion of the TextToSpeech engine initialization.
	* 
	* @param status 0 represents SUCCESS, -1 represents ERROR
	*/
    @Override
	public void onInit(int status)
	{
		if(status == TextToSpeech.SUCCESS)
		{
			// If tts is successfully initialized, set the params as required
			m_droid.setSpeechRate(.8f);
			
			// Welcome the user
			speakText(getString(R.string.welcome_text));
			
			// Speak the target details
			speakTargetDetails();
			
			// Speak the instrucitons
			speakInstructions();			
		}
	}
    
    // Represent the key for high score in shared pref
    public static final String HIGH_SCORE = "high_score";
    
    
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		// Register the sensor listener
		m_sensorManager.registerListener(m_sensorListener, 
				 m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
				 SensorManager.SENSOR_DELAY_NORMAL);
		
		// load the scores
		loadScores();
	}
    
	@Override
	protected void onPause() 
	{
		// TODO Auto-generated method stub
		super.onPause();
		
		// Pause the tts engine
		if(m_droid != null)
			m_droid.stop();
		
		// Save the high score
		saveHighScore();
	}
	
	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		
		// Stop the tts engine when the activity is destroyed
		if(m_droid != null)
			m_droid.shutdown();
		
		// Release all the resources used by music player
		MusicPlayer.getInstance(this).closeMusicPlayer();
	}
	
	@Override
	protected void onStop() 
	{
		// Unregister the sensor listener used for detecting shake event
		m_sensorManager.unregisterListener(m_sensorListener);
		
		super.onStop();
		
		// save the high score
		saveHighScore();
	}
	
	/**
	* This method is called when a launched activity exist. This is called before onResume().
	* 
	* The activity must have been launced as startActivityForResult
	*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		m_count = 0;
		
		if(data == null)
		{
			//Log.v("onActivityResult", "data is null");
			return;
		}
		
		switch (requestCode) 
		{
			// Represent the return of StartGame activity
			case LAUNCH_ROCKET:
				// check if the target is hit. If yes then update the data otherwise analyze the result
				if(data.getBooleanExtra("targetHit", false))
				{
					// increase the current score
					++m_currentScore;
					
					// Speak target hit text
					speakText(getString(R.string.target_successfully_hit_text));
					
					// update the scores
					updateScores();
					
					// Generate new target locations
					generateTargetXY();
					
					// Speak the target details
					speakTargetDetails();
					
					// Speak instructions
					speakInstructions();
				}
				else
					analyzeAndGiveFeedback(data.getFloatExtra("distanceX", 0), 
										   data.getFloatExtra("distanceY", 0),
										   data.getFloatExtra("maxHeight", 0));
						
				break;
			
			// Represents the return of TTS availability check activity 
			case TTS_CHECK:				
				if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
				{
					// tts is already installed
					//Log.v("GameManager", "TTS engine found");
				}
				else
				{
					// No tts found. Install tts
					//Log.v("GameManager", "No TTS engine. Installing");
					
					// Install the TTS if not found
					Intent installTts = new Intent();
					installTts.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
					startActivity(installTts);
				}
				
				break;

			default:
				break;
		}	
		
	}
	
	// Loads the current score
	private void loadScores()
	{
		m_currentScoreTv.setText(getString(R.string.current_score_text) +
				String.valueOf(m_currentScore));
				
	}
	
	// Saves the high score in the shared prefs
	private void saveHighScore() 
	{
		SharedPreferences.Editor editor = m_sharedPref.edit();
		editor.putInt(HIGH_SCORE, m_highScore);
		editor.commit();
	}
	
	// Updates the current and high scores
	private void updateScores()
	{
		if(m_currentScore > m_highScore)
		{
			m_highScore = m_currentScore;
			speakText("And You made a new high score of " + String.valueOf(m_highScore));
		}
		
		m_highScoreTv.setText(getString(R.string.maximum_score_text) + m_highScore);
		m_currentScoreTv.setText(getString(R.string.current_score_text) + m_currentScore);
		
	}

	// Initializes all the listeners used by this activity
	private void initializeListeners() 
	{
		// set the listener to get the updated values from the seek bar
        m_seekbar.setOnSeekBarChangeListener(this);
        
        // get the gesture detector object
        m_gestureDetector = new DetectGesture().getGestureDetector();
        
        // create a new touch listener which we will attach to the seek bar
        m_gestureListener = new View.OnTouchListener() 
        {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				return m_gestureDetector.onTouchEvent(event);
			}
		};
		
		// set a double tap listener to record double tap event by the user
		m_gestureDetector.setOnDoubleTapListener(new OnDoubleTapListener() 
        {
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) 
			{
				return false;
			}
			
			@Override
			public boolean onDoubleTapEvent(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onDoubleTap(MotionEvent e) 
			{
				// If TTS is active, then stops tts from reading what ever id being read
				stopReadingText();
				
				/**
				* Keep track of the number of taps made by the player. m_count can have only 0, 1, 2 values
				* 
				* 0 - angle has been set
				* 1 - velocity has been set
				* 2 - angle has been launched
				*/
				m_count = m_count % TAPS;
				
				switch (m_count) 
				{
					// Represents angle has been set
					case 0:
						//m_angle = m_progress;
						m_angle = m_prevProgress;
						speakText(getString(R.string.launch_angle_text) + String.valueOf(m_angle));
						speakText(getString(R.string.set_velocity_text));
						speakText(getString(R.string.select_value_text));
						break;
					
					// Represents velocity has been set
					case 1:
						//m_velocity = m_progress;
						m_velocity = m_prevProgress;
						speakText(getString(R.string.launch_velocity_text) + String.valueOf(m_velocity));
						speakText(getString(R.string.launch_rocket_text));
						break;
						
					// Represents rocket has been launched
					case 2:
						launchRocket();
						break;
						
					default:
						break;
				}
				
				m_count++;
				
				return false;
			}
		});
		
		// set the on click listener
		m_seekbar.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				//Log.v("Main act", "View clicked");		
			}
		});
		
		// attach the gesture listener to the seek bar
		m_seekbar.setOnTouchListener(m_gestureListener);
		
		// Attach the sensor event listener to detect the motion sensor. This detects the shake event
		m_sensorListener = new SensorEventListener() 
		{			
			@Override
			public void onSensorChanged(SensorEvent event)
			{
				if(event == null)
					return;
							
				float x = event.values[X_AXIS];
				float y = event.values[Y_AXIS];
				float z = event.values[Z_AXIS];
				
				double acceleration = Math.sqrt((x*x + y*y + z*z));
				
				// If the motion event is strong enough to be said as a shake, refresh game
				if(acceleration > 25)
					refresh();
			}		

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) 
			{
				// TODO Auto-generated method stub			
			}
		};
		
		
		// set listeners for shake motion detection
		m_sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        
        if(m_sensorManager != null)
        {
        	// Check if shake will be supported or not
        	boolean shakeSupported = m_sensorManager.registerListener(
        									 m_sensorListener, 
        									 m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
        									 SensorManager.SENSOR_DELAY_NORMAL);
        	
        	if(! shakeSupported)
        		;//Toast.makeText(this, "Shake not supported", Toast.LENGTH_LONG).show();
        }
		
	}
	
	/**
	 * The analysis is divided into four parts
	 * 1. Rocket falls short of distance in x direction but not in y 
	 * 2. Rocket falls short of distance in x direction and also in y
	 * 3. Rocket falls short of distance in y direction but not in x
	 * 4. Rocket exceeds distance in y direction and also in x
	 * 
	 * @param x - distance travelled by rocket in x direction
	 * @param y - distance travelled by rocket in y direction
	 * @param maxHeight - maximum height achieved by the rocket
	 */
	private void analyzeAndGiveFeedback(float x, float y, float heightFromTop)
	{
		float maxHeight = m_height - heightFromTop;
		
		if(x < m_targetX)
		{
			if(maxHeight > m_targetY)
				speakText(getString(R.string.decrease_angle_text));
			else
				speakText(getString(R.string.increase_velocity_text));
		}
		else
		{
			if(maxHeight > m_targetY)
				speakText(getString(R.string.decrease_velocity_text));
			else
				speakText(getString(R.string.increase_angle_text));
		}
		
		speakInstructions();		
		//Log.v("Target details", "Tx = " + m_targetX + " Ty = " + m_targetY);
		//Log.v("RESULTS", "x = " + x + " y = " + y + " Max height = " + maxHeight);
	}
	
	/**
	* Launches an activity which checks the availability of TTS engine. Results of
	* this activity are handled by onActivityResult method
	*/
	private void checkTts() 
	{
		// Check if a TTS engine is installed
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_CHECK);		
	}
	
	// Initializes the music player will all the background music required by the game
	private void setBackgroundMusic() 
    {
    	MusicPlayer player = MusicPlayer.getInstance(this);
        
        player.addSound("rocket", R.raw.rocket_sound);
        player.addSound("blast", R.raw.explosion_sound);
		
	}

	// Get the details of the screen height and width
	private void getScreenHeightWidth() 
	{
		Display d = getWindowManager().getDefaultDisplay();
        m_height = d.getHeight();
        m_width = d.getWidth();        
	}
	
	// Generates the target coordinates based on the screen height and width
	private boolean generateTargetXY()
	{
		if(m_height == 0 || m_width == 0)
			return false;
		
		m_targetX = m_width/2 + (int) (Math.random() * (m_width / 2 - (m_width / 4)));
		m_targetY = m_height / 3 + (int) (Math.random() * (m_height / 2 - (m_height / 4)));
		
		return true;
	}
	
	/**
	* This method launches the rocket with all the details provided by the player 
	*/
	private void launchRocket()
	{
		Intent intent = new Intent(this, StartGameAct.class);
		
		intent.putExtra("velocity", m_velocity);
		intent.putExtra("angle", m_angle);
		intent.putExtra("targetX", m_targetX);
		intent.putExtra("targetY", m_targetY);
		
		startActivityForResult(intent, LAUNCH_ROCKET);
	}
	
	/**
	* This activity speaks the instruction about the game
	*/
	private void speakInstructions() 
    {
    	speakText(getString(R.string.set_angle_text));
		speakText(getString(R.string.select_value_text));	
	}
	
	// Speak the target details as height of the target and distance from rocket
	private void speakTargetDetails()
	{
		speakText(getString(R.string.target_distance_text) + 
								String.valueOf(m_targetX) + "meters");
		speakText(getString(R.string.target_height_text) + 
								String.valueOf(m_height - m_targetY) + "meters");		
	}
	
	/**
	* This methods used tts engine to speak text
	* 
	* @param text - Text to speak
	*/
	private void speakText(String text)
	{
		if(text == null)
			return;
		
		m_droid.speak(text, TextToSpeech.QUEUE_ADD, null);
	}
	
	// Stops the tts engine from reading whatever text is being read
	private void stopReadingText()
	{
		//if(m_droid.isSpeaking())
		m_droid.stop();
	}
	
	/**
	* This method is called when a shake event is detected. It resets the velocity and launch angle
	* and target location.
	*/
	private void refresh() 
	{
		m_count = 0;
		stopReadingText();
		speakText(getString(R.string.reset_text));
		generateTargetXY();
		speakTargetDetails();
		speakInstructions();
	}
	
	private SensorEventListener m_sensorListener;
	
	private final int LAUNCH_ROCKET = 101;
	private final int TTS_CHECK = 102;
	
	private final int TAPS = 3;
		
	//variables for detecting shake motion
	private final int X_AXIS = 0;
	private final int Y_AXIS = 1;
	private final int Z_AXIS = 2;
	
	// Sensor manager for detecting shake event
	private SensorManager m_sensorManager;
	
	// represent the text view which displays the information
	private TextView m_reading = null;
	
	private SharedPreferences m_sharedPref;
	
	private TextView m_currentScoreTv = null;
	
	private TextView m_highScoreTv = null;
	
	// represent the vertical seek bar
	private SeekBar m_seekbar = null;
	
	// represent the tts object
	private TextToSpeech m_droid;
	
	// gesture listener for touch events
	private View.OnTouchListener m_gestureListener;
	
	// gesture detector for detecting touch events
	private GestureDetector m_gestureDetector;
	
	private int m_count = 0;
	private int m_progress = 0;
	private int m_prevProgress = 0;
	private int m_angle = 0;
	private int m_velocity = 0;
	private int m_currentScore = 0;
	private int m_highScore;
	private int m_height = 0;
	private int m_width = 0;
		
	private int m_targetX = 0;
	private int m_targetY = 0;
	
}