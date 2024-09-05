/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: MainEditorFragment.kt
 * @modified: Aug 21, 2024, 04:05 PM
 */

package com.braincraftapps.cropvideos.fragment.editor.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.Insets
import com.braincraftapps.cropvideos.databinding.FragmentMainEditorBinding
import com.braincraftapps.cropvideos.fragment.editor.EditorFragment
import com.braincraftapps.cropvideos.fragment.editor.main.component.MainEditorComponent
import com.braincraftapps.cropvideos.fragment.editor.main.component.export.ExportMainEditorComponent
import com.braincraftapps.cropvideos.fragment.editor.main.component.player.PlayerMainEditorComponent
import com.braincraftapps.cropvideos.fragment.editor.main.component.timeline.TimelineMainEditorComponent
import com.braincraftapps.droid.common.extension.core.doOnBackPressed
import com.braincraftapps.droid.common.extension.view.doOnClick
import com.braincraftapps.droid.llamaplayer.LlamaPlayer

class MainEditorFragment : EditorFragment<FragmentMainEditorBinding>() {
    private val exportMainEditorComponent: ExportMainEditorComponent by lazy { ExportMainEditorComponent(this) }
    private val playerMainEditorComponent: PlayerMainEditorComponent by lazy { PlayerMainEditorComponent(this) }
    private val timelineMainEditorComponent: TimelineMainEditorComponent by lazy { TimelineMainEditorComponent(this) }

    private val components: List<MainEditorComponent> by lazy {
        return@lazy listOf(
            exportMainEditorComponent,
            playerMainEditorComponent,
            timelineMainEditorComponent
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        components.forEach { it.onCreate(savedInstanceState) }
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMainEditorBinding {
        return FragmentMainEditorBinding.inflate(inflater, container, false)
    }

    override fun onViewBindingCreated(viewBinding: FragmentMainEditorBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        components.forEach { it.onViewBindingCreated(viewBinding, savedInstanceState) }
        doOnBackPressed {
            components.forEach {
                if (it.onBackPressed()) {
                    return@doOnBackPressed true
                }
            }
            activityCompat.finish()
            return@doOnBackPressed true
        }
        viewBinding.closeButton.doOnClick {
            activityCompat.finish()
        }
    }

    override fun onStart() {
        super.onStart()
        components.forEach { it.onStart() }
    }

    override fun onResume() {
        super.onResume()
        components.forEach { it.onResume() }
    }

    override fun onPause() {
        super.onPause()
        components.forEach { it.onPause() }
    }

    override fun onStop() {
        super.onStop()
        components.forEach { it.onStop() }
    }

    override fun onDestroyViewBinding(viewBinding: FragmentMainEditorBinding) {
        super.onDestroyViewBinding(viewBinding)
        components.forEach { it.onDestroyViewBinding(viewBinding) }
    }

    override fun onDestroy() {
        super.onDestroy()
        components.forEach { it.onDestroy() }
    }

    override fun onSystemBarInsets(viewBinding: FragmentMainEditorBinding, insets: Insets) {
        super.onSystemBarInsets(viewBinding, insets)
        components.forEach { it.onSystemBarInsets(viewBinding, insets) }
    }

    override fun onKeyboardHeight(viewBinding: FragmentMainEditorBinding, height: Int) {
        super.onKeyboardHeight(viewBinding, height)
        components.forEach { it.onKeyboardHeight(viewBinding, height) }
    }

    fun showExportTopSheet(show: Boolean) {
        exportMainEditorComponent.showTopSheet(show)
    }

    suspend fun doOnPlayer(block: LlamaPlayer.() -> Unit) {
        playerMainEditorComponent.doOnPlayer(block)
    }
}
