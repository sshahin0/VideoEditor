/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: ExportTopSheetFragment.kt
 * @modified: Aug 14, 2024, 11:01 AM
 */

package com.braincraftapps.cropvideos.fragment.editor.export.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.braincraftapps.cropvideos.databinding.FragmentExportTopSheetBinding
import com.braincraftapps.cropvideos.fragment.editor.EditorFragment
import com.braincraftapps.cropvideos.fragment.editor.main.MainEditorFragment
import com.braincraftapps.cropvideos.fragment.editor.main.MainEditorFragmentDirections
import com.braincraftapps.droid.common.extension.core.findParentFragment
import com.braincraftapps.droid.common.extension.core.navigate
import com.braincraftapps.droid.common.extension.view.doOnClick

class ExportTopSheetFragment : EditorFragment<FragmentExportTopSheetBinding>() {
    private val mainEditorFragment: MainEditorFragment
        get() = findParentFragment() ?: error("No parent main editor found!")

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentExportTopSheetBinding {
        return FragmentExportTopSheetBinding.inflate(inflater, container, false)
    }

    override fun onViewBindingCreated(viewBinding: FragmentExportTopSheetBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        viewBinding.smartHdrCheckView.doOnClick {
            viewBinding.smartHdrIconButton.isChecked = !viewBinding.smartHdrIconButton.isChecked
        }
        viewBinding.exportButton.doOnClick {
            mainEditorFragment.showExportTopSheet(false)
            navigate(MainEditorFragmentDirections.toExportProgressFragment())
        }
    }
}
