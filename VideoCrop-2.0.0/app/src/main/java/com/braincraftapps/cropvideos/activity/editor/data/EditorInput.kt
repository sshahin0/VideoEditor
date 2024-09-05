/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: EditorInput.kt
 * @modified: May 27, 2024, 02:33 PM
 */

package com.braincraftapps.cropvideos.activity.editor.data

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.io.File

@Keep
@Parcelize
data class EditorInput(
    val dataSet: Set<Data>
) : Parcelable {

    @Keep
    @Parcelize
    data class Data(
        val uri: Uri,
        val absoluteFile: File,
        val durationInMs: Long,
        val mimeType: String
    ) : Parcelable
}
