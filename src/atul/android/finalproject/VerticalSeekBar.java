package atul.android.finalproject;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
* Class VerticalSeekBar
* 
* Brief This is a custom SeekBar class which inherits from the SeekBar class. It implements
* 		 a vertical SeekBar with the width and height equal to the device width and height
* 
*/
public class VerticalSeekBar extends SeekBar 
{

    /**
    * This is the public default constructor
    * 
    * @param context - Represents the context in which the SeekBar is called
    */
	public VerticalSeekBar(Context context) 
    {
        super(context);        
    }

	/**
	* This is called to initialize the view from the layout file
	* 
	* @param context  - Represents the context in which the SeekBar is called
	* @param attrs    - A collection of properties specified in an xml resource file. 
	* @param defStyle - Default style to apply to this view. If 0 then no style will be applied
	*/
    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
    }

    /**
	* This is called to initialize the view from the layout file
	* 
	* @param context  - Represents the context in which the SeekBar is called
	* @param attrs    - A collection of properties specified in an xml resource file. 
	*/
    public VerticalSeekBar(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
    }

    
    /**
	* To implement the custom SeekBar, we have to override this method.
	* This is called during layout when the size of this view has changed
	* 
	* @param w    - Represents the current width of this view
	* @param h    - Represents the current height of this view
	* @param oldw - Represents the old width of this view
	* @param oldh - Represents the old height of this view 
	*/
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) 
    {
    	super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
	* To implement the custom SeekBar, we have to override this method 
	* 
	* This method should calculate a measurement width and height which will be required to render 
	* the component. It should try to stay within the specifications passed in, although it can choose
	* to exceed them (in this case, the parent can choose what to do, including clipping, scrolling, 
	* throwing an exception, or asking the onMeasure() to try again, perhaps with different 
	* measurement specifications).
	* Once the width and height are calculated, the setMeasuredDimension(int width, int height) method 
	* must be called with the calculated measurements. Failure to do this will result in an exception 
	* being thrown.
	* 
	* @param widthMeasureSpec  - Represents the horizontal space requirements as imposed by the parent
	* @param heightMeasureSpec - Represents the vertical space requirements as imposed by the parent
	*/
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
    {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        
        // If this method is not called then it will result in a run time exception 
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }    

    /**
    * To implement the custom SeekBar, we have to override this method
    * 
    * This method delivers a canvas object upon which we can implement anything we want: 2D graphics, 
    * other standard or custom components, styled text, or anything else you can think of.
    * 
    * @param c  - Represents the canvas object 
    */
    protected void onDraw(Canvas c) 
    {
    	// Rotate the canvas to make it vertical
        c.rotate(-90);
        
        // Translate the canvas
        c.translate(-getHeight(), 0);

        super.onDraw(c);
    }

    /**
    * This method is implemented to handle touch screen motion events
    * 
    * @param event - Object used to report movement (mouse, pen, finger, trackball) events
    */    
    @Override
    public boolean onTouchEvent(MotionEvent event) 
    {
    	// Check if the view is enabled or not. isEnabled returns True if this view is enabled, 
    	// false otherwise 
        if (!isEnabled()) 
        {
            return false;
        }

        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            
            // A pressed gesture has finished
            case MotionEvent.ACTION_UP:
                setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }
}