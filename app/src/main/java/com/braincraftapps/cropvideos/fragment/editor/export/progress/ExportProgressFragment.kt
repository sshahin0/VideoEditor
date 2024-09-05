/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: ExportProgressFragment.kt
 * @modified: Aug 14, 2024, 11:09 AM
 */

package com.braincraftapps.cropvideos.fragment.editor.export.progress

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.braincraftapps.cropvideos.databinding.FragmentExportProgressBinding
import com.braincraftapps.cropvideos.fragment.editor.EditorFragment
import com.braincraftapps.droid.common.extension.core.addListenerCompat
import com.braincraftapps.droid.common.extension.core.makeToast
import com.braincraftapps.droid.common.extension.core.navigate
import com.braincraftapps.droid.common.extension.core.navigateUp
import com.braincraftapps.droid.common.extension.view.doOnClick

class ExportProgressFragment : EditorFragment<FragmentExportProgressBinding>() {

    private var progressAnimator: Animator? = null

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentExportProgressBinding {
        return FragmentExportProgressBinding.inflate(inflater, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewBindingCreated(viewBinding: FragmentExportProgressBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        viewBinding.closeButton.doOnClick {
            navigateUp()
        }
        val animator = ValueAnimator.ofInt(0, 100).also {
            progressAnimator = it
        }
        animator.duration = 5000
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener {
            val animatedValue = it.animatedValue as? Int ?: return@addUpdateListener
            viewBinding.progressIndicator.progress = animatedValue
            viewBinding.progressTextView.text = "${animatedValue}%"
        }
        animator.addListenerCompat {
            doOnEnd {
                makeToast("Export completed!")
                navigate(ExportProgressFragmentDirections.toExportShareFragment())
            }
        }
        animator.start()
    }

    override fun onPause() {
        super.onPause()
        progressAnimator?.pause()
    }

    override fun onResume() {
        super.onResume()
        progressAnimator?.resume()
    }

    override fun onDestroyViewBinding(viewBinding: FragmentExportProgressBinding) {
        super.onDestroyViewBinding(viewBinding)
        progressAnimator?.removeAllListeners()
        progressAnimator?.cancel()
        progressAnimator = null
    }
}
