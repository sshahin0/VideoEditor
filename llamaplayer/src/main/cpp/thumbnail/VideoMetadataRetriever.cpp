//
// Created by dev-lock on ৯/৬/২৪.
//

#include "VideoMetadataRetriever.h"
#include "../olive/core.h"

void VideoMetadataRetriever::video_reader_open(const char *filename) {

    VideoReaderState *state = v.get();

    // Unpack members of state
    auto &width = state->width;
    auto &height = state->height;
    auto &time_base = state->time_base;
    auto &av_format_ctx = state->av_format_ctx;
    auto &av_codec_ctx = state->av_codec_ctx;
    auto &video_stream_index = state->video_stream_index;
    auto &av_frame = state->av_frame;
    auto &av_packet = state->av_packet;
    auto &durationUs = state->durationUs;

    auto &rotation = state->rotation;

    // Open the file using libavformat
    av_format_ctx = avformat_alloc_context();
    if (!av_format_ctx) {
        return;
    }

    int response = avformat_open_input(&av_format_ctx, filename, NULL, NULL);
    if (response != 0) {
        return;
    }

    if (avformat_find_stream_info(av_format_ctx, NULL) < 0) {
        return;
    }

    // Find the first valid video stream inside the file
    video_stream_index = -1;
    AVCodecParameters *av_codec_params;
    AVCodec *av_codec;

    for (int i = 0; i < av_format_ctx->nb_streams; ++i) {
        av_codec_params = av_format_ctx->streams[i]->codecpar;
        av_codec = const_cast<AVCodec *>(avcodec_find_decoder(av_codec_params->codec_id));
        if (!av_codec) {
            continue;
        }

        if (av_codec_params->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_stream_index = i;
            width = av_codec_params->width;
            height = av_codec_params->height;
            time_base = av_format_ctx->streams[i]->time_base;
            //durationUs = av_format_ctx->duration;
            durationUs = av_format_ctx->streams[i]->duration;
            rotation = getRotation(av_format_ctx->streams[video_stream_index]);
            break;
        }
    }

    if (video_stream_index == -1) {
        return;
    }

    // Set up a codec m_context for the decoder
    av_codec_ctx = avcodec_alloc_context3(av_codec);
    if (!av_codec_ctx) {
        return;
    }
    if (avcodec_parameters_to_context(av_codec_ctx, av_codec_params) < 0) {
        return;
    }

    av_codec_ctx->thread_count = 2;

    if (avcodec_open2(av_codec_ctx, av_codec, NULL) < 0) {
        return;
    }

    av_frame = av_frame_alloc();
    if (!av_frame) {
        return;
    }
    av_packet = av_packet_alloc();
    if (!av_packet) {
        return;
    }

    auto &sws = state->sws_scaler_ctx;
    current_req_width = width;
    current_req_height = width;

    sws = sws_getContext(
            width, height, av_codec_ctx->pix_fmt
            , current_req_width, current_req_height, AV_PIX_FMT_RGBA
            , SWS_FAST_BILINEAR, NULL, NULL, NULL
    );

    _isOpened = true;
}

int VideoMetadataRetriever::video_reader_close() {
    if(v->sws_scaler_ctx) sws_freeContext(v->sws_scaler_ctx);
    avformat_close_input(&v->av_format_ctx);
    avformat_free_context(v->av_format_ctx);
    av_frame_free(&v->av_frame);
    av_packet_free(&v->av_packet);
    avcodec_free_context(&v->av_codec_ctx);

    LOGCATD("close retriever");

    _isOpened = false;

    return 0;
}

bool VideoMetadataRetriever::isOpened() {
    return _isOpened;
}

void VideoMetadataRetriever::seek(int64_t timestamp) {
    VideoReaderState *vrs = v.get();

    avcodec_flush_buffers(vrs->av_codec_ctx);
    av_seek_frame(vrs->av_format_ctx, vrs->video_stream_index, timestamp, AVSEEK_FLAG_BACKWARD);
}

