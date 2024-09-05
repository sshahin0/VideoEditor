# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Declares and names the project.

project("llamaplayer")

set(IMPORT_DIR ${CMAKE_SOURCE_DIR}/../jniLibs)

# FFmpeg include file
include_directories(
        glm
        ${IMPORT_DIR}/${ANDROID_ABI}/include)

file(GLOB_RECURSE OPENGL_SOURCE "opengl/*.cpp")
file(GLOB_RECURSE OLIVE_SOURCE "olive/*.cpp")
file(GLOB_RECURSE DECODER_SOURCE "decoder/*.cpp")
file(GLOB_RECURSE PLAYER_SOURCE "player/*.cpp")
file(GLOB_RECURSE RENDER_SOURCE "render/*.cpp")
file(GLOB_RECURSE UTIL_SOURCE "util/*.cpp")
file(GLOB_RECURSE THUMBNAIL_SOURCE "thumbnail/*.cpp")

# Codec library
add_library(
        avcodec
        SHARED
        IMPORTED
)
set_target_properties(
        avcodec
        PROPERTIES IMPORTED_LOCATION
        ${IMPORT_DIR}/${ANDROID_ABI}/libavcodec.so
)

# The filter library is temporarily out of use
add_library(
        avfilter
        SHARED
        IMPORTED
)
set_target_properties(
        avfilter
        PROPERTIES IMPORTED_LOCATION
        ${IMPORT_DIR}/${ANDROID_ABI}/libavfilter.so
)

# File format libraries are required for most operations
add_library(
        avformat
        SHARED
        IMPORTED
)

set_target_properties(
        avformat
        PROPERTIES IMPORTED_LOCATION
        ${IMPORT_DIR}/${ANDROID_ABI}/libavformat.so
)

# Tool library
add_library(
        avutil
        SHARED
        IMPORTED
)
set_target_properties(
        avutil
        PROPERTIES IMPORTED_LOCATION
        ${IMPORT_DIR}/${ANDROID_ABI}/libavutil.so
)

# The resampling library is mainly used for audio conversion.
add_library(
        swresample
        SHARED
        IMPORTED
)
set_target_properties(
        swresample
        PROPERTIES IMPORTED_LOCATION
        ${IMPORT_DIR}/${ANDROID_ABI}/libswresample.so
)

# Video format conversion library is mainly used for video conversion.
add_library(
        swscale
        SHARED
        IMPORTED
)
set_target_properties(
        swscale
        PROPERTIES IMPORTED_LOCATION
        ${IMPORT_DIR}/${ANDROID_ABI}/libswscale.so
)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        llamaplayer

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp
        media_metadata_retriever.cpp
        #[[player/NativePlayer.cpp
        player/PlayerWrapper.cpp
        decoder/DecoderBase.cpp
        decoder/video/VideoDecoder.cpp
        render/NativeRender.cpp
        render/VideoGLRender.cpp
        util/GLUtils.cpp
        olive/rational.cpp
        olive/audioparams.cpp
        olive/color.cpp
        olive/samplebuffer.cpp
        olive/stringutils.cpp
        olive/timecodefunctions.cpp
        olive/timerange.cpp
        olive/value.cpp]]

        ${OPENGL_SOURCE}
        ${OLIVE_SOURCE}
        ${DECODER_SOURCE}
        ${PLAYER_SOURCE}
        ${RENDER_SOURCE}
        ${UTIL_SOURCE}
        ${THUMBNAIL_SOURCE}
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        llamaplayer

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        android
        mediandk
        EGL
        GLESv2
        OpenSLES
        log
        avcodec
        avfilter
        avformat
        avutil
        swresample
        swscale
        -ljnigraphics)