/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: SizeUtils.java
 * @modified: Aug 13, 2024, 11:17 AM
 */

package com.warkiz.widget;

import android.content.Context;
import android.util.TypedValue;

/**
 * created by zhuangguangquan on  2017/9/9
 */

public class SizeUtils {
    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float spValue) {
        return (int) (spValue * context.getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        return (int) (pxValue / context.getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }

}
