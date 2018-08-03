package com.mvcoder.testvideo.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class HorizontalScrollView extends ViewGroup {


    private int childWidth = 0;
    private int mChildrenIndex = 0;
    private int mChildCount = 0;

    private Scroller mScroller;
    private VelocityTracker velocityTracker;

    private int mLastX = -1;
    private int mLastY = -1;
    private int mLastInterceptX = -1;
    private int mLastInterceptY = -1;

    public HorizontalScrollView(Context context) {
        this(context, null);
    }

    public HorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        if (mScroller == null) {
            mScroller = new Scroller(context);
            velocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        int currentX = (int) ev.getX();
        int currentY = (int) ev.getY();
        boolean intercept = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                intercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = currentX - mLastInterceptX;
                int deltaY = currentY - mLastInterceptY;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    intercept = true;
                } else {
                    intercept = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
        }
        mLastInterceptX = currentX;
        mLastInterceptY = currentY;
        if (!intercept) {
            mLastX = currentX;
            mLastY = currentY;
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        velocityTracker.addMovement(ev);
        int action = ev.getAction();
        int currentX = (int) ev.getX();
        int currentY = (int) ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = currentX - mLastX;
                int moveScrollX = getScrollX() - deltaX;
                //处理边缘
                if(!((moveScrollX <= 0) || (moveScrollX >= (mChildCount - 1) * childWidth))) {
                    scrollBy(-deltaX, 0);
                }
                break;
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();
                velocityTracker.computeCurrentVelocity(1000);
                float xVelocity = velocityTracker.getXVelocity();
                //速度快于800px每秒，让其切换View
                if (Math.abs(xVelocity) >= 800) {
                    mChildrenIndex = xVelocity > 0 ? mChildrenIndex - 1 : mChildrenIndex + 1;
                } else {
                    //当前View滑动距离超过一半的话，滑动到下一个或者上一个View
                    mChildrenIndex = (scrollX + childWidth / 2) / childWidth;
                }
                //处理边界
                mChildrenIndex = Math.max(0, Math.min(mChildrenIndex, mChildCount - 1));

                int dx = mChildrenIndex * childWidth - scrollX;
                //让其自动滑动View的边缘
                smoothScrollBy(dx, 0);
                velocityTracker.clear();
                break;
        }
        mLastX = currentX;
        mLastY = currentY;
        return true;
    }

    private void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(getScrollX(), 0, dx, 0);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childCount = getChildCount();
        if (childCount == 0) {
            setMeasuredDimension(widthSize, heightSize);
            return;
        }

        int firstChildMeasureWidth = getChildAt(0).getMeasuredWidth();
        int firstChildMeasureHeight = getChildAt(0).getMeasuredHeight();

        childWidth = firstChildMeasureWidth;

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(firstChildMeasureWidth * childCount, firstChildMeasureHeight);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(firstChildMeasureWidth * childCount, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, firstChildMeasureHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mChildCount = getChildCount();
        int childLeft = 0;
        for (int i = 0; i < mChildCount; i++) {
            View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            child.layout(childLeft, 0, childLeft + width, height);
            childLeft += width;
        }
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }
}
