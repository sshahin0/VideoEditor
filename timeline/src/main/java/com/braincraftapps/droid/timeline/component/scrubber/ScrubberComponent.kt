/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: ScrubberComponent.kt
 * @modified: Aug 20, 2024, 08:44 AM
 */

package com.braincraftapps.droid.timeline.component.scrubber

import android.graphics.Canvas
import android.os.HandlerThread
import androidx.core.graphics.withClip
import androidx.core.os.HandlerCompat
import com.braincraftapps.droid.common.extension.lang.scale
import com.braincraftapps.droid.timeline.component.TimelineComponent
import com.braincraftapps.droid.timeline.component.scrubber.drawable.ScrubberFrameDrawable
import com.braincraftapps.droid.timeline.component.scrubber.provider.FrameProvider
import com.braincraftapps.droid.timeline.data.TimelineItem
import com.braincraftapps.droid.timeline.drawable.TimelineDrawable
import com.braincraftapps.droid.timeline.widget.TimelineView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlin.math.ceil

internal class ScrubberComponent(
    timelineView: TimelineView
) : TimelineComponent(timelineView, timelineView.scrubberFrameSize) {
    private val drawableList: ArrayList<TimelineDrawable> = ArrayList()
    internal val coroutineScope: CoroutineScope by lazy {
        val thread = HandlerThread("scrubber_frame_thread")
        thread.start()
        val handler = HandlerCompat.createAsync(thread.looper)
        return@lazy CoroutineScope(SupervisorJob() + handler.asCoroutineDispatcher("scrubber_frame_coroutine_dispatcher"))
    }
    internal val frameProvider: FrameProvider by lazy { FrameProvider(this) }

    private var video: TimelineItem.Video? = null

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope.coroutineContext.cancelChildren()
    }

    override fun onTimelineItemChanged(newItem: TimelineItem, previousItem: TimelineItem?) {
        super.onTimelineItemChanged(newItem, previousItem)
        drawableList.clear()
        video = newItem as? TimelineItem.Video ?: return
        var left = timelineLeft
        var right = left + frameSize
        repeat(ceil(windowWidth / frameSize).toInt() + 1) {
            val drawable = ScrubberFrameDrawable(this)
            drawable.setBoundsCompat(
                left = left,
                top = 0.0F,
                right = right,
                bottom = frameSize
            )
            left += frameSize
            right += frameSize
            drawableList.add(drawable)
        }
        updateFrameBitmaps()
    }

    override fun onSizeChanged(width: Float, height: Float) {
        super.onSizeChanged(width, height)
        updateFrameBounds()
    }

    override fun onTranslate(dx: Float, dy: Float, isScaling: Boolean): Boolean {
        if (dx == 0.0F || drawableList.isEmpty()) {
            // Nothing to do here.
            return false
        }
        drawableList.forEach { it.translate(dx) }
        updateFrameBounds(!isScaling)
        return true
    }

    override fun onScale(ds: Float): Boolean {
        updateFrameBitmaps()
        return true
    }

    override fun onOffsetChanged(offset: Float, total: Float): Boolean {
        return super.onOffsetChanged(offset, total)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.withClip(left = windowLeft, top = 0.0F, right = windowRight, bottom = height) {
            drawableList.forEach { drawable ->
                drawable.draw(this)
            }
        }
    }

    private fun updateFrameBounds(updateBitmap: Boolean = true) {
        if (drawableList.isEmpty()) {
            return
        }
        val drawableLeft = drawableList.first().boundF.left
        val drawableRight = drawableList.last().boundF.right
        if (drawableLeft > windowLeft) {
            val gap = drawableLeft - windowLeft
            if (gap <= 0) {
                return
            }
            val count = ceil(gap / frameSize).toInt()
            var nextRight = drawableLeft
            repeat(count) {
                val removed = drawableList.removeLast()
                removed.setBoundsCompat(
                    left = nextRight - frameSize,
                    right = nextRight
                )
                drawableList.add(0, removed)
                nextRight = removed.boundF.left
            }
        } else if (drawableRight < windowRight) {
            val gap = windowRight - drawableRight
            if (gap <= 0) {
                return
            }
            val count = ceil(gap / frameSize).toInt()
            var nextLeft = drawableRight
            repeat(count) {
                val removed = drawableList.removeFirst()
                removed.setBoundsCompat(
                    left = nextLeft,
                    right = nextLeft + frameSize
                )
                drawableList.add(removed)
                nextLeft = removed.boundF.right
            }
        }
        if (updateBitmap) {
            updateFrameBitmaps()
        }
    }

    private fun updateFrameBitmaps() {
        if (drawableList.isEmpty()) {
            return
        }
        val video = video ?: return
        drawableList.forEach { drawable ->
            val offset = (drawable.boundF.centerX() - timelineLeft).coerceAtLeast(0.0F)
            val frameDrawable = drawable as? ScrubberFrameDrawable ?: return@forEach
            val positionMs: Long = offset.scale(
                valueFrom = 0,
                valueTo = timelineWidth,
                scaleFrom = 0,
                scaleTo = timelineDurationMs
            )
            frameDrawable.updateBitmap(video, positionMs)
        }
    }
}
