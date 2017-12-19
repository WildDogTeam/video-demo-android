package com.wilddog.toolbar.boardtoolbar;

import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by he on 2017/9/11.
 */

public class TouchGestureListener implements GestureDetector.OnGestureListener {
    private static final int ANIMATION_DURATION = 300;
    private static Interpolator sExpandInterpolator = new OvershootInterpolator(3f);
    private static Interpolator sCollapseInterpolator = new OvershootInterpolator(3f);
    private ToolBarMenu mActionMenu;
    private int offetPadding = 10;
    private boolean mIsHide = false;
    private boolean mIsLongPressed = false;

    public TouchGestureListener(ToolBarMenu mActionMenu) {
        this.mActionMenu = mActionMenu;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.i("MyGesture", "onSingleTapUp");
        mIsLongPressed = false;
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.i("MyGesture", "onScroll");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.i("MyGesture", "onLongPress");
        mIsLongPressed = true;
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        int MIN_DISTANCE = 100, MIN_VELOCITY = 200;//最小距离和速度

        if (!mActionMenu.expandsHorizontally()) {//只考虑了 上下扩展情况
            if (e1.getX() - e2.getX() > MIN_DISTANCE && Math.abs(velocityX) > MIN_VELOCITY && !mIsHide) {
                ViewCompat.animate(mActionMenu)
                        .setInterpolator(sCollapseInterpolator)
                        .translationX(0 - mActionMenu.getWidth() + offetPadding)
                        .setDuration(ANIMATION_DURATION)
                        .start();

                mIsHide = true;
            } else if (e2.getX() - e1.getX() > MIN_DISTANCE && Math.abs(velocityX) > MIN_VELOCITY && mIsHide) {
                ViewCompat.animate(mActionMenu)
                        .setInterpolator(sExpandInterpolator)
                        .translationX(0)
                        .setDuration(ANIMATION_DURATION)
                        .start();
                mIsHide = false;
            }
        }
        return false;
    }

    int getScreenWidth() {
        int screenWidth = mActionMenu.getContext().getResources().getDisplayMetrics().widthPixels;
        return screenWidth;
    }

    int getScreenHeight() {
        int screenHeight = mActionMenu.getContext().getResources().getDisplayMetrics().heightPixels;
        return screenHeight;
    }
}
