package atul.android.finalproject;

import android.graphics.PointF;

/**
* Class TriggerArea
* 
* Brief This class represents the trigger area which governs the collision of rocket and pumpkin
*       The trigger area is set by specifying a rectangular box around the pumpkin. If the rocket
*       enters the trigger area, it is counted as a hit
* 
*/
public class TriggerArea
{
	/**
	* Default public constructor.
	* 
	* @param upperLeft - Represents the upper left coordinates of the rectangle
	* @param lowerRight - Represents the lower right coordinates of the rectangle
	* 
	*/
	public TriggerArea(PointF upperLeft, PointF lowerRight)
	{
		m_upperLeft = upperLeft;
		m_lowerRight = lowerRight;
	}

	/**
	* This methods checks whether the specified co-ordinates lies inside the trigger area or not
	* 
	* @param x - X coordinate to be checked
	* @param y - Y coordinate to be checked
	* 
	* @return True if the specified coordinate lies inside the trigger area. Otherwise false
	*/
	public boolean collider(float x, float y)
	{
		if(m_lowerRight.y < y || m_upperLeft.y > y || m_upperLeft.x > x  || m_lowerRight.x < x)
			return false;
		else
			return true;
	}
	
	// Represents the upper left co-ordinate of the trigger area(rectangle)
	private PointF m_upperLeft;
	
	// Represents the lower right co-ordinate of the trigger area(rectangle)
	private PointF m_lowerRight;
}
