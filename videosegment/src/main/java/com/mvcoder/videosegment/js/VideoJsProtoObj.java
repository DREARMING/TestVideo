package com.mvcoder.videosegment.js;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mvcoder.videosegment.bean.VideoConfiguration;
import com.mvcoder.videosegment.utils.Constants;
import com.mvcoder.videosegment.views.VideoContainer;

/**
 * 通过调用webview.addJavascriptInterface方法, 注入js对象，就可以直接操控接口，避免与原Android程序的代码产生过多耦合
 *
 * <p>
 * 注意：<br/>
 * 视频控件是覆盖在WebView层的，需要在WebView同View层次上添加视频容器{@link #videoContainer}</p>
 */
public class VideoJsProtoObj implements IVideoJsProto {

    private final int CONFIG_VIDEO = 100;
    private final int SWITCH_SEGMENT = 101;
    private final int CLEAR_VIDEO = 102;

    /**
     * 这是视频控件的容器,用于管理在容器上所有音视频
     */
    private VideoContainer videoContainer;
    private FrameLayout overlay;
    private WebView webView;
    private Context mContext;
    private static boolean isInit = false;
    private static VideoJsProtoObj instance;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    //用于从js线程池切换到主线程操作界面
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONFIG_VIDEO:
                    addSegmentVideoComponent((VideoConfiguration) msg.obj);
                    break;
                case SWITCH_SEGMENT:
                    SwitchItem item = (SwitchItem) msg.obj;
                    switchSegmentByIndex(item.videoId, item.segmentIndex, item.segmentTiTle);
                    break;
                case CLEAR_VIDEO:
                    clearSegmentVideo((String) msg.obj);
                    break;
            }
        }
    };

    private VideoJsProtoObj(@NonNull WebView webView, @NonNull FrameLayout overlay) {
        this.overlay = overlay;
        this.webView = webView;
        mContext = webView.getContext();

        videoContainer = new VideoContainer(mContext);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        videoContainer.setLayoutParams(params);
        overlay.addView(videoContainer);
        videoContainer.setJsBridge(this);
    }

    public static VideoJsProtoObj getInstance(WebView context, FrameLayout overlay) {
        if (instance == null) {
            synchronized (VideoJsProtoObj.class) {
                if (instance == null) {
                    instance = new VideoJsProtoObj(context, overlay);
                }
            }
        }
        return instance;
    }

    @JavascriptInterface
    @Override
    public void config(String configJsonStr) {
        VideoConfiguration info = gson.fromJson(configJsonStr, VideoConfiguration.class);
        if (info == null) {
            sendMsgToWeb(Constants.JSMethod.PRINT_MSG, "json配置信息错误");
            return;
        }
        mHandler.removeMessages(CONFIG_VIDEO);
        mHandler.obtainMessage(CONFIG_VIDEO, info).sendToTarget();
    }

    @JavascriptInterface
    @Override
    public void switchSegment(String str) {
        SwitchItem item = gson.fromJson(str, SwitchItem.class);
        if (item == null) {
            sendMsgToWeb(Constants.JSMethod.PRINT_MSG, "切换片段函数参数有误，请检查字段");
            return;
        }
        mHandler.removeMessages(SWITCH_SEGMENT);
        mHandler.obtainMessage(SWITCH_SEGMENT, item).sendToTarget();
    }

    @JavascriptInterface
    @Override
    public void clearVideo(String videoId) {
        mHandler.removeMessages(CLEAR_VIDEO);
        mHandler.obtainMessage(CLEAR_VIDEO, videoId).sendToTarget();
    }

    @Override
    public void videoDuration(long duration) {

    }

    public void onBackPressed(){
        if(webView.canGoBack()){
            webView.goBack();
            return;
        }
        if(mContext instanceof Activity){
            ((Activity) mContext).finish();
        }
    }

    private void addSegmentVideoComponent(VideoConfiguration info) {
        videoContainer.addVideoComponent(info);
    }

    private void switchSegmentByIndex(String videoId, int index, String title) {
        videoContainer.switchSegment(videoId, index, title);
    }

    private void clearSegmentVideo(String videoId){
        videoContainer.rmVideo(videoId);
    }

    /**
     * 发送消息到Web端
     *
     * @param method js方法名
     * @param msg    要传递给js方法的参数
     */
    public void sendMsgToWeb(String method, String msg) {
        StringBuilder builder = new StringBuilder("javascript:");
        builder.append(method);
        builder.append("('");
        builder.append(msg);
        builder.append("')");
        webView.loadUrl(builder.toString());
    }

    private class SwitchItem {
        private String videoId;
        private int segmentIndex;
        private String segmentTiTle;

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }

        public int getSegmentIndex() {
            return segmentIndex;
        }

        public void setSegmentIndex(int segmentIndex) {
            this.segmentIndex = segmentIndex;
        }

        public String getSegmentTiTle() {
            return segmentTiTle;
        }

        public void setSegmentTiTle(String segmentTiTle) {
            this.segmentTiTle = segmentTiTle;
        }
    }
}
