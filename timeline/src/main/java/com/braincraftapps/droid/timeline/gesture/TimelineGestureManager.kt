/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: TimelineGestureManager.kt
 * @modified: Jul 11, 2024, 12:05 PM
 */

package com.braincraftapps.droid.timeline.gesture

import android.graphics.PointF
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import com.braincraftapps.droid.common.extension.lang.currentTimeMillis
import com.braincraftapps.droid.common.extension.lang.toPrimitiveType
import com.braincraftapps.droid.timeline.widget.TimelineView
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sqrt

internal class TimelineGestureManager(val timelineView: TimelineView) : DynamicAnimation.OnAnimationUpdateListener,
    DynamicAnimation.OnAnimationEndListener {
    companion object {
        private const val INVALID_DIMENSION: Float = TimelineView.INVALID_DIMENSION
        private const val INVALID_TIME: Long = TimelineView.INVALID_TIME
        private const val MINIMUM_TRANSLATE_DISTANCE_TO_IGNORE_CLICK: Float = 10.0F
        private const val MINIMUM_DELAY_BETWEEN_SCALE_AND_TRANSLATION: Long = 350
    }

    private var isAttachedToWindow: Boolean = false
    private var fingerCount: Int = 0
    private val startPoint: PointF = PointF()
    private var moveDistanceOfFirstPointer: Float = 0F
    private var isNotAClickConfirmed: Boolean = false
    private val pointerMap: HashMap<Int, PointF> = HashMap()
    private val pivotPointF: PointF = PointF()
    private var previousPivotX: Float = 0F
    private var previousPivotY: Float = 0F
    private var previousTouchSpan: Float = 1F
    private var scale: Float = 1F
    private var flingTranslateX: Float = 0.0F
    private var minimumFlingVelocity: Float = 0.0F
    private var maximumFlingVelocity: Float = 0.0F
    private var velocityTracker: VelocityTracker? = null
    private val flingAnimation: FlingAnimation by lazy {
        val animation = FlingAnimation(FloatValueHolder())
        animation.addUpdateListener(this)
        animation.addEndListener(this)
        animation.friction = 1.0F
        return@lazy animation
    }
    private var scalePublishTime: Long = INVALID_TIME

    var isUserInteracting: Boolean = false
        private set

    override fun onAnimationUpdate(animation: DynamicAnimation<*>?, value: Float, velocity: Float) {
        if (fingerCount > 1) {
            return
        }
        val elapsedTime = currentTimeMillis - scalePublishTime
        if (elapsedTime < MINIMUM_DELAY_BETWEEN_SCALE_AND_TRANSLATION) {
            return
        }
        val dx = value - flingTranslateX
        timelineView.performTranslate(dx, 0.0F)
        flingTranslateX = value
    }

    override fun onAnimationEnd(animation: DynamicAnimation<*>?, canceled: Boolean, value: Float, velocity: Float) {
        isUserInteracting = false
        timelineView.publishSeekStop()
    }

    fun attachToWindow() {
        if (isAttachedToWindow) {
            return
        }
        velocityTracker = VelocityTracker.obtain()
        val configuration = ViewConfiguration.get(timelineView.context)
        minimumFlingVelocity = configuration.scaledMinimumFlingVelocity.toFloat()
        maximumFlingVelocity = configuration.scaledMaximumFlingVelocity.toFloat()
        isAttachedToWindow = true
    }

    fun detachFromWindow() {
        if (!isAttachedToWindow) {
            return
        }
        isUserInteracting = false
        isAttachedToWindow = false
        velocityTracker?.recycle()
        velocityTracker = null
    }

    fun cancelGesture() {
        flingAnimation.cancel()
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isAttachedToWindow || !timelineView.isEnabled) {
            return false
        }
        if (timelineView.performComponentTouchAction(event)) {
            return true
        }
        val velocityTracker = velocityTracker
        velocityTracker?.addMovement(event)
        event.findFingerCount()
        if (fingerCount > 1) {
            // If multi finger available, this gesture is not a click.
            isNotAClickConfirmed = true
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                timelineView.publishSeekStart()
                flingAnimation.cancel()
                isUserInteracting = true
                isNotAClickConfirmed = false
                moveDistanceOfFirstPointer = 0F
                startPoint.x = event.getX(0)
                startPoint.y = event.getY(0)
                event.updateTouchParameters()
                scale = 1F
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (isNotAClickConfirmed) {
                    event.updateTouchParameters()
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (isNotAClickConfirmed) {
                    // Check the dot product of current velocities.
                    // If the pointer that left was opposing another velocity vector, clear.
                    if (velocityTracker != null && fingerCount <= 1) {
                        velocityTracker.computeCurrentVelocity(1000, maximumFlingVelocity)
                        val upIndex: Int = event.actionIndex
                        val id1: Int = event.getPointerId(upIndex)
                        val x1: Float = velocityTracker.getXVelocity(id1)
                        val y1: Float = velocityTracker.getYVelocity(id1)
                        for (i in 0 until event.pointerCount) {
                            if (i == upIndex) continue
                            val id2: Int = event.getPointerId(i)
                            val x: Float = x1 * velocityTracker.getXVelocity(id2)
                            val y: Float = y1 * velocityTracker.getYVelocity(id2)
                            val dot = x + y
                            if (dot < 0) {
                                velocityTracker.clear()
                                break
                            }
                        }
                    } else {
                        velocityTracker?.clear()
                    }
                    event.updateTouchParameters()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isNotAClickConfirmed) {
                    // A fling must travel the minimum tap distance
                    if (velocityTracker != null && fingerCount <= 1) {
                        val pointerId: Int = event.getPointerId(0)
                        velocityTracker.computeCurrentVelocity(1000, maximumFlingVelocity)
                        val velocityY = velocityTracker.getYVelocity(pointerId)
                        val velocityX = velocityTracker.getXVelocity(pointerId)
                        if ((velocityY.absoluteValue > minimumFlingVelocity) || (velocityX.absoluteValue > minimumFlingVelocity)) {
                            flingTranslateX = 0.0F
                            flingAnimation.setStartValue(0.0F)
                            flingAnimation.setStartVelocity(velocityX)
                            flingAnimation.start()
                        } else {
                            isUserInteracting = false
                        }
                    } else {
                        velocityTracker?.clear()
                        isUserInteracting = false
                    }
                } else {
                    isUserInteracting = false
                    timelineView.publishSeekStop()
                    if (moveDistanceOfFirstPointer <= MINIMUM_TRANSLATE_DISTANCE_TO_IGNORE_CLICK) {
                        val pivotPoint = event.findPivotPoint()
                        timelineView.performClick(pivotPoint.x, pivotPoint.y)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                moveDistanceOfFirstPointer = findEuclideanDistance(startPoint.x, startPoint.y, event.getX(0), event.getY(0))
                if (!isNotAClickConfirmed && moveDistanceOfFirstPointer > MINIMUM_TRANSLATE_DISTANCE_TO_IGNORE_CLICK) {
                    isNotAClickConfirmed = true
                }
                if (isNotAClickConfirmed) {
                    velocityTracker?.addMovement(event)
                    val pivotPoint = event.findPivotPoint()
                    event.publishScale(pivot = pivotPoint)
                    publishTranslation(pivot = pivotPoint)
                    previousPivotX = pivotPoint.x
                    previousPivotY = pivotPoint.y
                }
            }
        }
        return true
    }

    private fun publishTranslation(pivot: PointF) {
        if (fingerCount > 1) {
            velocityTracker?.clear()
            return
        }
        val elapsedTime = currentTimeMillis - scalePublishTime
        if (elapsedTime < MINIMUM_DELAY_BETWEEN_SCALE_AND_TRANSLATION) {
            velocityTracker?.clear()
            return
        }
        val dx = pivot.x - previousPivotX
        val dy = pivot.y - previousPivotY
        timelineView.performTranslate(dx, dy)
    }

    private fun MotionEvent.publishScale(pivot: PointF) {
        if (fingerCount < 2) {
            return
        }
        velocityTracker?.clear()
        val touchSpan = findTouchSpan(pivot)
        val ds = touchSpan / previousTouchSpan
        previousTouchSpan = touchSpan
        scalePublishTime = currentTimeMillis
        timelineView.performScale(ds)
    }

    private fun MotionEvent.updateTouchParameters() {
        val pivotPoint = findPivotPoint()
        previousPivotX = pivotPoint.x
        previousPivotY = pivotPoint.y
        previousTouchSpan = findTouchSpan(pivotPoint)
        pointerMap.clear()
        repeat(pointerCount) { pointerIndex ->
            pointerMap[getPointerId(pointerIndex)] = PointF(getX(pointerIndex), getY(pointerIndex))
        }
    }

    private fun MotionEvent.findPivotPoint(): PointF {
        val upActionIndex = if (actionMasked == MotionEvent.ACTION_POINTER_UP) actionIndex else -1
        var sumX = 0F
        var sumY = 0F
        var sumCount = 0
        repeat(pointerCount) { pointerIndex ->
            if (pointerIndex != upActionIndex) {
                sumX += getX(pointerIndex)
                sumY += getY(pointerIndex)
                sumCount++
            }
        }
        pivotPointF.x = sumX / sumCount
        pivotPointF.y = sumY / sumCount
        return pivotPointF
    }

    private fun MotionEvent.findTouchSpan(pivot: PointF): Float {
        var spanSumX = 0F
        var spanSumY = 0F
        var sumCount = 0
        val ignoreIndex = if (actionMasked == MotionEvent.ACTION_POINTER_UP) actionIndex else -1
        repeat(pointerCount) { pointerIndex ->
            if (pointerIndex != ignoreIndex) {
                spanSumX += abs(pivot.x - getX(pointerIndex))
                spanSumY += abs(pivot.y - getY(pointerIndex))
                sumCount++
            }
        }
        if (sumCount > 1) {
            return (spanSumX / sumCount) + (spanSumY / sumCount)
        }
        return previousTouchSpan
    }

    private fun MotionEvent.findFingerCount(): Int {
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> fingerCount = 1
            MotionEvent.ACTION_POINTER_DOWN -> ++fingerCount
            MotionEvent.ACTION_POINTER_UP -> --fingerCount
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> fingerCount = 0
        }
        return fingerCount
    }

    private inline fun <reified N : Number> findEuclideanDistance(x1: Number, y1: Number, x2: Number, y2: Number): N {
        val dx = (x1.toFloat() - x2.toFloat()).absoluteValue
        val dy = (y1.toFloat() - y2.toFloat()).absoluteValue
        return sqrt(dx * dx + dy * dy).toPrimitiveType()
    }
}
