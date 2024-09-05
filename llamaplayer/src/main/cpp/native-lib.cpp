#include <jni.h>
#include <string>

#include "player/PlayerWrapper.h"

#define  LOG_TAG "FFMPEGNDK"

#define JNI_METHOD(NAME) \
    Java_com_braincraftapps_droid_llamaplayer_LlamaPlayerImpl_##NAME

extern "C" JNIEXPORT jlong JNICALL
JNI_METHOD(prepareVideo)(JNIEnv* env, jobject obj, jstring jurl, jlong surfaceId) {
    const char* url = env->GetStringUTFChars(jurl, nullptr);

    PlayerWrapper *nativePlayer = new PlayerWrapper();
    nativePlayer->setMediaSource(url);
    nativePlayer->prepare(env, obj);

   // nativePlayer->Init(env, obj, const_cast<char *>(url));

    LOGCATE("prepare video in native");

    return reinterpret_cast<jlong>(nativePlayer);
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(playWhenReady)(JNIEnv* env, jobject obj, jboolean playWhenReady, jlong videoReaderId) {
    auto* nativeRender = reinterpret_cast<PlayerWrapper*>(videoReaderId);

    if(nativeRender) {
        if(playWhenReady) {
            nativeRender->Play();
        } else {
            nativeRender->Pause();
        }
    }
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(nativeStop)(JNIEnv* , jobject , jlong videoReaderId) {
    auto* nativeRender = reinterpret_cast<PlayerWrapper*>(videoReaderId);

    if(nativeRender) {
        nativeRender->Stop();
    }
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(seekTo_1JJJ)(JNIEnv* env, jobject obj, jlong ptsUs, jlong surfaceId, jlong videoReaderId) {
   auto* nativeRender = reinterpret_cast<PlayerWrapper*>(videoReaderId);

    if(nativeRender == nullptr) return;
    nativeRender->SeekTo(ptsUs);
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(seekTo_1IJJJ)(JNIEnv* env, jobject obj, jint index, jlong ptsUs, jlong surfaceId, jlong videoReaderId) {
    auto* nativeRender = reinterpret_cast<PlayerWrapper*>(videoReaderId);

    if(nativeRender == nullptr) return;
    nativeRender->SeekTo(index, ptsUs);
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(release)(JNIEnv* env, jobject, jlong surfaceId) {
    auto* nativeRender = reinterpret_cast<PlayerWrapper*>(surfaceId);

    if(nativeRender) {
        nativeRender->release();

        delete nativeRender;
        nativeRender = nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(onSurfaceCreated)(JNIEnv* env, jobject, jlong surfaceId) {
    auto* nativeRender = reinterpret_cast<PlayerWrapper*>(surfaceId);

    if(nativeRender) {
        nativeRender->onSurfaceCreated();
    }
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(onSurfaceChanged)(JNIEnv* env, jobject, jlong surfaceId, jint width, jint height) {
    auto* nativeRender = reinterpret_cast<PlayerWrapper*>(surfaceId);

    if(nativeRender) {
        nativeRender->onSurfaceChanged(width, height);
    }
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(onDrawFrame)(JNIEnv* env, jobject, jlong surfaceId) {
    auto* nativeRender = reinterpret_cast<PlayerWrapper*>(surfaceId);

    if(nativeRender) {
        nativeRender->onDrawFrame();
    }
}

extern "C" JNIEXPORT jint JNICALL
JNI_METHOD(getTextureId)(JNIEnv* env, jobject, jlong surfaceId) {
    auto* nativeRender = reinterpret_cast<PlayerWrapper*>(surfaceId);

    if(nativeRender) {
        return nativeRender->getTextureId();
    }

    return -1;
}