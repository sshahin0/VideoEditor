/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: MediaItemViewHolder.kt
 * @modified: Aug 20, 2024, 09:34 AM
 */

package com.braincraftapps.cropvideos.fragment.gallery.utils.viewholder

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import coil.dispose
import coil.load
import coil.request.videoFramePercent
import com.braincraftapps.cropvideos.databinding.ViewHolderMediaItemBinding
import com.braincraftapps.droid.common.extension.coil.withCheckerboardBackground
import com.braincraftapps.droid.common.extension.core.dpToPx
import com.braincraftapps.droid.common.extension.core.getReadableTime
import com.braincraftapps.droid.common.extension.core.toColorStateList
import com.braincraftapps.droid.common.extension.core.withOpacity
import com.braincraftapps.droid.common.extension.graphics.randomDarkMutedColor
import com.braincraftapps.droid.common.extension.view.context
import com.braincraftapps.droid.common.extension.view.hide
import com.braincraftapps.droid.common.extension.view.layoutInflater
import com.braincraftapps.droid.common.extension.view.show
import com.braincraftapps.droid.picker.provider.media.MediaFile
import com.braincraftapps.droid.picker.provider.media.params.file.VideoFile
import com.braincraftapps.droid.picker.ui.data.media.UiMediaFile
import com.braincraftapps.droid.picker.ui.viewholder.content.MediaContentViewHolder
import com.google.android.material.shape.MaterialShapeDrawable

class MediaItemViewHolder<M : MediaFile>(
    viewBinding: ViewHolderMediaItemBinding
) : MediaContentViewHolder<M, ViewHolderMediaItemBinding>(viewBinding), View.OnClickListener, View.OnLongClickListener {
    companion object {
        fun <M : MediaFile> newInstance(parent: ViewGroup): MediaItemViewHolder<M> {
            val viewBinding = ViewHolderMediaItemBinding.inflate(parent.layoutInflater, parent, false)
            return MediaItemViewHolder(viewBinding)
        }
    }

    private val fallbackColor: Int by lazy { randomDarkMutedColor }
    private val durationBackground: Drawable by lazy {
        return@lazy MaterialShapeDrawable().also {
            it.fillColor = Color.BLACK.withOpacity(0.4F).toColorStateList()
            it.setCornerSize(context.dpToPx<Float>(5))
            it.elevation = context.dpToPx(1)
        }
    }

    override fun onBindPlaceholder(position: Int) {
        super.onBindPlaceholder(position)
        prepareContainer()
    }

    override fun onBind(content: UiMediaFile.Content<M>) {
        prepareContainer()
        val mediaFile = content.media
        if (mediaFile is VideoFile) {
            viewBinding.durationTextView.show()
            viewBinding.durationTextView.background = durationBackground
            viewBinding.durationTextView.text = context.getReadableTime(mediaFile.durationInMs)
        } else {
            viewBinding.durationTextView.hide()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewBinding.root.setOnClickListener(this)
        viewBinding.root.setOnLongClickListener(this)
        viewBinding.deselectButton.setOnClickListener(this)
        viewBinding.imageView.load(contentOrNull?.media) {
            allowHardware(false)
            videoFramePercent(0.1)
            withCheckerboardBackground()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewBinding.imageView.dispose()
        viewBinding.root.clearAnimation()
        viewBinding.imageView.clearAnimation()
        viewBinding.root.setOnLongClickListener(null)
        viewBinding.root.setOnClickListener(null)
        viewBinding.deselectButton.setOnClickListener(null)
    }

    override fun onSelected(enabled: Boolean) {
        viewBinding.selectionBackground.show(enabled)
        viewBinding.deselectButton.show(enabled)
        viewBinding.selectionCountTextView.show(enabled)
    }

    override fun onSelectedItemPositionChanged(position: Int) {
        super.onSelectedItemPositionChanged(position)
        if (position < 0) {
            return
        }
        viewBinding.selectionCountTextView.text = position.plus(1).toString().padStart(2, '0')
    }

    override fun onSelectionEnabled(enabled: Boolean) {}

    private fun prepareContainer() {
        if (viewBinding.imageView.background == null) {
            viewBinding.imageView.setBackgroundColor(fallbackColor)
        }
    }

    override fun onClick(v: View) {
        if (!config.selection.enabled) {
            return
        }
        when (v.id) {
            viewBinding.root.id -> {
                if (!selected) {
                    publishMediaClick()
                }
            }

            viewBinding.deselectButton.id -> {
                if (selected) {
                    publishMediaClick()
                }
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        when (v.id) {
            viewBinding.root.id -> publishMediaLongClick()
        }
        return config.mediaPreview
    }
}
