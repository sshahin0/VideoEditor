//
// Created by dev-lock on ২০/৬/২৪.
//

#ifndef VIDEOCROP_VIDEOMETADATA_H
#define VIDEOCROP_VIDEOMETADATA_H

#include "VideoMetadataRetriever.h"
#include "thread"
#include "EGL/egl.h"
#include "GLES2/gl2.h"
#include <android/bitmap.h>
#include <jni.h>
#include "atomic"
#include "../opengl/Core.h"
#include "queue"

class VideoMetadata {
public:
    VideoMetadata();

    ~VideoMetadata();

    void open(const char* url);

    jobject seek(JNIEnv *env, int64_t timestamp, int width, int height);

    void close();

private:

    void cleanUp();

    void cleanUpNativeImage() {
        if(_nativeImage) {
            //NativeImageUtil::FreeNativeImage(_nativeImage);
            if(_nativeImage->ppPlane[0]) {
                LOGCATD("clean up native image");
                delete[] _nativeImage->ppPlane[0];
                _nativeImage->ppPlane[0] = nullptr;
                _nativeImage->ppPlane[1] = nullptr;
                _nativeImage->ppPlane[2] = nullptr;
            }
            delete _nativeImage;
            _nativeImage = nullptr;
        }
    }

    void cleanUpTexture() {
    }

    Vivid::Ref<VideoMetadataRetriever> videoMetadataRetriever;

    NativeImage *_nativeImage;
   // std::mutex locking_mutex;
};


#endif //VIDEOCROP_VIDEOMETADATA_H
