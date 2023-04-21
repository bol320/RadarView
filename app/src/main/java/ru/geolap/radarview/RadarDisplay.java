package ru.geolap.radarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Iterator;

///////////////////////////////////////////////////////////////////////////////////////////////////
public class RadarDisplay extends View {

    private final int DEFAULT_COLOR = Color.parseColor("#91D7F4");

    private int mCircleColor   = DEFAULT_COLOR;
    private int mSweepColor    = DEFAULT_COLOR;
    private int mRaindropColor = DEFAULT_COLOR;

    private int mCircleNum   = 3;
    private int mRaindropNum = 4;
    private int mFrq         = 1000;

    private float mFlicker = 3.0f;
    private float mDegrees = 0.0f;

    private float mDeltaDegrees = 1.0f;

    private Paint mCirclePaint;
    private Paint mSweepPaint;
    private Paint mRaindropPaint;

    private boolean isScanning     = false;
    private boolean isShowCross    = true;
    private boolean isShowRaindrop = true;

    private final ArrayList<Raindrop> mRaindrops = new ArrayList<>();


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    Handler mHandler = new Handler();
    Runnable mTick = new Runnable() {
        @Override
        public void run() {
            invalidate();
            mHandler.postDelayed(this, mFrq);
        }
    };


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public RadarDisplay(Context context) {
        super(context);
        init();
    }

///////////////////////////////////////////////////////////////////////////////////////////////////
    public RadarDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs);
        init();
    }

