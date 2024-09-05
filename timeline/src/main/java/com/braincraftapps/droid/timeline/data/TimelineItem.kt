/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: TimelineItem.kt
 * @modified: Jul 11, 2024, 12:05 PM
 */

package com.braincraftapps.droid.timeline.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.io.File

sealed class TimelineItem(open val id: String) : Parcelable {

    @Keep
    @Parcelize
    data class Video(
        override val id: String,
        val file: File,
        val durationMs: Long
    ) : TimelineItem(id)
}
