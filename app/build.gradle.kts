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
 * @modified: Aug 13, 2024, 11:20 AM
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
    alias(libs.plugins.google.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = BuildVariables.APPLICATION_ID
    compileSdk = BuildVariables.COMPILE_SDK

    defaultConfig {
        applicationId = BuildVariables.APPLICATION_ID
        minSdk = BuildVariables.MINIMUM_SDK
        targetSdk = BuildVariables.TARGET_SDK
        versionName = BuildVariables.VERSION_NAME
        versionCode = BuildVariables.VERSION_NAME.replace(".", "").toIntOrNull() ?: 1

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            versionNameSuffix = ".debug"
            applicationIdSuffix = ".debug"
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
    implementation(project(":common"))
    implementation(project(":timeline"))
    implementation(project(":llamaplayer"))
    implementation(project(":videotimeline"))
    implementation(project(":indicatorseekbar"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.braincraftapps.picker.ui)

//    debugImplementation(libs.leakcanary) // Causing lags. Disable for now.

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
