/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: EditorActivity.kt
 * @modified: Aug 21, 2024, 04:08 PM
 */

package com.braincraftapps.cropvideos.activity.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.core.graphics.Insets
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.viewModelFactory
import com.braincraftapps.cropvideos.activity.editor.data.EditorInput
import com.braincraftapps.cropvideos.activity.editor.data.EditorOutput
import com.braincraftapps.cropvideos.databinding.ActivityEditorBinding
import com.braincraftapps.droid.common.app.activity.NavigationActivity
import com.braincraftapps.droid.common.extension.core.getParcelableCompat
import com.braincraftapps.droid.common.extension.core.withOpacity
import com.braincraftapps.droid.common.extension.lang.parseColor
import com.braincraftapps.droid.common.extension.view.setPaddingCompat

class EditorActivity : NavigationActivity<ActivityEditorBinding>() {
    companion object {
        private const val EXTRA_EDITOR_INPUT = "editor_activity_extra_editor_input"
    }

    val editorInput: EditorInput by lazy {
        val intentInput: EditorInput? = intent?.extras?.getParcelableCompat(EXTRA_EDITOR_INPUT)
        if (intentInput != null) {
            return@lazy intentInput
        }
        error("Not Implemented! Parse editor input from shared media file from 3rd party apps")
    }

    val editorViewModel: EditorViewModel by viewModels {
        viewModelFactory {
            addInitializer(EditorViewModel::class) {
                EditorViewModel(createSavedStateHandle(), editorInput)
            }
        }
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): ActivityEditorBinding {
        return ActivityEditorBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark("#181A1E".parseColor().withOpacity(0.3F)),
            navigationBarStyle = SystemBarStyle.dark("#0E0E0E".parseColor().withOpacity(0.3F))
        )
    }

    override fun onCreateFragmentContainerView(viewBinding: ActivityEditorBinding): FragmentContainerView {
        return viewBinding.navHostFragment
    }

    override fun onSystemBarInsets(viewBinding: ActivityEditorBinding, insets: Insets) {
        super.onSystemBarInsets(viewBinding, insets)
        viewBinding.root.setPaddingCompat(insets)
    }

    override fun finish() {
        super.finish()
        setResult(RESULT_OK)
    }

    class Contract : ActivityResultContract<EditorInput, EditorOutput>() {
        override fun createIntent(context: Context, input: EditorInput): Intent {
            val intent = Intent(context, EditorActivity::class.java)
            intent.putExtra(EXTRA_EDITOR_INPUT, input)
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): EditorOutput {
            return EditorOutput(resultCode == Activity.RESULT_OK)
        }
    }
}
