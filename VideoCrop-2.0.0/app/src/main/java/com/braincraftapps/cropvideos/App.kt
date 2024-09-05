/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: App.kt
 * @modified: May 15, 2024, 07:16 PM
 */

package com.braincraftapps.cropvideos

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import com.braincraftapps.droid.common.extension.core.isAtLeastSdk
import com.braincraftapps.droid.common.extension.core.shortAnimationDuration
import com.braincraftapps.droid.common.network.NetworkManager
import com.braincraftapps.droid.mp4composition.DecryptionKeyProvider
import com.braincraftapps.droid.picker.ui.coil.fetcher.MediaBitmapThumbnailFetcher
import com.braincraftapps.droid.picker.ui.coil.keyer.MediaThumbnailKeyer
import com.braincraftapps.droid.picker.ui.coil.mapper.MediaUriThumbnailMapper

class App : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        val key =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ+-/*=abcdefghijklmnopqrstuvwxyzÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡ\u03A2ΣΤΥΦΧΨΩαβγδεζηθικλμνξοπρςστυφχψωЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъы"
        DecryptionKeyProvider.setDecryptionKey(key)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(NetworkManager.getInstance(this).okHttpClient)
            .crossfade(shortAnimationDuration.toInt())
            .components {
                if (isAtLeastSdk(Build.VERSION_CODES.P)) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(VideoFrameDecoder.Factory())
                add(MediaBitmapThumbnailFetcher.Factory())
                add(MediaUriThumbnailMapper())
                add(MediaThumbnailKeyer())
            }
            .build()
    }

}
