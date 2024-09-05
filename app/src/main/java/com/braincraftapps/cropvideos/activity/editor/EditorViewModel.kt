/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: EditorViewModel.kt
 * @modified: May 28, 2024, 11:30 AM
 */

package com.braincraftapps.cropvideos.activity.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.braincraftapps.cropvideos.activity.editor.data.EditorInput

class EditorViewModel(
    val savedStateHandle: SavedStateHandle,
    val editorInput: EditorInput
) : ViewModel() {

}
