/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: HomeGalleryFragment.kt
 * @modified: Aug 21, 2024, 10:32 AM
 */

package com.braincraftapps.cropvideos.fragment.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.braincraftapps.common.extensions.core.onPrimaryColor
import com.braincraftapps.common.extensions.core.onSurfaceColor
import com.braincraftapps.common.extensions.core.primaryColor
import com.braincraftapps.common.extensions.core.surfaceColor
import com.braincraftapps.cropvideos.R
import com.braincraftapps.cropvideos.activity.editor.EditorActivity
import com.braincraftapps.cropvideos.activity.editor.data.EditorInput
import com.braincraftapps.cropvideos.databinding.FragmentGalleryHomeBinding
import com.braincraftapps.cropvideos.fragment.gallery.local.LocalGalleryFragment
import com.braincraftapps.cropvideos.fragment.gallery.pixabay.collection.PixabayVideoTabFragment
import com.braincraftapps.cropvideos.fragment.gallery.selection.MediaSelectionBottomSheetFragment
import com.braincraftapps.droid.common.extension.core.doOnBackPressed
import com.braincraftapps.droid.common.extension.core.getDrawableCompat
import com.braincraftapps.droid.common.extension.core.getRuntimePermissionResult
import com.braincraftapps.droid.common.extension.core.isPermissionGrantedAny
import com.braincraftapps.droid.common.extension.core.makeToast
import com.braincraftapps.droid.common.extension.core.savedStates
import com.braincraftapps.droid.common.extension.core.toColorStateList
import com.braincraftapps.droid.common.extension.lang.parseColor
import com.braincraftapps.droid.common.extension.lifecycle.observeCompat
import com.braincraftapps.droid.common.extension.view.doOnClick
import com.braincraftapps.droid.common.extension.view.hide
import com.braincraftapps.droid.common.extension.view.show
import com.braincraftapps.droid.common.extension.view.showKeyboard
import com.braincraftapps.droid.common.permission.data.Permission
import com.braincraftapps.droid.picker.provider.media.MediaFile
import com.braincraftapps.droid.picker.provider.media.params.file.VideoFile
import com.braincraftapps.droid.picker.provider.media.remote.file.RemoteMediaFile
import com.braincraftapps.droid.picker.provider.vendor.local.data.file.LocalMediaFile
import com.braincraftapps.droid.picker.ui.data.media.DownloadedMediaFile
import com.braincraftapps.droid.picker.ui.data.theme.MediaTheme
import com.braincraftapps.droid.picker.ui.fragment.MediaFragment
import com.braincraftapps.droid.picker.ui.fragment.collection.data.CollectionState
import com.braincraftapps.droid.picker.ui.fragment.selection.MediaSelectionFragment

class HomeGalleryFragment : MediaFragment<FragmentGalleryHomeBinding>() {
    companion object {
        private const val SAVED_STATE_EDITOR_ACTIVITY_LAUNCHED = "gallery_fragment_saved_state_editor_activity_launched"
        private const val SAVED_KEY_IS_REMOTE_COLLECTION = "gallery_fragment_saved_key_is_remote_collection"
    }

