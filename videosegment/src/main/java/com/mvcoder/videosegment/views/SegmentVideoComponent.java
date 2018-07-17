package com.mvcoder.videosegment.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.mvcoder.videosegment.BuildConfig;
import com.mvcoder.videosegment.R;
import com.mvcoder.videosegment.bean.VideoConfiguration;
import com.mvcoder.videosegment.bean.VideoSegment;

import java.util.ArrayList;
import java.util.List;

public class SegmentVideoComponent extends FrameLayout {

    private static final String TAG = SegmentVideoComponent.class.getSimpleName();
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private MyTextureView textureView;

    private boolean segmentMode = true;
    private boolean touchEnd = false;
    private boolean repeatMode = false;
    private boolean autoPlaySegment = false;
    private boolean showTitle = false;
    private boolean hasError = false;

    private long segmentMills = 3 * 1000;

    private long frameInterval = 40;

    private long startMills = -1;
    private long endMills = -1;
    private int curSegmentIndex = 0;

    private VideoSegment curSegment;
    private MediaSource videoSource;

    private boolean hasPrepared = false;
    private List<VideoSegment> segmentList;

    private VideoInfoListener infoListener;

    private boolean userCustomController = true;

    private SegmentPlayController playController;
    private TextView tvError;

    public SegmentVideoComponent(@NonNull Context context) {
        this(context, null);
    }

