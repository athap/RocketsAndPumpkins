package atul.android.finalproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
//import android.util.Log;
import android.view.Window;

/**
* Class StartGameAct
* 
* Brief This is the activity which is responsible for setting the SurfaceView layout.
* 	    This surface view renders the objects with the help of game engine class AnimationThread.
* 
*/
public class StartGameAct extends Activity 
{    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Hide the title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Starts the game
        startGame();        
    }
    
    /**
    * This function retrieves the various parameters set by the player in the 
    * main activity and passes them to the game engine class
    * 
    */
    private void startGame() 
    {
    	// Get the intent set by the caller activity
    	Intent intent = getIntent();
        
        if(intent == null)
        {
        	//Log.v("StartGameAct", "Failed to start the game. No intent received");
        	return;
        }
        
        // Extract the values set by the player in the main activity
        int velocity = intent.getIntExtra("velocity", 0);
        int angle = intent.getIntExtra("angle", 0);
        int targetX = intent.getIntExtra("targetX", 0);
        int targetY = intent.getIntExtra("targetY", 0);
        
        // Set the SurfaceView and pass the values to it
        setContentView(new Game(this, angle, velocity, targetX, targetY));		
	}

    /**
     * This function returns the result to the caller activity.
     * 
     * We explicitly call finish to return to the main activity
     * 
     * @param x         - x coordinate
     * @param y         - y coordinate
     * @param maxheight - max height travelled by the rocket
     * @param targetHit - target height
     * 
     */
	public void returnResult(float x, float y, float maxheight, boolean targetHit)
    {
    	Intent result = new Intent();
    	
    	result.putExtra("distanceX", x);
    	result.putExtra("distanceY", y);
    	result.putExtra("maxHeight", maxheight);
    	result.putExtra("targetHit", targetHit);
    	
    	setResult(RESULT_OK, result);
    	
    	finish();
    }
    
} 