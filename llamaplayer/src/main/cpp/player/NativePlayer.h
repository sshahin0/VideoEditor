//
// Created by dev-lock on ১৯/১১/২৩.
//

#ifndef FFMPEGNDKVIDEO_NATIVEPLAYER_H
#define FFMPEGNDKVIDEO_NATIVEPLAYER_H

#define JAVA_PLAYER_EVENT_CALLBACK_API_NAME "onPlaybackStateChanged"
#define JAVA_FRAME_RENDERED_EVENT_CALLBACK_API_NAME "onFrameAboutToBeRendered"

#include "../decoder/video/VideoDecoder.h"
#include "../opengl/fbo/GlFrameBufferObject.h"
#include "../opengl/filter/GlFrameInputFilter.h"
#include "../opengl/Core.h"
#include "future"
#include "atomic"
#include "jni.h"

class NativePlayer {

public:
    NativePlayer() {
        inputGlFilter = std::make_shared<Vivid::GlFrameInputFilter>();

       // videoRender = VideoGLRender::GetInstance();
        //videoDecoder = new VideoDecoder();
    };

    ~NativePlayer() {
        /*if(videoRender) {
            delete videoRender;
            videoRender = nullptr;
        }*/

        /*if(rendererThread) {
            rendererThread->join();

            delete rendererThread;
            rendererThread = nullptr;
        }*/

        //if(videoDecoder) {
           // videoDecoder->cancelDecodingLoop();
        //}

        /*if(decoderThread) {
            decoderThread->join();

            delete decoderThread;
            decoderThread = nullptr;
        }*/

        //VideoGLRender::ReleaseInstance();

      //  videoRender = nullptr;

       // if(videoDecoder) {
            //videoDecoder->video_reader_close(videoDecoder->v);
         //   delete videoDecoder;
           // videoDecoder = nullptr;
        //}

        inputGlFilter->release();
        fbo.release();

        /*bool isAttach = false;
        GetJNIEnv(&isAttach)->DeleteGlobalRef(jobj);
        if(isAttach)
            GetJavaVM()->DetachCurrentThread();*/
    };

    void addMediaSource(std::string url);

    void removeMediaSource(int index);

    void prepare(JNIEnv *env, jobject obj);

    void setPlayWhenReady(bool ready);

    void stop();

    void release();

    void seekTo(int64_t timestamp);

    void seekTo(int mediaItemIndex, int64_t timestamp);

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onDrawFrame();

    int getTexureId();

    //void prepareVideo(JNIEnv *env, jobject obj, const char *url);
    //static void seekToThread(NativePlayer* np, int64_t ts);
    //void startLoop();
    //void fillBuffer();
    //static void startDecodingLoop(NativePlayer* nativePlayer);
    //static void fillDecodedFrameBuffer(NativePlayer* nativePlayer);
    //static void decodingLoop(NativePlayer* nativePlayer);

protected:
   // VideoRender *videoRender = nullptr;
    //VideoDecoder *videoDecoder = nullptr;
    Vivid::Ref<VideoDecoder> videoDecoder = std::make_shared<VideoDecoder>();
    uint8_t* m_FrameBuffer = nullptr;
    //std::thread* rendererThread = nullptr;
    //std::thread* decoderThread = nullptr;

    virtual JNIEnv *GetJNIEnv(bool *isAttach);
    virtual jobject GetJavaObj();
    virtual JavaVM *GetJavaVM();

    static void PostMessage(void *context, int msgType);
    static void PostFrameToBeRendered(void *context, int index, int64_t currentPresentationTimeUs);
    static void PostFrameDecoded(void *context, NativeImage *nativeImage);

    JavaVM *jvm = nullptr;
    jobject jobj = nullptr;

    std::list<std::string> sourceList;

    Vivid::GlFrameBufferObject fbo;

    Vivid::Ref<Vivid::GlFrameInputFilter> inputGlFilter;

    static std::mutex m_Mutex;
};

#endif //FFMPEGNDKVIDEO_NATIVEPLAYER_H
