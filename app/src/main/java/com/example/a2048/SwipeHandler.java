package com.example.a2048;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SwipeHandler implements View.OnTouchListener {

    private static final int SWIPE_THRESHOLD = 100;      // min px distance to count as swipe
    private static final int SWIPE_VELOCITY_THRESHOLD = 100; // min px/sec

    private final GestureDetector gestureDetector;
    private final SwipeListener listener;

    public interface SwipeListener {
        void onSwipeLeft();
        void onSwipeRight();
        void onSwipeUp();
        void onSwipeDown();
    }

    public SwipeHandler(Context context, SwipeListener listener) {
        this.listener = listener;
        this.gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return true; // must return true or gestures won't fire
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();

                boolean isHorizontal = Math.abs(dx) > Math.abs(dy);

                if (isHorizontal) {
                    if (Math.abs(dx) > SWIPE_THRESHOLD
                            && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (dx > 0) listener.onSwipeRight();
                        else        listener.onSwipeLeft();
                        return true;
                    }
                } else {
                    if (Math.abs(dy) > SWIPE_THRESHOLD
                            && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (dy > 0) listener.onSwipeDown();
                        else        listener.onSwipeUp();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        return gestureDetector.onTouchEvent(event);
    }
}