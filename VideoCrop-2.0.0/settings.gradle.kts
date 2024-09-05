/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: settings.gradle.kts
 * @modified: Aug 13, 2024, 11:20 AM
 */

@file:Suppress("UnstableApiUsage")

import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven {
            name = "GitHub Packages - BCL File Picker"
            url = uri("https://maven.pkg.github.com/BrainCraftAndroid/file_picker")
            val properties = Properties()
            file("local.properties").inputStream().buffered().use { properties.load(it) }
            credentials {
                username = properties.getProperty("GITHUB.USERNAME", "").trim('"').takeIf { it.isNotBlank() }
                    ?: System.getenv("GH_USERNAME")?.takeIf { it.isNotBlank() }
                            ?: extra["bcl.maven.github.username"]?.toString()?.trim('"')?.takeIf { it.isNotBlank() }
                password = properties.getProperty("GITHUB.TOKEN", "").trim('"').takeIf { it.isNotBlank() }
                    ?: System.getenv("GH_TOKEN")?.takeIf { it.isNotBlank() }
                            ?: extra["bcl.maven.github.token"]?.toString()?.trim('"')?.takeIf { it.isNotBlank() }
            }
        }
        maven {
            name = "GitHub Packages - Media Composition"
            url = uri("https://maven.pkg.github.com/BrainCraftAndroid/MediaComposition")
            val properties = Properties()
            file("local.properties").inputStream().buffered().use { properties.load(it) }
            credentials {
                username = properties.getProperty("GITHUB.USERNAME", "").trim('"').takeIf { it.isNotBlank() }
                    ?: System.getenv("GH_USERNAME")?.takeIf { it.isNotBlank() }
                            ?: extra["bcl.maven.github.username"]?.toString()?.trim('"')?.takeIf { it.isNotBlank() }
                password = properties.getProperty("GITHUB.TOKEN", "").trim('"').takeIf { it.isNotBlank() }
                    ?: System.getenv("GH_TOKEN")?.takeIf { it.isNotBlank() }
                            ?: extra["bcl.maven.github.token"]?.toString()?.trim('"')?.takeIf { it.isNotBlank() }
            }
        }
    }
}

rootProject.name = "VideoCrop"
include(":app")
include(":common")
include(":timeline")
include(":llamaplayer")
include(":videotimeline")
include(":indicatorseekbar")
