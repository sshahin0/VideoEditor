/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: TimelineView.kt
 * @modified: Aug 21, 2024, 03:44 PM
 */

package com.braincraftapps.droid.timeline.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.braincraftapps.droid.common.extension.core.dpToPx
import com.braincraftapps.droid.common.extension.core.useCompat
import com.braincraftapps.droid.common.extension.core.withOpacity
import com.braincraftapps.droid.common.extension.lang.scale
import com.braincraftapps.droid.common.extension.lang.toUUID
import com.braincraftapps.droid.common.extension.lang.withIoContext
import com.braincraftapps.droid.common.extension.log.logWarning
import com.braincraftapps.droid.common.extension.view.lifecycleScope
import com.braincraftapps.droid.timeline.component.TimelineComponent
import com.braincraftapps.droid.timeline.component.bubble.BubbleComponent
import com.braincraftapps.droid.timeline.component.scrubber.ScrubberComponent
import com.braincraftapps.droid.timeline.component.time.TimeComponent
import com.braincraftapps.droid.timeline.data.TimelineItem
import com.braincraftapps.droid.timeline.extensions.LOG_TAG_TIMELINE
import com.braincraftapps.droid.timeline.gesture.TimelineGestureManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

@SuppressLint("ClickableViewAccessibility")
class TimelineView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    companion object {
        internal const val INVALID_DIMENSION: Float = -1.0F
        internal const val INVALID_TIME: Long = -1
        internal val DEFAULT_TIME_UNIT: Long = 1.seconds.inWholeMilliseconds
    }

    private val gestureManager: TimelineGestureManager = TimelineGestureManager(this)
    private val lifecycleScopeCompat: CoroutineScope
        get() = lifecycleScope ?: error("No lifecycle scope found!")
    private val changeListeners: LinkedHashSet<OnChangeListener> = LinkedHashSet()
    private val verticalBarWidth: Float = context.dpToPx(1.5)

    private val anchorLinePaint: Paint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE.withOpacity(0.7F)
        return@lazy paint
    }

    internal var frameCollector: FrameCollector? = null

    internal val scrubberFrameSize: Float = context.dpToPx(52)

    internal var timelineDurationMs: Long = INVALID_TIME
        private set

    internal var viewWidth: Float = INVALID_DIMENSION
        private set

    internal var viewHeight: Float = INVALID_DIMENSION
        private set

    internal var anchorX: Float = INVALID_DIMENSION
        private set

    internal var offset: Float = INVALID_DIMENSION
        private set

    internal var timelineLeft: Float = INVALID_DIMENSION
        private set

    internal var timelineRight: Float = INVALID_DIMENSION
        private set

    internal var timelineWidth: Float = INVALID_DIMENSION
        private set

    internal var isInTranslate: Boolean = false
        private set

    internal var isInScale: Boolean = false
        private set

    internal val timelineItemMap: HashMap<String, TimelineItem> by lazy { HashMap() }

    private var pendingTimelineItem: TimelineItem? = null
    private var isSeekStarted: Boolean = false

    private val timeComponent: TimeComponent = TimeComponent(this)
    private val bubbleComponent: BubbleComponent = BubbleComponent(this)
    private val scrubberComponent: ScrubberComponent = ScrubberComponent(this)
    private val components: ArrayList<TimelineComponent> = ArrayList<TimelineComponent>().also {
        it.add(timeComponent)
        it.add(bubbleComponent)
        it.add(scrubberComponent)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val width = w.toFloat()
        val height = h.toFloat()
        anchorX = width / 2.0F
        viewWidth = width
        viewHeight = height
        components.forEach { component ->
            component.viewSizeChanged(width, height)
        }
        calculateSize()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val timelineItem = pendingTimelineItem
        if (timelineItem != null) {
            addTimelineItem(timelineItem)
            pendingTimelineItem = null
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        gestureManager.attachToWindow()
        components.forEach { it.attachToWindow() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        components.forEach { it.detachFromWindow() }
        gestureManager.detachFromWindow()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureManager.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        // Nothing to do here.
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var translateY = 0.0F
        components.forEach { component ->
            canvas.withTranslation(0.0F, translateY) {
                if (component.isFreeForm) {
                    component.onDraw(this)
                } else {
                    withClip(0.0F, 0.0F, component.viewWidth, component.height) { component.onDraw(this) }
                }
            }
            translateY += component.height
        }
        drawAnchorBar(canvas)
    }

    fun setFrameCollector(collector: FrameCollector?) {
        frameCollector = collector
    }

    fun addChangeListener(listener: OnChangeListener) {
        changeListeners.add(listener)
    }

    fun removeChangeListener(listener: OnChangeListener) {
        changeListeners.remove(listener)
    }

    fun setProgress(positionInMs: Long) {
        if (!isAttachedToWindow || timelineWidth <= 0 || timelineDurationMs <= 0) {
            return
        }
        gestureManager.cancelGesture()
        val progress: Float = positionInMs.scale(
            valueFrom = 0,
            valueTo = timelineDurationMs,
            scaleFrom = 0.0F,
            scaleTo = 1.0F
        )
        val nextOffset = timelineWidth * progress
        if (offset != nextOffset) {
            val dx = offset - nextOffset
            offset = nextOffset
            components.forEach { it.onOffsetChanged(nextOffset, timelineWidth) }
            performTranslate(dx, 0.0F)
        }
    }

    fun addVideo(file: File) {
        doOnLifecycleScope {
            val durationMs = withIoContext {
                MediaMetadataRetriever().useCompat {
                    it.setDataSource(file.path)
                    return@useCompat it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: INVALID_TIME
                }
            }.takeIf { it > 0 } ?: return@doOnLifecycleScope
            val timelineItem = TimelineItem.Video(id = file.absolutePath.toUUID().toString(), file = file, durationMs = durationMs)
            if (isLaidOut) {
                addTimelineItem(timelineItem)
            } else {
                pendingTimelineItem = timelineItem
            }
        }
    }

    internal fun performComponentTouchAction(event: MotionEvent): Boolean {
        components.forEach {
            if (it.onTouchEvent(event)) {
                // A timeline component consumed the touch event.
                // No need to process this as a gesture.
                return true
            }
        }
        return false
    }

    internal fun performTranslate(dx: Float, dy: Float, isScaling: Boolean = false) {
        if (dx == 0.0F && dy == 0.0F) {
            return
        }
        if (isInTranslate) {
            logWarning(
                message = "Invalid translate operation. Already is in translate.",
                tag = LOG_TAG_TIMELINE
            )
            return
        }
        isInTranslate = true
        val nextLeft = timelineLeft + dx
        val nextRight = timelineRight + dx
        val x = if (nextLeft > anchorX) {
            anchorX - timelineLeft
        } else if (nextRight < anchorX) {
            anchorX - timelineRight
        } else dx
        if (x != 0.0F) {
            timelineLeft += x
            timelineRight += x
            val nextOffset = anchorX - timelineLeft
            if (nextOffset != offset) {
                offset = nextOffset
                components.forEach { it.onOffsetChanged(nextOffset, timelineWidth) }
                publishProgress()
            }
            components.forEach { it.onTranslate(x, dy, isScaling) }
            invalidate()
        }
        isInTranslate = false
    }

    internal fun performScale(ds: Float) {
        if (ds == 1.0F) {
            return
        }
        if (isInScale) {
            logWarning(
                message = "Invalid scale operation. Already is in scale.",
                tag = LOG_TAG_TIMELINE
            )
            return
        }
        isInScale = true
        var isInvalidated = false
        if (timelineWidth > 0.0F) {
            val deltaWidth = (timelineWidth * ds) - timelineWidth
            val nextTimelineWidth = timelineWidth + deltaWidth
            val width = if (nextTimelineWidth < scrubberFrameSize) {
                scrubberFrameSize - timelineWidth
            } else deltaWidth
            if (width != 0.0F) {
                val progress: Float = offset.scale(
                    valueFrom = 0.0F,
                    valueTo = timelineWidth,
                    scaleFrom = 0.0F,
                    scaleTo = 1.0F
                )
                val nextWidth = timelineWidth + width
                if (timeComponent.calculateTimeUnit(nextWidth, timelineDurationMs)) {
                    timelineWidth = nextWidth
                    val nextOffset = timelineWidth * progress
                    timelineRight = timelineLeft + timelineWidth
                    val dx = offset - nextOffset
                    performTranslate(dx, 0.0F, isScaling = true)
                    isInvalidated = true
                }
            }
        }
        components.forEach {
            if (it.onScale(ds)) {
                isInvalidated = true
            }
        }
        if (isInvalidated) {
            invalidate()
        }
        isInScale = false
    }

    internal fun publishProgress() {
        val maxOffset = timelineWidth.takeIf { it > 0.0 } ?: return
        val durationMs = timelineDurationMs.takeIf { it > 0 } ?: return
        val positionMs: Long = offset.scale(
            valueFrom = 0.0F,
            valueTo = maxOffset,
            scaleFrom = 0,
            scaleTo = durationMs
        )
        changeListeners.forEach {
            it.onTimelineProgressChange(positionMs, durationMs, gestureManager.isUserInteracting)
        }
    }

    internal fun publishSeekStart() {
        if (isSeekStarted) {
            return
        }
        isSeekStarted = true
        changeListeners.forEach { it.onTimelineSeekStart() }
    }

    internal fun publishSeekStop() {
        if (!isSeekStarted) {
            return
        }
        isSeekStarted = false
        changeListeners.forEach { it.onTimelineSeekStop() }
    }

    internal fun performClick(x: Float, y: Float) {
        components.forEach {
            if (it.onClick(x, y)) {
                return
            }
        }
    }

    internal fun doOnLifecycleScope(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return lifecycleScopeCompat.launch(context, start, block)
    }

    private fun drawAnchorBar(canvas: Canvas) {
        val halfWidth = verticalBarWidth / 2F
        val start = anchorX - halfWidth
        val end = anchorX + halfWidth
        val top = timeComponent.height
        val bottom = height.toFloat()
        canvas.withSave {
            drawRoundRect(start, top, end, bottom, halfWidth, halfWidth, anchorLinePaint)
        }
    }

    private fun addTimelineItem(item: TimelineItem) {
        val previous = timelineItemMap[item.id]
        timelineItemMap[item.id] = item
        timelineDurationMs = timelineItemMap.values.sumOf {
            return@sumOf (it as? TimelineItem.Video)?.durationMs ?: 0L
        }
        calculateSize()
        components.forEach { it.onTimelineItemChanged(item, previous) }
        invalidate()
    }

    fun calculateSize() {
        val timeUnit = timeComponent.timeUnit.takeIf { it > 0 } ?: DEFAULT_TIME_UNIT
        val nextWidth = ((timelineDurationMs / timeUnit) * scrubberFrameSize).takeIf { it > 0.0F } ?: run {
            timelineLeft = INVALID_DIMENSION
            timelineRight = INVALID_DIMENSION
            timelineWidth = INVALID_DIMENSION
            return
        }
        if (timeComponent.calculateTimeUnit(nextWidth, timelineDurationMs)) {
            if (timelineWidth != nextWidth) {
                timelineWidth = nextWidth
                if (timelineLeft == INVALID_DIMENSION && timelineLeft == timelineRight) {
                    timelineLeft = anchorX.takeIf { it > 0.0F } ?: INVALID_DIMENSION
                    timelineRight = (timelineLeft + nextWidth).takeIf { it > 0.0F } ?: INVALID_DIMENSION
                }
                components.forEach { it.onSizeChanged(nextWidth, it.height) }
            }
        }
    }

    interface OnChangeListener {
        fun onTimelineSeekStart() {}

        fun onTimelineSeekStop() {}

        fun onTimelineProgressChange(positionInMs: Long, durationInMs: Long, fromUser: Boolean)
    }

    interface FrameCollector {
        suspend fun getFrameAt(video: TimelineItem.Video, positionMs: Long, width: Int, height: Int): Bitmap?
    }
}
