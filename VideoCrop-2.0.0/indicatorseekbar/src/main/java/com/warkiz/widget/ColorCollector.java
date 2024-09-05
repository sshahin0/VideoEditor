/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: ColorCollector.java
 * @modified: Aug 13, 2024, 11:25 AM
 */

package com.warkiz.widget;

import androidx.annotation.ColorInt;

/**
 * for collecting each section track color
 * <p>
 * https://github.com/warkiz/IndicatorSeekBar
 * <p>
 * Donation/打赏:
 * If this library is helpful to you ,you can give me a donation by:
 *
 * @see <a href="https://www.paypal.me/BuyMeACupOfTeaThx">ZhuanGuangQuan's Paypal</a>, or
 * @see <a href="https://github.com/warkiz/IndicatorSeekBar/blob/master/app/src/main/res/mipmap-xxhdpi/wechat_pay.png?raw=true">微信支付</a>, or
 * @see <a href="https://github.com/warkiz/IndicatorSeekBar/blob/master/app/src/main/res/mipmap-xxhdpi/alipay.png?raw=true">支付宝</a>
 * <p>
 */
public interface ColorCollector {
    /**
     * to collect each section track's color
     *
     * @param colorIntArr ColorInt the container for each section tracks' color.
     *                    this array's length will auto equals section track' count.
     * @return True if apply color , otherwise no change
     */
    boolean collectSectionTrackColor(@ColorInt int[] colorIntArr);
}
