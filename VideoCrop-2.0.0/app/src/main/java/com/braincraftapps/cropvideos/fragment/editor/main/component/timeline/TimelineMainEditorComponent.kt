/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: TimelineMainEditorComponent.kt
 * @modified: Aug 21, 2024, 04:44 PM
 */

package com.braincraftapps.cropvideos.fragment.editor.main.component.timeline

import android.graphics.Bitmap
import android.os.Bundle
import com.braincraftapps.cropvideos.databinding.FragmentMainEditorBinding
import com.braincraftapps.cropvideos.fragment.editor.main.MainEditorFragment
import com.braincraftapps.cropvideos.fragment.editor.main.component.MainEditorComponent
import com.braincraftapps.droid.llamaplayer.MediaMetadataRetriever
import com.braincraftapps.droid.timeline.data.TimelineItem
import com.braincraftapps.droid.timeline.widget.TimelineView

class TimelineMainEditorComponent(fragment: MainEditorFragment) : MainEditorComponent(fragment), TimelineView.FrameCollector {
    private var mediaMetadataRetriever: MediaMetadataRetriever? = null

    override fun onViewBindingCreated(viewBinding: FragmentMainEditorBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        viewBinding.timelineView.setFrameCollector(this)
        val absoluteFile = editorInput.dataSet.first().absoluteFile
        viewBinding.timelineView.addVideo(absoluteFile)
        val metadataRetriever = MediaMetadataRetriever().also {
            mediaMetadataRetriever = it
        }
        metadataRetriever.setDataSource(absoluteFile)
    }

    override fun onDestroyViewBinding(viewBinding: FragmentMainEditorBinding) {
        super.onDestroyViewBinding(viewBinding)
        viewBinding.timelineView.setFrameCollector(null)
        mediaMetadataRetriever?.release()
        mediaMetadataRetriever = null
    }

    override suspend fun getFrameAt(video: TimelineItem.Video, positionMs: Long, width: Int, height: Int): Bitmap? {
        return mediaMetadataRetriever?.getFrameAt(positionMs.times(1000), width, height)
    }
}
