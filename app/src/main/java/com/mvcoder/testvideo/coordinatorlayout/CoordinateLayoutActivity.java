package com.mvcoder.testvideo.coordinatorlayout;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.blankj.utilcode.util.LogUtils;
import com.mvcoder.testvideo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CoordinateLayoutActivity extends AppCompatActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.bt_show_snack)
    Button btShowSnack;
    @BindView(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.constrantLayout)
    CoordinatorLayout constrantLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinate_layout);
        ButterKnife.bind(this);
        initToolbar();
        initView();
    }

    private void initView() {
       // collapsingToolbarLayout.setTitle("Title");
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
    }

    private void showSnakeBar() {
        Snackbar.make(fab, "text", Snackbar.LENGTH_INDEFINITE).setAction("confirm", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d("click snakebar confirm button");
            }
        }).show();
    }

    @OnClick(R.id.bt_show_snack)
    public void onViewClicked() {
        showSnakeBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,0,"opotion1");
        menu.findItem(1).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }
}
