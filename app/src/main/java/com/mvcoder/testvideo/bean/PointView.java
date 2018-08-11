package com.mvcoder.testvideo.bean;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.mvcoder.testvideo.R;

public class PointView extends View {

    private static final int DEFAULT_COLOR = Color.BLUE;
    private static final int DEFAULT_RADIUS = 30;
    private int mColor = DEFAULT_COLOR;
    private int mRadius = DEFAULT_RADIUS;
    private Point mPoint = new Point(mRadius, mRadius);

    private Paint mPaint;

    public PointView(Context context) {
        this(context, null);
    }

    public PointView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PointView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PointView, defStyleAttr, 0);
        int n = typedArray.getIndexCount();
        int x = mRadius;
        int y = mRadius;
        for (int i = 0; i < n; i++) {
            int index = typedArray.getIndex(i);
            switch (index) {
                case R.styleable.PointView_color:
                    mColor = typedArray.getColor(index, Color.BLUE);
                    break;
                case R.styleable.PointView_radius:
                    int dp = (int) typedArray.getDimension(index, 10);
                    mRadius = (int) (getResources().getDisplayMetrics().density * dp);
                    break;
                case R.styleable.PointView_raduisX:
                    int x1 = (int) typedArray.getDimension(index, mPoint.getX());
                    x = (int) (getResources().getDisplayMetrics().density * x1);
                    break;
                case R.styleable.PointView_raduisY:
                    int y1 = (int) typedArray.getDimension(index, mPoint.getY());
                    y = (int) (getResources().getDisplayMetrics().density * y1);
                    break;
                default:
                    break;
            }
        }

        typedArray.recycle();
        if(x != mPoint.getX() || y != mPoint.getY()){
            mPoint.setX(x);
            mPoint.setY(y);
        }

       /* ViewGroup.LayoutParams params1 = getLayoutParams();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
        params.leftMargin = mPoint.getX() - mRadius;
        params.topMargin = mPoint.getY() - mRadius;*/
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mColor);
    }

    public Point getmPoint() {
        return mPoint;
    }

    public void setmPoint(Point mPoint) {
        this.mPoint = mPoint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = widthSize;
        int height = heightSize;

        if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            width = mRadius * 2 + getPaddingLeft() + getPaddingRight();
            height = mRadius * 2 + getPaddingTop() + getPaddingBottom();
        }else if(widthMode == MeasureSpec.AT_MOST){
            width =  mRadius * 2 + getPaddingLeft() + getPaddingRight();
        }else if(heightMode == MeasureSpec.AT_MOST){
            height = mRadius * 2 + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left + mPoint.getX() - mRadius, top + mPoint.getY() - mRadius, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getPaddingLeft() + mRadius, getPaddingTop() + mRadius, mRadius, mPaint);
    }
}
