package com.ruily.cropper.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ruily.crop.CropImageLayout;

/**
 * Created by Ruily on 16/1/5.
 */
public class CropActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_crop_layout);
        Intent intent = getIntent();
        final CropImageLayout mCropLayout = (CropImageLayout) findViewById(R.id.cil_crop);
        mCropLayout.setImageUri(intent.getData());
        findViewById(R.id.btn_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropLayout.saveOutput(new CropImageLayout.CropListener() {
                    @Override
                    public void onSuccess(Uri saveUri) {
                        Intent intent = new Intent();
                        intent.setData(saveUri);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onError() {

                    }
                });
            }
        });
        findViewById(R.id.btn_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
