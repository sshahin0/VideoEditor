//
// Created by dev-lock on ১৯/১১/২৩.
//

#include "NativePlayer.h"

std::mutex NativePlayer::m_Mutex;


void NativePlayer::addMediaSource(std::string sourcePath) {
    sourceList.push_back(sourcePath);
}

void NativePlayer::removeMediaSource(int index) {
    if(sourceList.size() < index) {
        return;
    }

    int count = 0;

    for (std::list<std::string>::iterator itr = sourceList.begin(); itr != sourceList.end(); )
    {
        if(count == index) {
            sourceList.erase(itr);
            break;
        }
        ++itr;
    }
}

void NativePlayer::prepare(JNIEnv *env, jobject obj) {
    env->GetJavaVM(&jvm);
    this->jobj = env->NewGlobalRef(obj);

    videoDecoder->setPlaybackCallback(this, PostMessage);
    videoDecoder->setFrameAboutToBeRenderedCallback(this, PostFrameToBeRendered);
    videoDecoder->setFrameDecodedCallback(this, PostFrameDecoded);

    std::string url = sourceList.front();
    videoDecoder->url = url;

    videoDecoder->prepare();

    /*if(!videoDecoder->video_reader_open(url)) {
        LOGCATE("yo bro Could not open video file %s\n", url);
        return;
    }*/

    /*const int len = vrs.width * vrs.height * 4;
    uint8_t* data;
    if (posix_memalign((void**)&data, 128, len) != 0) {
        printf("Couldn't allocate frame buffer\n");
        return -1;
    }*/

    //  LOGCATE("prepare video %lld", surfaceId);

    /*if(videoRender) {
        int dst[2] = {0};
        videoRender->Init(videoDecoder->v->width, videoDecoder->v->height, dst);*/

        //  renderW = dst[0];
        //  renderH = dst[1];

        // LOGCATE("video w %lld, video h %lld, render w %lld, render h %lld", vrs.width, vrs.height, renderW, renderH);

        /*int bufferSize = av_image_get_buffer_size(AV_PIX_FMT_RGBA, renderW, renderH, 1);
        m_FrameBuffer = (uint8_t *) av_malloc(bufferSize * sizeof(uint8_t));*/

       // videoDecoder->setRender(videoRender);

        /*if(decoderThread == nullptr) {
            LOGCATE("init decoder thread");
            //decoderThread = new std::thread(fillDecodedFrameBuffer, this);

            //decoderThread = new std::thread(vrs->fillDecodedFrameBuffer);

            decoderThread = new std::thread(decodingLoop, this);
        }

        if(rendererThread == nullptr) {
            // rendererThread = new std::thread(startDecodingLoop, this);
        }*/

        /*const int len = renderW * renderH * 4;
        if (posix_memalign((void**)&m_FrameBuffer, 128, len) != 0) {
            LOGCATE("Couldn't allocate frame buffer\n");
            return -1;
        }*/

        /*auto& m_SwsContext = vrs.sws_scaler_ctx;

        m_SwsContext = sws_getContext(vrs.width, vrs.height, vrs.av_codec_ctx->pix_fmt,
                                      renderW, renderH, AV_PIX_FMT_RGBA,
                                      SWS_FAST_BILINEAR, NULL, NULL, NULL);*/
    //}
}

void NativePlayer::setPlayWhenReady(bool ready) {
    if(videoDecoder) {
        if(ready) {
            videoDecoder->start();
        } else {
            videoDecoder->pause();
        }
    }
}

/*void NativePlayer::start() {
}

void NativePlayer::pause() {
}*/

void NativePlayer::stop() {
    if(videoDecoder) {
        videoDecoder->stop();
    }
}

void NativePlayer::release() {
   // VideoGLRender::ReleaseInstance();
    if(videoDecoder) {
        videoDecoder->removePlaybackCallback();
        videoDecoder->removeFrameAboutToBeRenderedCallback();
        videoDecoder->release();
    }

    bool isAttach = false;
    GetJNIEnv(&isAttach)->DeleteGlobalRef(jobj);
    if(isAttach)
        GetJavaVM()->DetachCurrentThread();
}

void NativePlayer::seekTo(int mediaItemIndex, int64_t timestamp) {
    if(videoDecoder) {
        this->setPlayWhenReady(false);

        this->videoDecoder->seek(nullptr, timestamp, nullptr);
    }
}

