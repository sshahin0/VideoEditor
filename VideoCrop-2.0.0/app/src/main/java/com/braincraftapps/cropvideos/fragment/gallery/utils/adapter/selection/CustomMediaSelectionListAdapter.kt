/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: CustomMediaSelectionListAdapter.kt
 * @modified: Aug 21, 2024, 12:36 PM
 */

package com.braincraftapps.cropvideos.fragment.gallery.utils.adapter.selection

import android.view.ViewGroup
import com.braincraftapps.cropvideos.fragment.gallery.utils.viewholder.MediaSelectionViewHolder
import com.braincraftapps.droid.picker.ui.adapter.selection.MediaSelectionListAdapter
import com.braincraftapps.droid.picker.ui.viewholder.selection.SelectedMediaViewHolder

class CustomMediaSelectionListAdapter: MediaSelectionListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedMediaViewHolder<*> {
        return MediaSelectionViewHolder.newInstance(parent)
    }
}
