//
// Created by dev-lock on ৯/৬/২৪.
//

#ifndef VIDEOCROP_VIDEOREADERSTATE_H
#define VIDEOCROP_VIDEOREADERSTATE_H

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
#include "libavutil/time.h"
#include "libavutil/dict.h"
#include "libavutil/display.h"
#include "libavutil/eval.h"
#include <inttypes.h>
}

#include "../util/LogUtil.h"

struct VideoReaderState {
    // Public things for other parts of the program to read from
    int width, height;
    double rotation = 0.0f;
    AVRational time_base;

    // Private internal state
    AVFormatContext *av_format_ctx;
    AVCodecContext *av_codec_ctx;
    int video_stream_index;
    int64_t durationUs;
    AVFrame *av_frame;
    AVPacket *av_packet;
    SwsContext *sws_scaler_ctx;

    VideoReaderState() {
        width = 0, height = 0;
        rotation = 0.0f;

        av_format_ctx = nullptr;
        av_codec_ctx = nullptr;

        av_frame = nullptr;
        av_packet = nullptr;

        video_stream_index = -1;
        durationUs = 0;
    }

    ~VideoReaderState() {
    }
};

#endif //VIDEOCROP_VIDEOREADERSTATE_H
