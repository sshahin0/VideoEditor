/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: PixabayTabListAdapter.kt
 * @modified: Aug 20, 2024, 09:34 AM
 */

package com.braincraftapps.cropvideos.fragment.gallery.utils.adapter.pixabay

import android.view.ViewGroup
import com.braincraftapps.cropvideos.fragment.gallery.utils.viewholder.TabItemViewHolder
import com.braincraftapps.droid.common.widget.recycler.viewholder.ViewHolderCompat
import com.braincraftapps.droid.picker.provider.vendor.pixabay.data.PixabayCategory
import com.braincraftapps.droid.picker.ui.adapter.collection.MediaCollectionListAdapter

class PixabayTabListAdapter : MediaCollectionListAdapter<PixabayCategory>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCompat<PixabayCategory, *> {
        return TabItemViewHolder.newInstance(parent)
    }
}
