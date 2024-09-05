/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: TimelineDrawable.kt
 * @modified: Jul 09, 2024, 09:30 AM
 */

package com.braincraftapps.droid.timeline.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.withSave
import com.braincraftapps.droid.timeline.component.TimelineComponent
import com.braincraftapps.droid.timeline.widget.TimelineView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal abstract class TimelineDrawable(
    val timelineComponent: TimelineComponent
) : Drawable() {
    companion object {
        internal const val INVALID_DIMENSION: Float = TimelineView.INVALID_DIMENSION
        internal const val INVALID_TIME: Long = TimelineView.INVALID_TIME
    }
    val context: Context
        get() = timelineComponent.context

    val frameSize: Float
        get() = timelineComponent.frameSize

    val viewWidth: Float
        get() = timelineComponent.viewWidth

    val viewHeight: Float
        get() = timelineComponent.viewHeight

    val windowLeft: Float
        get() = timelineComponent.windowLeft

    val windowRight: Float
        get() = timelineComponent.windowRight

    private var boundsCompat: RectF? = null
    val boundF: RectF
        get() = boundsCompat ?: error("No bound set")

    private var attachState: State = State.UNKNOWN

    val isAttachedToWindow: Boolean
        get() = attachState == State.ENABLED

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    final override fun draw(canvas: Canvas) {
        canvas.withSave { onDraw(canvas) }
    }

    override fun invalidateSelf() {
        timelineComponent.invalidate()
    }

    fun doOnLifecycleScope(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return timelineComponent.doOnLifecycleScope(context, start, block)
    }

    fun setBoundsCompat(rectF: RectF) {
        setBoundsCompat(rectF.left, rectF.top, rectF.right, rectF.bottom)
    }

    fun setBoundsCompat(
        left: Float = boundsCompat?.left ?: 0.0F,
        top: Float = boundsCompat?.top ?: 0.0F,
        right: Float = boundsCompat?.right ?: frameSize,
        bottom: Float = boundsCompat?.bottom ?: frameSize
    ) {
        val bounds = boundsCompat ?: RectF()
        bounds.left = left
        bounds.top = top
        bounds.right = right
        bounds.bottom = bottom
        boundsCompat = bounds
        super.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        if (right < windowLeft || left > windowRight) {
            detachFromWindow()
            return
        }
        attachToWindow()
    }

    @Deprecated("Deprecated in favour of setBoundsCompat", ReplaceWith("setBoundsCompat"))
    final override fun setBounds(bounds: Rect) {
        error("Use setBoundsCompat")
    }

    @Deprecated("Deprecated in favour of setBoundsCompat", ReplaceWith("setBoundsCompat"))
    final override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        error("Use setBoundsCompat")
    }

    fun translate(dx: Float) {
        val bounds = boundsCompat ?: return
        bounds.left += dx
        bounds.right += dx
        setBoundsCompat(bounds)
    }

    fun attachToWindow() {
        if (attachState != State.ENABLED) {
            onAttachToWindow()
            attachState = State.ENABLED
        }
    }

    fun detachFromWindow() {
        if (attachState != State.DISABLED) {
            attachState = State.DISABLED
            onDetachFromWindow()
        }
    }

    protected open fun onAttachToWindow() {}

    protected open fun onDetachFromWindow() {}

    protected abstract fun onDraw(canvas: Canvas)

    enum class State {
        ENABLED,
        DISABLED,
        UNKNOWN
    }
}