    private var isEditorActivityLaunched: Boolean by savedStates(SAVED_STATE_EDITOR_ACTIVITY_LAUNCHED) { false }
    private var isRemoteCollection: Boolean by savedStates(SAVED_KEY_IS_REMOTE_COLLECTION) { false }
    private val viewPagerCallback: ViewPagerCallback by lazy { ViewPagerCallback() }
    private var editorActivityLauncher: ActivityResultLauncher<EditorInput>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editorActivityLauncher = registerForActivityResult(EditorActivity.Contract()) {
            isEditorActivityLaunched = false
        }
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGalleryHomeBinding {
        return FragmentGalleryHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewBindingCreated(viewBinding: FragmentGalleryHomeBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        doOnBackPressed {
            if (selectedMediaListSize > 0) {
                resetMediaSelection()
                return@doOnBackPressed true
            }
            return@doOnBackPressed false
        }
        viewBinding.localTabCardView.doOnClick {
            if (isEditorActivityLaunched) {
                return@doOnClick
            }
            if (viewBinding.viewPager.currentItem != 0) {
                updateTabAndViewPager(0)
            } else {
                toggleDropdownLayout(LocalGalleryFragment.COLLECTION_ID)
            }
        }
        viewBinding.stockVideoTabCardView.doOnClick {
            if (isEditorActivityLaunched) {
                return@doOnClick
            }
            updateTabAndViewPager(1)
        }
        getCollectionStateLiveData(LocalGalleryFragment.COLLECTION_ID).observeCompat(viewLifecycleOwner) { state ->
            updateDropdownIcon(state)
        }
        viewBinding.viewPager.offscreenPageLimit = 2
        viewBinding.viewPager.isUserInputEnabled = false
        viewBinding.viewPager.adapter = GalleryListAdapter()
        viewBinding.viewPager.registerOnPageChangeCallback(viewPagerCallback)
    }

    override fun onResume() {
        super.onResume()
        updateTabIndicator()
    }

    override fun onDestroyViewBinding(viewBinding: FragmentGalleryHomeBinding) {
        super.onDestroyViewBinding(viewBinding)
        viewBinding.viewPager.unregisterOnPageChangeCallback(viewPagerCallback)
        viewBinding.viewPager.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        editorActivityLauncher?.unregister()
        editorActivityLauncher = null
    }

    override fun onCreateMediaTheme(parentTheme: MediaTheme?): MediaTheme {
        return MediaTheme.Builder()
            .setPrimaryColor(context.primaryColor)
            .setOnPrimaryColor(context.onPrimaryColor)
            .setSurfaceColor(context.surfaceColor)
            .setOnSurfaceColor(context.onSurfaceColor)
            .build()
    }

    override fun onCreateMediaSelectionFragment(): MediaSelectionFragment<*> {
        return MediaSelectionBottomSheetFragment()
    }

    override fun onInterceptMediaDownload(mediaFile: RemoteMediaFile, position: Int, key: String, extras: Bundle?): Boolean {
        return !isRemoteCollection
    }

    override fun onSelectedMediaFileList(mediaFileList: List<MediaFile>, extras: Bundle?) {
        super.onSelectedMediaFileList(mediaFileList, extras)
        if (isEditorActivityLaunched) {
            // Already editor activity launched. Nothing to do.
            return
        }
        val launcher = editorActivityLauncher ?: run {
            // No launcher found. Nothing to do.
            return
        }
        val dataSet = mediaFileList.mapNotNull {
            val absoluteFile = when (it) {
                is DownloadedMediaFile -> it.absoluteFile
                is LocalMediaFile -> it.absoluteFile
                else -> return@mapNotNull null
            }
            val durationInMs = when (it) {
                is DownloadedMediaFile -> (it.mediaFile as? VideoFile)?.durationInMs ?: 0
                is VideoFile -> it.durationInMs
                else -> 0
            }
            return@mapNotNull EditorInput.Data(
                uri = it.uri,
                absoluteFile = absoluteFile,
                durationInMs = durationInMs,
                mimeType = it.mimeType
            )
        }.filter { it.mimeType.startsWith("video") }.toSet()
        if (dataSet.isEmpty()) {
            makeToast("No valid media found!")
            return
        }
        resetMediaSelection()
        launcher.launch(EditorInput(dataSet))
        isEditorActivityLaunched = true
    }

    private fun updateTabAndViewPager(
        currentItem: Int = viewBinding.viewPager.currentItem
    ) {
        if (isMediaDownloadActivated) {
            return
        }
        if (viewBinding.viewPager.currentItem != currentItem) {
            isRemoteCollection = currentItem != 0
            viewBinding.root.showKeyboard(false) {
                viewBinding.viewPager.setCurrentItem(currentItem, false)
            }
        }
        updateTabIndicator()
        updateDropdownIcon()
    }

    private fun updateTabIndicator() {
        when (viewBinding.viewPager.currentItem) {
            0 -> {
                viewBinding.localButtonText.setTextColor(context.primaryColor)
                viewBinding.stockVideosButtonText.setTextColor("#6B747F".parseColor())
                viewBinding.localExpandCollapseImageView.imageTintList = context.primaryColor.toColorStateList()
                viewBinding.localDivider.show()
                viewBinding.stockVideosDivider.hide()
            }

            1 -> {
                viewBinding.localButtonText.setTextColor("#6B747F".parseColor())
                viewBinding.stockVideosButtonText.setTextColor(context.primaryColor)
                viewBinding.localExpandCollapseImageView.imageTintList = "#6B747F".parseColor().toColorStateList()
                viewBinding.localDivider.hide()
                viewBinding.stockVideosDivider.show()
            }
        }
    }

    private fun updateDropdownIcon(state: CollectionState = getCollectionState(LocalGalleryFragment.COLLECTION_ID)) {
        viewBinding.localButtonText.text = state.collection?.title ?: context.getString(R.string.tab_recent)
        val isGranted = context.getRuntimePermissionResult(listOf(Permission.photos(), Permission.videos())).isPermissionGrantedAny()
        if (isGranted && viewBinding.viewPager.currentItem == 0) {
            val drawable = when (state) {
                is CollectionState.Hidden -> context.getDrawableCompat(R.drawable.ic_expand_more_24dp)
                is CollectionState.Unknown -> context.getDrawableCompat(R.drawable.ic_expand_more_24dp)
                is CollectionState.Visible -> context.getDrawableCompat(R.drawable.ic_expand_less_24dp)
            }
            viewBinding.localExpandCollapseImageView.setImageDrawable(drawable)
        }
    }

    private inner class ViewPagerCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            updateDropdownIcon()
            updateTabIndicator()
        }
    }

    private inner class GalleryListAdapter : FragmentStateAdapter(childFragmentManager, lifecycleCompat) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> LocalGalleryFragment.newInstance()
            1 -> PixabayVideoTabFragment.newInstance()
            else -> error("No fragment found for position: $position")
        }
    }
}
