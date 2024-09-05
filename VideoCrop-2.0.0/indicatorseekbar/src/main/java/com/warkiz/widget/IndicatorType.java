/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: IndicatorType.java
 * @modified: Aug 13, 2024, 11:17 AM
 */

package com.warkiz.widget;

/**
 * created by zhuangguangquan on  2017/9/9
 */
public interface IndicatorType {
    /**
     * don't have indicator to show.
     */
    int NONE = 0;
    /**
     * the indicator shape like water-drop
     */
    int CIRCULAR_BUBBLE = 1;

    /**
     * the indicator corners is rounded shape
     */
    int ROUNDED_RECTANGLE = 2;

    /**
     * the indicator corners is square shape
     */
    int RECTANGLE = 3;

    /**
     * set custom indicator you want
     */
    int CUSTOM = 4;

}
