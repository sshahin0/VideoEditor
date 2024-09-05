/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: ExportShareFragment.kt
 * @modified: Aug 14, 2024, 12:25 PM
 */

package com.braincraftapps.cropvideos.fragment.editor.export.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.underline
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.braincraftapps.cropvideos.databinding.FragmentExportShareBinding
import com.braincraftapps.cropvideos.fragment.editor.EditorFragment
import com.braincraftapps.cropvideos.fragment.editor.export.share.adapter.SocialMediaListAdapter
import com.braincraftapps.cropvideos.fragment.editor.export.share.data.SocialMediaType
import com.braincraftapps.droid.common.extension.core.makeToast
import com.braincraftapps.droid.common.extension.core.navigateUp
import com.braincraftapps.droid.common.extension.lang.capitalizeWords
import com.braincraftapps.droid.common.extension.view.doOnClick
import com.braincraftapps.droid.common.widget.recycler.adapter.listener.OnItemClickListener
import com.braincraftapps.droid.common.widget.recycler.animator.DefaultItemAnimatorCompat

class ExportShareFragment : EditorFragment<FragmentExportShareBinding>(), OnItemClickListener<SocialMediaType> {
    private var socialMediaListAdapter: SocialMediaListAdapter? = null

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentExportShareBinding {
        return FragmentExportShareBinding.inflate(inflater, container, false)
    }

    override fun onViewBindingCreated(viewBinding: FragmentExportShareBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        viewBinding.closeButton.doOnClick {
            navigateUp()
        }
        viewBinding.homeButton.doOnClick {
            activityCompat.finish()
        }
        viewBinding.descriptionTextView.text = buildSpannedString {
            append("Share video with your ")
            underline {
                append("#Friends!")
            }
        }
        val adapter = SocialMediaListAdapter().also {
            socialMediaListAdapter = it
        }
        adapter.addItemClickListener(this)
        viewBinding.recyclerView.itemAnimator = DefaultItemAnimatorCompat()
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        viewBinding.recyclerView.setHasFixedSize(true)
        viewBinding.recyclerView.adapter = adapter
        adapter.submitList(SocialMediaType.entries.toList())
    }

    override fun onDestroyViewBinding(viewBinding: FragmentExportShareBinding) {
        super.onDestroyViewBinding(viewBinding)
        viewBinding.recyclerView.adapter = null
        viewBinding.recyclerView.layoutManager = null
        socialMediaListAdapter?.removeItemClickListener(this)
        socialMediaListAdapter = null
    }

    override fun onItemClick(action: String, position: Int, item: SocialMediaType, extras: Bundle?) {
        makeToast("Shared via ${item.name.lowercase().capitalizeWords()}")
    }
}
