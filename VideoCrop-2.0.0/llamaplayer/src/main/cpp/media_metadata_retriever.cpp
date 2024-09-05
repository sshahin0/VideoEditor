//
// Created by dev-lock on ৬/৬/২৪.
//

#include <jni.h>
#include <string>
#include "../util/LogUtil.h"
#include "../thumbnail/VideoMetadata.h"

#define  LOG_TAG "FFMPEGNDK"

#define JNI_METHOD(NAME) \
    Java_com_braincraftapps_droid_llamaplayer_MediaMetadataRetriever_##NAME

VideoMetadata *metadata = nullptr;

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(_1create)(JNIEnv *env, jobject obj) {

    if (metadata == nullptr) {
        metadata = new VideoMetadata();

        LOGCATD("create in jni %p", metadata);
    }
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(_1open)(JNIEnv *env, jobject obj, jstring path) {
    const char *url = env->GetStringUTFChars(path, nullptr);

    if (metadata != nullptr) {
        metadata->open(url);

        LOGCATD("open in jni %p", metadata);
    }
}

extern "C" JNIEXPORT jobject JNICALL
JNI_METHOD(_1seek)(JNIEnv *env, jobject obj, jlong timestamp, int newWidth, int newHeight) {
    LOGCATD("jni metadata %p", metadata);
    if (metadata != nullptr) {
        jobject bitmap = metadata->seek(env, timestamp, newWidth, newHeight);
        return bitmap;
    }

    LOGCATD("jni return null");

    return nullptr;
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(_1destroy)(JNIEnv *env, jobject obj) {

    if (metadata != nullptr) {
        delete metadata;
        metadata = nullptr;

        LOGCATD("destroyed in jni %p", metadata);
    }
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(_1close)(JNIEnv *env, jobject obj) {

    if (metadata != nullptr) {
        metadata->close();

        LOGCATD("close in jni %p", metadata);
    }
}
