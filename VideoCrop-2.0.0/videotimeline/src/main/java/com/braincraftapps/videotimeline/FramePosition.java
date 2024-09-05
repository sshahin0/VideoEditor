package com.braincraftapps.videotimeline;

public class FramePosition{
    private final int videoIndexInVideoItemList;
    private final int frameIndexInVideoItem;

    public FramePosition(int videoIndexInVideoItemList, int frameIndexInVideoItem) {
        this.videoIndexInVideoItemList = videoIndexInVideoItemList;
        this.frameIndexInVideoItem = frameIndexInVideoItem;
    }

    public int getVideoIndexInVideoItemList() {
        return videoIndexInVideoItemList;
    }

    public int getFrameIndexInVideoItem() {
        return frameIndexInVideoItem;
    }

    @Override
    public String toString() {
        return "FramePosition{" +
                "videoIndexInVideoItemList=" + videoIndexInVideoItemList +
                ", frameIndexInVideoItem=" + frameIndexInVideoItem +
                '}';
    }
}
