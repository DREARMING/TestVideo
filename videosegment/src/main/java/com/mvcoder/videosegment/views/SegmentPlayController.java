package com.mvcoder.videosegment.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.mvcoder.videosegment.BuildConfig;
import com.mvcoder.videosegment.R;

public class SegmentPlayController extends FrameLayout implements View.OnClickListener, ValueAnimator.AnimatorUpdateListener{

    private Context mContext;
    private RadioGroup autoPlayRG;
    private AppCompatSeekBar playSpeedSB;
    private ImageButton btBack;
    private ImageButton btSetting;
    private ImageButton btNextNode;
    private ImageButton btLastNode;
    private ImageButton btPlay;
    private LinearLayout menuLayout;

    private TextView tvSegmentIndex;
    private TextView tvSegmentDuration;

    private Player player;
    private SegmentVideoComponent component;

    private ValueAnimator hideAnim;
    private ValueAnimator showAnim;

    private final static int DEFAULT_SPEED = 10;
    private final static int DEFAULT_ANIMATION_MS = 100;

    private int currentSpeed = DEFAULT_SPEED;

    private boolean autoPlay = false;
    private boolean menuOpen = true;

    public SegmentPlayController(@NonNull Context context) {
        this(context, null);
    }

    public SegmentPlayController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SegmentPlayController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    public void setVideoComponent(@NonNull SegmentVideoComponent component){
        this.component = component;
        player = component.getPlayer();
    }

    public void setSegmentIndex(int index){
        tvSegmentIndex.setText("片段序号：" + (index + 1));
    }

    public void setSegmentDuration(String duration){
        tvSegmentDuration.setText("时间段: " + duration);
    }

    public void setAutoPlay(boolean autoPlay){
        int checkId = autoPlay ? R.id.rb_autoplay_open : R.id.rb_autoplay_close;
        if(autoPlayRG.getCheckedRadioButtonId() != checkId)
            autoPlayRG.check(checkId);
    }

    private int menuLayoutX = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(menuOpen){
            if(menuLayoutX == -1){
                menuLayoutX = menuLayout.getLeft();
            }
            if(event.getX() < menuLayoutX){
                if(event.getAction() == MotionEvent.ACTION_UP){
                    menuLayoutX = -1;
                        hideMenu();
                }
                return true;
            }
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(hideAnim != null){
            hideAnim.cancel();
        }
        if(showAnim != null){
            showAnim.cancel();
        }
    }


    private void initPlayer(){
        int curSpeed = playSpeedSB.getProgress();
        if(curSpeed != DEFAULT_SPEED){
            setPlayerSpeed((float) curSpeed / 10);
        }
        setAutoPlaySegment(autoPlay);
    }

    private void initView(){
        LayoutInflater.from(mContext).inflate(R.layout.view_controller, this);
        tvSegmentDuration = findViewById(R.id.tv_segment_duration);
        tvSegmentIndex = findViewById(R.id.tv_segment_index);

        autoPlayRG = findViewById(R.id.rg_autoplay);
        playSpeedSB = findViewById(R.id.sb_playpeed);

        btBack = findViewById(R.id.ib_back);
        btLastNode = findViewById(R.id.ib_last_segment);
        btNextNode = findViewById(R.id.ib_next_segment);
        btPlay = findViewById(R.id.ib_play);
        btSetting = findViewById(R.id.ib_setting);
        menuLayout = findViewById(R.id.ll_menuLayout);

        btBack.setOnClickListener(this);
        btLastNode.setOnClickListener(this);
        btNextNode.setOnClickListener(this);
        btPlay.setOnClickListener(this);
        btBack.setOnClickListener(this);
        btSetting.setOnClickListener(this);

        autoPlayRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rb_autoplay_close){
                    autoPlay = false;
                }else{
                    autoPlay = true;
                }
                setAutoPlaySegment(autoPlay);
            }
        });

        playSpeedSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float playSpeed = (float) seekBar.getProgress() / 10;
                setPlayerSpeed(playSpeed);
            }
        });

        showAnim = ObjectAnimator.ofFloat(menuLayout,"translationX",menuLayout.getTranslationX(), 0.0f).setDuration(DEFAULT_ANIMATION_MS);
        showAnim.addUpdateListener(this);

        hideAnim = ObjectAnimator.ofFloat(menuLayout, "translationX", 0.0f,menuLayout.getTranslationX()).setDuration(DEFAULT_ANIMATION_MS);
        hideAnim.addUpdateListener(this);

        playSpeedSB.setProgress(currentSpeed);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(menuOpen){
            hideMenu();
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if(id == R.id.ib_play){
            onPlayBtClick();
        }else if(id == R.id.ib_next_segment){
            nextSegment();
        }else if(id == R.id.ib_back){
            onBackPressed();
        }else if(id == R.id.ib_last_segment){
            lastSegment();
        }else if(id == R.id.ib_setting){
            showMenu();
        }
    }

    private void onBackPressed(){
        if(component != null) component.onBackPressed();
    }

    public void setPlayBtState(boolean isPlay){
        int imgId = isPlay ? R.mipmap.segment_icon_pause : R.mipmap.segment_icon_play;
        btPlay.setImageResource(imgId);
    }

    private void onPlayBtClick(){
        if(component != null){
            component.onPlayBtClick();
        }
    }

    public void setPlayBtInRefreshState(){
        btPlay.setImageResource(R.mipmap.segment_icon_refresh);
    }

    private void lastSegment() {
        if(component != null){
            component.lastSegment();
        }
    }

    private void nextSegment(){
        if(component != null){
            component.nextSegment();
        }
    }


    private void setPlayerSpeed(float speed){
        if(player != null){
            player.setPlaybackParameters(new PlaybackParameters(speed));
        }
    }

    private void setAutoPlaySegment(boolean autoPlay){
        if(component != null){
            component.setAutoPlaySegment(autoPlay);
        }
    }

    private void showMenu(){
        if(!menuOpen) {
            showAnim.start();
        }
    }

    private void hideMenu(){
        if(menuOpen) {
            hideAnim.start();
        }
    }

    private static final String TAG = SegmentPlayController.class.getSimpleName();
    private void log(String msg){
        if(BuildConfig.DEBUG) {
            Log.d(TAG, msg);
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {

        if(animation.getAnimatedFraction() == 1.0f){
            if(animation == hideAnim){
                menuOpen = false;
                if(menuLayout.getVisibility() != VISIBLE){
                    menuLayout.setVisibility(VISIBLE);
                }
            }else if (animation == showAnim){
                menuOpen = true;
            }
        }
    }
}
