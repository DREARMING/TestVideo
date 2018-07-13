package com.mvcoder.testvideo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.PlaybackPreparer;
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
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExoPlayerActivity extends AppCompatActivity implements PlaybackPreparer {

    @BindView(R.id.playBt)
    Button playBt;
    @BindView(R.id.playerView)
    PlayerView playerView;
    @BindView(R.id.slowSpeed)
    Button slowSpeed;
    @BindView(R.id.normalSpeed)
    Button normalSpeed;
    @BindView(R.id.fastSpeed)
    Button fastSpeed;
    @BindView(R.id.nextNode)
    Button nextNode;
    @BindView(R.id.lastNode)
    Button lastNode;
    @BindView(R.id.loopNode)
    Button loopNode;
    @BindView(R.id.rv)
    RecyclerView recyclerView;
    @BindView(R.id.segment_mode)
    Button segmentMode;
    @BindView(R.id.tv_segment_mode)
    TextView tvSegmentMode;
    @BindView(R.id.tv_segment_index)
    TextView tvSegmentIndex;
    @BindView(R.id.tv_play_speed)
    TextView tvPlaySpeed;
    private SimpleExoPlayer player;
    private MyTextureView textureView;

    private final String rtmpTestUrl = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
    private final String videoFileUrl = "http://192.168.191.1:8080/test2.mp4";
    private final String TAG = ExoPlayer.class.getSimpleName();


    private long segmentMills = 3 * 1000;

    private boolean segementMode = true;

    private long startMills = 0;

    private long endMills = startMills + segmentMills;
    private int frameInterval = 40;

    private List<VideoSegement> segementList;
    private CommonAdapter<VideoSegement> adapter;
    private boolean repeatMode = false;
    private int repeatCount = 1;

    private boolean touchEnd = false;

    private int curNode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        initExoPlayer();
        prepardVideo();
    }


    private void prepardVideo() {

        Uri mp4VideoUri = Uri.parse(videoFileUrl);

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter1 = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this,
                        TestVideoApplication.class.getName()), bandwidthMeter1);
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mp4VideoUri);

        player.setPlayWhenReady(false);
        player.addListener(new PlayerEventListener());
        // Prepare the player with the source.
        player.prepare(videoSource);
        player.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

            }

            @Override
            public void onRenderedFirstFrame() {
                System.out.println("onRenderedFirstFrame ===== "  + player.getVideoFormat().frameRate);
                initSegement();
            }
        });
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
                ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        playerView.setPlayer(player);
        textureView = new MyTextureView(this);
        textureView.setSurfaceTextureListener(textureView);

        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        textureView.setLayoutParams(params);
        //textureviewContainer.addView(textureView,0);

        AspectRatioFrameLayout frameLayout = playerView.findViewById(R.id.exo_content_frame);
        frameLayout.removeViewAt(0);
        frameLayout.addView(textureView, 0);

        player.setVideoTextureView(textureView);

        playerView.setPlaybackPreparer(this);

    }

    @Override
    public void preparePlayback() {
        Log.d(TAG, "onPrepared");
    }

    private void initSegement() {
        if (segementList != null) return;
        //float rate = player.getVideoFormat().frameRate;
        //if(rate == Format.NO_VALUE){
        //  ToastUtils.showShort("unknow frameRate, can't segement this video");
        //return;
        //}
        frameInterval = (int) (1000 / frameInterval) + 1;
        if (segementList == null) initTimeLine();
        if (segementList == null && segementList.size() <= 0) return;
        initSegmentRecyclerView(segementList);
    }

    private void initSegmentRecyclerView(List<VideoSegement> segementList) {
        if (adapter != null) {
            adapter.getDatas().clear();
            adapter.getDatas().addAll(segementList);
            adapter.notifyDataSetChanged();
        } else {
            adapter = new CommonAdapter<VideoSegement>(this, R.layout.item_rv, segementList) {
                @Override
                protected void convert(ViewHolder holder, VideoSegement videoSegement, int position) {
                    holder.setText(R.id.segment_index, "" + videoSegement.getIndex());
                    holder.setText(R.id.segement_timeline, videoSegement.getTimeline());
                    holder.setText(R.id.segment_describe, videoSegement.getDescribe());
                    holder.setText(R.id.segment_evaluate, videoSegement.getQuality() + "");
                }
            };
            adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                    curNode = position;
                    switchSegement(position, false);
                }

                @Override
                public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                    return false;
                }
            });
            recyclerView.setAdapter(adapter);
        }
    }


    private void switchSegement(int segmentIndex, boolean play) {
        if (adapter == null) return;
        if (segmentIndex < 0) {
            ToastUtils.showShort("当前片段已经是第一个");
            return;
        }
        if (segmentIndex >= adapter.getDatas().size()) {
            ToastUtils.showShort("没有更多片段了");
            return;
        }
        tvSegmentIndex.setText("当前片段序号：" + (segmentIndex+1));
        player.setPlayWhenReady(false);
        VideoSegement segement = adapter.getDatas().get(segmentIndex);
        startMills = segement.getStartMs();
        endMills = segement.getEndMs();
        player.seekTo(startMills);
        if (play) {
            player.setPlayWhenReady(true);
        }
    }

    private void initTimeLine() {
        long duration = player.getDuration();
        if(duration > 10 * 3600000) {
            ToastUtils.showShort("该视频播放时长太长，无法剪切");
            return;
        }
        segementList = new ArrayList<>((int) (duration / segmentMills) + 1);
        int i = 1;
        for (long start = 0; start < duration; ) {
            VideoSegement segement = new VideoSegement();
            segement.setIndex(i++);
            segement.setStartMs(start);
            start += segmentMills;
            if(start > duration){
                duration = start;
            }
            segement.setEndMs(start);
            segement.setTimeline(segement.getStartTime() + "~" + segement.getEndTime());
            segementList.add(segement);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
        }
    }

    @OnClick({R.id.playBt, R.id.playerView, R.id.slowSpeed, R.id.normalSpeed, R.id.fastSpeed, R.id.nextNode, R.id.lastNode, R.id.loopNode, R.id.segment_mode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.playBt:
                if (touchEnd) {
                    player.seekTo(startMills);
                    touchEnd = false;
                    player.setPlayWhenReady(true);
                } else {
                    boolean isPlaying = player.getPlayWhenReady();
                    player.setPlayWhenReady(!isPlaying);
                }
                playBt.setText(player.getPlayWhenReady() ? "暂停":"播放");
                break;
            case R.id.playerView:
                break;
            case R.id.slowSpeed:
                tvPlaySpeed.setText("当前播放速度：0.1倍速");
                player.setPlaybackParameters(new PlaybackParameters(0.1f));
                break;
            case R.id.normalSpeed:
                tvPlaySpeed.setText("当前播放速度：1.0倍速");
                player.setPlaybackParameters(new PlaybackParameters(1f));
                break;
            case R.id.fastSpeed:
                tvPlaySpeed.setText("当前播放速度：2.0倍速");
                player.setPlaybackParameters(new PlaybackParameters(2f));
                break;
            case R.id.nextNode:
                if (adapter != null && curNode < adapter.getDatas().size() - 1) {
                    curNode++;
                    switchSegement(curNode, false);
                } else {
                    ToastUtils.showShort("没有更多片段了");
                }
                break;
            case R.id.lastNode:
                if (curNode == 0) {
                    ToastUtils.showShort("已经是第一个片段了");
                    return;
                }
                curNode--;
                switchSegement(curNode, false);
                break;
            case R.id.loopNode:
                //initSegement();
                repeatMode = !repeatMode;
                if (repeatMode)
                    player.setPlayWhenReady(true);
                break;
            case R.id.segment_mode:
                segementMode = !segementMode;
                tvSegmentMode.setText("片段播放模式：" + (segementMode?"开":"关"));
                break;
        }
    }


    private class PlayerEventListener extends Player.DefaultEventListener {

        @Override
        public void onSeekProcessed() {
            super.onSeekProcessed();
            Log.d(TAG, "onSeekProcessed");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            super.onPlayerStateChanged(playWhenReady, playbackState);
            Log.d(TAG, "onPlayerStateChange : " + " playWhenReady : " + playWhenReady + " , playbackState : " + playbackState);
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            super.onPlaybackParametersChanged(playbackParameters);
            Log.d(TAG, "onPlaybackParametersChanged : speed" + playbackParameters.speed);
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            super.onPlayerError(error);
            if(error.getSourceException() != null){
                ToastUtils.showShort("播放器不能此视频源");
            }
            Log.d(TAG, "Error : " + error.getMessage());
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            super.onLoadingChanged(isLoading);
        }

    }

    private int num = 0;

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
            Log.d(TAG, "delegate avialble");
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
            if (segementMode) {
                long curTime = player.getCurrentPosition();
                if (curTime + frameInterval >= endMills) {
                    touchTimelineEnd();
                }
            }
            System.out.println("delegate TextureView" + (num++) + " time stamp : " + player.getCurrentPosition());
        }
    }

    private void touchTimelineEnd() {
        touchEnd = true;
        Log.d(TAG, "touch timeline end");
        player.setPlayWhenReady(false);
        if (repeatMode) {
            player.seekTo(startMills);
            player.setPlayWhenReady(true);
        }
        playBt.setText("播放");
    }

    class VideoSegement {

        private long startMs;
        private int index;  //序号
        private long endMs;
        private int code;   //编码
        private String codeStr;  //编码对应的评课标准内容
        private int quality; //评价

        private String describe;
        private String content; //视频片段点评

        private String timeline;

        private String startTime;
        private String endTime;

        private String formatTime(long ms) {
            int second, minute, hour = 0;
            int seconds = (int) (ms / 1000);
            second = seconds % 60;
            minute = seconds / 60;
            if (minute >= 60) {
                hour = minute / 60;
                minute = minute % 60;
            }
            String secondStr = second < 10 ? "0".intern() + second : ("" + second);
            String minStr = minute < 10 ? "0".intern() + minute : ("" + minute);
            String hourStr = hour < 10 ? "0".intern() + hour : ("" + hour);
            return hourStr + ":".intern() + minStr + ":".intern() + secondStr;
        }

        public String getTimeline() {
            return timeline;
        }

        public void setTimeline(String timeline) {
            this.timeline = timeline;
        }

        public String getStartTime() {
            if (startTime == null) {
                startTime = formatTime(startMs);
            }
            return startTime;
        }

        public String getEndTime() {
            if (endTime == null) {
                endTime = formatTime(endMs);
            }
            return endTime;
        }


        public long getStartMs() {
            return startMs;
        }

        public void setStartMs(long startMs) {
            this.startMs = startMs;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public long getEndMs() {
            return endMs;
        }

        public void setEndMs(long endMs) {
            this.endMs = endMs;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getCodeStr() {
            return codeStr;
        }

        public void setCodeStr(String codeStr) {
            this.codeStr = codeStr;
        }

        public int getQuality() {
            return quality;
        }

        public void setQuality(int quality) {
            this.quality = quality;
        }

        public String getDescribe() {
            return describe;
        }

        public void setDescribe(String describe) {
            this.describe = describe;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

}
