//
// Created by dev-lock on ১৪/৫/২৪.
//

#include "PlayerWrapper.h"

void PlayerWrapper::setMediaSource(const char *url) {
    if(m_MediaPlayer) {
        m_MediaPlayer->addMediaSource(url);
    }
}

void PlayerWrapper::prepare(JNIEnv *env, jobject obj) {
    if(m_MediaPlayer) {
        m_MediaPlayer->prepare(env, obj);
    }
}

/*void PlayerWrapper::Init(JNIEnv *jniEnv, jobject obj, char* url) {
   // m_MediaPlayer = new NativePlayer();
    if(m_MediaPlayer) m_MediaPlayer->prepareVideo()
    if(m_MediaPlayer) m_MediaPlayer->prepareVideo(jniEnv, obj, url);
}

void PlayerWrapper::UnInit() {
}*/

void PlayerWrapper::Play() {
    if(m_MediaPlayer) {
        m_MediaPlayer->setPlayWhenReady(true);
    }
}

void PlayerWrapper::Pause() {
    if(m_MediaPlayer) {
        m_MediaPlayer->setPlayWhenReady(false);
    }
}

void PlayerWrapper::SeekTo(int64_t timestamp) {
    this->SeekTo(0, timestamp);
}

void PlayerWrapper::SeekTo(int mediaItemIndex, int64_t timestamp) {
    if(m_MediaPlayer) m_MediaPlayer->seekTo(mediaItemIndex, timestamp);
}

void PlayerWrapper::Stop() {
    if(m_MediaPlayer) {
        m_MediaPlayer->stop();
    }
}

void PlayerWrapper::release() {
    if(m_MediaPlayer) {
        m_MediaPlayer->release();
    }
}

void PlayerWrapper::onSurfaceCreated() {
    if(m_MediaPlayer) {
        m_MediaPlayer->onSurfaceCreated();
    }
}

void PlayerWrapper::onSurfaceChanged(int width, int height) {
    if(m_MediaPlayer) {
        m_MediaPlayer->onSurfaceChanged(width, height);
    }
}

void PlayerWrapper::onDrawFrame() {
    if(m_MediaPlayer) {
        m_MediaPlayer->onDrawFrame();
    }
}

int PlayerWrapper::getTextureId() {
    if(m_MediaPlayer) {
        return m_MediaPlayer->getTexureId();
    }

    return -1;
}
