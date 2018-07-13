package com.mvcoder.videosegment.bean;

public class VideoConfiguration {

    private String videoId;

    private String videoUrl;

    private int segmentMills;

    private Position position;

    private boolean segmentMode;

    private boolean loopSegment;

    private boolean autoPlaySegment;

    private boolean showTitle;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getSegmentMills() {
        return segmentMills;
    }

    public void setSegmentMills(int segmentMills) {
        this.segmentMills = segmentMills;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isSegmentMode() {
        return segmentMode;
    }

    public void setSegmentMode(boolean segmentMode) {
        this.segmentMode = segmentMode;
    }

    public boolean isLoopSegment() {
        return loopSegment;
    }

    public void setLoopSegment(boolean loopSegment) {
        this.loopSegment = loopSegment;
    }

    public boolean isAutoPlaySegment() {
        return autoPlaySegment;
    }

    public void setAutoPlaySegment(boolean autoPlaySegment) {
        this.autoPlaySegment = autoPlaySegment;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }
}
