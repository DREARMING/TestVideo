package com.mvcoder.videosegment.js;

public interface IVideoJsProto {

    /**
     *
     * @param configJsonStr
     */
    void config(String configJsonStr);

    void switchSegment(String item);

    void clearVideo(String videoId);

    void videoDuration(long duration);

}
