/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: SocialMediaListAdapter.kt
 * @modified: Aug 14, 2024, 09:29 AM
 */

package com.braincraftapps.cropvideos.fragment.editor.export.share.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.braincraftapps.cropvideos.databinding.ViewHolderShareSocialMediaBinding
import com.braincraftapps.cropvideos.fragment.editor.export.share.data.SocialMediaType
import com.braincraftapps.droid.common.extension.view.context
import com.braincraftapps.droid.common.extension.view.layoutInflater
import com.braincraftapps.droid.common.widget.recycler.adapter.ListAdapterCompat
import com.braincraftapps.droid.common.widget.recycler.viewholder.ViewHolderCompat

class SocialMediaListAdapter : ListAdapterCompat<SocialMediaType>(DiffItemCallback()) {
    companion object {
        const val ACTION_SOCIAL_MEDIA_ITEM = "social_media_list_adapter_action_social_media_item"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCompat<SocialMediaType, *> {
        return SocialItemViewHolder.newInstance(parent)
    }

    private class DiffItemCallback : DiffUtil.ItemCallback<SocialMediaType>() {
        override fun areItemsTheSame(oldItem: SocialMediaType, newItem: SocialMediaType): Boolean {
            return oldItem.ordinal == newItem.ordinal
        }

        override fun areContentsTheSame(oldItem: SocialMediaType, newItem: SocialMediaType): Boolean {
            return oldItem == newItem
        }
    }

    private class SocialItemViewHolder(
        viewBinding: ViewHolderShareSocialMediaBinding
    ) : ViewHolderCompat<SocialMediaType, ViewHolderShareSocialMediaBinding>(viewBinding), View.OnClickListener {
        companion object {
            fun newInstance(parent: ViewGroup): SocialItemViewHolder {
                val viewBinding = ViewHolderShareSocialMediaBinding.inflate(parent.layoutInflater, parent, false)
                return SocialItemViewHolder(viewBinding)
            }
        }

        override fun onBind(item: SocialMediaType) {
            viewBinding.iconImageView.load(item.iconRes)
            viewBinding.nameTextView.text = context.getString(item.titleRes)
        }

        override fun onPostBind(item: SocialMediaType) {
            super.onPostBind(item)
            viewBinding.root.setOnClickListener(this)
        }

        override fun onRecycled() {
            super.onRecycled()
            viewBinding.root.setOnClickListener(null)
        }

        override fun onClick(v: View) {
            if (v.id == viewBinding.root.id) {
                publishClick(ACTION_SOCIAL_MEDIA_ITEM)
            }
        }
    }
}
