/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: PixabaySearchFragment.kt
 * @modified: Aug 20, 2024, 11:02 AM
 */

package com.braincraftapps.cropvideos.fragment.gallery.pixabay.search

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.doOnPreDraw
import com.braincraftapps.cropvideos.databinding.FragmentPixabaySearchBinding
import com.braincraftapps.droid.common.extension.core.asOpaque
import com.braincraftapps.droid.common.extension.core.doOnBackPressed
import com.braincraftapps.droid.common.extension.core.isDarkColor
import com.braincraftapps.droid.common.extension.core.withOpacity
import com.braincraftapps.droid.common.extension.lang.takeIfNotBlank
import com.braincraftapps.droid.common.extension.lang.toStringOr
import com.braincraftapps.droid.common.extension.lifecycle.observeCompat
import com.braincraftapps.droid.common.extension.view.doOnClick
import com.braincraftapps.droid.common.extension.view.showKeyboard
import com.braincraftapps.droid.common.widget.text.utils.SimpleTextWatcher
import com.braincraftapps.droid.picker.ui.data.theme.MediaTheme
import com.braincraftapps.droid.picker.ui.fragment.collection.data.CollectionState
import com.braincraftapps.droid.picker.ui.fragment.search.SearchMediaFragment

class PixabaySearchFragment : SearchMediaFragment<FragmentPixabaySearchBinding>(), SimpleTextWatcher {
    companion object {
        private const val SAVED_KEY_EDIT_TEXT_SELECTION_END = "simple_search_media_fragment_saved_key_edit_text_selection_end"
    }

    private var editTextSelectionEnd: Int
        get() = savedStateHandle[SAVED_KEY_EDIT_TEXT_SELECTION_END] ?: -1
        set(value) {
            savedStateHandle[SAVED_KEY_EDIT_TEXT_SELECTION_END] = value
        }

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPixabaySearchBinding {
        return FragmentPixabaySearchBinding.inflate(inflater, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewBindingCreated(viewBinding: FragmentPixabaySearchBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        viewBinding.root.setBackgroundColor(mediaTheme.surfaceColor)
        viewBinding.searchEditText.setHintTextColor(mediaTheme.onSurfaceColor.withOpacity(MediaTheme.OPACITY_LOW))
        viewBinding.searchEditText.setTextColor(mediaTheme.onSurfaceColor.withOpacity(MediaTheme.OPACITY_HIGH))
        viewBinding.cancelButton.setTextColor(mediaTheme.onSurfaceColor.withOpacity(MediaTheme.OPACITY_HIGH))
        doOnBackPressed {
            if (isSearchActivated) {
                cancelSearch()
                return@doOnBackPressed true
            }
            return@doOnBackPressed false
        }
        viewBinding.searchEditText.addTextChangedListener(this)
        viewBinding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewBinding.searchEditText.showKeyboard(false)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        viewBinding.searchEditText.setOnTouchListener { _, _ ->
            enableSearch()
            return@setOnTouchListener false
        }
        isSearchActivatedLiveData.observe(viewLifecycleOwner) { isActivated ->
            if (isActivated) {
                viewBinding.searchMotionLayout.transitionToEnd()
            } else {
                viewBinding.searchMotionLayout.transitionToStart()
            }
            if (!isActivated) {
                viewBinding.searchEditText.text = null
            }
            viewBinding.searchEditText.showKeyboard(isActivated)
        }
        val collectionId = parentCollectionFragment.collectionId
        parentCollectionFragment.getCollectionStateLiveData(collectionId).observeCompat(viewLifecycleOwner) {
            if (it is CollectionState.Visible) {
                if (viewBinding.searchEditText.text?.toString()?.trim()?.isEmpty() == true) {
                    cancelSearch()
                } else {
                    viewBinding.searchEditText.showKeyboard(false)
                }
            }
        }
        viewBinding.cancelButton.doOnClick { cancelSearch() }
    }

    override fun onResume() {
        super.onResume()
        viewBinding.searchEditText.setText(searchQuery.takeIfNotBlank())
        viewBinding.searchEditText.doOnPreDraw {
            val position = editTextSelectionEnd.coerceIn(0, viewBinding.searchEditText.length())
            viewBinding.searchEditText.setSelection(position)
        }
        viewBinding.searchEditText.invalidate()
    }

    override fun onPause() {
        super.onPause()
        editTextSelectionEnd = viewBinding.searchEditText.selectionEnd
    }

    override fun onDestroyViewBinding(viewBinding: FragmentPixabaySearchBinding) {
        super.onDestroyViewBinding(viewBinding)
        viewBinding.searchEditText.removeTextChangedListener(this)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        performSearch(s?.takeIfNotBlank().toStringOr(""), null)
    }

    override fun onHideKeyboard() {
        viewBinding.searchEditText.showKeyboard(false)
    }

    private fun getTextInputBackgroundColor(): Int {
        val isDark = mediaTheme.surfaceColor.isDarkColor()
        val surfaceColor = if (isDark) {
            Color.WHITE
        } else Color.BLACK
        val opacity = if (isDark) 0.9F else 0.95F
        return mediaTheme.surfaceColor.withOpacity(opacity).asOpaque(surfaceColor)
    }
}