///////////////////////////////////////////////////////////////////////////////////////////////////
    public RadarDisplay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrs(context, attrs);
        init();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void getAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RadarDisplay);

            mCircleColor = mTypedArray.getColor(R.styleable.RadarDisplay_circleColor, DEFAULT_COLOR);
            mCircleNum   = mTypedArray.getInt(R.styleable.RadarDisplay_circleNum, mCircleNum);

            if (mCircleNum < 1) {
                mCircleNum = 3;
            }

            mSweepColor    = mTypedArray.getColor(R.styleable.RadarDisplay_sweepColor, DEFAULT_COLOR);
            mRaindropColor = mTypedArray.getColor(R.styleable.RadarDisplay_raindropColor, DEFAULT_COLOR);
            mRaindropNum   = mTypedArray.getInt(R.styleable.RadarDisplay_raindropNum, mRaindropNum);
            isShowCross    = mTypedArray.getBoolean(R.styleable.RadarDisplay_showCross, true);
            isShowRaindrop = mTypedArray.getBoolean(R.styleable.RadarDisplay_showRaindrop, true);

            mFlicker = mTypedArray.getFloat(R.styleable.RadarDisplay_flicker, mFlicker);
            if (mFlicker <= 0) {
                mFlicker = 3;
            }
            mTypedArray.recycle();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void init() {
        mCirclePaint = new Paint();
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStrokeWidth(2);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setAntiAlias(true);

        mRaindropPaint = new Paint();
        mRaindropPaint.setStyle(Paint.Style.FILL);
        mRaindropPaint.setAntiAlias(true);

        mSweepPaint = new Paint();
        mSweepPaint.setAntiAlias(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultSize = dp2px(getContext(), 200);
        setMeasuredDimension(measureWidth(widthMeasureSpec, defaultSize),
                             measureHeight(heightMeasureSpec, defaultSize));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private int measureWidth(int measureSpec, int defaultSize) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        result = Math.max(result, getSuggestedMinimumWidth());
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private int measureHeight(int measureSpec, int defaultSize) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        result = Math.max(result, getSuggestedMinimumHeight());
        return result;
    }

    public int getFrq() {
        return mFrq;
    }

    public void setFrq(int mFrq) {
        this.mFrq = mFrq;
    }

    public float getDeltaDegrees() {
        return mDeltaDegrees;
    }

    public void setDeltaDegrees(float mDeltaDegrees) {
        this.mDeltaDegrees = mDeltaDegrees;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onDraw(Canvas canvas) {

        int width  = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        int radius = Math.min(width, height) / 2;

        int cx = getPaddingLeft() + (getWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        int cy = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;

        drawCircle(canvas, cx, cy, radius);

        if (isShowCross) {
            drawCross(canvas, cx, cy, radius);
        }

        if (isScanning) {
            if (isShowRaindrop) {
                drawRaindrop(canvas, cx, cy, radius);
            }

            drawSweep(canvas, cx, cy, radius);

            mDegrees = (mDegrees + mDeltaDegrees) % 360;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void drawCircle(Canvas canvas, int cx, int cy, int radius) {
        for (int i = 0; i < mCircleNum; i++) {
            canvas.drawCircle(cx, cy, radius - (radius / mCircleNum * i), mCirclePaint);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void drawCross(@NonNull Canvas canvas, int cx, int cy, int radius) {
        canvas.drawLine(cx - radius, cy, cx + radius, cy, mCirclePaint);

        canvas.drawLine(cx, cy - radius, cx, cy + radius, mCirclePaint);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void generateRaindrop(int cx, int cy, int radius) {

        if (mRaindrops.size() < mRaindropNum) {
            boolean probability = (int) (Math.random() * 20) == 0;
            if (probability) {
                int x = 0;
                int y = 0;
                int xOffset = (int) (Math.random() * (radius - 20));
                int yOffset = (int) (Math.random() * (int) Math.sqrt(1.0 * (radius - 20) * (radius - 20) - xOffset * xOffset));

                if ((int) (Math.random() * 2) == 0) {
                    x = cx - xOffset;
                } else {
                    x = cx + xOffset;
                }

                if ((int) (Math.random() * 2) == 0) {
                    y = cy - yOffset;
                } else {
                    y = cy + yOffset;
                }

                mRaindrops.add(new Raindrop(x, y, 0, mRaindropColor));
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void removeRaindrop() {
        Iterator<Raindrop> iterator = mRaindrops.iterator();

        while (iterator.hasNext()) {
            Raindrop raindrop = iterator.next();
            if (raindrop.radius > 20 || raindrop.alpha < 0) {
                iterator.remove();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void drawRaindrop(Canvas canvas, int cx, int cy, int radius) {
        generateRaindrop(cx, cy, radius);
        for (Raindrop raindrop : mRaindrops) {
            mRaindropPaint.setColor(raindrop.changeAlpha());
            canvas.drawCircle(raindrop.x, raindrop.y, raindrop.radius, mRaindropPaint);

            raindrop.radius += 1.0f * 20 / 60 / mFlicker;
            raindrop.alpha -= 1.0f * 255 / 60 / mFlicker;
        }
        removeRaindrop();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void drawSweep(@NonNull Canvas canvas, int cx, int cy, int radius) {
        SweepGradient sweepGradient = new SweepGradient(cx, cy,
                new int[]{Color.TRANSPARENT, changeAlpha(mSweepColor, 0), changeAlpha(mSweepColor, 168),
                        changeAlpha(mSweepColor, 255), changeAlpha(mSweepColor, 255)
                }, new float[]{0.0f, 0.6f, 0.99f, 0.998f, 1f});

        mSweepPaint.setShader(sweepGradient);
        canvas.rotate(-90 + mDegrees, cx, cy);
        canvas.drawCircle(cx, cy, radius, mSweepPaint);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void start() {
        mHandler.removeCallbacks(mTick);
        mHandler.post(mTick);

        if (!isScanning) {
            isScanning = true;
            invalidate();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void stop() {
        mHandler.removeCallbacks(mTick);

        if (isScanning) {
            isScanning = false;
            mRaindrops.clear();
            mDegrees = 0.0f;
        }
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private static class Raindrop {

        int x;
        int y;
        float radius;
        int color;
        float alpha = 255;

        public Raindrop(int x, int y, float radius, int color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color  = color;
        }

        public int changeAlpha() {
            return RadarDisplay.changeAlpha(color, (int) alpha);
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private static int dp2px(@NonNull Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private static int changeAlpha(int color, int alpha) {
        int red   = Color.red(color);
        int green = Color.green(color);
        int blue  = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
