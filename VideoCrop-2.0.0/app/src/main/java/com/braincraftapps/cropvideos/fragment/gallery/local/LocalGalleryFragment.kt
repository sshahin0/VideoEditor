/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: LocalGalleryFragment.kt
 * @modified: Aug 21, 2024, 12:24 PM
 */

package com.braincraftapps.cropvideos.fragment.gallery.local

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import com.braincraftapps.common.extensions.core.dividerColor
import com.braincraftapps.cropvideos.R
import com.braincraftapps.cropvideos.databinding.FragmentGalleryLocalBinding
import com.braincraftapps.cropvideos.fragment.gallery.local.photo.LocalImageListFragment
import com.braincraftapps.cropvideos.fragment.gallery.local.video.LocalVideoListFragment
import com.braincraftapps.droid.common.extension.core.buildColorStateList
import com.braincraftapps.droid.common.extension.core.dpToPx
import com.braincraftapps.droid.common.extension.core.savedStates
import com.braincraftapps.droid.common.extension.core.savedStatesLiveData
import com.braincraftapps.droid.common.extension.core.toColorStateList
import com.braincraftapps.droid.common.extension.lang.parseColor
import com.braincraftapps.droid.common.extension.lifecycle.observeCompat
import com.braincraftapps.droid.common.extension.view.doOnClick
import com.braincraftapps.droid.common.extension.view.hide
import com.braincraftapps.droid.common.extension.view.setMarginCompat
import com.braincraftapps.droid.common.permission.data.Permission
import com.braincraftapps.droid.picker.provider.media.params.MediaType
import com.braincraftapps.droid.picker.provider.paging.data.PagingRequest
import com.braincraftapps.droid.picker.provider.vendor.local.LocalMediaProvider
import com.braincraftapps.droid.picker.provider.vendor.local.data.collection.LocalMediaCollection
import com.braincraftapps.droid.picker.provider.vendor.local.data.file.LocalMediaFile
import com.braincraftapps.droid.picker.provider.vendor.local.request.collection.params.LocalCollectionOrder
import com.braincraftapps.droid.picker.provider.vendor.local.request.file.LocalMediaFileRequest
import com.braincraftapps.droid.picker.ui.data.theme.MediaTheme
import com.braincraftapps.droid.picker.ui.databinding.FragmentMediaCollectionDropdownBinding
import com.braincraftapps.droid.picker.ui.fragment.collection.data.MediaQuery
import com.braincraftapps.droid.picker.ui.fragment.collection.data.Result
import com.braincraftapps.droid.picker.ui.fragment.collection.dropdown.local.LocalMediaCollectionDropdownFragment
import com.braincraftapps.droid.picker.ui.fragment.file.MediaFileListFragment
import com.braincraftapps.droid.picker.ui.fragment.search.SearchMediaFragment
import com.braincraftapps.droid.picker.ui.fragment.utils.capture.CapturedMedia
import com.google.android.material.shape.MaterialShapeDrawable

class LocalGalleryFragment : LocalMediaCollectionDropdownFragment() {
    companion object {
        private const val SAVED_STATE_SELECTED_POSITION = "local_video_list_fragment_saved_state_selected_position"
        private const val SAVED_KEY_CAMERA_CARD_MARGIN = "local_gallery_fragment_saved_key_camera_card_margin"
        const val COLLECTION_ID = "local_gallery_collection_id"
        fun newInstance(): LocalGalleryFragment {
            return LocalGalleryFragment()
        }
    }

    private var _localViewBinding: FragmentGalleryLocalBinding? = null
    private val localViewBinding: FragmentGalleryLocalBinding
        get() = _localViewBinding ?: error("No local view binding found")

    private val cameraCardBottomMargin: Int by lazy {
        return@lazy context.resources.getDimensionPixelSize(R.dimen.camera_button_bottom_margin)
    }

    private val cameraCardBottomMarginActivated: Int by lazy {
        return@lazy context.resources.getDimensionPixelSize(com.braincraftapps.droid.picker.ui.R.dimen.bottom_media_selection_height) + context.dpToPx<Int>(24)
    }

    private var selectedPosition: Int by savedStates(SAVED_STATE_SELECTED_POSITION) { 0 }
    private val selectedPositionLiveData: LiveData<Int> by savedStatesLiveData(SAVED_STATE_SELECTED_POSITION) { 0 }
    private var cameraCardMargin: Int by savedStates(SAVED_KEY_CAMERA_CARD_MARGIN) { cameraCardBottomMargin }
    private val cameraCardMarginLiveData: LiveData<Int> by savedStatesLiveData(SAVED_KEY_CAMERA_CARD_MARGIN) { cameraCardBottomMargin }

    private val tabBackground: Drawable
        get() = MaterialShapeDrawable().also {
            it.fillColor = buildColorStateList {
                add(Color.TRANSPARENT) {
                    without(com.braincraftapps.droid.common.utils.color.state.State.ACTIVATED)
                }
                add("#212224".parseColor()) {
                    with(com.braincraftapps.droid.common.utils.color.state.State.ACTIVATED)
                }
            }
            it.strokeColor = context.dividerColor.toColorStateList()
            it.strokeWidth = context.dpToPx(0.6)
            it.setCornerSize(context.dpToPx<Float>(48))
        }

    private val tabTextColor: ColorStateList
        get() = buildColorStateList {
            add("#6B747F".parseColor()) {
                without(com.braincraftapps.droid.common.utils.color.state.State.ACTIVATED)
            }
            add("#C3D7E6".parseColor()) {
                with(com.braincraftapps.droid.common.utils.color.state.State.ACTIVATED)
            }
        }

    private val provider: LocalMediaProvider by lazy {
        return@lazy LocalMediaProvider.Builder(context).build()
    }

