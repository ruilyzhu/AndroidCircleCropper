package com.ruily.crop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by Ruily on 15/12/31.
 */
public class CropImageBorderView extends View {
    private int mBorderWidth = 1;
    private int mBorderColor = Color.parseColor("#FFFFFF");


    private int mHorizontalPadding = 20;
    private int mVerticalPadding;

    private int mWidth;
    private Paint mPaint;

    public CropImageBorderView(Context context) {
        this(context, null);
    }

    public CropImageBorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageBorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBorderWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBorderWidth, getResources().getDisplayMetrics());
//        mPaint = new Paint();
//        mPaint.setAntiAlias(true);
        mPaint = newBorderPaint(context);
        clipPath = new Path();
    }

    private Path clipPath;

    public void setHorizontalPadding(int mHorizontalPadding) {
        this.mHorizontalPadding=mHorizontalPadding;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWidth = getWidth() - 2 * mHorizontalPadding;
        mVerticalPadding = (getHeight() - mWidth) / 2;
//        mPaint.setColor(Color.parseColor("#aa000000"));
//        mPaint.setStyle(Paint.Style.FILL);
//
//        //draw左边的区域
//        canvas.drawRect(0, 0, mHorizontalPadding, getHeight(), mPaint);
//        //draw右边
//        canvas.drawRect(getWidth() - mHorizontalPadding, 0, getWidth(), getHeight(), mPaint);
//        //draw上边
//        canvas.drawRect(mHorizontalPadding, 0, getWidth() - mHorizontalPadding, mVerticalPadding, mPaint);
//        //draw底边
//        canvas.drawRect(mHorizontalPadding, getHeight() - mVerticalPadding, getWidth() - mHorizontalPadding, getHeight(), mPaint);
        float cx = getWidth() / 2;
        float cy = getHeight() / 2;
        float radius = mWidth / 2;
        clipPath.addCircle(cx, cy, radius, Path.Direction.CW);
        canvas.clipPath(clipPath, Region.Op.DIFFERENCE);
        canvas.drawColor(Color.parseColor("#aa000000"));
        if (Build.VERSION.SDK_INT >= 23) {
        } else {
            canvas.restore();
        }
        canvas.drawCircle(cx, cy, radius, mPaint);

    }

    public Paint newBorderPaint(Context context) {

        // Set the line thickness for the crop window border.
        final float lineThicknessPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());

        final Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(Color.parseColor("#FFFFFFFF"));
        borderPaint.setStrokeWidth(lineThicknessPx);
        borderPaint.setStyle(Paint.Style.STROKE);

        return borderPaint;
    }
}
