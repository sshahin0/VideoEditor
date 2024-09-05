/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: FrameProvider.kt
 * @modified: Jul 11, 2024, 12:23 PM
 */

package com.braincraftapps.droid.timeline.component.scrubber.provider

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.braincraftapps.droid.common.extension.core.useCompat
import com.braincraftapps.droid.common.extension.graphics.scaleCompat
import com.braincraftapps.droid.common.utils.bitmap.ScaleType
import com.braincraftapps.droid.timeline.component.scrubber.ScrubberComponent
import com.braincraftapps.droid.timeline.component.scrubber.provider.cache.FrameLruCache
import com.braincraftapps.droid.timeline.data.TimelineItem
import com.braincraftapps.droid.timeline.widget.TimelineView

internal class FrameProvider(
    private val scrubberComponent: ScrubberComponent
) {
    private val frameLruCache: FrameLruCache by lazy { FrameLruCache(scrubberComponent.context) }
    private val simpleFrameCollector: SimpleFrameCollector by lazy { SimpleFrameCollector() }

    private val frameCollector: TimelineView.FrameCollector
        get() = scrubberComponent.timelineView.frameCollector ?: simpleFrameCollector

    suspend fun getFrameAt(
        video: TimelineItem.Video,
        positionMs: Long,
        width: Int,
        height: Int
    ): Bitmap? {
        val key: String = buildString {
            append(video.file.absolutePath.toString())
            append(":")
            append(positionMs)
            append(":")
            append(width)
            append(":")
            append(height)
        }
        return frameLruCache.getFrame(key) {
            return@getFrame frameCollector.getFrameAt(
                video = video,
                positionMs = positionMs,
                width = width,
                height = height
            )
        }
    }

    private class SimpleFrameCollector : TimelineView.FrameCollector {

        override suspend fun getFrameAt(video: TimelineItem.Video, positionMs: Long, width: Int, height: Int): Bitmap? {
            return MediaMetadataRetriever().useCompat {
                it.setDataSource(video.file.path)
                it.getFrameAtTime(positionMs.times(1000))?.scaleCompat(width, height, scaleType = ScaleType.CENTER_CROP)
            }
        }
    }
}
