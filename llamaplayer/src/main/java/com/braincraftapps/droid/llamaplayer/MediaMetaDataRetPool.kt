/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: MediaMetaDataRetPool.kt
 * @modified: Aug 21, 2024, 04:44 PM
 */

package com.braincraftapps.droid.llamaplayer

object MediaMetaDataRetPool {
//
//    private const val POOL_SIZE = 1
//
//    private val lock = Any()
//
//    private val pool: MutableList<MediaMetadataRetriever> = mutableListOf()
//
//    private var currentIndex = 0
//
//    private var handle: Long = -1
//
//    var released = false
//
//    fun open(path: String) {
//        synchronized(lock) {
//            if (released) return
//            if (pool.size < POOL_SIZE) {
//                pool.add(MediaMetadataRetriever())
//            }
//            pool[currentIndex].open(path)
//        }
//    }
//
//    fun passEglContextToNative(handle: Long) {
//        this.handle = handle
//    }
//
//    fun seek(timestamp: Long, width: Int, height: Int): Bitmap? {
//        synchronized(lock) {
//            if (pool.size != POOL_SIZE) {
//                return null
//            }
//
//            return pool[currentIndex].seek(timestamp, width, height)
//        }
//    }
//
//    fun close() {
//        synchronized(lock) {
////            if (pool.size != POOL_SIZE) throw InstantiationException("No retriever added in pool")
//            return pool[currentIndex].close()
//        }
//    }
//
//    fun release() {
//        synchronized(lock) {
//            pool[currentIndex]._finalize()
//            pool.clear()
//            released = true
//        }
//    }
}
