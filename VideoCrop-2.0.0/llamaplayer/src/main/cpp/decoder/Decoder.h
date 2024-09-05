//
// Created by dev-lock on ১৫/৫/২৪.
//

#ifndef FFMPEGNDKVIDEO_DECODER_H
#define FFMPEGNDKVIDEO_DECODER_H

#include <thread>

typedef void (*PlaybackCallback)(void*, int);
typedef void (*FrameAboutToBeRenderedCallback)(void*, int, int64_t);
typedef void (*AVSyncCallback)(void*, int);

class Decoder {
public:
    virtual void prepare() = 0;
    virtual void start() = 0;
    virtual void pause() = 0;
    virtual void stop() = 0;
    virtual void seekToPosition(int64_t timestamp) = 0;

    virtual void release() = 0;

    virtual void setPlaybackCallback(void *context, PlaybackCallback callback) = 0;
    virtual void removePlaybackCallback() = 0;

    virtual void setFrameAboutToBeRenderedCallback(void *context, FrameAboutToBeRenderedCallback callback) = 0;
    virtual void removeFrameAboutToBeRenderedCallback() = 0;
};

#endif //FFMPEGNDKVIDEO_DECODER_H
