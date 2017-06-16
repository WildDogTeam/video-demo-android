package com.wilddog.conversationdemo.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wilddog.conversationdemo.R;

/**
 * Created by fly on 17-6-9.
 */

public class TvChannelItem extends LinearLayout {


    private TextView name;
    private TextView time;
    private Button delete;

    private float downX;
    private float downY;
    private int touchSlop;
    private boolean touchMode;
    private boolean slide;
    private int lastScrollX;

    public TvChannelItem(Context context) {
        super(context, null);
    }

    public TvChannelItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvChannelItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(HORIZONTAL);

        LayoutInflater.from(context).inflate(R.layout.widget_tv_channel, this);
        name = (TextView) findViewById(R.id.widget_channel_name);
        time = (TextView) findViewById(R.id.widget_channel_time);
        delete = (Button) findViewById(R.id.widget_channel_delete);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录按下的位置
                downX = event.getRawX();
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float nowX = event.getRawX();
                float nowY = event.getRawY();

                //判断用户是上下滑动还是左右滑动
                if (!touchMode && (Math.abs(nowX - downX) > touchSlop || Math.abs(nowY - downY) > touchSlop)) {
                    touchMode = true;   //一旦该变量被置为true，则滑动方向确定
                    if (Math.abs(nowX - downX) > touchSlop && Math.abs(nowY - downY) <= touchSlop) {
                        slide = true;   //此时认为是左右滑动
                        getParent().requestDisallowInterceptTouchEvent(true);   //请求父控件不要拦截触摸事件

                        //以下代码避免出发点击事件
                        MotionEvent cancelEvent = MotionEvent.obtain(event);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        onTouchEvent(cancelEvent);
                    }
                }

                if (slide) {
                    float diffX = downX - nowX + lastScrollX;
                    if (diffX < 0)  //设置阻尼
                        diffX /= 3;
                    else if (diffX > delete.getWidth())
                        diffX = (diffX - delete.getWidth()) / 3 + delete.getWidth();

                    scrollTo((int) diffX, 0);   //滑动到手指位置
                }

                break;
            case MotionEvent.ACTION_UP:
                if (slide) {    //如果是左右滑动，那么松手时需要自动滑到指定位置
                    ValueAnimator animator;     //使用的是ValueAnimator，而非Scroller
                    if (getScrollX() > delete.getWidth() / 2) {
                        animator = ValueAnimator.ofInt(getScrollX(), delete.getWidth());
                    } else {
                        animator = ValueAnimator.ofInt(getScrollX(), 0);
                    }
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            scrollTo((Integer) animation.getAnimatedValue(), 0);
                        }
                    });
                    animator.start();
                    slide = false;
                }
                touchMode = false;  //重置变量
                break;
        }

        return super.onTouchEvent(event);
    }

    public TextView getName() {
        return name;
    }

    public TextView getTime() {
        return time;
    }

    public Button getDelete() {
        return delete;
    }

}

