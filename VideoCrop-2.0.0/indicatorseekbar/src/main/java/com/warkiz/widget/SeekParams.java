/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: SeekParams.java
 * @modified: Aug 13, 2024, 11:17 AM
 */

package com.warkiz.widget;

/**
 * created by zhuangguangquan on 2018/6/3
 * <p>
 * save the params when the seek bar is seeking.
 */
public class SeekParams {

    SeekParams(IndicatorSeekBar seekBar) {
        this.seekBar = seekBar;
    }

    //for continuous series seek bar
    // The SeekBar whose progress has changed
    public IndicatorSeekBar seekBar;
    //The current progress level.The default value for min is 0, max is 100.
    public int progress;
    //The current progress level.The default value for min is 0.0, max is 100.0.
    public float progressFloat;
    //True if the progress change was initiated by the user, otherwise by setProgress() programmatically.
    public boolean fromUser;
    //for discrete series seek bar
    //the thumb location on tick when the section changed, continuous series will be zero.
    public int thumbPosition;
    //the text below tick&thumb when the section changed.
    public String tickText;
}
