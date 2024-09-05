/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: MediaSelectionViewHolder.kt
 * @modified: Aug 21, 2024, 02:06 PM
 */

package com.braincraftapps.cropvideos.fragment.gallery.utils.viewholder

import android.view.View
import android.view.ViewGroup
import coil.dispose
import coil.load
import coil.request.videoFramePercent
import com.braincraftapps.cropvideos.databinding.ViewHolderMediaSelectionBinding
import com.braincraftapps.droid.common.extension.coil.withCheckerboardBackground
import com.braincraftapps.droid.common.extension.core.setListener
import com.braincraftapps.droid.common.extension.graphics.randomDarkMutedColor
import com.braincraftapps.droid.common.extension.view.hide
import com.braincraftapps.droid.common.extension.view.layoutInflater
import com.braincraftapps.droid.common.extension.view.show
import com.braincraftapps.droid.picker.provider.media.MediaFile
import com.braincraftapps.droid.picker.provider.media.params.file.AudioFile
import com.braincraftapps.droid.picker.provider.media.params.file.ImageFile
import com.braincraftapps.droid.picker.provider.media.params.file.VideoFile
import com.braincraftapps.droid.picker.ui.adapter.selection.utils.download.DownloadStatus
import com.braincraftapps.droid.picker.ui.viewholder.selection.SelectedMediaViewHolder

class MediaSelectionViewHolder(
    viewBinding: ViewHolderMediaSelectionBinding
) : SelectedMediaViewHolder<ViewHolderMediaSelectionBinding>(viewBinding), View.OnClickListener {
    companion object {
        fun newInstance(parent: ViewGroup): MediaSelectionViewHolder {
            val viewBinding = ViewHolderMediaSelectionBinding.inflate(parent.layoutInflater, parent, false)
            return MediaSelectionViewHolder(viewBinding)
        }
    }

    val fallbackColor: Int by lazy { randomDarkMutedColor }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewBinding.root.clearAnimation()
        viewBinding.imageView.clearAnimation()
        val content = itemOrNull ?: return
        onLoadThumbnail(content)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewBinding.imageView.dispose()
        viewBinding.imageView.setImageDrawable(null)
        viewBinding.root.clearAnimation()
        viewBinding.imageView.clearAnimation()
    }

    override fun onBindPlaceholder(position: Int) {
        super.onBindPlaceholder(position)
        viewBinding.typeImageView.hide()
        viewBinding.errorTextView.hide()
        viewBinding.removeButton.hide()
        prepareContainer()
    }

    override fun onBind(item: MediaFile) {
        prepareContainer()
        viewBinding.removeButton.show()
        if (selectionConfig.showTypeIcon) {
            val typeDrawableRes = when (item) {
                is ImageFile -> com.braincraftapps.droid.picker.ui.R.drawable.vector_image_24dp
                is AudioFile -> com.braincraftapps.droid.picker.ui.R.drawable.vector_music_note_24dp
                is VideoFile -> com.braincraftapps.droid.picker.ui.R.drawable.vector_movie_24dp
                else -> null
            }
            if (typeDrawableRes != null) {
                viewBinding.typeImageView.load(typeDrawableRes)
                viewBinding.typeImageView.show()
            } else {
                viewBinding.typeImageView.hide()
            }
        } else {
            viewBinding.typeImageView.hide()
        }
    }

    override fun onPostBind(item: MediaFile) {
        super.onPostBind(item)
        viewBinding.removeButton.setOnClickListener(this)
    }

    override fun onChangeDownloadStatus(status: DownloadStatus) {
        super.onChangeDownloadStatus(status)
        when (status) {
            is DownloadStatus.Downloading -> {
                viewBinding.imageView.animate()
                    .alpha(0.6F)
                    .start()
                viewBinding.progressCircular.animate()
                    .alpha(1.0F)
                    .setListener(
                        onStart = {
                            viewBinding.progressCircular.show()
                        }
                    )
                    .start()
                viewBinding.errorTextView.hide()
            }

            is DownloadStatus.Failed -> {
                viewBinding.imageView.animate()
                    .alpha(1.0F)
                    .start()
                viewBinding.progressCircular.animate()
                    .alpha(0.0F)
                    .setListener(
                        onEnd = {
                            viewBinding.progressCircular.hide()
                        }
                    )
                    .start()
                viewBinding.errorTextView.show()
            }

            else -> {
                viewBinding.imageView.animate()
                    .alpha(1.0F)
                    .start()
                viewBinding.progressCircular.animate()
                    .alpha(0.0F)
                    .setListener(
                        onEnd = {
                            viewBinding.progressCircular.hide()
                        }
                    )
                    .start()
                viewBinding.errorTextView.hide()
            }
        }
    }

    override fun onRecycled() {
        super.onRecycled()
        viewBinding.imageView.dispose()
        viewBinding.imageView.setImageDrawable(null)
        viewBinding.removeButton.setOnClickListener(null)
    }

    override fun onClick(view: View) {
        when (view.id) {
            viewBinding.removeButton.id -> {
                publishClick(KEY_REMOVE_ITEM)
            }
        }
    }

    private fun prepareContainer() {
        viewBinding.progressCircular.setIndicatorColor(mediaTheme.primaryColor)
        if (viewBinding.imageView.background == null) {
            viewBinding.imageView.setBackgroundColor(fallbackColor)
        }
    }

    private fun onLoadThumbnail(content: MediaFile) {
        viewBinding.imageView.load(content) {
            allowHardware(false)
            videoFramePercent(0.1)
            withCheckerboardBackground()
        }
    }
}
