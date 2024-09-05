/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: TimeComponent.kt
 * @modified: Jul 31, 2024, 11:03 AM
 */

package com.braincraftapps.droid.timeline.component.time

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.braincraftapps.droid.common.extension.core.dpToPx
import com.braincraftapps.droid.common.extension.core.getReadableTime
import com.braincraftapps.droid.common.extension.lang.parseColor
import com.braincraftapps.droid.common.extension.lang.scale
import com.braincraftapps.droid.timeline.component.TimelineComponent
import com.braincraftapps.droid.timeline.widget.TimelineView
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal class TimeComponent(
    timelineView: TimelineView
) : TimelineComponent(timelineView, timelineView.context.dpToPx(24)) {
    companion object {
        private const val DEFAULT_TIME_UNIT_INDEX = 5
        private const val TIME_UNIT_WIDTH_TOLERANCE = 5 // Allow 5 pixels
    }

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = "#5F666B".parseColor()
        it.textSize = context.dpToPx(10)
        it.typeface = Typeface.DEFAULT_BOLD
    }

    private val textHeight: Float
        get() = paint.textSize

    private val dotRadius: Float = context.dpToPx(1)

    var timeUnit: Long = INVALID_TIME
        private set
    private var activeTimeUnitWidth: Float = INVALID_DIMENSION
    private var timeWindowLeft: Float = INVALID_DIMENSION

    private val textBounds: Rect = Rect()

    private val timeUnitList: List<Float> = listOf(
        66.66F, // 2f
        100.0F, // 3f
        166.66F, // 5f
        333.33F, // 10f
        500.0F, // 15f
        1000.0F, // 1 second (Default)
        2000.0F, // 2 seconds
        3000.0F, // 3 seconds
        5000.0F, // 5 seconds
        10000.0F, // 10 seconds
        15000.0F // 15 seconds
    )
    private var activeTimeUnitIndex: Int = DEFAULT_TIME_UNIT_INDEX
    private var activeTimeUnit: Float = timeUnitList[DEFAULT_TIME_UNIT_INDEX]

    override fun onSizeChanged(width: Float, height: Float) {
        super.onSizeChanged(width, height)
        calculateBounds()
    }

    override fun onTranslate(dx: Float, dy: Float, isScaling: Boolean): Boolean {
        return super.onTranslate(dx, dy, isScaling).also { calculateBounds() }
    }

    override fun onDraw(canvas: Canvas) {
        drawText(canvas)
    }

    private fun drawText(canvas: Canvas) {
        val unitWidth = activeTimeUnitWidth
        val timeUnit = activeTimeUnit
        if (unitWidth <= 0.0F) {
            return
        }
        // Find the left of the first time unit.
        // Each unit contains a text (such as 00:04 or 15f) and a dot.
        var unitLeft: Float = timeWindowLeft

        // Take the first time unit as starting point to draw and calculate the first unit's time in ms
        var activeUnitTimeMs: Float = (unitLeft - timelineLeft).scale(
            valueFrom = 0.0F,
            valueTo = timelineWidth,
            scaleFrom = 0.0F,
            scaleTo = timelineDurationMs
        )

        // Find the fraction (15f, 10f, 5f etc) if available (based on the time unit).
        // If the time unit more than or equal to 1 seconds, there will be fraction value.
        var fraction = 0
        var activeFractionToDraw = 0
        if (timeUnit < 1000.0F) {
            fraction = (30 / (1000.0F / timeUnit)).roundToInt()
            // Add the remaining fraction that is outside of the unit left if available.
            val count = (((activeUnitTimeMs + (timeUnit / 2.0F)) % 1000.0F) / timeUnit).toInt()
            if (count >= 1) {
                repeat(count) {
                    activeFractionToDraw += fraction
                }
            }
        }
        val textBaselineY: Float = (height / 2.0F) - (textHeight / 2.0F)
        while (unitLeft < windowRight) {
            var text = "-"
            // First check if time unit is more than or equal to 1 seconds.
            // If true, get the readable time value by rounding the time in ms.
            if (timeUnit >= 1000.0F) {
                text = context.getReadableTime(activeUnitTimeMs.roundToLong())
                activeFractionToDraw = 0
            } else {
                // Otherwise check if active time unit is a full second.
                // If true, get the readable time value and reset the fraction to default fraction.
                val time = (activeUnitTimeMs + (timeUnit / 2.0F))
                if (time % 1000.0F < timeUnit) {
                    text = context.getReadableTime(time.roundToLong())
                    activeFractionToDraw = fraction
                } else {
                    // Otherwise draw the time fraction
                    text = "${activeFractionToDraw}f"
                    activeFractionToDraw += fraction
                }
            }
            paint.getTextBounds(text, 0, text.length, textBounds)
            val textLeft = unitLeft - (textBounds.width() / 2.0F)
            canvas.drawText(text, textLeft, textBaselineY - paint.ascent(), paint)

            // Find the center x of the dot. If it is in the bound of the window, draw it.
            val cx = unitLeft + unitWidth / 2.0F
            if (cx < windowRight) {
                canvas.drawCircle(cx, height / 2.0F, dotRadius, paint)
            }
            activeUnitTimeMs += timeUnit
            unitLeft += unitWidth
        }
    }

    /**
     * Calculate the active time unit and it's width from the current timeline width and timeline duration.
     */
    fun calculateTimeUnit(timelineWidth: Float, timelineDurationMs: Long): Boolean {
        if (timelineWidth <= 0 || timelineDurationMs <= 0) {
            activeTimeUnitWidth = INVALID_DIMENSION
            return false
        }
        var nextTimeUnitIndex = activeTimeUnitIndex
        val nextTimeUnit: Float = activeTimeUnit
        val nextTimeUnitWidth: Float = (timelineWidth / (timelineDurationMs / nextTimeUnit))
        val frameSize = frameSize
        // First check if next time unit width is lower than the frame size.
        // If true, find the next time unit index.
        if (frameSize - nextTimeUnitWidth > TIME_UNIT_WIDTH_TOLERANCE) {
            val lastUnitIndex = timeUnitList.lastIndex
            if (nextTimeUnitIndex >= lastUnitIndex) {
                return false
            }
            ++nextTimeUnitIndex
        } else if ((nextTimeUnitWidth / 2.0F) - frameSize > TIME_UNIT_WIDTH_TOLERANCE) {
            // Otherwise check if the half of the next time unit width is higher than the frame size.
            // If true, find the next time unit index.
            if (nextTimeUnitIndex <= 0) {
                return false
            }
            --nextTimeUnitIndex
        }
        activeTimeUnitIndex = nextTimeUnitIndex
        activeTimeUnit = timeUnitList[nextTimeUnitIndex]
        activeTimeUnitWidth = (timelineWidth / (timelineDurationMs / activeTimeUnit))
        return true
    }

    private fun calculateBounds() {
        val unitWidth = activeTimeUnitWidth
        if (unitWidth <= 0.0F) {
            timeWindowLeft = INVALID_DIMENSION
            return
        }
        timeWindowLeft = timelineLeft - ((timelineLeft - windowLeft) / unitWidth).toInt().times(unitWidth)
    }
}
