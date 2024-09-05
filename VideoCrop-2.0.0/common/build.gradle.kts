/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: build.gradle.kts
 * @modified: May 28, 2024, 04:05 PM
 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "${BuildVariables.APPLICATION_ID}.common"
    compileSdk = BuildVariables.COMPILE_SDK

    defaultConfig {
        minSdk = BuildVariables.MINIMUM_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = BuildVariables.JAVA_VERSION
        targetCompatibility = BuildVariables.JAVA_VERSION
    }
    kotlinOptions {
        jvmTarget = BuildVariables.JVM_TARGET
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.braincraftapps.common)

    api(libs.mp4compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
