package com.wilddog.toolbar.boardtoolbar;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.wilddog.board.WilddogBoard;
import com.wilddog.board.utils.BoardUtil;
import com.wilddog.toolbar.R;

import java.util.ArrayList;
import java.util.List;

import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.CICLE;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.DEL;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.LINE;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.PEN;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.PIC;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.RECT;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.TEXT;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.UNDO;

public class ToolBarMenu extends ViewGroup {

    public static final int EXPAND_UP = 0;
    public static final int EXPAND_DOWN = 1;
    public static final int EXPAND_LEFT = 2;
    public static final int EXPAND_RIGHT = 3;

    private static final int ANIMATION_DURATION = 300;
    private static Interpolator sExpandInterpolator = new OvershootInterpolator();
    private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
    private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();
    private int mExpandDirection;
    private int mButtonSpacing = dip2px(2);
    private boolean mExpanded;
    private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    GestureDetectorCompat mDetectorCompat;
    private ViewGroup mAddView;
    private int mMaxButtonWidth;
    private int mMaxButtonHeight;
    private int mButtonsCount;
    private TouchDelegateGroup mTouchDelegateGroup;
    private OnFloatingActionsMenuClickListener mClickListener;
    private int width;
    private int height;
    private Paint mPaint = new Paint();
    private int interval;
    private Context context;
    private ToolBarOption toolBarOption;
    private List<ToolBarFloatingButton> fabs = new ArrayList<>();

    public ToolBarMenu(Context context) {
        this(context, null);
    }

    public ToolBarMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ToolBarMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    public void setOnFloatingActionsMenuClickListener(OnFloatingActionsMenuClickListener flLister) {
        mClickListener = flLister;
    }

    /**
     * 切换控制按钮背景图
     */
    public void setAddButtonBackground(int drawable) {
        mAddView.setBackgroundResource(drawable);

    }

    /**
     * dip转px
     */
    private int dip2px(float dpValue) {
        final float scale = BoardUtil.getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void init(Context context, AttributeSet attributeSet) {
        this.context = context;
        toolBarOption = new ToolBarOption(this);
        mDetectorCompat = new GestureDetectorCompat(context, new TouchGestureListener(this));

        mPaint.setAntiAlias(true);
        setWillNotDraw(false);

        mTouchDelegateGroup = new TouchDelegateGroup(this);
        setTouchDelegate(mTouchDelegateGroup);

        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
//        mButtonSpacing = dip2px(attr.getDimension(R.styleable.FloatingActionsMenu_fab_buttonSpacing, 2));
//        mAddButtonBackground = attr.getDrawable(R.styleable.FloatingActionsMenu_fab_addButtonBackground);
//        mAddButtonWidth = attr.getDimension(R.styleable.FloatingActionsMenu_fab_addButtonWidth, 20);
//        mAddButtonHeight = attr.getDimension(R.styleable.FloatingActionsMenu_fab_addButtonHeight, 20);
//        mAddButtonStyle = attr.getInt(R.styleable.FloatingActionsMenu_fab_addButtonStyle, STYLE_CUSTOM);

//        mAddButtonPlusColor = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonPlusIconColor, getColor(android.R.mColor.white));
//        mAddButtonColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormal, getColor(android.R.mColor.holo_blue_dark));
//        mAddButtonColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorPressed, getColor(android.R.mColor.holo_blue_light));
//        mAddButtonSize = attr.getInt(R.styleable.FloatingActionsMenu_fab_addButtonSize, FloatingActionButton.SIZE_NORMAL);
//        mAddButtonStrokeVisible = attr.getBoolean(R.styleable.FloatingActionsMenu_fab_addButtonStrokeVisible, true);
        mExpandDirection = attr.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, EXPAND_UP);
        interval = dip2px(attr.getDimension(R.styleable.FloatingActionsMenu_fab_interval, 0));
        attr.recycle();

        createControllButtons(context);
    }

