/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: EditorFragment.kt
 * @modified: May 28, 2024, 11:30 AM
 */

package com.braincraftapps.cropvideos.fragment.editor

import androidx.viewbinding.ViewBinding
import com.braincraftapps.cropvideos.activity.editor.EditorActivity
import com.braincraftapps.cropvideos.activity.editor.EditorViewModel
import com.braincraftapps.cropvideos.activity.editor.data.EditorInput
import com.braincraftapps.droid.common.app.fragment.ViewFragment

abstract class EditorFragment<VB : ViewBinding> : ViewFragment<VB>() {

    override val activityOrNull: EditorActivity?
        get() = super.activityOrNull as? EditorActivity

    override val activityCompat: EditorActivity
        get() = activityOrNull ?: error("Fragment ${toString()} is not attached to EditorActivity")

    val editorInput: EditorInput
        get() = activityCompat.editorInput

    val editorViewModel: EditorViewModel
        get() = activityCompat.editorViewModel
}
