/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: BubbleComponent.kt
 * @modified: Jul 09, 2024, 10:54 AM
 */

package com.braincraftapps.droid.timeline.component.bubble

import android.graphics.Canvas
import com.braincraftapps.droid.common.extension.core.dpToPx
import com.braincraftapps.droid.timeline.component.TimelineComponent
import com.braincraftapps.droid.timeline.widget.TimelineView

internal class BubbleComponent(
    timelineView: TimelineView
) : TimelineComponent(timelineView, timelineView.context.dpToPx(24)) {
    override fun onDraw(canvas: Canvas) {

    }
}
