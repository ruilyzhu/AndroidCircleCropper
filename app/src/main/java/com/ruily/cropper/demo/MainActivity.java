package com.ruily.cropper.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ruily.crop.FileUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_GALLERY = 21;
    private static final int REQUEST_CAMERA = 20;
    private static final int REQUEST_CROP = 22;
    private String mCurrentPhotoPath;
    private ImageView mIvCropShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIvCropShow = (ImageView) findViewById(R.id.iv_crop_show);
        findViewById(R.id.btn_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoDialog();
            }
        });


    }

    private void showPhotoDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setItems(new String[]{"拍照", "图库", "取消"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (which == 0) {
                    dispatchTakePictureIntent();
                } else if (which == 1) {
                    startGalleryIntent();
                }

            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            goCrop(Uri.parse(mCurrentPhotoPath));
        } else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            goCrop(data.getData());
        } else if (requestCode == REQUEST_CROP && resultCode == RESULT_OK) {
            mIvCropShow.setImageURI(data.getData());
        }
    }

    private void goCrop(Uri sourUri) {
        Intent intent = new Intent(MainActivity.this, CropActivity.class);
        intent.setData(sourUri);
        startActivityForResult(intent, REQUEST_CROP);
    }

    private void startGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            photoFile = FileUtils.getOutputMediaFileUri();
            mCurrentPhotoPath = "file:" + photoFile.getAbsolutePath();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }
}
