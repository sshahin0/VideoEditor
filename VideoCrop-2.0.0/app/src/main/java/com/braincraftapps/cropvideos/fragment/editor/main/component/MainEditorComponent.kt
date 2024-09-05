/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: MainEditorComponent.kt
 * @modified: Aug 21, 2024, 03:48 PM
 */

package com.braincraftapps.cropvideos.fragment.editor.main.component

import android.content.Context
import android.os.Bundle
import androidx.core.graphics.Insets
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.braincraftapps.cropvideos.activity.editor.EditorActivity
import com.braincraftapps.cropvideos.activity.editor.EditorViewModel
import com.braincraftapps.cropvideos.activity.editor.data.EditorInput
import com.braincraftapps.cropvideos.databinding.FragmentMainEditorBinding
import com.braincraftapps.cropvideos.fragment.editor.main.MainEditorFragment
import com.braincraftapps.droid.common.app.LifecycleComponent
import com.braincraftapps.droid.common.app.SavedStateComponent
import com.braincraftapps.droid.llamaplayer.LlamaPlayer

abstract class MainEditorComponent(val fragment: MainEditorFragment) : SavedStateComponent, LifecycleComponent {
    override val savedStateHandle: SavedStateHandle
        get() = fragment.savedStateHandle

    override val fragmentManagerCompat: FragmentManager
        get() = fragment.fragmentManagerCompat

    override val lifecycleScopeCompat: LifecycleCoroutineScope
        get() = fragment.lifecycleScopeCompat

    override val lifecycleCompat: Lifecycle
        get() = fragment.viewLifecycleOwner.lifecycle

    val viewLifecycleOwner: LifecycleOwner
        get() = fragment.viewLifecycleOwner

    val context: Context
        get() = fragment.context

    val isViewBindingCreated: Boolean
        get() = fragment.isViewBindingCreated

    val viewBinding: FragmentMainEditorBinding
        get() = fragment.viewBinding

    val navController: NavController
        get() = fragment.findNavController()

    val systemBarInsetsLiveData: LiveData<Insets>
        get() = fragment.systemBarInsetsLiveData

    val activityOrNull: EditorActivity?
        get() = fragment.activityOrNull

    val activity: EditorActivity
        get() = fragment.activityCompat

    val editorInput: EditorInput
        get() = activity.editorInput

    val editorViewModel: EditorViewModel
        get() = activity.editorViewModel

    fun doOnViewBinding(action: FragmentMainEditorBinding.() -> Unit) {
        fragment.doOnViewBinding(action)
    }

    open suspend fun doOnPlayer(block: LlamaPlayer.() -> Unit) {
        fragment.doOnPlayer(block)
    }

    open fun onCreate(savedInstanceState: Bundle?) {}

    open fun onViewBindingCreated(viewBinding: FragmentMainEditorBinding, savedInstanceState: Bundle?) {}

    open fun onStart() {}

    open fun onResume() {}

    open fun onPause() {}

    open fun onStop() {}

    open fun onDestroyViewBinding(viewBinding: FragmentMainEditorBinding) {}

    open fun onDestroy() {}

    open fun onSystemBarInsets(viewBinding: FragmentMainEditorBinding, insets: Insets) {}

    open fun onKeyboardHeight(viewBinding: FragmentMainEditorBinding, height: Int) {}

    open fun onBackPressed(): Boolean = false
}
