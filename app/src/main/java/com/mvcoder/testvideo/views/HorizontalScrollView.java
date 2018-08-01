package com.mvcoder.testvideo.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class HorizontalScrollView extends ViewGroup {


    private int childWidth = 0;

    private Scroller mScroller;

    private int mLastX = -1;
    private int mLastY = -1;

    public HorizontalScrollView(Context context) {
        this(context, null);
    }

    public HorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        mScroller = new Scroller(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        int currentX = (int) ev.getX();
        int currentY = (int) ev.getY();
        boolean intercept = false;
        switch (action){
            case MotionEvent.ACTION_DOWN:
                if(!mScroller.isFinished()){
                    mScroller.abortAnimation();
                }
                intercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = currentX - mLastX;
                int deltaY = currentY - mLastY;
                if(Math.abs(deltaX) > Math.abs(deltaY)){
                    intercept = true;
                }else{
                    intercept = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
        }
        mLastX = currentX;
        mLastY = currentY;
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        int currentX = (int) ev.getX();
        int currentY = (int) ev.getY();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = currentX - mLastX;
                scrollBy(-deltaX,0);
                break;
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();

                break;
        }
        mLastX = currentX;
        mLastY = currentY;
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChildren(widthMeasureSpec,heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childCount = getChildCount();
        if(childCount == 0) {
            setMeasuredDimension(widthSize, heightSize);
            return;
        }

        int firstChildMeasureWidth = getChildAt(0).getMeasuredWidth();
        int firstChildMeasureHeight = getChildAt(0).getMeasuredHeight();

        childWidth = firstChildMeasureWidth;

        if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(firstChildMeasureWidth * childCount, firstChildMeasureHeight);
        }else if(widthMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(firstChildMeasureWidth * childCount, heightSize);
        }else if(heightMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(widthSize, firstChildMeasureHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int childLeft = 0;
        for(int i = 0; i < childCount; i++){
            View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            child.layout(childLeft, 0, childLeft + width, height);
            childLeft += width;
        }
    }


    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }
}
