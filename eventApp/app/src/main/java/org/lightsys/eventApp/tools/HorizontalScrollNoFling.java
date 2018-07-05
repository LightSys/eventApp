package org.lightsys.eventApp.tools;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created by otter57 on 4/25/17.
 *
 * Scrollview for displaying schedule
 */
//TODO: THINK ABOUT THIS DECISION ABOUT SCROLL VELOCITY
public class HorizontalScrollNoFling extends HorizontalScrollView{


    public HorizontalScrollNoFling (Context context){
        super(context);
    }

    public HorizontalScrollNoFling(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollNoFling(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HorizontalScrollNoFling(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void fling (int velocityY)
    {
        //TODO: MATHY STUFF TO MANUALLY ADJUST SCROLL, OR ELSE REDESIGN THE SCROLLVIEW FROM THE XML
    /*Scroll view is no longer gonna handle scroll velocity.
     * super.fling(velocityY);
    */
    }

}
