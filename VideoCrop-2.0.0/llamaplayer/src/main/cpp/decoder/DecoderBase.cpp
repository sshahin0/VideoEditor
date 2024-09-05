//
// Created by dev-lock on ১৫/৫/২৪.
//

#include "DecoderBase.h"
#include "../util/LogUtil.h"

void DecoderBase::prepare() {
    if(m_thread != nullptr) {
        stop();

        m_thread->join();
        delete m_thread;
        m_thread = nullptr;
    }

    std::unique_lock<std::mutex> lock(m_mutex);
    m_decoderState = STATE_UNKNOWN;
    m_decoderPrepared.store(false);
    lock.unlock();

    startDecodingThread();
}

void DecoderBase::start() {
    /*if(m_thread == nullptr) {
        startDecodingThread();
        return;
    }*/

    std::unique_lock<std::mutex> lock(m_mutex);
    if(m_decoderState.load() != STATE_DECODING) {
        m_decoderState.store(STATE_DECODING);

        m_startTimestamp = GetSysCurrentTime() - m_currentTimestamp;
    }

    lock.unlock();

    if(m_context && m_playbackCallback) {
        m_playbackCallback(m_context, PLAYING);
    }

    m_cond.notify_all();
}

void DecoderBase::pause() {
    std::unique_lock<std::mutex> lock(m_mutex);
    if(m_decoderState.load() != STATE_PAUSE) {
        m_decoderState.store(STATE_PAUSE);
        m_startTimestamp = GetSysCurrentTime() - m_currentTimestamp;
    }
    lock.unlock();

    if(m_context && m_playbackCallback) {
        m_playbackCallback(m_context, PAUSED);
    }

    m_cond.notify_all();
}

void DecoderBase::stop() {
    std::unique_lock<std::mutex> lock(m_mutex);
    m_decoderState = STATE_STOP;
    lock.unlock();
    m_cond.notify_all();
}

void DecoderBase::seekToPosition(int64_t timestamp) {
    std::unique_lock<std::mutex> lock(m_mutex);
    m_decoderState = STATE_PAUSE;
    seekPtsStack.push_back(timestamp);
    m_cond.notify_all();
}

void DecoderBase::startDecodingThread() {
    m_thread = new std::thread(DoDecodingLoop, this);
}

void DecoderBase::DoDecodingLoop(DecoderBase *decoderBase) {
    do {

        if(decoderBase->initDecoder(decoderBase->url) != 0) {
            break;
        }

        decoderBase->onDecoderReady();
        decoderBase->decodingLoop();
    } while (false);

    decoderBase->unInitDecoder();
    decoderBase->onDecoderDone();
}

void DecoderBase::release() {
    if(m_thread) {
        m_thread->join();
        delete m_thread;
        m_thread = nullptr;
    }
}