int VideoMetadataRetriever::getPacket(AVPacket *pkt) {
    int ret;

    VideoReaderState *state = v.get();

    // auto& width = state->width;
    // auto& height = state->height;
    auto &av_format_ctx = state->av_format_ctx;
    // auto& av_codec_ctx = state->av_codec_ctx;
    auto &video_stream_index = state->video_stream_index;
    // auto& av_frame = state->av_frame;
    // auto& av_packet = state->av_packet;
    //auto& sws_scaler_ctx = state->sws_scaler_ctx;

    do {
        av_packet_unref(pkt);
        ret = av_read_frame(av_format_ctx, pkt);
    } while (pkt->stream_index != av_format_ctx->streams[video_stream_index]->index && ret >= 0);

    return ret;
}

int VideoMetadataRetriever::getFrame(AVPacket *pkt, AVFrame *frame) {
    VideoReaderState *state = v.get();

//    olive::core::Timecode::time_to_timestamp()
    // auto& width = state->width;
    // auto& height = state->height;
    // auto& av_format_ctx = state->av_format_ctx;
    auto &av_codec_ctx = state->av_codec_ctx;
    // auto& video_stream_index = state->video_stream_index;
    // auto& av_frame = state->av_frame;
    // auto& av_packet = state->av_packet;
    //auto& sws_scaler_ctx = state->sws_scaler_ctx;

    bool eof = false;

    int ret;

    // Clear any previous frames
    av_frame_unref(frame);

    while ((ret = avcodec_receive_frame(av_codec_ctx, frame)) == AVERROR(EAGAIN) && !eof) {

        // Find next packet in the correct stream index
        ret = getPacket(pkt);

        if (ret == AVERROR_EOF) {
            // Don't break so that receive gets called again, but don't try to read again
            eof = true;

            // Send a null packet to signal end of
            avcodec_send_packet(av_codec_ctx, nullptr);
        } else if (ret < 0) {
            // Handle other error by breaking loop and returning the code we received
            break;
        } else {
            // Successful read, send the packet
            ret = avcodec_send_packet(av_codec_ctx, pkt);

            // We don't need the packet anymore, so free it
            av_packet_unref(pkt);

            if (ret < 0) {
                break;
            }
        }
    }

    return ret;
}

double VideoMetadataRetriever::getRotation(AVStream *avStream) {
    AVDictionaryEntry *rotate_tag = av_dict_get(avStream->metadata, "rotate", NULL, 0);
    uint8_t *displaymatrix = av_stream_get_side_data(avStream,
                                                     AV_PKT_DATA_DISPLAYMATRIX, NULL);
    double theta = 0;

    if (rotate_tag && (*rotate_tag->value) && strcmp(rotate_tag->value, "0")) {
        char *tail;
        theta = av_strtod(rotate_tag->value, &tail);
        if (*tail) {
            theta = 0;
        }
    }

    if (displaymatrix && !theta) {
        theta = -av_display_rotation_get((int32_t *) displaymatrix);
    }

    theta -= 360 * floor(theta / 360 + 0.9 / 360);

    //if (fabs(theta - 90*round(theta/90)) > 2)
    //    av_log(NULL, AV_LOG_WARNING, "Odd rotation angle.\n"
    //           "If you want to help, upload a sample "
    //           "of this file to ftp://upload.ffmpeg.org/incoming/ "
    //           "and contact the ffmpeg-devel mailing list. (ffmpeg-devel@ffmpeg.org)");

    return theta;
}

