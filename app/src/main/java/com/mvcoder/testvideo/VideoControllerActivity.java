package com.mvcoder.testvideo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mvcoder.videosegment.bean.VideoConfiguration;
import com.mvcoder.videosegment.views.SegmentVideoComponent;

public class VideoControllerActivity extends AppCompatActivity {


    private final String videoUrl = "http://192.168.191.1:8080/test2.mp4";
    private SegmentVideoComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_controller);
        initView();
    }

    private void initView() {
        component = findViewById(R.id.videoComponent);
        VideoConfiguration configuration = new VideoConfiguration();
        configuration.setVideoId(1+"");
        configuration.setSegmentMode(true);
        configuration.setVideoUrl(videoUrl);
        configuration.setSegmentMills(3 * 1000);
        component.apply(configuration);
    }
}
