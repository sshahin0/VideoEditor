/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: PixabayVideoTabFragment.kt
 * @modified: Aug 20, 2024, 11:01 AM
 */

package com.braincraftapps.cropvideos.fragment.gallery.pixabay.collection

import android.os.Bundle
import androidx.paging.PagingSource
import com.braincraftapps.cropvideos.fragment.gallery.pixabay.file.PixabayVideoListFragment
import com.braincraftapps.cropvideos.fragment.gallery.pixabay.search.PixabaySearchFragment
import com.braincraftapps.cropvideos.fragment.gallery.utils.adapter.pixabay.PixabayTabListAdapter
import com.braincraftapps.droid.common.extension.core.dpToPx
import com.braincraftapps.droid.common.extension.view.setPaddingCompat
import com.braincraftapps.droid.common.widget.recycler.decoration.ItemMarginDecoration
import com.braincraftapps.droid.picker.provider.paging.data.PagingRequest
import com.braincraftapps.droid.picker.provider.vendor.pixabay.PixabayProvider
import com.braincraftapps.droid.picker.provider.vendor.pixabay.data.PixabayCategory
import com.braincraftapps.droid.picker.provider.vendor.pixabay.data.PixabayVideo
import com.braincraftapps.droid.picker.provider.vendor.pixabay.request.video.PixabayVideoRequest
import com.braincraftapps.droid.picker.ui.adapter.collection.MediaCollectionListAdapter
import com.braincraftapps.droid.picker.ui.databinding.FragmentMediaCollectionTabBinding
import com.braincraftapps.droid.picker.ui.fragment.collection.data.MediaQuery
import com.braincraftapps.droid.picker.ui.fragment.collection.tab.remote.pixabay.PixabayCategoryTabFragment
import com.braincraftapps.droid.picker.ui.fragment.file.MediaFileListFragment
import com.braincraftapps.droid.picker.ui.fragment.search.SearchMediaFragment

class PixabayVideoTabFragment : PixabayCategoryTabFragment<PixabayVideo>() {
    companion object {
        fun newInstance(): PixabayVideoTabFragment {
            return PixabayVideoTabFragment()
        }
    }

    private val provider: PixabayProvider by lazy {
        return@lazy PixabayProvider.Builder(context).build()
    }

    override fun onViewBindingCreated(viewBinding: FragmentMediaCollectionTabBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        viewBinding.root.setPaddingCompat(top = context.dpToPx(10))
        viewBinding.tabRecyclerView.setPaddingCompat(start = context.dpToPx(8), end = context.dpToPx(8))
        viewBinding.tabRecyclerView.clipToPadding = false
        viewBinding.tabRecyclerView.isHorizontalFadingEdgeEnabled = false
        viewBinding.tabRecyclerView.addItemDecoration(ItemMarginDecoration(context.dpToPx(8)))
    }

    override fun onCreatePagingMediaListRequest(mediaQuery: MediaQuery<PixabayCategory>): PagingRequest {
        val builder = PixabayVideoRequest.Builder()
        when (mediaQuery) {
            is MediaQuery.Collection -> builder.setCategory(mediaQuery.collection)
            is MediaQuery.SearchQuery -> builder.setSearchQuery(mediaQuery.query)
        }
        return builder.build()
    }

    override fun onCreateMediaListFragment(mediaQuery: MediaQuery<PixabayCategory>, pagingRequest: PagingRequest): MediaFileListFragment<out PixabayVideo> {
        return PixabayVideoListFragment()
    }

    override fun onMediaCollectionPagingSource(): PagingSource<Int, out PixabayCategory> {
        return provider.getCategoryPagingSource()
    }

    override fun onMediaCollectionListAdapter(): MediaCollectionListAdapter<PixabayCategory> {
        return PixabayTabListAdapter()
    }

    override fun onCreateMediaSearchFragment(): SearchMediaFragment<*> {
        return PixabaySearchFragment()
    }
}
