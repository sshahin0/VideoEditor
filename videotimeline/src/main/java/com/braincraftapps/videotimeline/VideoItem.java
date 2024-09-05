package com.braincraftapps.videotimeline;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class VideoItem {

    private static final String TAG = VideoItem.class.getSimpleName();

    private final Uri videoUri;
    private Integer joinerIconResId;
    private Uri joinerIconUri;

    private long startPositionUs;
    private long endPositionUs;
    private float speed;
    private long startOffsetDurationUs;
    private long endOffsetDurationUs;

    private boolean updateNeeded;

    private List<FrameItem> frameItems = new ArrayList<>();
    private int totalRequiredPixels;

    public VideoItem(Uri videoUri, long startPositionUs, long endPositionUs){
        this.videoUri = videoUri;
        this.startPositionUs = startPositionUs;
        this.endPositionUs = endPositionUs;
        speed = 1f;
        updateNeeded = true;
    }

    public void setJoinerIconResId(Integer joinerIconResId) {
        this.joinerIconResId = joinerIconResId;
    }

    public void setJoinerIconUri(Uri joinerIconUri) {
        this.joinerIconUri = joinerIconUri;
    }

    public void setStartPositionUs(long startPositionUs) {
        if(this.startPositionUs != startPositionUs){
            this.startPositionUs = startPositionUs;
            updateNeeded = true;
        }
    }

    public void setEndPositionUs(long endPositionUs) {
        if(this.endPositionUs != endPositionUs){
            this.endPositionUs = endPositionUs;
            updateNeeded = true;
        }
    }

    public void setSpeed(float speed) {
        if(this.speed != speed){
            this.speed = speed;
            updateNeeded = true;
        }
    }

    public void setStartOffsetDurationUs(long startOffsetDurationUs) {
        if(this.startOffsetDurationUs != startOffsetDurationUs){
            this.startOffsetDurationUs = startOffsetDurationUs;
            updateNeeded = true;
        }
    }

    public void setEndOffsetDurationUs(long endOffsetDurationUs) {
        if(this.endOffsetDurationUs != endOffsetDurationUs){
            this.endOffsetDurationUs = endOffsetDurationUs;
            updateNeeded = true;
        }
    }

    public Uri getVideoUri() {
        return videoUri;
    }

    public Integer getJoinerIconResId() {
        return joinerIconResId;
    }

    public Uri getJoinerIconUri() {
        return joinerIconUri;
    }

    public long getStartPositionUs() {
        return startPositionUs;
    }

    public long getEndPositionUs() {
        return endPositionUs;
    }

    public float getSpeed() {
        return speed;
    }

    public long getStartOffsetDurationUs() {
        return startOffsetDurationUs;
    }

    public long getEndOffsetDurationUs() {
        return endOffsetDurationUs;
    }

    public boolean isUpdateNeeded() {
        return updateNeeded;
    }




    public long getDurationUs(){
       return getEndPositionUs() - getStartPositionUs();
    }

    public long getFinalVideoDurationUs(){
        return Math.round(getActiveDurationUs() / (double)getSpeed());
    }

    public long getActiveDurationUs(){
        return getActiveEndPositionUs() - getActiveStartPositionUs();
    }

    public long getActiveStartPositionUs(){
        return getStartPositionUs() + getStartOffsetUs();
    }

    public long getActiveEndPositionUs(){
        return getEndPositionUs() - getEndOffsetUs();
    }

    public long getStartOffsetUs(){
        return getPositionChangeUsForDurationUs(getStartOffsetDurationUs());
    }

    public long getEndOffsetUs(){
        return getPositionChangeUsForDurationUs(getEndOffsetDurationUs());
    }

    public long getDurationUsForPositionChangeUs(long positionChangeUs){
        return Math.round(positionChangeUs / (double)getSpeed());
    }

    public long getPositionChangeUsForDurationUs(long durationUs){
        return Math.round(durationUs * (double)getSpeed());
    }



    public int getTotalRequiredPixels() {
        return totalRequiredPixels;
    }

    public int getNumberOfFrameItems(){
        return frameItems.size();
    }

    public int getLastFrameItemIndex(){
        return getNumberOfFrameItems() - 1;
    }

    public FrameItem getFrameItem(int index){
        return frameItems.get(index);
    }

    public int getRequiredPixelsTillFrameItemIndex(int index){
        /*
         * All frame-item-sizes are equal except the last one
         */

        final int frameItemPixels = frameItems.get(0).getRequiredPixels();

        if(index == 0) return frameItemPixels;

        if(index == getLastFrameItemIndex()){
            return frameItemPixels * index + frameItems.get(index).getRequiredPixels();
        }else {
            return frameItemPixels * (index + 1);
        }

        /*int pixels = 0;
        for(int i = 0; i <= index; i++){
            pixels = pixels + frameItems.get(index).getRequiredPixels();
        }
        return pixels;*/
    }

    public int getRequiredPixelsOfFrameItem(int index){
        return frameItems.get(index).getRequiredPixels();
    }


    public long getUsForPixels(int pixels){
        long us = (long)((pixels / (double)totalRequiredPixels) * getActiveDurationUs());
        return us;
    }

    public int getPixelsForUs(long us){
        int pixels = (int)((us / (double)(getActiveDurationUs())) * totalRequiredPixels);
        return pixels;
    }


    public void applyChanges(int pixelsPerSecond,
                             int desiredFrameItemPixels,
                             int minPixelsForLastFrameItem){

        totalRequiredPixels = (int)Math.ceil( pixelsPerSecond * ((getActiveDurationUs() / (double)getSpeed()) / 1000000.0) );
        final int numberOfDesiredSizedFrameItems = (int)(totalRequiredPixels / desiredFrameItemPixels);
        final int remainingPixels = (int)(totalRequiredPixels % desiredFrameItemPixels);


        /*
        * All frame-item-sizes are equal except the last one
        */

        List<Integer> frameItemPixelsList = new ArrayList<>();
        if(remainingPixels == 0){
            for(int i = 0; i < numberOfDesiredSizedFrameItems; i++){
                frameItemPixelsList.add(desiredFrameItemPixels);
            }
        }else{

            if(numberOfDesiredSizedFrameItems > 0 && remainingPixels < minPixelsForLastFrameItem){
                final int additionalPixels = remainingPixels / numberOfDesiredSizedFrameItems;
                final int remainingAdditionalPixels = remainingPixels % numberOfDesiredSizedFrameItems;
                for(int i = 0; i < numberOfDesiredSizedFrameItems; i++){
                    int pixels = desiredFrameItemPixels + additionalPixels;
                    if(i == numberOfDesiredSizedFrameItems - 1) pixels = pixels + remainingAdditionalPixels;
                    frameItemPixelsList.add(pixels);
                }

            }else{

                for(int i = 0; i < numberOfDesiredSizedFrameItems; i++){
                    frameItemPixelsList.add(desiredFrameItemPixels);
                }
                frameItemPixelsList.add(remainingPixels);
            }
        }

        frameItems.clear();
        final int numberOfFrameItems = frameItemPixelsList.size();
        final long activeStartPositionUs = getActiveStartPositionUs();
        //final long activeEndPositionUs = getEndPositionUs() - (long) Math.round(getEndOffsetDurationUs() * getSpeed());
        final float framePositionDiffInSec = (float)( (getActiveDurationUs()/1000000.0) / numberOfFrameItems);
        for(int i = 0; i < numberOfFrameItems; i++){
            long positionUs = activeStartPositionUs + (long)(i * framePositionDiffInSec * 1000000);
            FrameItem frameItem = new FrameItem(positionUs, frameItemPixelsList.get(i));
            frameItems.add(frameItem);
        }

        updateNeeded = false;
    }
}
