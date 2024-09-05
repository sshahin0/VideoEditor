/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: ContextX.kt
 * @modified: May 26, 2024, 01:53 PM
 */

package com.braincraftapps.common.extensions.core

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.braincraftapps.cropvideos.common.R

@ColorInt
fun Context.getColorAttr(@AttrRes attrRes: Int): Int {
    return TypedValue()
        .apply { theme.resolveAttribute(attrRes, this, true) }
        .data
}

@get:ColorInt
val Context.primaryColor: Int
    get() = getColorAttr(com.google.android.material.R.attr.colorPrimary)

@get:ColorInt
val Context.onPrimaryColor: Int
    get() = getColorAttr(com.google.android.material.R.attr.colorOnPrimary)

@get:ColorInt
val Context.surfaceColor: Int
    get() = getColorAttr(com.google.android.material.R.attr.colorSurface)

@get:ColorInt
val Context.onSurfaceColor: Int
    get() = getColorAttr(com.google.android.material.R.attr.colorOnSurface)

@get:ColorInt
val Context.dividerColor: Int
    get() = getColorAttr(R.attr.colorDivider)
