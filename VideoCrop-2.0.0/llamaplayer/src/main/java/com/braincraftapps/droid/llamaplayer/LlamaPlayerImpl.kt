package com.braincraftapps.droid.llamaplayer

import android.util.Log

class LlamaPlayerImpl : LlamaPlayer, Player.Listener, Player.OnPlaybackStateListener, Player.OnFrameAboutToBeRenderedListener {

    companion object {
        // Used to load the 'llamaplayer' library on application startup.
        init {
            System.loadLibrary("llamaplayer")
        }

        private const val TAG = "LlamaPlayerImpl"
    }

    private var srcPath: String? = null

    private var frameAboutToBeRenderedListener: Player.OnFrameAboutToBeRenderedListener? = null
    private var playbackStateListener: Player.OnPlaybackStateListener? = null

    fun setPlaybackStateListener(playbackStateListener: Player.OnPlaybackStateListener?) {
        this.playbackStateListener = playbackStateListener
    }

    fun setFrameAboutToBeRenderedListener(frameAboutToBeRenderedListener: Player.OnFrameAboutToBeRenderedListener?) {
        this.frameAboutToBeRenderedListener = frameAboutToBeRenderedListener
    }

    override fun setMediaSource(srcPath: String) {
        this.srcPath = srcPath
    }

    override fun prepare() {
        this.srcPath?.let {
            prepareVideoFromJNI(it)
        }
    }

    override fun play() {
        playWhenReady(true, videoReaderId)
    }

    override fun pause() {
        playWhenReady(false, videoReaderId)
    }

    override fun stop() {
        nativeStop(videoReaderId)
    }

    override fun release() {
        releaseFromJNI()
    }

    override fun seekTo(positionUs: Long) {
        seekToFromJNI(0, positionUs)
    }

    override fun seekTo(mediaItemIndex: Int, positionUs: Long) {
        seekToFromJNI(mediaItemIndex, positionUs)
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        Log.e(TAG, "onVideoSizeChanged $width, $height")
    }

    override fun onPlaybackStateChanged(state: Int) {
        Log.e(TAG, "onPlaybackStateChanged $state")

        this.playbackStateListener?.onPlaybackStateChanged(state)
    }

    private val mSurfaceId: Long = 0

    private var videoReaderId: Long = 0

    private fun prepareVideoFromJNI(filePath: String): Int {
        Log.e("hello", "prepare video $filePath")
        videoReaderId = prepareVideo(filePath, mSurfaceId)
        return videoReaderId.toInt()
    }

    private fun seekToFromJNI(index: Int, ptsUs: Long) {
        seekTo_IJJJ(index, ptsUs, mSurfaceId, videoReaderId)
    }

    private fun releaseFromJNI() {
        release(videoReaderId)
    }

    fun onSurfaceCreated() {
        onSurfaceCreated(videoReaderId)
    }

    fun onSurfaceChanged(w: Int, h: Int) {
        onSurfaceChanged(videoReaderId, w, h)
    }

    fun onDrawFrame() {
        onDrawFrame(videoReaderId)
    }

    fun getTextureId(): Int {
        return getTextureId(videoReaderId)
    }

    private fun playerEventCallback(msgType: Int, msgValue: Int) {
        Log.e(TAG, "player event callback $msgType $msgValue")
    }

    override fun onFrameAboutToBeRendered(index: Int, currentPresentationTimeUs: Long) {
      //  Log.e(TAG, "frame about to be rendered $index, $currentPresentationTimeUs")

        this.frameAboutToBeRenderedListener?.let {
            it.onFrameAboutToBeRendered(index, currentPresentationTimeUs)
        }
    }

    private external fun prepareVideo(filePath: String, surfaceId: Long): Long

    private external fun playWhenReady(playWhenReader: Boolean, videoReaderId: Long)

    private external fun nativeStop(playerId: Long)

    private external fun seekTo_JJJ(ptsUs: Long, surfaceId: Long, videoReaderId: Long)
    private external fun seekTo_IJJJ(index: Int, ptsUs: Long, surfaceId: Long, videoReaderId: Long)

    private external fun onSurfaceCreated(videoReaderId: Long)
    private external fun onSurfaceChanged(videoReaderId: Long, w: Int, h: Int)
    private external fun onDrawFrame(videoReaderId: Long)

    private external fun release(surfaceId: Long)

    private external fun getTextureId(videoReaderId: Long): Int
}