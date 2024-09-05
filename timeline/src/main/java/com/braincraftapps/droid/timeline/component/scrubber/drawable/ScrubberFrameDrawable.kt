/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: ScrubberFrameDrawable.kt
 * @modified: Jul 31, 2024, 11:05 AM
 */

package com.braincraftapps.droid.timeline.component.scrubber.drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.withTranslation
import com.braincraftapps.droid.common.extension.core.withOpacity
import com.braincraftapps.droid.common.extension.graphics.randomDarkMutedColor
import com.braincraftapps.droid.timeline.component.scrubber.ScrubberComponent
import com.braincraftapps.droid.timeline.data.TimelineItem
import com.braincraftapps.droid.timeline.drawable.TimelineDrawable
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min


internal class ScrubberFrameDrawable(
    val scrubberComponent: ScrubberComponent
) : TimelineDrawable(scrubberComponent) {
    private var frame: Pair<Long, Bitmap>? = null
    private var updateJob: Job? = null
    private var pendingPositionMs: Long? = null

    val framePositionMs: Long
        get() = frame?.first ?: INVALID_TIME

    private val paint: Paint by lazy {
        return@lazy Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = randomDarkMutedColor
        }
    }

    override fun onAttachToWindow() {}

    override fun onDetachFromWindow() {}

    override fun onDraw(canvas: Canvas) {
        val bitmap = frame?.second ?: run {
            canvas.drawRect(boundF, paint)
            return
        }
        canvas.withTranslation(boundF.left, boundF.top) {
            drawBitmap(bitmap, 0.0F, 0.0F, paint)
        }
    }

    fun updateBitmap(video: TimelineItem.Video, positionMs: Long) {
        val previousPosition = frame?.first
        if (positionMs == previousPosition || positionMs == pendingPositionMs) {
            return
        }
        pendingPositionMs = positionMs
        val provider = scrubberComponent.frameProvider
        updateJob?.cancel()
        updateJob = scrubberComponent.coroutineScope.launch {
            if (!isActive) {
                pendingPositionMs = null
                return@launch
            }
            val nextBitmap = provider.getFrameAt(video, positionMs, frameSize.toInt(), frameSize.toInt())
            pendingPositionMs = null
            if (isActive) {
                frame = nextBitmap?.let { positionMs to it }
                invalidateSelf()
            }
        }
    }

    private fun drawText(canvas: Canvas, text: String) {
        val width = boundF.width()
        val height = boundF.height()
        paint.textSize = min(width, height) / 5F
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        val backgroundColor = paint.color
        paint.color = Color.BLACK.withOpacity(0.3F)
        canvas.drawRect(boundF, paint)
        paint.color = Color.WHITE
        canvas.drawText(text, boundF.left + (width / 2F), (height / 2F) - ((paint.descent() + paint.ascent()) / 2F), paint)
        paint.color = backgroundColor
    }
}