/*void NativePlayer::prepareVideo(JNIEnv *env, jobject jobj, const char *url) {
    env->GetJavaVM(&jvm);
    this->jobj = env->NewGlobalRef(jobj);

    videoDecoder = new VideoDecoder();

    videoDecoder->setPlaybackCallback(this, PostMessage);

    if(!videoDecoder->video_reader_open(url)) {
        LOGCATE("yo bro Could not open video file %s\n", url);
        return;
    }

    LOGCATE("prepare video in native");

    *//*const int len = vrs.width * vrs.height * 4;
    uint8_t* data;
    if (posix_memalign((void**)&data, 128, len) != 0) {
        printf("Couldn't allocate frame buffer\n");
        return -1;
    }*//*

    //  LOGCATE("prepare video %lld", surfaceId);

    LOGCATE("video property duration %lld", videoDecoder->v->durationUs);

    if(videoRender) {
        int dst[2] = {0};
        videoRender->Init(videoDecoder->v->width, videoDecoder->v->height, dst);

        //  renderW = dst[0];
        //  renderH = dst[1];

        // LOGCATE("video w %lld, video h %lld, render w %lld, render h %lld", vrs.width, vrs.height, renderW, renderH);

        *//*int bufferSize = av_image_get_buffer_size(AV_PIX_FMT_RGBA, renderW, renderH, 1);
        m_FrameBuffer = (uint8_t *) av_malloc(bufferSize * sizeof(uint8_t));*//*

        videoDecoder->setRender(videoRender);

        if(decoderThread == nullptr) {
            LOGCATE("init decoder thread");
              //decoderThread = new std::thread(fillDecodedFrameBuffer, this);

            //decoderThread = new std::thread(vrs->fillDecodedFrameBuffer);

            decoderThread = new std::thread(decodingLoop, this);
        }

        if(rendererThread == nullptr) {
           // rendererThread = new std::thread(startDecodingLoop, this);
        }

        *//*const int len = renderW * renderH * 4;
        if (posix_memalign((void**)&m_FrameBuffer, 128, len) != 0) {
            LOGCATE("Couldn't allocate frame buffer\n");
            return -1;
        }*//*

        *//*auto& m_SwsContext = vrs.sws_scaler_ctx;

        m_SwsContext = sws_getContext(vrs.width, vrs.height, vrs.av_codec_ctx->pix_fmt,
                                      renderW, renderH, AV_PIX_FMT_RGBA,
                                      SWS_FAST_BILINEAR, NULL, NULL, NULL);*//*
    }
}*/

void NativePlayer::seekTo(int64_t ptsUs) {

    //   LOGCATE("seek to %lld", ptsUs);

    /*const int len = vrs.width * vrs.height * 4;
    uint8_t* data;
    if (posix_memalign((void**)&data, 128, len) != 0) {
        printf("Couldn't allocate frame buffer\n");
        return nullptr;
    }*/

 //   AVRational timeBase = {1, AV_TIME_BASE};

 //time =ptsUs;

//    int64_t timestamp = av_rescale_q(ptsUs, AV_TIME_BASE_Q, videoDecoder->v->time_base);
int64_t timestamp = ptsUs;

    //LOGCATE("ptsUs from java layer %lld, timestamp after rescale %lld, %d, %d", ptsUs, timestamp, videoDecoder->v->time_base.num, videoDecoder->v->time_base.den);

    //av_rescale_q()

    //int64_t timestamp = static_cast<int64_t>(((double )ptsUs / (double ) AV_TIME_BASE) * (double ) videoDecoder->v->time_base.den / (double ) videoDecoder->v->time_base.num);

    //double pt_in_seconds = timestamp * av_q2d(videoDecoder->v->time_base);

  //  LOGCATE("ptsUs %lld, timestamp %lld, in sec %lf", ptsUs, timestamp, pt_in_seconds);

 // if(decoderThread == nullptr) {
     // decoderThread = new std::thread(this->decodingLoop, this);
 // }

  /*if(decoder_thread == nullptr) {
      decoder_thread = new std::thread(seekToThread, this, timestamp);
  } else if(decoder_thread_done) {
      decoder_thread_done = false;
      decoder_thread_done = new std::thread(seekToThread, this, timestamp);
  } else {
      videoDecoder->cancelSeek = true;
      decoder_thread->join();

      decoder_thread_done = false;
      decoder_thread_done = new std::thread(seekToThread, this, timestamp);
  }*/

    videoDecoder->seek(nullptr, timestamp, m_FrameBuffer);
}

