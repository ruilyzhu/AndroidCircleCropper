package com.ruily.crop;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLES10;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Ruily on 16/1/4.
 */
public class CropImageLayout extends RelativeLayout {
    private final Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG;

    private static final int SIZE_DEFAULT = 2048;

    private CropZoomImageView mCropIv;
    private CropImageBorderView mBorderView;
    private int mHorizontalPadding=20;
    private Context context;


    private Uri sourceUri;
    private Uri mSaveUri;

    public CropImageLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.rl_crop_layot, this);
        mCropIv = (CropZoomImageView) findViewById(R.id.iv_crop);
        mBorderView = (CropImageBorderView) findViewById(R.id.borderView);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CropImageLayout);
        mHorizontalPadding = (int) a.getDimension(R.styleable.CropImageLayout_mHorizontalPadding, 20);
        mCropIv.setHorizontalPadding(mHorizontalPadding);
        mBorderView.setHorizontalPadding(mHorizontalPadding);
        a.recycle();

    }

    public void setImageUri(Uri sourceUri
                            ) {
        this.sourceUri = sourceUri;

        Bitmap returnBitmap = getBitmap(sourceUri);
        if (returnBitmap != null) {
            mCropIv.setImageBitmap(returnBitmap);
        }
    }

    private Bitmap getBitmap(Uri imgPath) {
        Bitmap returnedBitmap = null;
        try {
            int sampleSize = calculateBitmapSampleSize(imgPath);
            InputStream is = context.getContentResolver().openInputStream(imgPath);
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = sampleSize;
            returnedBitmap = BitmapFactory.decodeStream(is, null, option);
            returnedBitmap = fixOrientationBugOfProcessedBitmap(returnedBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnedBitmap;
    }

    public boolean saveOutput(CropListener cropListener) {
        if (mSaveUri == null) {
            mSaveUri = Uri.fromFile(FileUtils.getOutputMediaFileUri());
        }
        Bitmap croppedImage = mCropIv.clip();
        if (mSaveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = context.getContentResolver().openOutputStream(mSaveUri);
                if (outputStream != null) {
                    croppedImage.compress(mOutputFormat, 90, outputStream);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                cropListener.onError();
                return false;
            } finally {
                CropUtils.closeSilently(outputStream);
            }
        } else {
            cropListener.onError();
            return false;
        }
        croppedImage.recycle();
        cropListener.onSuccess(mSaveUri);
        return true;
    }

    private int getMaxImageSize() {
        int textureLimit = getMaxTextureSize();
        if (textureLimit == 0) {
            return SIZE_DEFAULT;
        } else {
            return Math.min(textureLimit, SIZE_DEFAULT);
        }
    }

    private int getMaxTextureSize() {
        // The OpenGL texture size is the maximum size that can be drawn in an ImageView
        int[] maxSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
        return maxSize[0];
    }

    private int calculateBitmapSampleSize(Uri bitmapUri) throws IOException {
        InputStream is = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            is = context.getContentResolver().openInputStream(bitmapUri);
            BitmapFactory.decodeStream(is, null, options); // Just get image size
        } finally {
            CropUtils.closeSilently(is);
        }

        int maxSize = getMaxImageSize();
        int sampleSize = 1;
        while (options.outHeight / sampleSize > maxSize || options.outWidth / sampleSize > maxSize) {
            sampleSize = sampleSize << 1;
        }
        return sampleSize;
    }

    private Bitmap fixOrientationBugOfProcessedBitmap(Bitmap bitmap) {
        try {
            if (getCameraPhotoOrientation(context, sourceUri) == 0) {
                return bitmap;
            } else {
                Matrix matrix = new Matrix();
                matrix.postRotate(getCameraPhotoOrientation(context, sourceUri));
                // recreate the new Bitmap and set it back
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private int getCameraPhotoOrientation(@NonNull Context context, Uri imageUri) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            ExifInterface exif = new ExifInterface(
                    imageUri.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public interface CropListener {
        void onSuccess(Uri saveUri);

        void onError();
    }

}
