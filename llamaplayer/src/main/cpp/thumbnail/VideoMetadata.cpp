//
// Created by dev-lock on ২০/৬/২৪.
//

#include "VideoMetadata.h"
#include "../opengl/filter/GlFrameInputFilter.h"
#include "../opengl/fbo/GlFrameBufferObject.h"

VideoMetadata::VideoMetadata() {
    videoMetadataRetriever = std::make_shared<VideoMetadataRetriever>();
    _nativeImage = nullptr;
}

VideoMetadata::~VideoMetadata() {
    close();
}

void VideoMetadata::open(const char *url) {
    //std::lock_guard<std::mutex> lock(locking_mutex);
    if (videoMetadataRetriever && !videoMetadataRetriever->isOpened()) {
        videoMetadataRetriever->video_reader_open(url);
    }
}

jobject VideoMetadata::seek(JNIEnv *env, int64_t timestamp, int req_width, int req_height) {

    //std::unique_lock<std::mutex> lock(locking_mutex);

    auto *retriever = videoMetadataRetriever.get();

    if (retriever != nullptr) {

        try {
            int64_t time = timestamp;
            if (time >= 0 && videoMetadataRetriever->isOpened()) {
                _nativeImage = videoMetadataRetriever->seekFrame(time, req_width, req_height);
            }

            auto *texture_pixels = _nativeImage->ppPlane[0];

            if (!texture_pixels) {
                cleanUpNativeImage();
                cleanUpTexture();

                LOGCATD("return null bitmap for null pixels. meaning seek frame return null native image");
                return nullptr;
            }

            // Calculate new width and height based on the rotation angle
            float radians = _nativeImage->rotation * M_PI / -180.0;
            float cosAngle = std::cos(radians);
            float sinAngle = std::sin(radians);
            int newWidth = static_cast<int>(std::abs(_nativeImage->width * cosAngle) + std::abs(_nativeImage->height * sinAngle));
            int newHeight = static_cast<int>(std::abs(_nativeImage->width * sinAngle) + std::abs(_nativeImage->height * cosAngle));


            // Create a bitmap and copy the pixels into it
            jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
            jmethodID createBitmap = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
            jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");

            jfieldID rgb565Field = env->GetStaticFieldID(bitmapConfigClass, "RGB_565",
                                                         "Landroid/graphics/Bitmap$Config;");
            jobject rgb565Config = env->GetStaticObjectField(bitmapConfigClass, rgb565Field);

            jobject rgbBitmap = env->CallStaticObjectMethod(bitmapClass, createBitmap, newWidth, newHeight, rgb565Config);

            LOGCATD("create bitmap with size (%d, %d), (%d, %d), %lf", newWidth, newHeight, _nativeImage->width, _nativeImage->height, _nativeImage->rotation);

            AndroidBitmapInfo rgb_info;
            void *rgbPixels;
            AndroidBitmap_getInfo(env, rgbBitmap, &rgb_info);
            AndroidBitmap_lockPixels(env, rgbBitmap, &rgbPixels);

            // Convert ARGB_8888 to RGB_565
            auto *src = (uint32_t *) texture_pixels;
            auto *dst = (uint16_t *) rgbPixels;

            /*for (int y = 0; y < info.height; ++y) {
                for (int x = 0; x < info.width; ++x) {
                    uint32_t pixel = src[y * info.width + x];
                    uint8_t b = (pixel >> 16) & 0xFF;
                    uint8_t g = (pixel >> 8) & 0xFF;
                    uint8_t r = pixel & 0xFF;

                    uint16_t rgb565 = ((r >> 3) << 11) | ((g >> 2) << 5) | (b >> 3);
                    dst[(*//*info.height - 1 - *//*y) * info.width + x] = rgb565; // mirrored flipped
                }
            }*/

            // Center of the original and new bitmaps
            int cx = _nativeImage->width / 2;
            int cy = _nativeImage->height / 2;
            int ncx = newWidth / 2;
            int ncy = newHeight / 2;

            // Rotate the bitmap by the specified angle
            for (int y = 0; y < newHeight; ++y) {
                for (int x = 0; x < newWidth; ++x) {
                    // Calculate the coordinates in the original image
                    int srcX = static_cast<int>((x - ncx) * cosAngle - (y - ncy) * sinAngle + cx);
                    int srcY = static_cast<int>((x - ncx) * sinAngle + (y - ncy) * cosAngle + cy);

                    // If the coordinates are within bounds, copy the pixel
                    if (srcX >= 0 && srcX < _nativeImage->width && srcY >= 0 && srcY < _nativeImage->height) {
                        //dst[y * newWidth + x] = src[srcY * info.width + srcX];

                        uint32_t pixel = src[srcY * _nativeImage->width + srcX];
                        uint8_t b = (pixel >> 16) & 0xFF;
                        uint8_t g = (pixel >> 8) & 0xFF;
                        uint8_t r = pixel & 0xFF;

                        uint16_t rgb565 = ((r >> 3) << 11) | ((g >> 2) << 5) | (b >> 3);
                        dst[(/*info.height - 1 - */y) * newWidth + x] = rgb565; // mirrored flipped
                    } else {
                        dst[y * newWidth + x] = 0; // Set to transparent if out of bounds
                    }
                }
            }

            AndroidBitmap_unlockPixels(env, rgbBitmap);

            cleanUpNativeImage();
            cleanUpTexture();

            LOGCATD("return bitmap %d, %d", rgb_info.width, rgb_info.height);

            return rgbBitmap;
        } catch (...) {
            cleanUpNativeImage();
            cleanUpTexture();
        }
    }

    LOGCATD("return null bitmap");

    return nullptr;
}

void VideoMetadata::close() {
    //std::lock_guard<std::mutex> lock(locking_mutex);

    cleanUp();
}

void VideoMetadata::cleanUp() {
    if (videoMetadataRetriever && videoMetadataRetriever->isOpened()) videoMetadataRetriever->video_reader_close();

    cleanUpNativeImage();
    cleanUpTexture();
}