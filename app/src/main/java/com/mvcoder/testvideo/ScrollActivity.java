package com.mvcoder.testvideo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.mvcoder.testvideo.views.HorizontalScrollView;

public class ScrollActivity extends AppCompatActivity {

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 10:
                    int width = linearLayout.getWidth();
                    int height = linearLayout.getHeight();
                    LogUtils.d("viewgroup ----- width : " + width + " , height:" + height);

                    int width1 = firstChild.getWidth();
                    int height1 = firstChild.getHeight();
                    LogUtils.d("firstChild ----- width : " + width1 + " , height:" + height1);
                    break;
            }
        }
    };

    HorizontalScrollView linearLayout;
    View firstChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll);
        initView();
    }

    private void initView() {
        linearLayout = findViewById(R.id.horizontalView);
        firstChild = findViewById(R.id.firstChild);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getViewHeight();
    }

    private void getViewHeight() {
        handler.sendEmptyMessageDelayed(10,5 * 1000);

    }
}
