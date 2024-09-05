/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: LocalVideoListFragment.kt
 * @modified: Aug 21, 2024, 12:59 PM
 */

package com.braincraftapps.cropvideos.fragment.gallery.local.video

import androidx.paging.PagingSource
import com.braincraftapps.cropvideos.fragment.gallery.utils.adapter.local.LocalMediaListAdapter
import com.braincraftapps.droid.common.extension.core.dpToPx
import com.braincraftapps.droid.common.permission.data.Permission
import com.braincraftapps.droid.picker.provider.paging.data.PagingRequest
import com.braincraftapps.droid.picker.provider.vendor.local.LocalMediaProvider
import com.braincraftapps.droid.picker.provider.vendor.local.data.file.video.LocalVideo
import com.braincraftapps.droid.picker.provider.vendor.local.request.file.LocalMediaFileRequest
import com.braincraftapps.droid.picker.ui.adapter.file.MediaFileListAdapter
import com.braincraftapps.droid.picker.ui.data.config.MediaListConfig
import com.braincraftapps.droid.picker.ui.fragment.file.local.LocalMediaFileListFragment

class LocalVideoListFragment : LocalMediaFileListFragment<LocalVideo>() {

    private val localMediaProvider: LocalMediaProvider by lazy {
        return@lazy LocalMediaProvider.Builder(context).build()
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

    override fun onMediaListAdapter(): MediaFileListAdapter<LocalVideo> {
        return LocalMediaListAdapter()
    }

    override fun onMediaPagingSource(pagingRequest: PagingRequest?): PagingSource<Int, out LocalVideo>? {
        val request = pagingRequest as? LocalMediaFileRequest ?: return null
        return localMediaProvider.getVideoPagingSource(request)
    }

    override fun onRuntimePermissions(): List<Permission> = buildList {
        add(Permission.photos())
        add(Permission.videos())
    }
}
