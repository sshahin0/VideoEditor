/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: TimelineComponent.kt
 * @modified: Aug 20, 2024, 10:12 AM
 */

package com.braincraftapps.droid.timeline.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.view.MotionEvent
import androidx.annotation.RestrictTo
import com.braincraftapps.droid.timeline.data.TimelineItem
import com.braincraftapps.droid.timeline.widget.TimelineView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class TimelineComponent(
    val timelineView: TimelineView,
    val initialHeight: Float
) {
    companion object {
        internal const val INVALID_DIMENSION: Float = TimelineView.INVALID_DIMENSION
        internal const val INVALID_TIME: Long = TimelineView.INVALID_TIME
    }

    constructor(timelineView: TimelineView) : this(timelineView, INVALID_DIMENSION)

    val context: Context
        get() = timelineView.context

    val timelineItemSnapshot: List<TimelineItem>
        get() = timelineView.timelineItemMap.values.toList()

    val timelineDurationMs: Long
        get() = timelineView.timelineDurationMs

    val viewWidth: Float
        get() = timelineView.viewWidth

    val viewHeight: Float
        get() = timelineView.viewHeight

    val timelineWidth: Float
        get() = timelineView.timelineWidth

    var height: Float = INVALID_DIMENSION
        private set

    open val isFreeForm: Boolean = false

    val anchorX: Float
        get() = timelineView.anchorX

    val timelineLeft: Float
        get() = timelineView.timelineLeft

    val timelineRight: Float
        get() = timelineView.timelineRight

    val frameSize: Float
        get() = timelineView.scrubberFrameSize

    private val windowBoundF: RectF = RectF(INVALID_DIMENSION, INVALID_DIMENSION, INVALID_DIMENSION, INVALID_DIMENSION)

    val windowWidth: Float
        get() = windowBoundF.width()

    val windowHeight: Float
        get() = windowBoundF.height()

    val windowLeft: Float
        get() {
            if (timelineLeft >= windowBoundF.left) {
                return timelineLeft
            }
            if (timelineRight < windowBoundF.right) {
                return timelineRight - windowWidth
            }
            return windowBoundF.left
        }

    val windowRight: Float
        get() {
            if (timelineRight <= windowBoundF.right) {
                return timelineRight
            }
            if (timelineLeft > windowBoundF.left) {
                return timelineLeft + windowWidth
            }
            return windowBoundF.right
        }

    fun doOnLifecycleScope(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return timelineView.doOnLifecycleScope(context, start, block)
    }

    fun attachToWindow() {
        onAttachedToWindow()
    }

    fun detachFromWindow() {
        onDetachedFromWindow()
    }

    fun viewSizeChanged(width: Float, height: Float) {
        onViewSizeChanged(width, height)
        windowBoundF.left = -frameSize
        windowBoundF.top = 0.0F
        windowBoundF.right = width + frameSize
        windowBoundF.bottom = height
        if (this.height <= 0.0F) {
            this.height = initialHeight.takeIf { it > 0.0F } ?: INVALID_DIMENSION
        }
    }

    fun setComponentHeight(height: Float) {
        val nextHeight = height.takeIf { it > 0.0F } ?: INVALID_DIMENSION
        if (this.height != nextHeight) {
            this.height = nextHeight
            timelineView.calculateSize()
            timelineView.invalidate()
        }
    }

    fun invalidate() {
        timelineView.invalidate()
    }

    protected open fun onAttachedToWindow() {}

    protected open fun onDetachedFromWindow() {}

    protected open fun onViewSizeChanged(width: Float, height: Float) {}

    internal open fun onSizeChanged(width: Float, height: Float) {}

    internal open fun onTouchEvent(event: MotionEvent): Boolean = false

    internal open fun onOffsetChanged(offset: Float, total: Float): Boolean = false

    internal open fun onTranslate(dx: Float, dy: Float, isScaling: Boolean): Boolean = false

    internal open fun onScale(ds: Float): Boolean = false

    internal open fun onClick(x: Float, y: Float): Boolean = false

    internal open fun onTimelineItemChanged(newItem: TimelineItem, previousItem: TimelineItem?) {}

    internal abstract fun onDraw(canvas: Canvas)
}
