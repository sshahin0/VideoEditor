//
// Created by dev-lock on ১৪/৫/২৪.
//

#ifndef FFMPEGNDKVIDEO_PLAYERWRAPPER_H
#define FFMPEGNDKVIDEO_PLAYERWRAPPER_H

#include <jni.h>
#include "NativePlayer.h"
#include "../opengl/Core.h"

class PlayerWrapper {
public:
    PlayerWrapper(){
        //this->m_MediaPlayer = new NativePlayer();
    };
    ~PlayerWrapper(){
        //if(this->m_MediaPlayer) {
          //  delete this->m_MediaPlayer;
           // this->m_MediaPlayer = nullptr;
        //}
    };

    void setMediaSource(const char *url);
    void prepare(JNIEnv *env, jobject obj);

    //void Init(JNIEnv *jniEnv, jobject obj, char *url);
    //void UnInit();

    void Play();
    void Pause();
    void Stop();
    void SeekTo(int64_t timestamp);
    void SeekTo(int mediaItemIndex, int64_t timestamp);
    void release();

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onDrawFrame();

    int getTextureId();

private:
    //NativePlayer* m_MediaPlayer = nullptr;
    Vivid::Ref<NativePlayer> m_MediaPlayer = std::make_shared<NativePlayer>();
};


#endif //FFMPEGNDKVIDEO_PLAYERWRAPPER_H
