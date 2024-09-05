package com.braincraftapps.droid.llamaplayer

interface Player {

    companion object {
        const val STATE_PLAYING = 1
        const val STATE_PAUSE = 2
        const val STATE_ENDED = 4
    }

    fun setMediaSource(srcPath: String)

    fun prepare()

    fun play()

    fun pause()

    fun stop()

    fun release()

    fun seekTo(positionUs: Long)

    fun seekTo(mediaItemIndex: Int, positionUs: Long)

    interface Listener {

        fun onVideoSizeChanged(width: Int, height: Int)
    }

    interface OnPlaybackStateListener {
        fun onPlaybackStateChanged(state: Int)
    }

    interface OnFrameAboutToBeRenderedListener {
        fun onFrameAboutToBeRendered(index: Int, currentPresentationTimeUs: Long)
    }
}