    private var cameraCardMarginAnimator: ValueAnimator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewBinding = FragmentGalleryLocalBinding.inflate(inflater, container, false)
        val collectionView = super.onCreateView(inflater, viewBinding.collectionContainer, savedInstanceState)
        collectionView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        viewBinding.collectionContainer.addView(collectionView)
        _localViewBinding = viewBinding
        return viewBinding.root
    }

    override fun onViewBindingCreated(viewBinding: FragmentMediaCollectionDropdownBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        localViewBinding.cameraCardView.doOnClick {
            if (selectedPosition == 0) {
                captureWithCamera(CapturedMedia.video())
            } else {
                captureWithCamera(CapturedMedia.image())
            }
        }
        localViewBinding.cameraCardView.setCardBackgroundColor(mediaTheme.primaryColor)
        cameraCardMarginLiveData.observeCompat(viewLifecycleOwner) { updateCameraButtonMargin(it) }
        localViewBinding.photosTextView.background = tabBackground
        localViewBinding.photosTextView.setTextColor(tabTextColor)
        localViewBinding.videosTextView.background = tabBackground
        localViewBinding.videosTextView.setTextColor(tabTextColor)
        localViewBinding.videosFrameLayout.doOnClick {
            if (selectedPosition == 0) {
                return@doOnClick
            }
            selectedPosition = 0
            recreateMediaListFragment()
        }
        localViewBinding.photosFrameLayout.doOnClick {
            if (selectedPosition == 1) {
                return@doOnClick
            }
            selectedPosition = 1
            recreateMediaListFragment()
        }
        selectedPositionLiveData.observeCompat(viewLifecycleOwner) { position ->
            when (position) {
                0 -> {
                    localViewBinding.videosTextView.isActivated = true
                    localViewBinding.photosTextView.isActivated = false
                }

                1 -> {
                    localViewBinding.videosTextView.isActivated = false
                    localViewBinding.photosTextView.isActivated = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraCardMarginAnimator?.cancel()
        cameraCardMarginAnimator = null
        _localViewBinding = null
    }

    override fun onCollectionId(): String {
        return COLLECTION_ID
    }

    override fun onCreateMediaTheme(parentTheme: MediaTheme?): MediaTheme? {
        return parentTheme?.buildUpon()?.setSurfaceColor("#141414".parseColor())?.build()
    }

    override fun onMediaDropdownContainerLayout(): FrameLayout {
        super.onMediaDropdownContainerLayout().hide()
        return localViewBinding.localDropdownContainer
    }

    override fun onCreateMediaSearchFragment(): SearchMediaFragment<*>? {
        return null
    }

    override fun onSelectionBottomSheetVisibilityChanged(show: Boolean) {
        super.onSelectionBottomSheetVisibilityChanged(show)
        cameraCardMargin = if (show) cameraCardBottomMarginActivated else cameraCardBottomMargin
    }

    override fun onCreatePagingMediaListRequest(mediaQuery: MediaQuery<LocalMediaCollection>): PagingRequest {
        val builder = when (mediaQuery) {
            is MediaQuery.Collection -> when (selectedPosition) {
                0 -> LocalMediaFileRequest.Builder(MediaType.VIDEO)
                else -> LocalMediaFileRequest.Builder(MediaType.IMAGE).also {
                    it.filter(MediaStore.Images.Media.MIME_TYPE) {
                        notLikeAny("image/gif")
                    }
                }
            }

            is MediaQuery.SearchQuery -> error("Search is not supported")
        }
        builder.addCollection(mediaQuery.collection)
        builder.orderBy(MediaStore.MediaColumns.DATE_ADDED) {
            descending(true)
        }
        return builder.build()
    }

    override fun onCreateMediaListFragment(
        mediaQuery: MediaQuery<LocalMediaCollection>,
        pagingRequest: PagingRequest
    ): MediaFileListFragment<out LocalMediaFile> {
        return when (selectedPosition) {
            0 -> LocalVideoListFragment()
            else -> LocalImageListFragment()
        }
    }

    override fun onMediaCollectionPagingSource(): PagingSource<Int, out LocalMediaCollection> {
        return provider.getMediaCollectionPagingSource {
            exclude(MediaType.AUDIO)
            withVirtualRecentCollection()
            filter(MediaType.IMAGE, MediaStore.Images.Media.MIME_TYPE) {
                notLikeAny("image/gif")
            }
            setOrderBy(LocalCollectionOrder.TITLE)
        }
    }

    override fun onRuntimePermissions(): List<Permission> = buildList {
        add(Permission.videos())
        add(Permission.photos())
    }

    override fun onMediaListSavedStateKey(result: Result): String {
        return "${super.onMediaListSavedStateKey(result)}:${selectedPosition}"
    }

    private fun updateCameraButtonMargin(margin: Int) {
        cameraCardMarginAnimator?.cancel()
        cameraCardMarginAnimator = null
        val viewBinding = _localViewBinding ?: return
        val layoutParams = viewBinding.cameraCardView.layoutParams as? MarginLayoutParams ?: return
        val marginFrom = layoutParams.bottomMargin
        if (marginFrom == margin) {
            return
        }
        val animator = ValueAnimator.ofInt(marginFrom, margin).also {
            cameraCardMarginAnimator = it
        } ?: run {
            viewBinding.cameraCardView.setMarginCompat {
                bottomMargin = margin
            }
            return
        }
        animator.addUpdateListener {
            val animatedValue = it.animatedValue as? Int ?: return@addUpdateListener
            viewBinding.cameraCardView.setMarginCompat {
                bottomMargin = animatedValue
            }
        }
        animator.duration = 250
        animator.interpolator = PathInterpolator(0.0F, 0.0F, 0.2F, 1.0F)
        animator.start()
    }
}
