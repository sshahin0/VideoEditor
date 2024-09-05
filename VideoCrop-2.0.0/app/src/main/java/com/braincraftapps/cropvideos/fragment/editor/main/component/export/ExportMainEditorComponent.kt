/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: ExportMainEditorComponent.kt
 * @modified: Aug 13, 2024, 03:34 PM
 */

package com.braincraftapps.cropvideos.fragment.editor.main.component.export

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.PathInterpolator
import androidx.core.view.doOnLayout
import androidx.fragment.app.commit
import com.braincraftapps.cropvideos.databinding.FragmentMainEditorBinding
import com.braincraftapps.cropvideos.fragment.editor.export.sheet.ExportTopSheetFragment
import com.braincraftapps.cropvideos.fragment.editor.main.MainEditorFragment
import com.braincraftapps.cropvideos.fragment.editor.main.component.MainEditorComponent
import com.braincraftapps.droid.common.extension.core.addListenerCompat
import com.braincraftapps.droid.common.extension.core.savedStates
import com.braincraftapps.droid.common.extension.lang.scale
import com.braincraftapps.droid.common.extension.view.doOnClick
import com.braincraftapps.droid.common.extension.view.hide
import com.braincraftapps.droid.common.extension.view.show

class ExportMainEditorComponent(fragment: MainEditorFragment) : MainEditorComponent(fragment) {
    companion object {
        private const val SAVED_KEY_IS_EXPORT_SHEET_VISIBLE = "export_main_editor_component_saved_key_is_export_sheet_visible"
    }

    private var isExportSheetVisible: Boolean by savedStates(SAVED_KEY_IS_EXPORT_SHEET_VISIBLE) { false }
    private var exportTopSheetAnimator: ValueAnimator? = null

    override fun onViewBindingCreated(viewBinding: FragmentMainEditorBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        fragmentManagerCompat.commit {
            replace(viewBinding.exportTopSheetContainer.id, ExportTopSheetFragment())
        }
        if (isExportSheetVisible) {
            viewBinding.exportTopSheetScrimLayout.alpha = 1.0F
            viewBinding.exportTopSheetScrimLayout.show()
        } else {
            viewBinding.exportTopSheetScrimLayout.alpha = 0.0F
            viewBinding.exportTopSheetScrimLayout.hide()
        }
        viewBinding.exportTopSheetContainer.doOnLayout { view ->
            if (isExportSheetVisible) {
                view.translationY = 0.0F
                view.show()
            } else {
                view.translationY = -view.height.toFloat()
                view.hide(invisible = true)
            }
        }
        viewBinding.exportButton.doOnClick {
            showTopSheet(true)
        }
        viewBinding.exportTopSheetScrimLayout.doOnClick {
            showTopSheet(false)
        }
    }

    override fun onDestroyViewBinding(viewBinding: FragmentMainEditorBinding) {
        super.onDestroyViewBinding(viewBinding)
        exportTopSheetAnimator?.cancel()
        exportTopSheetAnimator = null
    }

    override fun onBackPressed(): Boolean {
        val isVisible = isExportSheetVisible
        if (isVisible) {
            showTopSheet(false)
        }
        return isVisible
    }

    fun showTopSheet(show: Boolean) {
        if (isExportSheetVisible != show) {
            isExportSheetVisible = show
            exportTopSheetAnimator?.cancel()
            exportTopSheetAnimator = null
            viewBinding.exportTopSheetContainer.doOnLayout { view ->
                if (!isViewBindingCreated) {
                    return@doOnLayout
                }
                val scrimLayout = viewBinding.exportTopSheetScrimLayout
                val alphaFrom = scrimLayout.alpha
                val alphaTo = if (show) 1.0F else 0.0F
                val valueFrom = view.translationY
                val valueTo = if (show) 0.0F else -view.height.toFloat()
                val animator = ValueAnimator.ofFloat(valueFrom, valueTo).also { exportTopSheetAnimator = it }
                animator.addUpdateListener {
                    val animatedValue = it.animatedValue as? Float ?: return@addUpdateListener
                    view.translationY = animatedValue
                    scrimLayout.alpha = animatedValue.scale(
                        valueFrom = valueFrom,
                        valueTo = valueTo,
                        scaleFrom = alphaFrom,
                        scaleTo = alphaTo
                    )
                }
                animator.addListenerCompat {
                    if (show) {
                        doOnStart {
                            scrimLayout.show()
                            view.show()
                        }
                    } else {
                        doOnEnd {
                            scrimLayout.hide()
                            view.hide(invisible = true)
                        }
                    }
                }
                animator.duration = 250
                animator.interpolator = PathInterpolator(0.0F, 0.0F, 0.2F, 1.0F)
                animator.start()
            }
        }
    }
}
