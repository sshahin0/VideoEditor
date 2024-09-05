//
// Created by dev-lock on ১৫/৫/২৪.
//

#ifndef FFMPEGNDKVIDEO_DECODERBASE_H
#define FFMPEGNDKVIDEO_DECODERBASE_H

#include "Decoder.h"
#include "atomic"
#include "list"
#include "../util/LogUtil.h"

enum DecoderState {
    STATE_UNKNOWN,
    STATE_PREPARED,
    STATE_DECODING,
    STATE_PAUSE,
    STATE_STOP,
    STATE_DONE
};

enum PlaybackState {
    PLAYING = 1 << 0,
    PAUSED = 1 << 1,
    ENDED = 1 << 2
};

class DecoderBase : public Decoder {
public:
    DecoderBase()
    {};
    ~ DecoderBase()
    {
    };

    virtual void prepare();
    virtual void start();
    virtual void pause();
    virtual void stop();
    virtual void seekToPosition(int64_t timestamp);

    void release();

    virtual void setPlaybackCallback(void *context, PlaybackCallback callback) override {
        this->m_context = context;
        this->m_playbackCallback = callback;
    }

    virtual void setFrameAboutToBeRenderedCallback(void *context, FrameAboutToBeRenderedCallback callback) override {
        this->m_context = context;
        this->m_renderedCallback = callback;
    }

    virtual void removePlaybackCallback() override {
        this->m_playbackCallback = nullptr;
    }

    virtual void removeFrameAboutToBeRenderedCallback() override {
        this->m_renderedCallback = nullptr;
    }

    std::string url;

protected:

    /*virtual void startDecodingThread();
    virtual void decodingLoop();
    virtual void updateCurrentTimestamp() = 0;
    virtual void AVSync() = 0;

    static void DoDecodingLoop(DecoderBase *decoderBase);

    void *m_context = nullptr;
    PlaybackCallback m_playbackCallback = nullptr;

    int64_t m_startTimestamp = -1;
    int64_t m_currentTimestamp = 0;

    std::list<int64_t> seekPtsStack;
    std::atomic<DecoderState> m_decoderState = STATE_UNKNOWN;

    std::thread *m_thread = nullptr;
    std::mutex m_mutex;
    std::condition_variable m_cond;*/

    virtual int initDecoder(std::string url) = 0;
    virtual void onDecoderReady() = 0;
    virtual void decodingLoop() = 0;
    virtual void unInitDecoder() = 0;
    virtual void onDecoderDone() = 0;

    void *m_context = nullptr;
    PlaybackCallback m_playbackCallback = nullptr;
    FrameAboutToBeRenderedCallback m_renderedCallback = nullptr;

    int64_t m_startTimestamp = -1;
    int64_t m_currentTimestamp = 0;

    std::list<int64_t> seekPtsStack;
    std::atomic<DecoderState> m_decoderState = STATE_UNKNOWN;

    std::atomic<bool> m_decoderPrepared = false;

    std::mutex m_mutex;
    std::condition_variable m_cond;

private:

    virtual void startDecodingThread();
    //virtual void updateCurrentTimestamp() = 0;
    //virtual void AVSync() = 0;

    static void DoDecodingLoop(DecoderBase *decoderBase);

    std::thread *m_thread = nullptr;

};

#endif //FFMPEGNDKVIDEO_DECODERBASE_H
