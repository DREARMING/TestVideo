package com.mvcoder.videosegment.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.mvcoder.videosegment.bean.Position;
import com.mvcoder.videosegment.bean.VideoConfiguration;
import com.mvcoder.videosegment.js.VideoJsProtoObj;
import com.mvcoder.videosegment.utils.Constants;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by mvcoder on 2018/4/3.
 */

public class VideoContainer extends FrameLayout {

    private Context context;

    Map<String, SegmentVideoComponent> videoMap = new HashMap<>();
    private float desity = getContext().getResources().getDisplayMetrics().density;

    /**
     * 用线程池管理音视频的播放，关闭，防止堵塞主线程。
     */
    Executor videoThreadPool = Executors.newCachedThreadPool(new ThreadFactory() {
        int num = 0;

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "Video Thread - " + (++num));
        }
    });

    public VideoContainer(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public VideoContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VideoContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
    }

    private VideoJsProtoObj jsBridge;

    public void setJsBridge(VideoJsProtoObj bridge) {
        this.jsBridge = bridge;
    }

    /*public void addVideoComponents(List<VideoParams> paramList) {
        //逆序添加音视频控件，确保添加完音视频控件后，NestScrollView的scrollY == 0
        Collections.sort(paramList, new Comparator<VideoParams>() {
            @Override
            public int compare(VideoParams o1, VideoParams o2) {
                int top1 = o1.getTop();
                int top2 = o2.getTop();
                if (top1 > top2) {
                    return -1;
                } else if (top1 < top2) {
                    return 1;
                }
                return 0;
            }
        });
        for (VideoParams params : paramList) {
            addVideoComponent(params);
        }
    }*/


    public void addVideosByConf(List<VideoConfiguration> configurations) {
        Collections.sort(configurations, new Comparator<VideoConfiguration>() {
            @Override
            public int compare(VideoConfiguration o1, VideoConfiguration o2) {
                Position position1 = o1.getPosition();
                Position position2 = o2.getPosition();
                int top1 = position1.getTop();
                int top2 = position2.getTop();
                if (top1 > top2) {
                    return -1;
                } else if (top1 < top2) {
                    return 1;
                }
                return 0;
            }
        });

        for (VideoConfiguration conf : configurations) {
            addVideoComponent(conf);
        }
    }

    public void addVideoComponent(VideoConfiguration conf) {
        if (conf == null) return;
        final SegmentVideoComponent videoComponent = new SegmentVideoComponent(context);
        Position pos = conf.getPosition();
        if (conf.getPosition() != null) {
            //网页中的像素与手机端是不一样的，在html页面设置了View-port mata标签后，可通过如下换算，将网页中的px转化为手机端的真实像素
            //换算公式： density * 网页px == 手机px
            float desity = getContext().getResources().getDisplayMetrics().density;
            LayoutParams layoutParams = new LayoutParams(pos.getWidth(), pos.getHeight());
            layoutParams.leftMargin = (int) (pos.getLeft() * desity + 0.5);
            layoutParams.topMargin = (int) (pos.getTop() * desity + 0.5);
            layoutParams.width = (int) (pos.getWidth() * desity + 0.5);
            layoutParams.height = (int) (pos.getHeight() * desity + 0.5);
            if (pos.getMarginBottom() > 0)
                layoutParams.bottomMargin = (int) (pos.getMarginBottom() * desity + 0.5);
            videoComponent.setLayoutParams(layoutParams);
            addView(videoComponent);
            videoMap.put(conf.getVideoId(), videoComponent);
        }
        videoComponent.setInfoListener(new SegmentVideoComponent.VideoInfoListener() {
            @Override
            public void onPrepared(long duration) {
                if (jsBridge != null)
                    jsBridge.sendMsgToWeb(Constants.JSMethod.ONPREPARED_MSG, duration + "");
            }

            @Override
            public void onError(String msg) {
                if (jsBridge != null)
                    jsBridge.sendMsgToWeb(Constants.JSMethod.ERROR_MSG, msg);
            }

            @Override
            public void onBackPressed() {
                if(jsBridge != null){
                    jsBridge.onBackPressed();
                }
            }
        });
        //配置音视频控件
        videoComponent.apply(conf);
    }


   /* public void pauseVideo(String id, boolean isPause) {
        SegmentVideoComponent component = videoMap.get(id);
        if (component != null) {
            if (isPause) {
                component.pauseVideo();
            } else {
                component.startPlay();
            }
        }
    }

    public void switchVideo(String id, String videoUrl, String title) {
        SegmentVideoComponent component = videoMap.get(id);
        if (component != null) {
            VideoItem item = new VideoItem();
            item.setVideoUrl(videoUrl);
            item.setVideoTitle(title);
            item.setSelect(true);
            component.switchVideo(item);
        }
    }

    public void closeVideo(String id) {
        SegmentVideoComponent component = videoMap.get(id);
        if (component != null) {
            component.closeVideo();
        }
    }*/

    public void rmVideo(String id) {
        SegmentVideoComponent component = videoMap.get(id);
        if (component != null) {
            removeView(component);
            videoMap.remove(id);
        }
    }

/*

    public void stopAllVideos() {
        for (Map.Entry<String, SegmentVideoComponent> entry : videoMap.entrySet()) {
            if (entry.getValue() != null) {
                entry.getValue().closeVideo();
            }
        }
    }
*/


    public void switchSegment(String videoId, int index, String segmentTitle) {
        SegmentVideoComponent component = videoMap.get(videoId);
        if (component != null) {
            component.switchSegment(index, segmentTitle);
        }
    }

    public void rmAllVideos() {
        videoMap.clear();
        removeAllViews();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        jsBridge = null;
        videoMap.clear();

    }


}
