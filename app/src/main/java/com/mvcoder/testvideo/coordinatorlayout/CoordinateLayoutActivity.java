package com.mvcoder.testvideo.coordinatorlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;
import com.jakewharton.disklrucache.DiskLruCache;
import com.mvcoder.testvideo.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;

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
    @BindView(R.id.iv_bg)
    ImageView ivBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        setContentView(R.layout.activity_coordinate_layout);
        ButterKnife.bind(this);
        initToolbar();
        initView();
    }

    private void initStatusBar() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){

        }
    }

    @SuppressLint("StaticFieldLeak")
    private  AsyncTask<String,Integer,Boolean> task = new AsyncTask<String, Integer, Boolean>() {
        @Override
        protected Boolean doInBackground(String... strings) {
            return null;
        }

    };

    private void initView() {
        // collapsingTool   barLayout.setTitle("Title");
        task.execute("");
        Looper.prepare();

        HandlerThread handlerThread;

        FutureTask<String> futureTask = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return null;
            }
        });

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        ExecutorService service = Executors.newCachedThreadPool();

        LruCache<String,Bitmap> bitmapLruCache = new LruCache<String,Bitmap>(1000){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return super.sizeOf(key, value);
            }
        };

        try {
            Thread thread;
            DiskLruCache diskLruCache = DiskLruCache.open(new File(""),1,1,1000);
            DiskLruCache.Editor editor = diskLruCache.edit("");
            editor.commit();
            editor.newInputStream(0);

            DiskLruCache.Snapshot snapshot = diskLruCache.get("");
            diskLruCache.close();
            snapshot.getInputStream(0);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Message message;
        ThreadLocal threadLocal;

        View view = null;
        ObjectAnimator animator = ObjectAnimator.ofInt(view,"mTranslationX",0,100);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

            }
        });


        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {

            }

            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {

            }
        });
        animator.start();

        TranslateAnimation translateAnimation = new TranslateAnimation(0,100,0,200);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        translateAnimation.start();



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
        menu.add(0, 1, 0, "opotion1");
        menu.findItem(1).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }
}