NativeImage *VideoMetadataRetriever::retrieveFrame(int64_t timestamp, AVPacket *pkt, AVFrame *frame, int reqW, int reqH) {

    auto fpsRational = olive::core::rational(
            v->av_format_ctx->streams[v->video_stream_index]->avg_frame_rate);
    double fps = fpsRational.toDouble();

    double frame_number_for_default_timebase = timestamp / (AV_TIME_BASE / fps);
    double duration_per_frame_ffmpeg_timebase =
            v->av_format_ctx->streams[v->video_stream_index]->time_base.den / fps;


    double eps = 0.000000001;
    int64_t rounded_frame_number;
    if (frame_number_for_default_timebase > std::ceil(frame_number_for_default_timebase) - eps) {
        rounded_frame_number = std::ceil(frame_number_for_default_timebase);
    } else {
        rounded_frame_number = std::floor(frame_number_for_default_timebase);
    }

    int64_t target_ts = std::ceil(rounded_frame_number * duration_per_frame_ffmpeg_timebase);

    const int64_t min_seek = 0;
    int64_t seek_ts = std::max(min_seek, target_ts - 0);

    seek(seek_ts);

    // Pull from the decoder
    getFrame(pkt, frame);

    NativeImage *filteredImage = frameToImage(frame, reqW, reqH);

    av_frame_unref(frame);

    return filteredImage;
}

NativeImage *VideoMetadataRetriever::seekFrame(int64_t timestamp, int reqW, int reqH) {
    VideoReaderState *state = v.get();
    auto &av_packet = state->av_packet;
    auto &av_frame = state->av_frame;

    if (!av_frame) {
        av_frame = av_frame_alloc();
    }

    if(current_req_width != reqW || current_req_height != reqH) {
        auto &sws = state->sws_scaler_ctx;

        float xScale = reqW / (state->width * 1.0f);
        float yScale = reqH / (state->height * 1.0f);
        float scale = std::min(xScale, yScale);
        int scaledWidth = std::floor(state->width * scale);
        int scaledHeight = std::floor(state->height * scale);

        current_req_width = scaledWidth;
        current_req_height = scaledHeight;

        if(sws) sws_freeContext(sws);

        sws = sws_getContext(
                state->width, state->height, state->av_codec_ctx->pix_fmt
                , current_req_width, current_req_height, AV_PIX_FMT_RGBA
                , SWS_FAST_BILINEAR, NULL, NULL, NULL
        );
    }

    return retrieveFrame(timestamp, av_packet, av_frame, current_req_width, current_req_height);
}

NativeImage *VideoMetadataRetriever::frameToImage(AVFrame *frame, int reqW, int reqH) {
    NativeImage image;
    image.pts = frame->pts;
    image.rotation = v->rotation;
    AVFrame *dstFrame = nullptr;

    image.pts = frame->pts;

    int srcWidth = frame->width;
    int srcHeight = frame->height;

    AVPixelFormat dstFormat = AV_PIX_FMT_RGBA;

    // Allocate destination frame
    dstFrame = av_frame_alloc();
    dstFrame->format = dstFormat;
    dstFrame->width = reqW;
    dstFrame->height = reqH;

    av_image_alloc(dstFrame->data, dstFrame->linesize, reqW, reqH, dstFormat, 32);

    // Convert the frame from YUV to RGBA
    sws_scale(v->sws_scaler_ctx,
              frame->data, frame->linesize,
              0, srcHeight,
              dstFrame->data, dstFrame->linesize);

    image.format = IMAGE_FORMAT_RGBA;
    image.width = reqW;
    image.height = reqH;
    image.ppPlane[0] = dstFrame->data[0];
    image.pLineSize[0] = dstFrame->linesize[0];

    auto *renderImage = new NativeImage();
    renderImage->format = image.format;
    renderImage->width = image.width;
    renderImage->height = image.height;
    renderImage->pts = image.pts;
    renderImage->rotation = image.rotation;
    NativeImageUtil::AllocNativeImage(renderImage);
    NativeImageUtil::CopyNativeImage(&image, renderImage);

    if(dstFrame) {
        av_frame_unref(dstFrame);
        av_frame_free(&dstFrame);
        dstFrame = nullptr;
    }

    return renderImage;
}
