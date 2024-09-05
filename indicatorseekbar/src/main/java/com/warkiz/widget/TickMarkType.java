/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: TickMarkType.java
 * @modified: Aug 13, 2024, 11:17 AM
 */

package com.warkiz.widget;

/**
 * created by zhuangguangquan on 2017/9/11
 */

public interface TickMarkType {
    /**
     * don't show the tickMarks
     */
    int NONE = 0;
    /**
     * show tickMarks shape as regular oval
     */
    int OVAL = 1;
    /**
     * show tickMarks shape as regular square
     */
    int SQUARE = 2;
    /**
     * show tickMarks shape as vertical line , line'size is 2 dp.
     */
    int DIVIDER = 3;
}
