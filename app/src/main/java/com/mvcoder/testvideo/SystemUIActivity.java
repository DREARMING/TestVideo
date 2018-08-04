package com.mvcoder.testvideo;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowManager;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

public class SystemUIActivity extends AppCompatActivity {

    private boolean hideStateusBar = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(hideStateusBar && Build.VERSION.SDK_INT < 16){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_system_ui);
        initView();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private void initView() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if((visibility & View.SYSTEM_UI_FLAG_IMMERSIVE) == 0){
                    ToastUtils.showShort("show status bar");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideSystemUI();
                            LogUtils.d("hide SystemUi method call");
                        }
                    },5000);
                }else{
                    ToastUtils.showShort("show status bar");

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    private void hideSystemUI(){
        if(hideStateusBar && Build.VERSION.SDK_INT >= 16){
            View view = getWindow().getDecorView();
            int uiOption = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN; /*View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;*/
            view.setSystemUiVisibility(uiOption);
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null)
                actionBar.hide();
        }
    }
}
