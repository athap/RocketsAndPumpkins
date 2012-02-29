package atul.android.finalproject;

import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
* Class DetectGesture
* 
* Brief This class is a wrapper around the GestureDetector. It act as a listener which sends a 
* 		notification when a gesture occurs. This listener is attached to the custom vertical seek bar 
* 		so as to detect touch (for scrolling) and double tap events
*
*/
public class DetectGesture implements OnGestureListener
{
	// Default constructor
	public DetectGesture()
	{
		// create the gesture detector object
		m_gd = new GestureDetector(this);
	}
	
	public GestureDetector getGestureDetector()
	{
		return m_gd;		
	}
	
	@Override
	public boolean onDown(MotionEvent e) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	private GestureDetector m_gd;
}
