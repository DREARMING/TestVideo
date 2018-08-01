package com.mvcoder.testvideo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;

public class ImagesActivity extends AppCompatActivity {

    private Button btFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        initView();
    }

    private void initView() {
        btFileUri = findViewById(R.id.btFileUri);
        btFileUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // finishA();
               // showExternalInfo();
                finishB();
            }
        });
    }

    private void finishB() {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(file, "icon_API.png");
        Uri uri = FileProvider.getUriForFile(this,"com.mvcoder.testvideo.fileprovider",imageFile);
        LogUtils.d("uri ： " + uri.toString());
        Intent intent = getIntent();
        intent.setDataAndType(uri, getContentResolver().getType(uri));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showExternalInfo(){
        if(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            printInfo();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

    }

    private void printInfo(){
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        LogUtils.d("file path : " + file.getAbsolutePath());
        File imageFile = new File(file, "icon_API.png");
        Uri uri = FileProvider.getUriForFile(this,"com.mvcoder.testvideo.fileprovider",imageFile);
        LogUtils.d("uri ： " + uri.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        printInfo();
                    }
                }
                break;
        }
    }

    void finishA(){
        File imageFile = new File(getFilesDir(), "icon_API.png");
        LogUtils.d("imagefile exist : " + imageFile.exists());
        Uri uri = FileProvider.getUriForFile(ImagesActivity.this,"com.mvcoder.testvideo.fileprovider",imageFile);
        LogUtils.d("uri : " + uri.toString() + " , exist : " + imageFile.exists());

        Intent intent = getIntent();
        intent.setDataAndType(uri, getContentResolver().getType(uri));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        setResult(RESULT_OK, intent);
        finish();
    }

}