    public SegmentVideoComponent(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SegmentVideoComponent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context,attrs, defStyleAttr);
    }

    public void setSegementMode(boolean segmentMode) {
        this.segmentMode = segmentMode;
    }


    public void setRepeatMode(boolean repeatMode) {
        this.repeatMode = repeatMode;
    }

    public void setSegmentMills(long segmentMills) {
        this.segmentMills = segmentMills;
    }

    public void setAutoPlaySegment(boolean autoPlaySegment) {
        if(this.autoPlaySegment != autoPlaySegment)
            this.autoPlaySegment = autoPlaySegment;
    }

    public Player getPlayer(){
        return player;
    }

    public void setInfoListener(VideoInfoListener infoListener) {
        this.infoListener = infoListener;
    }

    public void apply(VideoConfiguration config){
        this.segmentMills = config.getSegmentMills();
        this.segmentMode = config.isSegmentMode();
        this.repeatMode = config.isLoopSegment();
        this.autoPlaySegment = config.isAutoPlaySegment();
        this.showTitle = config.isShowTitle();
        prepardVideo(config.getVideoUrl());
    }

    public void switchSegment(int index, String title){
        if(index < 0 || index > segmentList.size()) {
            showToast("segment index 越标");
            return;
        }
        switchSegementByIndex(index, autoPlaySegment);
    }

    public void nextSegment(){
        int newIndex = curSegmentIndex + 1;
        switchSegementByIndex(newIndex, autoPlaySegment);
    }

    public void lastSegment(){
        int newIndex = curSegmentIndex - 1;
        switchSegementByIndex(newIndex, autoPlaySegment);
    }

    public void onPlayBtClick(){
        if(hasError){
            player.setPlayWhenReady(false);
            player.prepare(videoSource);
            player.seekTo(curSegment.getStartMs());
            return;
        }
        if(touchEnd){
            player.seekTo(curSegment.getStartMs());
            touchEnd = false;
            player.setPlayWhenReady(true);
        }else{
            boolean isPlaying = player.getPlayWhenReady();
            player.setPlayWhenReady(!isPlaying);
        }
        if(playController != null){
            playController.setPlayBtState(player.getPlayWhenReady());
        }
    }

    public void onBackPressed(){
        if(player != null){
            player.stop();
            player.release();
        }
        if(infoListener != null){
            infoListener.onBackPressed();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(player != null){
            player.stop();
            player.release();
        }
    }

    private void switchSegementByIndex(int segmentIndex, boolean play) {
        if(segmentList == null || segmentList.size() <= 0) return;
        if (segmentIndex < 0) {
            curSegmentIndex = 0;
            showToast("当前片段已经是第一个");
            return;
        }
        if (segmentIndex >= segmentList.size()) {
            curSegmentIndex = segmentList.size() - 1;
            showToast("没有更多片段了");
            return;
        }
        //tvSegmentIndex.setText("当前片段序号：" + (segmentIndex+1));
        player.setPlayWhenReady(false);
        curSegmentIndex = segmentIndex;
        curSegment = segmentList.get(segmentIndex);
        player.seekTo(curSegment.getStartMs());
        if (play) {
            if(playController != null){
                playController.setPlayBtState(true);
            }
            player.setPlayWhenReady(true);
        }
        if(playController != null){
            playController.setSegmentIndex(curSegmentIndex);
            playController.setSegmentDuration(curSegment.getTimeline());
        }
    }

    private void showToast(String msg){
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void initView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.view_segment_video, this);
        playerView = findViewById(R.id.playerView);
        tvError = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_error_message);
        tvError.setTextColor(Color.WHITE);
        if(userCustomController){
            playController = new SegmentPlayController(context,attrs,defStyleAttr);

            View controllerPlaceholder = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_overlay);
            if(controllerPlaceholder != null) {
                playController.setLayoutParams(controllerPlaceholder.getLayoutParams());
                ViewGroup parent = ((ViewGroup) controllerPlaceholder.getParent());
                int controllerIndex = parent.indexOfChild(controllerPlaceholder);
                parent.removeView(controllerPlaceholder);
                parent.addView(playController, controllerIndex);
            }
        }
        initExoPlayer();
    }

    private void initExoPlayer() {
        // 1. Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the player
        player =
                ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

        playerView.setPlayer(player);
        textureView = new MyTextureView(getContext());
        textureView.setSurfaceTextureListener(textureView);

        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        textureView.setLayoutParams(params);

        AspectRatioFrameLayout frameLayout = playerView.findViewById(R.id.exo_content_frame);
        frameLayout.removeViewAt(0);
        frameLayout.addView(textureView, 0);

        if(playController != null)
            playController.setVideoComponent(this);

        player.setVideoTextureView(textureView);
        player.addListener(new PlayerEventListener());
        player.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

            }

            @Override
            public void onRenderedFirstFrame() {
                log("onRenderedFirstFrame");
                if(hasError){
                    hasError = false;
                    playerView.setCustomErrorMessage(null);
                    tvError.setVisibility(INVISIBLE);
                    if(playController != null) playController.setPlayBtState(autoPlaySegment);
                    return;
                }
                if (!hasPrepared) {
                    hasPrepared = true;
                    onPreparedFinish();
                    log("segment video finish");
                }
            }
        });
    }

    private void onPreparedFinish() {
        if (segmentList != null) segmentList.clear();
        initTimeLine();
        if (segmentList != null && segmentList.size() <= 0) {
            log("音视频分段失败");
            return;
        }
        switchSegementByIndex(0,false);
        //curSegment = segmentList.get(0);

        if(infoListener != null){
            infoListener.onPrepared(player.getDuration());
        }
    }

    private void prepardVideo(String videoFileUrl) {
        Uri mp4VideoUri = Uri.parse(videoFileUrl);
        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter1 = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(),
                Util.getUserAgent(getContext(),
                        getContext().getApplicationInfo().className), bandwidthMeter1);
        // This is the MediaSource representing the media to be played.
        videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mp4VideoUri);

        player.setPlayWhenReady(false);
        // Prepare the player with the source.
        player.prepare(videoSource);
    }

    private void initTimeLine() {
        long duration = player.getDuration();
        segmentList = new ArrayList<>((int) (duration / segmentMills) + 1);
        int i = 1;
        for (long start = 0; start < duration; ) {
            VideoSegment segement = new VideoSegment();
            segement.setIndex(i++);
            segement.setStartMs(start);
            start += segmentMills;
            if (start > duration) {
                duration = start;
            }
            segement.setEndMs(start);
            segement.setTimeline(segement.getStartTime() + "~" + segement.getEndTime());
            segmentList.add(segement);
        }
    }

    private void log(String msg) {
        if (!BuildConfig.DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public interface VideoInfoListener{
        void onPrepared(long duration);
        void onError(String msg);
        void onBackPressed();
    }

    private class PlayerEventListener extends Player.DefaultEventListener {

        @Override
        public void onSeekProcessed() {
            super.onSeekProcessed();
            log("onSeekProcessed");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            super.onPlayerStateChanged(playWhenReady, playbackState);
            log("onPlayerStateChange : " + " playWhenReady : " + playWhenReady + " , playbackState : " + playbackState);
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            super.onPlaybackParametersChanged(playbackParameters);
            log("onPlaybackParametersChanged : speed" + playbackParameters.speed);
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            super.onPlayerError(error);
            String errMsg = "unknow error";
            if (error.getSourceException() != null) {
                hasPrepared = false;
                hasError = true;
                errMsg = "播放器不能此视频源";
                playerView.setCustomErrorMessage("发生错误：请检查网络配置以及视频源是否可用");
                if(infoListener != null){
                    infoListener.onError(errMsg);
                }
                Toast.makeText(getContext(), "播放器不能此视频源", Toast.LENGTH_SHORT).show();
            } else if (error.getRendererException() != null) {
                hasPrepared = false;
                hasError = true;
                errMsg = "渲染失败,可能因为手机不支持硬件加速所致";
                playerView.setCustomErrorMessage("发生错误：手机不支持硬件加速");
            } else if(error.getUnexpectedException().getMessage() != null){
                hasPrepared = false;
                hasError = true;
                errMsg = error.getMessage();
                playerView.setCustomErrorMessage("发生未知错误");
            }
            log("Error : " + errMsg);
            if(playController != null){
                playController.setPlayBtInRefreshState();
            }

            if(infoListener != null){
                infoListener.onError(errMsg);
            }
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            super.onLoadingChanged(isLoading);
        }
    }

    private class MyTextureView extends TextureView implements TextureView.SurfaceTextureListener {

        private SurfaceTextureListener delegateListener;

        public MyTextureView(Context context) {
            super(context);
        }

        public MyTextureView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MyTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }


        @Override
        public void setSurfaceTextureListener(SurfaceTextureListener listener) {
            this.delegateListener = listener;
            super.setSurfaceTextureListener(this);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            delegateListener.onSurfaceTextureAvailable(surface, width, height);
            log("delegate avialble");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            delegateListener.onSurfaceTextureAvailable(surface, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return delegateListener.onSurfaceTextureDestroyed(surface);
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            if (segmentMode) {
                long curTime = player.getCurrentPosition();
                if (curSegment != null && curTime + frameInterval >= curSegment.getEndMs()) {
                    touchTimelineEnd();
                }
            }
            //System.out.println("delegate TextureView" + (num++) + " time stamp : " + player.getCurrentPosition());
        }
    }

    private void touchTimelineEnd() {
        touchEnd = true;
        log("touch timeline end");
        player.setPlayWhenReady(false);
        if (repeatMode) {
            player.seekTo(curSegment.getStartMs());
            player.setPlayWhenReady(true);
        }
        //playBt.setText("播放");
        if(playController != null) playController.setPlayBtState(false);
    }


}
