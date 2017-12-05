package com.wilddog.toolbar.boardtoolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.widget.ImageButton;


import com.wilddog.toolbar.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressLint("AppCompatCustomView")
public class ToolBarControllButton extends ImageButton {
    Paint mPaint;


    @IntDef({
            ControllButtonType.CICLE,
            ControllButtonType.PEN,
            ControllButtonType.LINE,
            ControllButtonType.RECT,
            ControllButtonType.PIC,
            ControllButtonType.TEXT,
            ControllButtonType.UNDO,
            ControllButtonType.DEL

    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ControllButtonType {
        int PEN = 0;
        int LINE = 1;
        int RECT = 2;
        int CICLE = 3;
        int PIC = 4;
        int TEXT = 5;
        int UNDO = 6;
        int DEL = 7;
    }


    /**不做绘画操作*/
    static final int DRAW_NULL = 0;
    /**画圆环*/
    static final int DRAW_RING = 1;
    /**画圆*/
    static final int DRAW_CIRCLE= 2;
    /**画圆和圆环*/
    static final int DRAW_CIRCLE_AND_RING= 3;
    static final int DRAW_INTERVER= 4;
    int mDrawType ;
    private int mExpandDirection;
    private float mAddButtonInterval;
    private int mConctrollButtonType;

    public ToolBarControllButton(Context context) {
        this(context, null);
    }

    public ToolBarControllButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public ToolBarControllButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    private void init(Context context, AttributeSet attrs){
        mPaint = new Paint();
        mPaint.setAntiAlias(true); //消除锯齿

        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.FloatingButton, 0, 0);
        mConctrollButtonType = attr.getInt(R.styleable.FloatingButton_fab_type, ControllButtonType.PEN);
        attr.recycle();

    }

    public @ControllButtonType int getControllButtonType(){
        return mConctrollButtonType;
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
//        drawCircle(mSize, R.mColor.light_red);
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int center = getWidth()/2;
        if(mDrawType==DRAW_RING){
            canvas.drawCircle(center, center, center - mPaint.getStrokeWidth(),mPaint);
        }else if(mDrawType==DRAW_CIRCLE){
            canvas.drawCircle(center, center, mPaint.getStrokeWidth(),mPaint);
        }else if(mDrawType==DRAW_INTERVER){
            if(mExpandDirection==ToolBarMenu.EXPAND_UP||mExpandDirection==ToolBarMenu.EXPAND_DOWN) {
                RectF rect = new RectF(-1, getHeight() / 5, mAddButtonInterval, getHeight() * 4 / 5);
                mPaint.setColor(Color.parseColor("#cdcdcd"));
                canvas.drawRect(rect, mPaint);
            }else if(mExpandDirection==ToolBarMenu.EXPAND_LEFT||mExpandDirection==ToolBarMenu.EXPAND_RIGHT){
                RectF rect = new RectF(getWidth()/5, 0, getWidth()*4/5, mAddButtonInterval);
                mPaint.setColor(Color.parseColor("#cdcdcd"));
                canvas.drawRect(rect, mPaint);
            }
        }


    }

}