/*void NativePlayer::decodingLoop(NativePlayer *nativePlayer) {
    //nativePlayer->videoDecoder->startDecodingLoop();
}

void NativePlayer::startLoop() {
}

void NativePlayer::fillBuffer() {
}

void NativePlayer::startDecodingLoop(NativePlayer* nativePlayer) {
    nativePlayer->startLoop();
}

void NativePlayer::fillDecodedFrameBuffer(NativePlayer* nativePlayer) {
    nativePlayer->fillBuffer();
}

void NativePlayer::seekToThread(NativePlayer* np, int64_t ts) {
    np->videoDecoder->seek(np->videoRender, ts, np->m_FrameBuffer);
}*/

JNIEnv *NativePlayer::GetJNIEnv(bool *isAttach) {
    JNIEnv *env;
    int status;
    if (nullptr == jvm) {
        return nullptr;
    }
    *isAttach = false;
    status = jvm->GetEnv((void **)&env, JNI_VERSION_1_4);
    if (status != JNI_OK) {
        status = jvm->AttachCurrentThread(&env, nullptr);
        if (status != JNI_OK) {
            return nullptr;
        }
        *isAttach = true;
    }
    return env;
}

jobject NativePlayer::GetJavaObj() {
    return jobj;
}

JavaVM *NativePlayer::GetJavaVM() {
    return jvm;
}

void NativePlayer::PostMessage(void *context, int msgType) {
    if(context != nullptr)
    {
        NativePlayer *player = static_cast<NativePlayer *>(context);
        bool isAttach = false;
        JNIEnv *env = player->GetJNIEnv(&isAttach);
        if(env == nullptr)
            return;
        jobject javaObj = player->GetJavaObj();
        jmethodID mid = env->GetMethodID(env->GetObjectClass(javaObj), JAVA_PLAYER_EVENT_CALLBACK_API_NAME, "(I)V");
        env->CallVoidMethod(javaObj, mid, msgType);
        if(isAttach)
            player->GetJavaVM()->DetachCurrentThread();

    }
}

void NativePlayer::PostFrameToBeRendered(void *context, int index,
                                         int64_t currentPresentationTimeUs) {

    if(context != nullptr)
    {
        NativePlayer *player = static_cast<NativePlayer *>(context);
        bool isAttach = false;
        JNIEnv *env = player->GetJNIEnv(&isAttach);
        if(env == nullptr)
            return;
        jobject javaObj = player->GetJavaObj();
        jmethodID mid = env->GetMethodID(env->GetObjectClass(javaObj), JAVA_FRAME_RENDERED_EVENT_CALLBACK_API_NAME, "(IJ)V");
        env->CallVoidMethod(javaObj, mid, index, currentPresentationTimeUs);
        if(isAttach)
            player->GetJavaVM()->DetachCurrentThread();

    }
}

void NativePlayer::PostFrameDecoded(void *context, NativeImage *nativeImage) {
    {
        std::lock_guard<std::mutex> lock(m_Mutex);
        if (nativeImage) {
            static_cast<NativePlayer *>(context)->inputGlFilter->setInput(nativeImage);
        }
    }
}

void NativePlayer::onSurfaceCreated() {
    LOGCATE("native player surface created");
    inputGlFilter->setup();
}

void NativePlayer::onSurfaceChanged(int width, int height) {
    glViewport(0, 0, width, height);
    glClearColor(0.0, 0.0, 0.0, 1.0);

    this->fbo.setUp(width, height);
    inputGlFilter->setFrameSize(width, height);
}

void NativePlayer::onDrawFrame() {
    //fbo.enable();
    {
        std::lock_guard<std::mutex> lock(m_Mutex);
        fbo.enable();
        glClear(GL_COLOR_BUFFER_BIT);
        glViewport(0, 0, fbo.getWidth(), fbo.getHeight());

        inputGlFilter->draw(-1, fbo);
    }
}

int NativePlayer::getTexureId() {
    return fbo.getTexName();
}