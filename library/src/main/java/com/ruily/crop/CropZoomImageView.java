package com.ruily.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by Ruily on 15/12/30.
 */
public class CropZoomImageView extends ImageView implements ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = CropZoomImageView.class.getSimpleName();

    public static final float MAX_SCALE = 4.0f;

    private float initScale = 1.0f;

    private ScaleGestureDetector scaleGestureDetector;
    private final float[] matrixValues = new float[9];
    private boolean once = true;

    private final Matrix mScaleMatrix = new Matrix();


    /**
     * 水平方向与View的边距
     */
    private int mHorizontalPadding;
    /**
     * 垂直方向与View的边距
     */
    private int mVerticalPadding;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    public CropZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
        scaleGestureDetector = new ScaleGestureDetector(context, this);
        this.setOnTouchListener(this);
    }

    public void setHorizontalPadding(int mHorizontalPadding) {
        this.mHorizontalPadding = mHorizontalPadding;
    }

    @Override
    public void onGlobalLayout() {
        if (once) {

            Drawable d = getDrawable();
            if (d == null)
                return;
            Log.e(TAG, d.getIntrinsicWidth() + "-" + d.getIntrinsicHeight());

            int width = getWidth();
            int height = getHeight();
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();
            mVerticalPadding = (height - (width - mHorizontalPadding * 2)) / 2;

            float scale = 1.0f;
            if (dw < (width - mHorizontalPadding * 2) && dh >= (height - mVerticalPadding * 2)) {
                scale = (width * 1.0f - mHorizontalPadding * 2) / dw;
            }
            if (dh < (height - mVerticalPadding * 2) && dw >= (width - mHorizontalPadding * 2)) {
                scale = (height * 1.0f - mVerticalPadding * 2) / dh;
            }
            if (dw < (width - mHorizontalPadding * 2) && dh < (height - mVerticalPadding * 2)) {
                float wScale = (width * 1.0f - mHorizontalPadding * 2) / dw;
                float hScale = (height * 1.0f - mVerticalPadding * 2) / dh;
                scale = Math.max(wScale, hScale);

            }
            initScale = scale;
            mScaleMatrix.postTranslate((width - dw) / 2, (height - dh) / 2);
            mScaleMatrix.postScale(scale, scale, width / 2, height / 2);
            setImageMatrix(mScaleMatrix);
            once = false;
        }
    }

    /**
     * 剪切图片，返回剪切后的bitmap对象
     *
     * @return
     */
    public Bitmap clip() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return Bitmap.createBitmap(bitmap, mHorizontalPadding,
                mVerticalPadding, getWidth() - 2 * mHorizontalPadding,
                getWidth() - 2 * mHorizontalPadding);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (getDrawable() == null)
            return true;
        float scale = getScale();
        float scaleFactor = detector.getScaleFactor();

        if ((scale < MAX_SCALE && scaleFactor > 1.0f) || (scale > initScale && scaleFactor < 1.0f)) {
            if (scaleFactor * scale < initScale) {
                scaleFactor = initScale / scale;
            }
            if (scaleFactor * scale > MAX_SCALE) {
                scaleFactor = MAX_SCALE / scale;
            }

            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            checkRectWhenScacle();
            setImageMatrix(mScaleMatrix);
        }

        return true;
    }

    public final float getScale() {
        mScaleMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    private void checkRectWhenScacle() {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();
        if (rect.width() > width) {
            if (rect.left > 0) {
                deltaX = -rect.left;
            }
            if (rect.right < width) {  //right坐标位置 小于view宽, 则width-right 计算往右的偏移量
                deltaX = width - rect.right;
            }
        }
        if (rect.height() > height) {
            if (rect.top > 0) {
                deltaY = -rect.top;
            }
            if (rect.bottom < height) {
                deltaY = height - rect.bottom;
            }
        }

        if (rect.width() < width) {
            deltaX = width * 0.5f - rect.right + rect.width() * 0.5f;
        }
        if (rect.height() < height) {
            deltaY = height * 0.5f - rect.bottom + rect.height() * 0.5f;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }


    public RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rectF = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    private int lastPointerCount;
    private float mLastX;
    private float mLastY;


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        float x = 0, y = 0;
        final int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x = x / pointerCount;
        y = y / pointerCount;


        /**
         * 每当触摸点发生变化时，重置mLasX , mLastY
         */
        if (pointerCount != lastPointerCount) {
            mLastX = x;
            mLastY = y;
        }
        lastPointerCount = pointerCount;

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - mLastX;
                float dy = y - mLastY;
                RectF rectF = getMatrixRectF();
                if (getDrawable() != null) {
                    // 如果宽度小于屏幕宽度，则禁止左右移动
                    if (rectF.width() < getWidth()) {
                        dx = 0;
                    }
                    // 如果高度小雨屏幕高度，则禁止上下移动
                    if (rectF.height() < getHeight()) {
                        dy = 0;
                    }
                    mScaleMatrix.postTranslate(dx, dy);
                    checkBorder();
                    setImageMatrix(mScaleMatrix);
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                lastPointerCount = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                lastPointerCount = 0;
                break;
        }

        return true;
    }


    /**
     * 边界检测
     */
    private void checkBorder() {

        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();
        // 如果宽或高大于屏幕，则控制范围
        if (rect.width() >= width - 2 * mHorizontalPadding) {
            if (rect.left > mHorizontalPadding) {
                deltaX = -rect.left + mHorizontalPadding;
            }
            if (rect.right < width - mHorizontalPadding) {
                deltaX = width - mHorizontalPadding - rect.right;
            }
        }
        if (rect.height() >= height - 2 * mVerticalPadding) {
            if (rect.top > mVerticalPadding) {
                deltaY = -rect.top + mVerticalPadding;
            }
            if (rect.bottom < height - mVerticalPadding) {
                deltaY = height - mVerticalPadding - rect.bottom;
            }
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);

    }

}
