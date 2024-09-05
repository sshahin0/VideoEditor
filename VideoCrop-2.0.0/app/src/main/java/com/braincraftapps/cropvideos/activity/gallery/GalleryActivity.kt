/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: GalleryActivity.kt
 * @modified: May 19, 2024, 02:16 PM
 */

package com.braincraftapps.cropvideos.activity.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.fragment.app.FragmentContainerView
import com.braincraftapps.common.extensions.core.surfaceColor
import com.braincraftapps.cropvideos.databinding.ActivityGalleryBinding
import com.braincraftapps.droid.common.app.activity.NavigationActivity
import com.braincraftapps.droid.common.extension.core.withOpacity
import com.braincraftapps.droid.common.extension.view.setPaddingCompat

class GalleryActivity : NavigationActivity<ActivityGalleryBinding>() {
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): ActivityGalleryBinding {
        return ActivityGalleryBinding.inflate(inflater, container, false)
    }

    override fun onViewBindingCreated(viewBinding: ActivityGalleryBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(surfaceColor.withOpacity(0.3F)),
            navigationBarStyle = SystemBarStyle.dark(surfaceColor.withOpacity(0.3F))
        )
    }

    override fun onCreateFragmentContainerView(viewBinding: ActivityGalleryBinding): FragmentContainerView {
        return viewBinding.navHostFragment
    }

    override fun onSystemBarInsets(viewBinding: ActivityGalleryBinding, insets: Insets) {
        super.onSystemBarInsets(viewBinding, insets)
        viewBinding.root.setPaddingCompat(insets)
    }
}
