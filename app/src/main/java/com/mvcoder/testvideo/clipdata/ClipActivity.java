package com.mvcoder.testvideo.clipdata;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.mvcoder.testvideo.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ClipActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip);
        testClipData();
    }


    private void testClipData(){
        LogUtils.d("package name: " + getPackageName());
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/"  + R.raw.hello);

        try {
            AssetFileDescriptor descriptor = getContentResolver().openAssetFileDescriptor(uri,"r");
            if(descriptor == null){
                LogUtils.d("descript is null");
            }else {
                try {
                    FileInputStream fis = new FileInputStream(descriptor.getFileDescriptor());
                    int len = -1;
                    byte[] bs = new byte[1024 * 8];
                    StringBuilder builder = new StringBuilder();
                    while ((len = fis.read(bs)) != -1) {
                        builder.append(new String(bs,0,len));
                    }
                    LogUtils.d("content : " + builder.toString());
                    fis.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
