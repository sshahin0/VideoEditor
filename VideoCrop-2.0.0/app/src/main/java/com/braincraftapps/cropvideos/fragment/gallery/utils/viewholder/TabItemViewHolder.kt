/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: TabItemViewHolder.kt
 * @modified: Aug 20, 2024, 11:00 AM
 */

package com.braincraftapps.cropvideos.fragment.gallery.utils.viewholder

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import com.braincraftapps.common.extensions.core.dividerColor
import com.braincraftapps.cropvideos.databinding.ViewHolderMediaTabBinding
import com.braincraftapps.droid.common.extension.core.buildColorStateList
import com.braincraftapps.droid.common.extension.core.dpToPx
import com.braincraftapps.droid.common.extension.core.toColorStateList
import com.braincraftapps.droid.common.extension.lang.parseColor
import com.braincraftapps.droid.common.extension.view.context
import com.braincraftapps.droid.common.extension.view.layoutInflater
import com.braincraftapps.droid.picker.provider.vendor.pixabay.data.PixabayCategory
import com.braincraftapps.droid.picker.ui.viewholder.collection.MediaCollectionViewHolder
import com.google.android.material.shape.MaterialShapeDrawable

class TabItemViewHolder(
    viewBinding: ViewHolderMediaTabBinding
) : MediaCollectionViewHolder<PixabayCategory, ViewHolderMediaTabBinding>(viewBinding), View.OnClickListener {
    companion object {
        fun newInstance(parent: ViewGroup): TabItemViewHolder {
            val viewBinding = ViewHolderMediaTabBinding.inflate(parent.layoutInflater, parent, false)
            return TabItemViewHolder(viewBinding)
        }
    }

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

    override fun onBind(item: PixabayCategory) {
        viewBinding.tabTextView.background = tabBackground
        viewBinding.tabTextView.setTextColor(tabTextColor)
        viewBinding.tabTextView.text = item.title
    }

    override fun onPostBind(item: PixabayCategory) {
        super.onPostBind(item)
        viewBinding.root.setOnClickListener(this)
    }

    override fun onRecycled() {
        super.onRecycled()
        viewBinding.root.setOnClickListener(null)
    }

    override fun onActivated(activated: Boolean) {
        super.onActivated(activated)
        viewBinding.tabTextView.isActivated = activated
    }

    override fun onClick(v: View) {
        val collection = itemOrNull ?: return
        when (v.id) {
            viewBinding.root.id -> publishSelected(collection)
        }
    }
}
