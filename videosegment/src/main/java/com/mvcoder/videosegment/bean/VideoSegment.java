package com.mvcoder.videosegment.bean;

public class VideoSegment {

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
