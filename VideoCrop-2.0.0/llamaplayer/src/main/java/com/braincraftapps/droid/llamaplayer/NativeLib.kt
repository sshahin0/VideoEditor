package com.braincraftapps.droid.llamaplayer

import android.util.Log

class NativeLib {

    /**
     * A native method that is implemented by the 'llamaplayer' native library,
     * which is packaged with this application.
     */
    //private external fun stringFromJNI(): String

    companion object {
        fun getLlamaPlayer(): LlamaPlayer {
            return LlamaPlayerImpl()
        }
    }

    /*companion object {
        // Used to load the 'llamaplayer' library on application startup.
        init {
            System.loadLibrary("llamaplayer")
        }
    }

    private val mSurfaceId: Long = 0

    private var videoReaderId: Long = 0

    fun prepareVideoFromJNI(filePath: String): Int {
        Log.e("hello", "prepare video $filePath")
        videoReaderId = prepareVideo(filePath, mSurfaceId)
        return videoReaderId.toInt()
    }

    fun seekToFromJNI(ptsUs: Long): ByteArray? {
        return seekTo(ptsUs, mSurfaceId, videoReaderId)
    }

    fun releaseFromJNI() {
        release(videoReaderId)
    }

    fun onSurfaceCreated() {
        native_OnSurfaceCreated()
    }

    fun onSurfaceChanged(w: Int, h: Int) {
        native_OnSurfaceChanged(w, h)
    }

    fun onDrawFrame() {
        native_OnDrawFrame()
    }

    private fun playerEventCallback(msgType: Int, msgValue: Int) {
        Log.e("xyz", "player event callback $msgType $msgValue")
    }

    private external fun prepareVideo(filePath: String, surfaceId: Long): Long
    private external fun seekTo(ptsUs: Long, surfaceId: Long, videoReaderId: Long): ByteArray?

    private external fun native_OnSurfaceCreated()
    private external fun native_OnSurfaceChanged(w: Int, h: Int)
    private external fun native_OnDrawFrame()

    private external fun release(surfaceId: Long)*/
}