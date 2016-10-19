package com.piguy.Temporal_Bot;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Custom ScrollView that prevents any scrolling or clicking
 *
 * @author Alex Vanyo
 */
public class LockedScrollView extends ScrollView {

    public LockedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return false;
            default:
                return super.onTouchEvent(motionEvent);
        }
    }
}