    boolean expandsHorizontally() {
        return mExpandDirection == EXPAND_LEFT || mExpandDirection == EXPAND_RIGHT;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mDetectorCompat.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }


    /**
     * 增加菜单的控制按钮
     */
    private void createControllButtons(Context context) {
        mAddView = (ViewGroup) View.inflate(context, R.layout.controllbuttons, null);
        LinearLayout ll = (LinearLayout) mAddView.findViewById(R.id.root);
        switch (mExpandDirection) {
            case EXPAND_UP:
            case EXPAND_DOWN:
                ll.setOrientation(LinearLayout.HORIZONTAL);
                break;
            case EXPAND_LEFT:
            case EXPAND_RIGHT:
                ll.setOrientation(LinearLayout.VERTICAL);
                break;
        }
//        mAddView.setId(R.id.fab_expand_menu_button);

        for (int i = 0; i < mAddView.getChildCount(); i++) {
            final ToolBarControllButton controllButton = (ToolBarControllButton) mAddView.getChildAt(i);
            controllButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (controllButton.getControllButtonType()) {
                        case PEN:
                        case LINE:
                        case CICLE:
                        case RECT:
                            setChildImageDot();
                            toggleWithState(v);
                            break;
                        case TEXT:
                            setChildImageText();
                            toggleWithState(v);
                            break;
                        case PIC:
                        case UNDO:
                        case DEL:
                            deSelectExpand();
                            break;
                    }

                    clearOthersSelectedState(controllButton);

                    clearAllFoloatingSelectedState();

                    if (mClickListener != null) {
                        mClickListener.addButtonLister(controllButton, controllButton.getControllButtonType());
                    }
                }
            });
        }
        addView(mAddView, super.generateDefaultLayoutParams());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        width = 0;
        height = 0;

        mMaxButtonWidth = 0;
        mMaxButtonHeight = 0;

       /* for (int i = 0; i < mButtonsCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            switch (mExpandDirection) {

                case EXPAND_UP:
                case EXPAND_DOWN:
//                    mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
//                    height += child.getMeasuredHeight();
                    if (child == mAddView) {
                        height = mAddView.getMeasuredHeight();
//                        width += mAddView.getMeasuredWidth() / 2;
                    } else {
                        width += child.getMeasuredWidth();
                        mMaxButtonHeight = Math.max(mMaxButtonHeight, child.getMeasuredHeight());
                    }
                    break;
                case EXPAND_LEFT:
                case EXPAND_RIGHT:
                    if (child == mAddView) {
                        width = mAddView.getMeasuredWidth();
//                        width += mAddView.getMeasuredWidth() / 2;
                    } else {
                        height += child.getMeasuredHeight();
                        mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
                    }
//                    width += child.getMeasuredWidth();
//                    mMaxButtonHeight = Math.max(mMaxButtonHeight, child.getMeasuredHeight());
                    break;
            }

        }*/

        switch (mExpandDirection) {
            case EXPAND_UP:
            case EXPAND_DOWN:
                height = mAddView.getMeasuredHeight() * 2 + interval /*+ mButtonSpacing*/;
                width = mAddView.getMeasuredWidth();
                break;
            case EXPAND_LEFT:
            case EXPAND_RIGHT:
                height = mAddView.getMeasuredHeight();
                width = mAddView.getMeasuredWidth() * 2 + interval /*+ mButtonSpacing*/;
                break;
        }

        setMeasuredDimension(width, height);
    }

    private int adjustForOvershoot(int dimension) {
        return dimension * 12 / 10;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
        mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
        switch (mExpandDirection) {
            case EXPAND_UP:
            case EXPAND_DOWN:

                boolean expandUp = mExpandDirection == EXPAND_UP;

                if (changed) {
                    mTouchDelegateGroup.clearTouchDelegates();
                }

                int controllButtonY = expandUp ? b - t - mAddView.getMeasuredHeight() : 0;

                mAddView.layout(0, controllButtonY, mAddView.getMeasuredWidth(), controllButtonY + mAddView.getMeasuredHeight());


                int nextY = expandUp ?
                        controllButtonY - interval :
                        controllButtonY + mAddView.getMeasuredHeight() + interval;

                int nextX0 = mButtonSpacing;

                int weight = (width - mButtonSpacing * 2) / (mButtonsCount - 1);

                for (int i = 0; i < mButtonsCount; i++) {

                    final View child = getChildAt(i);

                    if (child == mAddView || child.getVisibility() == GONE || child instanceof LinearLayout) {
                        continue;
                    }

                    int offset = (mAddView.getMeasuredHeight() - Math.min(child.getMeasuredWidth(), child.getMeasuredHeight())) / 2;

                    int childX = nextX0;
                    int childY = expandUp ? nextY - mAddView.getMeasuredHeight() : nextY;
                    child.layout(childX, childY + offset, childX + child.getMeasuredWidth(), childY + offset + Math.min(child.getMeasuredWidth(), child.getMeasuredHeight()) /*- mButtonSpacing * 2 - interval*/);

                    float collapsedTranslation = controllButtonY - childY;
                    float expandedTranslation = 0f;

                    child.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
                    child.setTranslationX(0);
                    child.setAlpha(mExpanded ? 1f : 0f);

                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    params.init();
                    params.mCollapseDir.setPropertyName("translationY");
                    params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
                    params.mExpandDir.setPropertyName("translationY");
                    params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);

                    params.setAnimationsTarget(child);


                    nextX0 = childX + weight;
                }
                break;

            case EXPAND_LEFT:
            case EXPAND_RIGHT:
                boolean expandLeft = mExpandDirection == EXPAND_LEFT;

                int controllButtonX = expandLeft ? r - l - mAddView.getMeasuredWidth() : 0;
                int controllButtonTop = 0;
                mAddView.layout(controllButtonX, controllButtonTop, controllButtonX + mAddView.getMeasuredWidth(), controllButtonTop + mAddView.getMeasuredHeight());

                int nextX = expandLeft ?
                        controllButtonX - interval :
                        controllButtonX + mAddView.getMeasuredWidth() + mButtonSpacing;

                int nextY0 = mButtonSpacing;


                int weightrl = (height - mButtonSpacing * 2) / (mButtonsCount - 1);

                for (int i = 0; i < mButtonsCount; i++) {
                    final View child = getChildAt(i);

                    if (child == mAddView || child.getVisibility() == GONE || child instanceof LinearLayout)
                        continue;

                    int offset = (mAddView.getMeasuredWidth() - Math.min(child.getMeasuredWidth(), child.getMeasuredHeight())) / 2;


                    int childX = expandLeft ? nextX - mAddView.getMeasuredWidth() : nextX;
//                    int childY = controllButtonTop + (mAddView.getMeasuredHeight() - child.getMeasuredHeight()) / 2;
                    int childY = nextY0;
                    child.layout(childX + offset, childY, childX + offset + child.getMeasuredWidth(), childY + Math.min(child.getMeasuredWidth(), child.getMeasuredHeight()) /*- mButtonSpacing * 2 - interval*/);

                    float collapsedTranslation = controllButtonX - childX;
                    float expandedTranslation = 0f;

                    child.setTranslationX(mExpanded ? expandedTranslation : collapsedTranslation);
                    child.setTranslationY(0);
                    child.setAlpha(mExpanded ? 1f : 0f);

                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    params.init();
                    params.mCollapseDir.setProperty(View.TRANSLATION_X);
                    params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
                    params.mExpandDir.setProperty(View.TRANSLATION_X);
                    params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
                    params.setAnimationsTarget(child);

                    nextY0 = childY + weightrl;
                }

                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        mPaint.setColor(Color.parseColor("#5d5d5d"));
        switch (mExpandDirection) {
            case EXPAND_UP:
                if (isExpanded()) {
//                    canvas.translate(0, mButtonSpacing);
                    RectF rect1 = new RectF(0, 0, width, height - mAddView.getMeasuredHeight() - interval);
                    canvas.drawRoundRect(rect1, 10, 10, mPaint);
                }

                break;
            case EXPAND_DOWN:
                if (isExpanded()) {
                    RectF rect1 = new RectF(0, mAddView.getMeasuredHeight() + interval, width, height);
                    canvas.drawRoundRect(rect1, 10, 10, mPaint);
                }
                break;
            case EXPAND_LEFT:
                if (isExpanded()) {
//                    canvas.translate(mButtonSpacing, 0);
                    RectF rect2 = new RectF(0, 0, width - mAddView.getMeasuredWidth() - interval, height);
                    canvas.drawRoundRect(rect2, 10, 10, mPaint);
                }
                break;
            case EXPAND_RIGHT:
                if (isExpanded()) {
                    RectF rect2 = new RectF(mAddView.getMeasuredWidth() + interval, 0, width, height);
                    canvas.drawRoundRect(rect2, 10, 10, mPaint);
                }
                break;
        }
        canvas.restore();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(super.generateLayoutParams(attrs));
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(super.generateLayoutParams(p));
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();


        createChildButton();

        bringChildToFront(mAddView);

        mButtonsCount = getChildCount();

    }

    private void createChildButton() {

        LayoutParams layoutParams = generateDefaultLayoutParams();
        layoutParams.height = dip2px(30);
        layoutParams.width = dip2px(30);

        ToolBarFloatingButton small = new ToolBarFloatingButton(context, ToolBarFloatingButton.FloatingType.SIZE);
        small.setId(R.id.fab_small);
        small.setBackgroundResource(R.drawable.childbg);
        small.setImageResource(R.mipmap.smalldot);
        addView(small, layoutParams);
        fabs.add(small);

        ToolBarFloatingButton middle = new ToolBarFloatingButton(context, ToolBarFloatingButton.FloatingType.SIZE);
        middle.setId(R.id.fab_middle);
        middle.setBackgroundResource(R.drawable.childbg);
        middle.setImageResource(R.mipmap.middot);
        addView(middle, layoutParams);
        fabs.add(middle);

        ToolBarFloatingButton big = new ToolBarFloatingButton(context, ToolBarFloatingButton.FloatingType.SIZE);
        big.setId(R.id.fab_big);
        big.setBackgroundResource(R.drawable.childbg);
        big.setImageResource(R.mipmap.bigdot);
        addView(big, layoutParams);
        fabs.add(big);

        ToolBarFloatingButton intavel = new ToolBarFloatingButton(context);
        intavel.setBackgroundResource(R.drawable.addbuttonbg);

        if (mExpandDirection == EXPAND_DOWN || mExpandDirection == EXPAND_UP) {
            intavel.setImageResource(R.mipmap.inteval);
        } else {
            intavel.setImageResource(R.mipmap.inteval2);
        }
        addView(intavel, generateDefaultLayoutParams());

        ToolBarFloatingButton red = new ToolBarFloatingButton(context, ToolBarFloatingButton.FloatingType.COLOR);
        red.setId(R.id.fab_red);
        red.setBackgroundResource(R.drawable.childbg);
        red.drawRect(ToolBarOption.RED_COLOR);
        addView(red, layoutParams);
        fabs.add(red);

        ToolBarFloatingButton yellow = new ToolBarFloatingButton(context, ToolBarFloatingButton.FloatingType.COLOR);
        yellow.setId(R.id.fab_yellow);
        yellow.setBackgroundResource(R.drawable.childbg);
        yellow.drawRect(ToolBarOption.YELLOW_COLOR);
        addView(yellow, layoutParams);
        fabs.add(yellow);

        ToolBarFloatingButton green = new ToolBarFloatingButton(context, ToolBarFloatingButton.FloatingType.COLOR);
        green.setId(R.id.fab_green);
        green.setBackgroundResource(R.drawable.childbg);
        green.drawRect(ToolBarOption.GREEN_COLOR);
        addView(green, layoutParams);
        fabs.add(green);

        ToolBarFloatingButton blue = new ToolBarFloatingButton(context, ToolBarFloatingButton.FloatingType.COLOR);
        blue.setId(R.id.fab_blue);
        blue.setBackgroundResource(R.drawable.childbg);
        blue.drawRect(ToolBarOption.BLUE_COLOR);
        addView(blue, layoutParams);
        fabs.add(blue);

        ToolBarFloatingButton gray = new ToolBarFloatingButton(context, ToolBarFloatingButton.FloatingType.COLOR);
        gray.setId(R.id.fab_gray);
        gray.setBackgroundResource(R.drawable.childbg);
        gray.drawRect(ToolBarOption.GRAY_COLOR);
        addView(gray, layoutParams);
        fabs.add(gray);

        ToolBarFloatingButton black = new ToolBarFloatingButton(context, ToolBarFloatingButton.FloatingType.COLOR);
        black.setId(R.id.fab_black);
        black.setBackgroundResource(R.drawable.childbg);
        black.drawRect(ToolBarOption.BLACK_COLOR);
        addView(black, layoutParams);
        fabs.add(black);


        small.setOnClickListener(toolBarOption);
        middle.setOnClickListener(toolBarOption);
        big.setOnClickListener(toolBarOption);
        red.setOnClickListener(toolBarOption);
        yellow.setOnClickListener(toolBarOption);
        green.setOnClickListener(toolBarOption);
        blue.setOnClickListener(toolBarOption);
        gray.setOnClickListener(toolBarOption);
        black.setOnClickListener(toolBarOption);
    }

    public void clearOthersSelectedState(ToolBarControllButton fab) {
        for (int i = 0; i < mAddView.getChildCount(); i++) {
            View childAt = mAddView.getChildAt(i);
            if (childAt instanceof ToolBarControllButton) {
                if (fab != childAt)
                    childAt.setSelected(false);
            }
        }
    }

    public void clearOthersSelectedState(ToolBarFloatingButton barFloatingButton) {
        for (ToolBarFloatingButton fab : fabs) {
            if (fab != barFloatingButton && fab.getType() == barFloatingButton.getType())
                fab.setSelected(false);
        }
    }

    public void collapse() {
        if (mExpanded) {
            mExpanded = false;
            mTouchDelegateGroup.setEnabled(false);
            mCollapseAnimation.start();
            mExpandAnimation.cancel();
        }
    }

    public void toggle() {
        if (mExpanded) {
            collapse();
        } else {
            expand();
        }
        invalidate();
    }

    private void selectionExpand() {
        collapse();
        expand();
        invalidate();
    }

    public void deSelectExpand() {
        collapse();
        invalidate();
    }

    private void toggle(View v) {
        if (mExpanded) {
            v.setSelected(false);
            collapse();
        } else {
            v.setSelected(true);
            expand();
        }
        invalidate();
    }

    private void toggleWithState(View v) {
        if (mExpanded) {
            collapse();
            if (v.isSelected()) {
                v.setSelected(false);
            } else {
                expand();
                v.setSelected(true);
            }
        } else {
            v.setSelected(true);
            expand();
        }
        invalidate();
    }

    public void expand() {
        if (!mExpanded) {
            mExpanded = true;
            mTouchDelegateGroup.setEnabled(true);
            mCollapseAnimation.cancel();
            mExpandAnimation.start();
        }
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void setOritention(int direction) {
        mExpandDirection = direction;
        requestLayout();
    }

   /* @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mExpanded = mExpanded;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mExpanded = savedState.mExpanded;
            mTouchDelegateGroup.setEnabled(mExpanded);
            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }*/

    public void bindingBoard(WilddogBoard boardView, Activity activity) {

        toolBarOption.setBoardView(boardView, activity);
    }

    public void expandText() {
        setChildImageText();
        selectionExpand();
    }

    public void expandDot() {
        setChildImageDot();
        selectionExpand();
    }

    private void setChildImageText() {
        ((ToolBarFloatingButton) getChildAt(0)).setImageResource(R.mipmap.asmall);
        ((ToolBarFloatingButton) getChildAt(1)).setImageResource(R.mipmap.amid);
        ((ToolBarFloatingButton) getChildAt(2)).setImageResource(R.mipmap.abig);
    }

    private void setChildImageDot() {
        ((ToolBarFloatingButton) getChildAt(0)).setImageResource(R.mipmap.smalldot);
        ((ToolBarFloatingButton) getChildAt(1)).setImageResource(R.mipmap.middot);
        ((ToolBarFloatingButton) getChildAt(2)).setImageResource(R.mipmap.bigdot);
    }

    public void clearAllControllSelectedState() {
        for (int i = 0; i < mAddView.getChildCount(); i++) {
            View childView = mAddView.getChildAt(i);
            if (childView instanceof ToolBarControllButton) {
                childView.setSelected(false);
            }
        }
    }

    public void clearAllFoloatingSelectedState() {
        for (ToolBarFloatingButton fab : fabs) {
            fab.setSelected(false);
        }
    }

    public void setFloatingButtonSelected(Integer... floatingButtonIndex) {
        clearAllFoloatingSelectedState();
        for (int index : floatingButtonIndex)
            fabs.get(index).setSelected(true);
    }

    public interface OnFloatingActionsMenuClickListener {

        void addButtonLister(ToolBarControllButton controllButton, int controllButtonType);
    }
/*

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public boolean mExpanded;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mExpanded = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mExpanded ? 1 : 0);
        }
    }
*/

    private class LayoutParams extends ViewGroup.LayoutParams {

        private ObjectAnimator mExpandDir;
        private ObjectAnimator mExpandAlpha;
        private ObjectAnimator mCollapseDir;
        private ObjectAnimator mCollapseAlpha;

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        private void init() {
            mExpandDir = new ObjectAnimator();
            mExpandAlpha = new ObjectAnimator();
            mCollapseDir = new ObjectAnimator();
            mCollapseAlpha = new ObjectAnimator();

            mExpandDir.setInterpolator(sExpandInterpolator);
            mExpandAlpha.setInterpolator(sAlphaExpandInterpolator);
            mCollapseDir.setInterpolator(sCollapseInterpolator);
            mCollapseAlpha.setInterpolator(sCollapseInterpolator);

            mCollapseAlpha.setProperty(View.ALPHA);
            mCollapseAlpha.setFloatValues(1f, 0f);

            mExpandAlpha.setProperty(View.ALPHA);
            mExpandAlpha.setFloatValues(0f, 1f);

            switch (mExpandDirection) {
                case EXPAND_UP:
                case EXPAND_DOWN:
                    mCollapseDir.setProperty(View.TRANSLATION_Y);
                    mExpandDir.setProperty(View.TRANSLATION_Y);
                    break;
                case EXPAND_LEFT:
                case EXPAND_RIGHT:
                    mCollapseDir.setProperty(View.TRANSLATION_X);
                    mExpandDir.setProperty(View.TRANSLATION_X);
                    break;
            }
        }

        public void setAnimationsTarget(View view) {
            mCollapseAlpha.setTarget(view);
            mCollapseDir.setTarget(view);
            mExpandAlpha.setTarget(view);
            mExpandDir.setTarget(view);

            mCollapseAnimation.play(mCollapseAlpha);
            mCollapseAnimation.play(mCollapseDir);
            mExpandAnimation.play(mExpandAlpha);
            mExpandAnimation.play(mExpandDir);
        }
    }
}
