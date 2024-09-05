/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: PixabayVideoListFragment.kt
 * @modified: Aug 21, 2024, 12:59 PM
 */

package com.braincraftapps.cropvideos.fragment.gallery.pixabay.file

import androidx.paging.PagingSource
import com.braincraftapps.cropvideos.fragment.gallery.utils.adapter.pixabay.PixabayVideoListAdapter
import com.braincraftapps.droid.common.extension.core.dpToPx
import com.braincraftapps.droid.picker.provider.paging.data.PagingRequest
import com.braincraftapps.droid.picker.provider.vendor.pixabay.PixabayProvider
import com.braincraftapps.droid.picker.provider.vendor.pixabay.data.PixabayVideo
import com.braincraftapps.droid.picker.provider.vendor.pixabay.request.video.PixabayVideoRequest
import com.braincraftapps.droid.picker.ui.adapter.file.MediaFileListAdapter
import com.braincraftapps.droid.picker.ui.data.config.MediaListConfig
import com.braincraftapps.droid.picker.ui.fragment.file.remote.pixabay.video.PixabayVideoListFragment

class PixabayVideoListFragment : PixabayVideoListFragment() {

    private val pixabayProvider: PixabayProvider by lazy {
        return@lazy PixabayProvider.Builder(context).build()
    }

    override fun onMediaListConfig(): MediaListConfig {
        return super.onMediaListConfig().buildUpon()
            .enableGridLayout {
                setSpanCount(4)
                setItemMargin(context.dpToPx(2))
            }
            .enableMultiSelection()
            .enableMediaPreview(true)
            .build()
    }

    override fun onMediaListAdapter(): MediaFileListAdapter<PixabayVideo> {
        return PixabayVideoListAdapter()
    }

    override fun onMediaPagingSource(pagingRequest: PagingRequest?): PagingSource<Int, out PixabayVideo>? {
        val request = pagingRequest as? PixabayVideoRequest ?: return null
        return pixabayProvider.getVideoPagingSource(request)
    }
}
