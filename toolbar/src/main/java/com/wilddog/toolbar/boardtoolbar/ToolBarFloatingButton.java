package com.wilddog.toolbar.boardtoolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.widget.ImageButton;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressLint("AppCompatCustomView")
public class ToolBarFloatingButton extends ImageButton {

    private int type;

    @IntDef({
            FloatingType.SIZE,
            FloatingType.COLOR
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FloatingType {
        int SIZE = 0;
        int COLOR = 1;
    }

    Paint mPaint;

    static final int DRAW_NULL = 0;
    static final int DRAW_RING = 1;
    static final int DRAW_CIRCLE= 2;
    static final int DRAW_RECT= 5;
    static final int DRAW_LINE= 6;

    static final int DRAW_CIRCLE_AND_RING= 3;
    static final int DRAW_INTERVER= 4;

    int mDrawType ;
    private boolean isInterval;
    private int mExpandDirection;
    private float mAddButtonInterval;

    public ToolBarFloatingButton(Context context) {
        this(context, null);
    }

    public ToolBarFloatingButton(Context context,@FloatingType int type) {
        this(context, null);
        this.type = type;
    }

    public ToolBarFloatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ToolBarFloatingButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    private void init(Context context){
       mPaint = new Paint();
       mPaint.setAntiAlias(true); //消除锯齿
    }
    /**
     * 在图片中间画圆环
     * @param size  圆的半径
     * @param color  圆的颜色
     * */
    public void drawRing(int size,int color){
       mPaint.setColor(color);
       mPaint.setStrokeWidth(size);
       mPaint.setStyle(Paint.Style.STROKE); //绘制空心圆
        mDrawType = DRAW_RING;
        postInvalidate();
    }
    /**
     * 在图片中间画圆
     * @param size  圆的半径
     * */
    public void drawCircle(int size){
//        drawCircle(mSize, OperationUtils.getInstance().mCurrentColor);
//        drawCircle(size, R.color.light_red);
    }


    /**
     * 在图片中间画圆
     * @param size  圆的半径
     * @param color  圆的颜色
     * */
    public void drawCircle(int size,int color){
        mPaint.setColor(color);
        mPaint.setStrokeWidth(size);
        mPaint.setStyle(Paint.Style.FILL); //绘制实心圆
        mDrawType = DRAW_CIRCLE;
        postInvalidate();
    }

    /**
     * 画矩形
     * @param color
     */
    public void drawRect(int color) {
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mDrawType = DRAW_RECT;
        postInvalidate();
    }

    /**
     * 画线
     * @param color
     */
    public void drawLine(int color) {
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mDrawType = DRAW_LINE;
        postInvalidate();
    }
    /**
     * 在图片中间画圆和外圈圆环
     * @param size  圆的半径
     * @param color  圆的颜色
     * */
    public void drawCircleAndRing(int size,@ColorInt int color){
        mPaint.setColor(color);
        mPaint.setStrokeWidth(size);
        mPaint.setStyle(Paint.Style.FILL); //绘制实心圆
        mDrawType = DRAW_CIRCLE_AND_RING;
        postInvalidate();
    }
    /**
     * 清除绘画
     * */
    public void clearDraw(){
        mDrawType = DRAW_NULL;
        postInvalidate();
    }


    @FloatingType
    public int getType() {
        return type;
    }

    public void drawInterval(int mExpandDirection, float mAddButtonInterval){
        this.mExpandDirection = mExpandDirection;
        this.mAddButtonInterval = mAddButtonInterval;

        mDrawType=DRAW_INTERVER;
        postInvalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int center = getWidth()/2;
//                canvas.drawRect(0, mAddButton.getTop(), 2, height,mPaint);
        if(mDrawType==DRAW_RING){
            canvas.drawCircle(center, center, center - mPaint.getStrokeWidth(),mPaint);
        }else if(mDrawType==DRAW_CIRCLE){
            canvas.drawCircle(center, center, mPaint.getStrokeWidth(),mPaint);
        }else if(mDrawType==DRAW_RECT){
            int paddingoffset=3;
            RectF rect1 = new RectF(paddingoffset, paddingoffset, getWidth()-paddingoffset, getWidth()-paddingoffset);
            canvas.drawRoundRect(rect1, 10, 10, mPaint);
        }else if(mDrawType==DRAW_INTERVER){
            if(mExpandDirection== ToolBarMenu.EXPAND_UP||mExpandDirection== ToolBarMenu.EXPAND_DOWN) {
                RectF rect = new RectF(-1, getHeight() / 5, mAddButtonInterval, getHeight() * 4 / 5);
                mPaint.setColor(Color.parseColor("#666666"));
                canvas.drawRect(rect, mPaint);
            }else if(mExpandDirection== ToolBarMenu.EXPAND_LEFT||mExpandDirection== ToolBarMenu.EXPAND_RIGHT){
                RectF rect = new RectF(getWidth()/5, 0, getWidth()*4/5, mAddButtonInterval);
                mPaint.setColor(Color.parseColor("#666666"));
                canvas.drawRect(rect, mPaint);
            }
        }


    }
}
