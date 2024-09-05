/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: MediaMetadataRetriever.kt
 * @modified: Aug 21, 2024, 04:43 PM
 */

package com.braincraftapps.droid.llamaplayer

import android.graphics.Bitmap
import com.braincraftapps.droid.common.extension.graphics.scaleCompat
import com.braincraftapps.droid.common.extension.log.logWarning
import com.braincraftapps.droid.common.utils.bitmap.ScaleType
import java.io.File

class MediaMetadataRetriever {

    // 95089388
    // 74718881
    companion object {
        init {
            System.loadLibrary("llamaplayer")
        }
    }

    private var absoluteFile: File? = null

    fun setDataSource(file: File) {
        if (file.absoluteFile == absoluteFile) {
            return
        }
        _create()
        absoluteFile = file.absoluteFile
        _open(file.absolutePath)
    }

    fun getFrameAt(positionInUs: Long, width: Int, height: Int): Bitmap? {
        if (absoluteFile == null) {
            logWarning(
                message = "Media metadata retriever is not prepared yet. Call setDataSource",
                tag = "metadata_retriever"
            )
            return null
        }
        return _seek(positionInUs, width, height)?.scaleCompat(width, height, scaleType = ScaleType.CENTER_CROP)
    }

    fun release() {
        _close()
        _destroy()
        absoluteFile = null
    }

    private external fun _create()
    private external fun _open(path: String)
    private external fun _seek(timestamp: Long, width: Int, height: Int): Bitmap?
    private external fun _close()
    private external fun _destroy()
}
