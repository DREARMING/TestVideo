package com.mvcoder.testvideo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;

import java.io.FileNotFoundException;

public class TestUriActivity extends AppCompatActivity {

    private Button btShow;
    private ImageView ivShow;
    private TextView tvUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_uri);
        initView();
    }

    private void initView() {
        ivShow = findViewById(R.id.iv_show);
        btShow = findViewById(R.id.bt_show);
        tvUri = findViewById(R.id.tvUri);
        btShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinToSelectActivity();
            }
        });
    }

    private void joinToSelectActivity() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 100:
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    if(uri != null){
                        try {
                            if(checkCallingUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION) == PackageManager.PERMISSION_GRANTED){
                                ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                                Bitmap bitmap =  BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor());
                                ivShow.setImageBitmap(bitmap);
                            }else{
                                LogUtils.d("no uri permission");
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }
                break;
        }
    }
}
