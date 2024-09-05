/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: FrameLruCache.kt
 * @modified: Jul 11, 2024, 12:24 PM
 */

package com.braincraftapps.droid.timeline.component.scrubber.provider.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Environment.isExternalStorageRemovable
import androidx.collection.LruCache
import com.braincraftapps.droid.common.cache.disk.DiskLruCache
import com.braincraftapps.droid.common.extension.core.versionCode
import com.braincraftapps.droid.common.extension.lang.runOrNull

class FrameLruCache(private val context: Context) {
    companion object {
        private const val MEMORY_CACHE_SIZE = 1024 * 1024 // 1MB
        private const val DISK_CACHE_SIZE: Long = 1024 * 1024 * 10 // 10MB
        private const val DISK_CACHE_SUBDIR = "scrubber_frames"
    }

    private val memoryCache: LruCache<String, Bitmap> by lazy {
        return@lazy object : LruCache<String, Bitmap>(MEMORY_CACHE_SIZE) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return value.allocationByteCount / 1024
            }
        }
    }

    private val diskCache: DiskLruCache? by lazy {
        val cacheDir = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !isExternalStorageRemovable()) {
            context.externalCacheDir
        } else {
            context.cacheDir
        }?.resolve(DISK_CACHE_SUBDIR) ?: return@lazy null
        return@lazy DiskLruCache(
            cacheDirectory = cacheDir,
            maxCacheSize = DISK_CACHE_SIZE,
            appVersionCode = context.versionCode.toInt(),
            itemsPerEntry = 1
        )
    }

    suspend fun getFrame(key: String, collector: suspend (String) -> Bitmap?): Bitmap? {
        val fromMemory = memoryCache[key]
        if (fromMemory != null) {
            // Found a cached bitmap from the memory cache.
            return fromMemory
        }
        val fromDiskFile = diskCache?.get(key)?.firstOrNull()
        if (fromDiskFile != null) {
            val bitmap = runOrNull {
                fromDiskFile.inputStream().use {
                    BitmapFactory.decodeStream(it)
                }
            }
            if (bitmap != null) {
                // Found a cached bitmap from the disk cache.
                // First put it in the memory cache and then return it.
                memoryCache.put(key, bitmap)
                return bitmap
            }
        }
        // No cached bitmap found. Collect a new bitmap using the collector()
        val bitmap = collector(key) ?: return null
        memoryCache.put(key, bitmap) // First put it in the memory cache
        val diskLruCache = diskCache ?: return bitmap
        // Put the collected bitmap on the disk cache and then return it.
        diskLruCache.edit(key) {
            val file = firstOrNull() ?: return@edit
            file.outputStream().buffered().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                it.flush()
            }
        }
        return bitmap
    }
}
