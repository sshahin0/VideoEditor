/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: PlayerMainEditorComponent.kt
 * @modified: Aug 21, 2024, 04:40 PM
 */

package com.braincraftapps.cropvideos.fragment.editor.main.component.player

import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import com.braincraftapps.cropvideos.databinding.FragmentMainEditorBinding
import com.braincraftapps.cropvideos.fragment.editor.main.MainEditorFragment
import com.braincraftapps.cropvideos.fragment.editor.main.component.MainEditorComponent
import com.braincraftapps.droid.common.extension.core.doOnStart
import com.braincraftapps.droid.common.extension.lang.withMainContext
import com.braincraftapps.droid.common.extension.lifecycle.doOnLifecycleScope
import com.braincraftapps.droid.common.extension.view.doOnClick
import com.braincraftapps.droid.llamaplayer.LlamaPlayer
import com.braincraftapps.droid.llamaplayer.LlamaPlayerImpl
import com.braincraftapps.droid.llamaplayer.NativeLib
import com.braincraftapps.droid.llamaplayer.Player
import com.braincraftapps.droid.timeline.widget.TimelineView
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import java.io.IOException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PlayerMainEditorComponent(fragment: MainEditorFragment) : MainEditorComponent(fragment), TimelineView.OnChangeListener {
    private val llamaPlayer: LlamaPlayer by lazy { NativeLib.getLlamaPlayer() }
    private val renderer: MyRenderer by lazy { MyRenderer() }

    private var transformFilter: AspectFitCanvasFilter? = null
    private var seekJob: Job? = null
    private var paused = true

    private val data: Pair<Int, Int> by lazy {
        getWidthAndHeightOfVideo(editorInput.dataSet.first().absoluteFile.path)
    }


    override fun onViewBindingCreated(viewBinding: FragmentMainEditorBinding, savedInstanceState: Bundle?) {
        super.onViewBindingCreated(viewBinding, savedInstanceState)
        viewBinding.timelineView.addChangeListener(this)
        viewBinding.surfaceView.setEGLContextClientVersion(2)
        viewBinding.surfaceView.setRenderer(renderer)

        (llamaPlayer as LlamaPlayerImpl).setFrameAboutToBeRenderedListener(renderer)
        (llamaPlayer as LlamaPlayerImpl).setPlaybackStateListener(renderer)

        llamaPlayer.setMediaSource(editorInput.dataSet.first().absoluteFile.path)

        llamaPlayer.prepare()

        viewBinding.playButton.doOnClick {
            it.isActivated = !it.isActivated
            paused = !it.isActivated

            if (it.isActivated) {
                llamaPlayer.play()
            } else llamaPlayer.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        paused = true
        llamaPlayer.pause()
    }

    override fun onDestroyViewBinding(viewBinding: FragmentMainEditorBinding) {
        super.onDestroyViewBinding(viewBinding)
        viewBinding.timelineView.removeChangeListener(this)
        (llamaPlayer as LlamaPlayerImpl).setPlaybackStateListener(null)
        seekJob?.cancel()
        seekJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        paused = true
        llamaPlayer.pause()
        llamaPlayer.stop()
        llamaPlayer.release()
    }

    override suspend fun doOnPlayer(block: LlamaPlayer.() -> Unit): Unit = withMainContext {
        block(llamaPlayer)
    }

    override fun onTimelineSeekStart() {
        super.onTimelineSeekStart()
        paused = true
        llamaPlayer.pause()
        viewBinding.playButton.isActivated = false
    }

    override fun onTimelineProgressChange(positionInMs: Long, durationInMs: Long, fromUser: Boolean) {
        if (fromUser) {
            llamaPlayer.seekTo(positionInMs.times(1000))
        }
    }

    private fun getWidthAndHeightOfVideo(filePath: String?): Pair<Int, Int> {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)

        val metaRotation = retriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION)
        val rotation = metaRotation?.toInt() ?: 0

        val widthString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val heightString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)

        var videoHeight = 0
        var videoWidth = 0
        if (rotation == 90 || rotation == 270) {
            videoWidth = heightString!!.toInt()
            videoHeight = widthString!!.toInt()
        } else {
            videoWidth = widthString!!.toInt()
            videoHeight = heightString!!.toInt()
        }

        try {
            retriever.release()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return Pair(videoWidth, videoHeight)
    }

    private inner class MyRenderer : GLSurfaceView.Renderer, Player.OnFrameAboutToBeRenderedListener, Player.OnPlaybackStateListener {

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

            (llamaPlayer as LlamaPlayerImpl).onSurfaceCreated()
            transformFilter = AspectFitCanvasFilter()
            transformFilter?.setup()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            (llamaPlayer as LlamaPlayerImpl).onSurfaceChanged(width, height)

            transformFilter?.setFrameSize(width, height)
        }

        override fun onDrawFrame(gl: GL10?) {

            (llamaPlayer as LlamaPlayerImpl).onDrawFrame()

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            transformFilter?.setInputRatio(data.first.toFloat() / data.second, 0)

            transformFilter?.draw((llamaPlayer as LlamaPlayerImpl).getTextureId(), null)
        }

        override fun onFrameAboutToBeRendered(index: Int, currentPresentationTimeUs: Long) {
            val previous = seekJob
            val paused = paused
            //Log.e("xyz", "current presentation time $currentPresentationTimeUs")
            seekJob = doOnLifecycleScope {
                previous?.join()
                lifecycleCompat.doOnStart {
                    if (!paused && isActive) {
                        viewBinding.timelineView.setProgress(currentPresentationTimeUs.toDouble().div(1000.0).toLong().coerceAtLeast(0))
                    }
                }
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                doOnLifecycleScope {
                    viewBinding.playButton.isActivated = false
                    llamaPlayer.pause()
                }
            }

            paused = !viewBinding.playButton.isActivated
        }
    }

}
