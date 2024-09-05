/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: LocalMediaListAdapter.kt
 * @modified: Aug 20, 2024, 09:34 AM
 */

package com.braincraftapps.cropvideos.fragment.gallery.utils.adapter.local

import android.view.ViewGroup
import com.braincraftapps.cropvideos.fragment.gallery.utils.viewholder.MediaItemViewHolder
import com.braincraftapps.droid.picker.provider.vendor.local.data.file.LocalMediaFile
import com.braincraftapps.droid.picker.ui.adapter.file.local.LocalMediaFileListAdapter
import com.braincraftapps.droid.picker.ui.viewholder.content.MediaContentViewHolder

class LocalMediaListAdapter<M : LocalMediaFile> : LocalMediaFileListAdapter<M>("local_media_list_adapter") {

    override fun onCreateGridViewHolder(parent: ViewGroup, viewType: Int): MediaContentViewHolder<M, *> {
        return MediaItemViewHolder.newInstance(parent)
    }
}
