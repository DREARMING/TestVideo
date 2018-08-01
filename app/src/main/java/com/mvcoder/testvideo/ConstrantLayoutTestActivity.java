package com.mvcoder.testvideo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ConstrantLayoutTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constrant_layout_test);
        View view = null;
        view.scrollTo(0,0);
    }
}
