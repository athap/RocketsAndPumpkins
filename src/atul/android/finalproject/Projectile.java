package atul.android.finalproject;

import android.graphics.Bitmap;

/**
* Class Projectile
* 
* Brief This class represent the projectile object which is rendered in the 2d space like the rocket.* 
*
*/
public class Projectile 
{
	/**
	* Represents the default constructor of the Projectile
	* 
	* @param bitmap - Represents the image associated with the projectile
	*/
	public Projectile(Bitmap bitmap)
	{
		m_bitmap = bitmap;
	}
	
	// Public property exposing the image of the projectile
	public Bitmap getProjectile()
	{
		return m_bitmap;
	}
	
	// Public property exposing the height of the image of the projectile
	public float getImageHeight()
	{
		if(m_bitmap == null)
			return 0;
		else
			return m_bitmap.getHeight();
	}
	
	// Public property exposing the width of the image of the projectile
	public float getImageWidth()
	{
		if(m_bitmap == null)
			return 0;
		else
			return m_bitmap.getWidth();
	}
	
	// Public property exposing the x coordinate of the projectile
	public float getX() 
	{
        return m_X;
    }

	// Public property for setting the x coordinate of the projectile
    public void setX(float value) 
    {
        m_X = value;
    }

    // Public property exposing the y coordinate of the projectile
    public float getY() 
    {
        return m_Y;
    }

    // Public property for setting the y coordinate of the projectile
    public void setY(float value) 
    {
    	// Subtract the height of the image so that it does not appear below the left corner
        m_Y = value;// - m_bitmap.getHeight();
    }
	
    // represents the container of the image of the projectile
	private Bitmap m_bitmap;
	
	// Represents the x coordinate of the projectile
	private float m_X = 0;
	
	// Represents the y coordinate of the projectile
	private float m_Y = 0;
}