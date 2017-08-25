package com.wilddog.conversation.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by fly on 2017/6/15.
 */
public class SideslipView extends LinearLayout {

    private ViewGroup viewContent;
    private ViewGroup viewLeft;
    private int viewLeftWidth;
    private int viewLeftHeight;
    private int x = -1;
    private int distance = 0;
    private Scroller mScroller;
    private int scrollerPosition = 0;
    private ScrollerAnimation scrollerAnimation;
    private String toggleState = "CLOSE";

    public SideslipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SideslipView(Context context) {
        this(context, null);
    }

    private void init() {
        /**设置横向布局**/
        setOrientation(LinearLayout.HORIZONTAL);
        mScroller = new Scroller(getContext());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        System.out.println("viewLeftWidth:" + viewLeftWidth);
        viewContent.layout(l, t, r, b);
        viewLeft.layout(r, 0, r + viewLeftWidth, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        viewContent = (ViewGroup) this.getChildAt(0);
        viewLeft = (ViewGroup) this.getChildAt(1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //  viewLeftWidth = viewLeft.getMeasuredWidth();
        int childCount = viewLeft.getChildCount();
        if (childCount == 0) {
            viewLeftWidth = viewLeft.getMeasuredWidth();
        } else {
            for (int i = 0; i < childCount; i++) {
                viewLeftWidth += viewLeft.getChildAt(i).getMeasuredWidth();
            }
        }
        viewLeftHeight = viewLeft.getMeasuredHeight();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(x - event.getX()) > 8) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (x == -1) {
                    x = (int) event.getX();
                }
                if (Math.abs(x - event.getX()) > 8) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                int currentDistanceX = (int) (x - event.getX());
                int i = currentDistanceX + distance;
                if (i > viewLeftWidth || i < 0) {
                    break;
                }
                scrollTo(i, 0);
                break;
            case MotionEvent.ACTION_UP:
                if (x - event.getX() > viewLeftWidth / 5) {
                    toggleState = "OPEN";
                } else if (x - event.getX() < -(viewLeftWidth / 5)) {
                    toggleState = "CLOSE";
                }
                distance = distance + (int) (x - event.getX());
                if (distance >= viewLeftWidth) {
                    distance = viewLeftWidth;
                } else if (distance <= 0) {
                    distance = 0;
                }
                x = -1;
                toggle();
                break;
        }
        return true;
    }

    private void toggle() {
        if ("OPEN".equals(toggleState)) {
            //**开**/
            scrollerPosition = viewLeftWidth;
            distance = viewLeftWidth;
            mScroller.startScroll(getScrollX(), 0, -viewLeftWidth - getScrollX(), 0, 400);
            scrollerAnimation = new ScrollerAnimation(this, viewLeftWidth);
        } else if ("CLOSE".equals(toggleState)) {
            //**关**/
            scrollerPosition = 0;
            distance = 0;
            scrollerAnimation = new ScrollerAnimation(this, 0);
        }
        scrollerAnimation.setDuration((long) (0.1 * 1000));
        startAnimation(scrollerAnimation);
    }

    public void open() {
        if ("OPEN".equals(toggleState)) {
            return;
        }
        toggleState = "OPEN";
        toggle();
    }

    public void close() {
        if ("CLOSE".equals(toggleState)) {
            return;
        }
        toggleState = "CLOSE";
        toggle();
    }

    private class ScrollerAnimation extends Animation {
        private View view;
        private int targetScrollX;
        private int startScrollX;
        private int totalValue;

        public ScrollerAnimation(View view, int targetScrollX) {
            this.view = view;
            this.targetScrollX = targetScrollX;

            startScrollX = view.getScrollX();
            totalValue = this.targetScrollX - startScrollX;

            int time = Math.abs(totalValue);
            setDuration(time);
        }
        @Override
        protected void applyTransformation(float interpolatedTime,
                                           Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            int currentScrollX = (int) (startScrollX + totalValue * interpolatedTime);
            view.scrollTo(currentScrollX, 0);
        }
    }
}
