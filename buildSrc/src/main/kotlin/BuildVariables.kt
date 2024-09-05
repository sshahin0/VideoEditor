/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: BuildVariables.kt
 * @modified: May 14, 2024, 02:44 PM
 */

import org.gradle.api.JavaVersion

object BuildVariables {
    const val VERSION_NAME: String = "2.0.0"

    const val TARGET_SDK: Int = 34
    const val COMPILE_SDK: Int = 34
    const val MINIMUM_SDK: Int = 24
    const val APPLICATION_ID = "com.braincraftapps.cropvideos"

    const val JVM_TARGET: String = "17"
    val JAVA_VERSION = JavaVersion.VERSION_17
}
