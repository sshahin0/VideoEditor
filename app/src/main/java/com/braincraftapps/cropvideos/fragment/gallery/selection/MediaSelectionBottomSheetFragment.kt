/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: MediaSelectionBottomSheetFragment.kt
 * @modified: Aug 21, 2024, 12:42 PM
 */

package com.braincraftapps.cropvideos.fragment.gallery.selection

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.braincraftapps.cropvideos.R
import com.braincraftapps.cropvideos.databinding.FragmentMediaSelectionBottomSheetBinding
import com.braincraftapps.cropvideos.fragment.gallery.utils.adapter.selection.CustomMediaSelectionListAdapter
import com.braincraftapps.droid.common.extension.core.buildColorStateList
import com.braincraftapps.droid.common.extension.core.dpToPx
import com.braincraftapps.droid.common.extension.core.getReadableTime
import com.braincraftapps.droid.common.extension.core.savedStates
import com.braincraftapps.droid.common.extension.core.savedStatesLiveData
import com.braincraftapps.droid.common.extension.core.toColorStateList
import com.braincraftapps.droid.common.extension.core.withOpacity
import com.braincraftapps.droid.common.extension.graphics.setShapeAppearanceModel
import com.braincraftapps.droid.common.extension.lifecycle.observeCompat
import com.braincraftapps.droid.common.extension.view.doOnClick
import com.braincraftapps.droid.common.extension.view.setShapeDrawableBackground
import com.braincraftapps.droid.common.extension.view.smoothScrollToPositionCompat
import com.braincraftapps.droid.common.widget.recycler.animator.DefaultItemAnimatorCompat
import com.braincraftapps.droid.picker.provider.media.MediaFile
import com.braincraftapps.droid.picker.provider.media.params.file.ImageFile
import com.braincraftapps.droid.picker.provider.media.params.file.VideoFile
import com.braincraftapps.droid.picker.ui.adapter.selection.MediaSelectionListAdapter
import com.braincraftapps.droid.picker.ui.data.media.DownloadedMediaFile
import com.braincraftapps.droid.picker.ui.extension.view.applyMediaTheme
import com.braincraftapps.droid.picker.ui.fragment.selection.MediaSelectionFragment
import com.google.android.material.shape.CornerFamily

class MediaSelectionBottomSheetFragment : MediaSelectionFragment<FragmentMediaSelectionBottomSheetBinding>() {
    private companion object {
        private const val SAVED_KEY_SELECTED_ITEM_DURATION = "selection_example_fragment_saved_key_selected_item_duration"
    }

    private var selectedItemDuration: Long by savedStates(SAVED_KEY_SELECTED_ITEM_DURATION) { 0 }
    private val selectedItemDurationLiveData: LiveData<Long> by savedStatesLiveData(SAVED_KEY_SELECTED_ITEM_DURATION) { 0 }

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMediaSelectionBottomSheetBinding {
        return FragmentMediaSelectionBottomSheetBinding.inflate(inflater, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewBindingCreated(viewBinding: FragmentMediaSelectionBottomSheetBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        viewBinding.root.setShapeDrawableBackground {
            val cornerSize: Float = context.dpToPx(14)
            setShapeAppearanceModel {
                setTopLeftCorner(CornerFamily.ROUNDED, cornerSize)
                setTopRightCorner(CornerFamily.ROUNDED, cornerSize)
            }
            fillColor = mediaTheme.surfaceColor.toColorStateList()
        }
        viewBinding.recyclerView.applyMediaTheme(mediaTheme)
        viewBinding.nextButton.backgroundTintList = buildColorStateList {
            add(mediaTheme.primaryColor, android.R.attr.state_enabled)
            add(mediaTheme.onSurfaceColor.withOpacity(0.12F))
        }
        viewBinding.nextButton.doOnClick { publishSelectedMedia() }
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        viewBinding.recyclerView.itemAnimator = DefaultItemAnimatorCompat()
        viewBinding.recyclerView.setHasFixedSize(true)
        isLoadingLiveData.observeCompat(viewLifecycleOwner) { isLoading ->
            viewBinding.nextButton.isEnabled = !isLoading
        }
        selectedItemDurationLiveData.observeCompat(viewLifecycleOwner) {
            viewBinding.durationTextView.text = context.getReadableTime(it)
        }
        selectedMediaListSizeLiveData.observeCompat(viewLifecycleOwner) {
            viewBinding.itemCountTextView.text = context.resources.getQuantityString(R.plurals.number_of_clips_selected, it, it)
        }
    }

    override fun onDestroyViewBinding(viewBinding: FragmentMediaSelectionBottomSheetBinding) {
        super.onDestroyViewBinding(viewBinding)
        viewBinding.recyclerView.layoutManager = null
    }

    override fun onMediaSelectionListAdapter(): MediaSelectionListAdapter {
        return CustomMediaSelectionListAdapter()
    }

    override fun onListAdapterCreated(
        viewBinding: FragmentMediaSelectionBottomSheetBinding,
        adapter: MediaSelectionListAdapter,
        savedInstanceState: Bundle?
    ) {
        viewBinding.recyclerView.adapter = adapter
    }

    override fun onDestroyListAdapter(adapter: MediaSelectionListAdapter) {
        viewBinding.recyclerView.adapter = null
    }

    override fun onMediaSelectionStateChanged(media: MediaFile, key: String, selected: Boolean) {
        super.onMediaSelectionStateChanged(media, key, selected)
        if (selectedMediaListSize <= 0) {
            return
        }
        val durationInMs: Long = when (media) {
            is DownloadedMediaFile -> when (val mediaFile = media.mediaFile) {
                is VideoFile -> mediaFile.durationInMs
                is ImageFile -> 3000L
                else -> 0L
            }

            is VideoFile -> media.durationInMs
            is ImageFile -> 3000L
            else -> 0L
        }
        val currentDuration = selectedItemDuration
        val nextDuration = if (selected) currentDuration + durationInMs else currentDuration - durationInMs
        selectedItemDuration = nextDuration.coerceAtLeast(0)
    }

    override fun onMediaFileSelectionCountChanged(count: Int) {
        super.onMediaFileSelectionCountChanged(count)
        if (count <= 0) {
            selectedItemDuration = 0
        }
    }

    override fun onRecyclerViewScrollTo(position: Int) {
        super.onRecyclerViewScrollTo(position)
        viewBinding.recyclerView.smoothScrollToPositionCompat(
            position = position,
            millisecondsPerInch = 50.0F
        )
    }
}
