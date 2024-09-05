package com.braincraftapps.videotimeline;

public class FrameItem {

    private final long videoFramePositionUs;
    private final int requiredPixels;

    public FrameItem(long videoFramePositionUs, int requiredPixels) {
        this.videoFramePositionUs = videoFramePositionUs;
        this.requiredPixels = requiredPixels;
    }

    public long getVideoFramePositionUs() {
        return videoFramePositionUs;
    }

    public int getRequiredPixels() {
        return requiredPixels;
    }

    @Override
    public String toString() {
        return "FrameItem{" +
                "videoFramePositionUs=" + videoFramePositionUs +
                ", requiredPixels=" + requiredPixels +
                '}';
    }
}
