//
// Created by dev-lock on ১২/১১/২৩.
//

#include "VideoDecoder.h"
#include "cmath"

// av_err2str returns a temporary array. This doesn't work in gcc.
// This function can be used as a replacement for av_err2str.
static const char *av_make_error(int errnum) {
    static char str[AV_ERROR_MAX_STRING_SIZE];
    memset(str, 0, sizeof(str));
    return av_make_error_string(str, AV_ERROR_MAX_STRING_SIZE, errnum);
}

static AVPixelFormat correct_for_deprecated_pixel_format(AVPixelFormat pix_fmt) {
    // Fix swscaler deprecated pixel format warning
    // (YUVJ has been deprecated, change pixel format to regular YUV)
    switch (pix_fmt) {
        case AV_PIX_FMT_YUVJ420P:
            return AV_PIX_FMT_YUV420P;
        case AV_PIX_FMT_YUVJ422P:
            return AV_PIX_FMT_YUV422P;
        case AV_PIX_FMT_YUVJ444P:
            return AV_PIX_FMT_YUV444P;
        case AV_PIX_FMT_YUVJ440P:
            return AV_PIX_FMT_YUV440P;
        default:
            return pix_fmt;
    }
    // return pix_fmt;
}

int VideoDecoder::video_reader_open(const char *filename) {

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
        return false;
    }

    int response = avformat_open_input(&av_format_ctx, filename, NULL, NULL);
    if (response != 0) {
        return response;
    }

    if (avformat_find_stream_info(av_format_ctx, NULL) < 0) {
        return -1;
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

            /*LOGCATD("duration %lld, %lld, %lld", durationUs, av_format_ctx->streams[i]->duration, olive::core::Timecode::rescale_timestamp(
                    av_format_ctx->streams[i]->duration, olive::core::rational(v->av_format_ctx->streams[v->video_stream_index]->time_base)
                    , olive::core::rational(av_make_q(1, AV_TIME_BASE))
            ));*/
            rotation = getRotation(av_format_ctx->streams[video_stream_index]);
            break;
        }
    }

    /*if(avformat_find_stream_info(av_format_ctx, NULL) < 0) {
        printf("failed to retrieve input stream information");
        return false;
    }*/

    if (video_stream_index == -1) {
        return -1;
    }

    // Set up a codec m_context for the decoder
    av_codec_ctx = avcodec_alloc_context3(av_codec);
    if (!av_codec_ctx) {
        return -1;
    }
    if (avcodec_parameters_to_context(av_codec_ctx, av_codec_params) < 0) {
        return -1;
    }

    av_codec_ctx->thread_count = 2;

    if (avcodec_open2(av_codec_ctx, av_codec, NULL) < 0) {
        return -1;
    }

    av_frame = av_frame_alloc();
    if (!av_frame) {
        return -1;
    }
    av_packet = av_packet_alloc();
    if (!av_packet) {
        return -1;
    }

    auto &sws = state->sws_scaler_ctx;
    sws = sws_getContext(
            width, height, av_codec_ctx->pix_fmt
            , width, height, AV_PIX_FMT_RGBA
            , SWS_FAST_BILINEAR, NULL, NULL, NULL
    );

    if(m_context && m_playbackCallback) {
        m_playbackCallback(m_context, 0);
    }

    return response;
}

void VideoDecoder::video_reader_close(VideoReaderState *state) {
    if(state->sws_scaler_ctx) sws_freeContext(state->sws_scaler_ctx);
    avformat_close_input(&state->av_format_ctx);
    avformat_free_context(state->av_format_ctx);
    av_frame_free(&state->av_frame);
    av_packet_free(&state->av_packet);
    avcodec_free_context(&state->av_codec_ctx);
}

/*void VideoDecoder::OnFrameAvailable(uint8_t *frame_buffer, VideoReaderState *state,
                                    VideoRender *nativeRender, int m_RenderWidth,
                                    int m_RenderHeight) {
    //  FUN_BEGIN_TIME("VideoDecoder:OnFrameAvailable");
    auto &width = state->width;
    auto &height = state->height;
    auto &time_base = state->time_base;
    auto &av_format_ctx = state->av_format_ctx;
    auto &av_codec_ctx = state->av_codec_ctx;
    auto &video_stream_index = state->video_stream_index;
    auto &frame = state->av_frame;
    auto &av_packet = state->av_packet;
    auto &durationUs = state->durationUs;
    auto &m_SwsContext = state->sws_scaler_ctx;

    *//*AVRational framerate = av_guess_frame_rate(av_format_ctx, av_format_ctx->streams[video_stream_index],
                                               nullptr);*//*

    *//*AVRational framerate = av_format_ctx->streams[video_stream_index]->avg_frame_rate;

    LOGCATE("average frame rate %f", av_q2d(framerate));

    framerate = av_format_ctx->streams[video_stream_index]->r_frame_rate;

    LOGCATE("r frame rate %f, %d, %d", av_q2d(framerate), framerate.num, framerate.den);*//*

    if (frame == nullptr) return;

    uint8_t *dest[4] = {frame_buffer, nullptr, nullptr, nullptr};
    int dest_linesize[4] = {m_RenderWidth * 4, 0, 0, 0};

    // LOGCATE("VideoDecoder::OnFrameAvailable frame=%p", frame);
    if (nativeRender != nullptr && frame != nullptr) {
        NativeImage image;
        //   LOGCATE("VideoDecoder::OnFrameAvailable frame[w,h]=[%d, %d],format=%d,[line0,line1,line2]=[%d, %d, %d]", frame->width, frame->height, av_codec_ctx->pix_fmt, frame->linesize[0], frame->linesize[1],frame->linesize[2]);
        *//*if(nativeRender->GetRenderType() == VIDEO_RENDER_ANWINDOW)
        {
        //    LOGCATE("native window");
            sws_scale(m_SwsContext, frame->data, frame->linesize, 0,
                      height, dest, dest_linesize);

            image.format = IMAGE_FORMAT_RGBA;
            image.width = m_RenderWidth;
            image.height = m_RenderHeight;
            image.ppPlane[0] = dest[0];
            image.pLineSize[0] = image.width * 4;
        } else *//*if (
                correct_for_deprecated_pixel_format(av_codec_ctx->pix_fmt) == AV_PIX_FMT_YUV420P
                ||
                correct_for_deprecated_pixel_format(av_codec_ctx->pix_fmt) == AV_PIX_FMT_YUVJ420P) {
            //     LOGCATE("yuv");
            image.format = IMAGE_FORMAT_I420;
            image.width = frame->width;
            image.height = frame->height;
            image.pLineSize[0] = frame->linesize[0];
            image.pLineSize[1] = frame->linesize[1];
            image.pLineSize[2] = frame->linesize[2];
            image.ppPlane[0] = frame->data[0];
            image.ppPlane[1] = frame->data[1];
            image.ppPlane[2] = frame->data[2];
            if (frame->data[0] && frame->data[1] && !frame->data[2] &&
                frame->linesize[0] == frame->linesize[1] && frame->linesize[2] == 0) {
                // on some android device, output of h264 mediacodec decoder is NV12 兼容某些设备可能出现的格式不匹配问题
                image.format = IMAGE_FORMAT_NV12;
            }
        } else if (correct_for_deprecated_pixel_format(av_codec_ctx->pix_fmt) == AV_PIX_FMT_NV12) {
            //     LOGCATE("nv12");
            image.format = IMAGE_FORMAT_NV12;
            image.width = frame->width;
            image.height = frame->height;
            image.pLineSize[0] = frame->linesize[0];
            image.pLineSize[1] = frame->linesize[1];
            image.ppPlane[0] = frame->data[0];
            image.ppPlane[1] = frame->data[1];
        } else if (correct_for_deprecated_pixel_format(av_codec_ctx->pix_fmt) == AV_PIX_FMT_NV21) {
            //    LOGCATE("nv21");
            image.format = IMAGE_FORMAT_NV21;
            image.width = frame->width;
            image.height = frame->height;
            image.pLineSize[0] = frame->linesize[0];
            image.pLineSize[1] = frame->linesize[1];
            image.ppPlane[0] = frame->data[0];
            image.ppPlane[1] = frame->data[1];
        } else if (correct_for_deprecated_pixel_format(av_codec_ctx->pix_fmt) == AV_PIX_FMT_RGBA) {
            //    LOGCATE("rgba");
            image.format = IMAGE_FORMAT_RGBA;
            image.width = frame->width;
            image.height = frame->height;
            image.pLineSize[0] = frame->linesize[0];
            image.ppPlane[0] = frame->data[0];
        } else {
            //   LOGCATE("else");
            sws_scale(m_SwsContext, frame->data, frame->linesize, 0,
                      width, dest, dest_linesize);
            image.format = IMAGE_FORMAT_RGBA;
            image.width = m_RenderWidth;
            image.height = m_RenderHeight;
            image.ppPlane[0] = dest[0];
            image.pLineSize[0] = image.width * 4;
        }

        // FUN_BEGIN_TIME("copy native image start")
        nativeRender->RenderVideoFrame(&image);
        // FUN_END_TIME("copy native image end")
    }

    //  FUN_END_TIME("VideoDecoder:OnFrameAvailable");
}*/

void VideoDecoder::seek(void *nativeRender, int64_t timestamp, uint8_t *m_FrameBuffer) {

    {
        std::unique_lock<std::mutex> lock(m_mutex);

        for (auto item: seekPtsStack) {
            if (item == timestamp) return;
        }

        seekPtsStack.push_back(timestamp);

        lock.unlock();

        m_cond.notify_one();
    }
}

void VideoDecoder::seek(int64_t timestamp) {
    VideoReaderState *vrs = v.get();

    LOGCATE("video decoder seek");

    avcodec_flush_buffers(vrs->av_codec_ctx);
    av_seek_frame(vrs->av_format_ctx, vrs->video_stream_index, timestamp, AVSEEK_FLAG_BACKWARD);
}

int VideoDecoder::getPacket(AVPacket *pkt) {
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

int VideoDecoder::getFrame(AVPacket *pkt, AVFrame *frame) {
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

            //LOGCATD("EOF true");

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

    //LOGCATD("EOF return %d", ret);

    return ret;
}

//https://stackoverflow.com/questions/75524300/get-displaymatrix-and-change-it-using-ffmpeg-c-api
//https://cpp.hotexamples.com/site/file?hash=0x05a4995b978621f208fab8032d172786e9c207e97b0ac341a640b3bd9a980f6b&fullName=ijkplayer-master/ijkmedia/ff_cmdutils.c&project=15034189148/ijkplayer
double VideoDecoder::getRotation(AVStream* avStream) {
    AVDictionaryEntry *rotate_tag = av_dict_get(avStream->metadata, "rotate", NULL, 0);
    uint8_t* displaymatrix = av_stream_get_side_data(avStream,
                                                     AV_PKT_DATA_DISPLAYMATRIX, NULL);
    double theta = 0;

    if (rotate_tag && (*rotate_tag->value) && strcmp(rotate_tag->value, "0"))
    {
        char *tail;
        theta = av_strtod(rotate_tag->value, &tail);
        if (*tail)
        {
            theta = 0;
        }
    }

    if (displaymatrix && !theta)
    {
        theta = -av_display_rotation_get((int32_t*)displaymatrix);
    }

    theta -= 360*floor(theta/360 + 0.9/360);

    //if (fabs(theta - 90*round(theta/90)) > 2)
    //    av_log(NULL, AV_LOG_WARNING, "Odd rotation angle.\n"
    //           "If you want to help, upload a sample "
    //           "of this file to ftp://upload.ffmpeg.org/incoming/ "
    //           "and contact the ffmpeg-devel mailing list. (ffmpeg-devel@ffmpeg.org)");

    return theta;
}

int VideoDecoder::decodeOne() {
    int ret = 0;

    VideoReaderState *state = v.get();
    auto &av_packet = state->av_packet;
    auto &av_frame = state->av_frame;

    if(!av_frame) {
        av_frame = av_frame_alloc();
    }

    NativeImage* ni = nullptr;

    if(m_seekPts != AV_NOPTS_VALUE) {
        ni = retrieveFrame(m_seekPts, av_packet, av_frame);
        ret = ni == nullptr;

        if(ret == 0) av_frame->pts = ni->pts;
    } else {
        ret = getFrame(av_packet, av_frame);
    }

    updateCurrentTimestamp();

    if(m_seekPts == AV_NOPTS_VALUE) {
        AVSync();
    }

    if(ret == 0) {

        int64_t pts = olive::core::Timecode::rescale_timestamp(
                av_frame->pts, olive::core::rational(v->av_format_ctx->streams[v->video_stream_index]->time_base)
                , olive::core::rational(av_make_q(1, AV_TIME_BASE))
        );

        //LOGCATD("frame time %lld, %lld", pts, state->durationUs);

        if(ni != nullptr) addImageToQueue(ni);
        else {
            addImageToQueue(frameToImage(av_frame));
            NativeImageUtil::FreeNativeImage(_native_image);

            _native_image->format = av_frame->format;
            _native_image->width = av_frame->width;
            _native_image->height = av_frame->height;
            _native_image->pts = av_frame->pts;
            _native_image->rotation = state->rotation;

            NativeImageUtil::AllocNativeImage(_native_image);
            NativeImageUtil::CopyNativeImage(ni, _native_image);
        }
    } /*else {
        LOGCATD("EOF frame %p, %lld, %p", av_frame, av_frame->pts, av_frame->data);
    }*/ else if(ret == AVERROR_EOF) {
        _native_image->pts = state->durationUs;
        addImageToQueue(_native_image);

        if(m_context && m_playbackCallback) {
            m_playbackCallback(m_context, ENDED);
        }
    }
//    else if (ret == AVERROR_EOF) {
//        addImageToQueue(frameToImage(av_frame));
//    }
    m_seekPts = AV_NOPTS_VALUE;

    if(av_frame) {
//        LOGCATD("width %d, height %d", av_frame->width, av_frame->height);
    }

    av_packet_unref(av_packet);
    av_frame_unref(av_frame);

    return ret;
}

NativeImage* VideoDecoder::retrieveFrame(int64_t timestamp, AVPacket *pkt, AVFrame *frame) {

    int ret = -1;

    VideoReaderState *state = v.get();
   // auto &av_packet = state->av_packet;

    olive::core::rational fpsRational = olive::core::rational(
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

    //int64_t target_ts = rounded_frame_number <= 1 ? (rounded_frame_number * std::floor(duration_per_frame_ffmpeg_timebase)) : (std::floor(duration_per_frame_ffmpeg_timebase) + ((rounded_frame_number-1) * std::ceil(duration_per_frame_ffmpeg_timebase)));

    int64_t target_ts = std::ceil(rounded_frame_number * duration_per_frame_ffmpeg_timebase);

    const int64_t min_seek = 0;
    int64_t seek_ts = std::max(min_seek, target_ts - 2);

    int64_t second_ts_ = (int64_t) av_q2d(
            av_inv_q(v->av_format_ctx->streams[v->video_stream_index]->time_base));

    if (cached_frames_.empty()
        || (target_ts < cached_frames_.front()->pts ||
            target_ts > cached_frames_.back()->pts + 2 * second_ts_)) {

        clearFrameCache();

        seek(seek_ts);
        if (seek_ts == min_seek) {
        }
    } else {
        // Search cache for frame
        NativeImage *cached_frame = getImageFromCache(target_ts);
        //  cached_frames_locker.unlock();

        if (cached_frame) {
            //addImageToQueue(cached_frame);
            return cached_frame;
        }
    }

    // cached_frames_locker.unlock();

    //int ret;
    NativeImage *return_frame = nullptr;
    //auto &filtered = state->av_frame;

    //int count = 0;

    while (true) {
        //count++;

        /*if (cancelSeek) {
            cancelSeek = false;
            break;
        }*/

        /*if (!filtered) {
            filtered = av_frame_alloc();
        }*/

        // Pull from the decoder
        ret = getFrame(pkt, frame);

        /*if (cancelSeek) {
            av_frame_free(&filtered);
            filtered = nullptr;

            cancelSeek = false;

            break;
        }*/

        // Handle any errors that aren't EOF (EOF is handled later on)
        if (ret < 0 && ret != AVERROR_EOF) {
            // qCritical() << "Failed to retrieve frame:" << ret;
            break;
        }

        if (ret == AVERROR_EOF) {

            // Handle an "expected" EOF by using the last frame of our cache
            // cache_at_eof_ = true;

            //   cached_frames_locker.lock();

            if (cached_frames_.empty()) {
                // qCritical() << "Unexpected codec EOF - unable to retrieve frame";
            } else {
                return_frame = cached_frames_.back();
            }

            // cached_frames_locker.unlock();

            break;

        } else {

            // Cut down to thread count - 1 before we acquire a new frame

            //  cached_frames_locker.lock();

            if (cached_frames_.size() > 3) {
                removeFirstFrame();
            }

            // Store frame before just in case
            NativeImage *previous = nullptr;
            if (cached_frames_.empty()) {
                previous = nullptr;
            } else {
                previous = cached_frames_.back();
            }

            // Append this frame and signal to other threads that a new frame has arrived

            NativeImage *filteredImage = frameToImage(frame);

            cached_frames_.push_back(filteredImage);

            //  cached_frames_locker.unlock();

            // If this is a valid frame, see if this or the frame before it are the one we need
            if (frame->pts == target_ts) {
                return_frame = filteredImage;
                break;
            } else if (frame->pts > target_ts) {
                if (!previous) {
                    return_frame = filteredImage;
                    break;
                } else {
                    //return_frame = previous;
                    return_frame =
                            abs(target_ts - previous->pts) > abs(filteredImage->pts - target_ts)
                            ? filteredImage : previous;
                    break;
                }
            }
        }

        av_frame_unref(frame);
        //filtered = nullptr;
    }

    av_frame_unref(frame);

    /*av_packet_unref(av_packet);

    if (return_frame != nullptr) addImageToQueue(return_frame);

    av_frame_unref(filtered);*/
    //filtered = nullptr;

    //cancelSeek = false;

    return return_frame;
}

void VideoDecoder::addImageToQueue(NativeImage *image) {
    /*while (!rendering_frames_.empty()) {
        rendering_frames_.pop();
    }*/

    //LOGCATE("addImagetoqueue %p", image);

    //NativeImage *cloneImg = NativeImageUtil::cloneNativeImage(image);

    //LOGCATE("addImagetoqueue after clone %p", cloneImg);

    //rendering_frames_.push(cloneImg);

   // LOGCATE("addImagetoqueue free nr %p", nr);

    //NativeImage *ni = rendering_frames_.front();
    //rendering_frames_.pop();

    int64_t pts = olive::core::Timecode::rescale_timestamp(
            image->pts, olive::core::rational(v->av_format_ctx->streams[v->video_stream_index]->time_base)
            , olive::core::rational(av_make_q(1, AV_TIME_BASE))
            );


    {
        if (this->m_context && this->frameDecodedCallback) {
            this->frameDecodedCallback(this->m_context, image);
        }
    }
//    if (nr) nr->RenderVideoFrame(image);

    if(this->m_context && this->m_renderedCallback) {
        this->m_renderedCallback(this->m_context, 0, pts);
    }

    //LOGCATE("addImagetoqueue free %p, nr %p", cloneImg, nr);

//    NativeImageUtil::FreeNativeImage(cloneImg);
    NativeImageUtil::FreeNativeImage(image);
}

void VideoDecoder::clearFrameCache() {
    if (!cached_frames_.empty()) {
        for (auto frame: cached_frames_) {
            NativeImageUtil::FreeNativeImage(frame);
        }

        cached_frames_.clear();
        //cache_at_eof_ = false;
        //cache_at_zero_ = false;
    }
}

NativeImage *VideoDecoder::getImageFromCache(int64_t t) {
    // We already have this frame in the cache, find it
    for (auto it = cached_frames_.cbegin(); it != cached_frames_.cend(); it++) {
        NativeImage *this_frame = *it;

        auto next = it;
        next++;

        if (this_frame->pts == t) { // exact match
            return this_frame;
        } else if (next != cached_frames_.cend() && (*next)->pts > t) {
            NativeImage *nextFrame = *next;

            return abs(t - this_frame->pts) > abs(nextFrame->pts - t) ? nextFrame : this_frame;
        }
    }

    return nullptr;
}

void VideoDecoder::removeFirstFrame() {

    if (!cached_frames_.empty()) {
        NativeImage *frame = cached_frames_.front();
        cached_frames_.pop_front();
        NativeImageUtil::FreeNativeImage(frame);

        //cache_at_zero_ = false;
    }
}

NativeImage *VideoDecoder::frameToImage(AVFrame *frame) {
    NativeImage image;
    image.pts = frame->pts;
    image.rotation = v->rotation;

    AVFrame *dstFrame = nullptr;

    if (
            correct_for_deprecated_pixel_format(v->av_codec_ctx->pix_fmt) == AV_PIX_FMT_YUV420P
            ||
            correct_for_deprecated_pixel_format(v->av_codec_ctx->pix_fmt) ==
            AV_PIX_FMT_YUVJ420P) {
             //LOGCATE("yuv");
        image.format = IMAGE_FORMAT_I420;
        image.width = frame->width;
        image.height = frame->height;
        image.pLineSize[0] = frame->linesize[0];
        image.pLineSize[1] = frame->linesize[1];
        image.pLineSize[2] = frame->linesize[2];
        image.ppPlane[0] = frame->data[0];
        image.ppPlane[1] = frame->data[1];
        image.ppPlane[2] = frame->data[2];
        image.pts = frame->pts;

        if (frame->data[0] && frame->data[1] && !frame->data[2] &&
            frame->linesize[0] == frame->linesize[1] && frame->linesize[2] == 0) {
            // on some android device, output of h264 mediacodec decoder is NV12 兼容某些设备可能出现的格式不匹配问题
            image.format = IMAGE_FORMAT_NV12;
        }
    } else if (correct_for_deprecated_pixel_format(v->av_codec_ctx->pix_fmt) ==
               AV_PIX_FMT_NV12) {
        //     LOGCATE("nv12");
        image.format = IMAGE_FORMAT_NV12;
        image.width = frame->width;
        image.height = frame->height;
        image.pLineSize[0] = frame->linesize[0];
        image.pLineSize[1] = frame->linesize[1];
        image.ppPlane[0] = frame->data[0];
        image.ppPlane[1] = frame->data[1];
        image.pts = frame->pts;
    } else if (correct_for_deprecated_pixel_format(v->av_codec_ctx->pix_fmt) ==
               AV_PIX_FMT_NV21) {
        //    LOGCATE("nv21");
        image.format = IMAGE_FORMAT_NV21;
        image.width = frame->width;
        image.height = frame->height;
        image.pLineSize[0] = frame->linesize[0];
        image.pLineSize[1] = frame->linesize[1];
        image.ppPlane[0] = frame->data[0];
        image.ppPlane[1] = frame->data[1];
        image.pts = frame->pts;
    } else if (correct_for_deprecated_pixel_format(v->av_codec_ctx->pix_fmt) ==
               AV_PIX_FMT_RGBA) {
        //    LOGCATE("rgba");
        image.format = IMAGE_FORMAT_RGBA;
        image.width = frame->width;
        image.height = frame->height;
        image.pLineSize[0] = frame->linesize[0];
        image.ppPlane[0] = frame->data[0];
        image.pts = frame->pts;
    } else {
        LOGCATE("else deocdedframe");
        image.pts = frame->pts;

        int srcWidth = frame->width;
        int srcHeight = frame->height;
        AVPixelFormat srcFormat = AV_PIX_FMT_YUV420P10LE;

        AVPixelFormat dstFormat = AV_PIX_FMT_RGBA;

        // Allocate destination frame
        dstFrame = av_frame_alloc();
        dstFrame->format = dstFormat;
        dstFrame->width = srcWidth;
        dstFrame->height = srcHeight;
        av_frame_get_buffer(dstFrame, 32);  // Align the destination buffer

        /*uint8_t *dest[4] = {m_FrameBuffer, nullptr, nullptr, nullptr};
        int dest_linesize[4] = {v->width * 4, 0, 0, 0};

        sws_scale(v->sws_scaler_ctx, frame->data, frame->linesize, 0,
                  v->width, dest, dest_linesize);*/

        // Convert the frame from YUV to RGBA
        sws_scale(v->sws_scaler_ctx,
                  frame->data, frame->linesize,
                  0, srcHeight,
                  dstFrame->data, dstFrame->linesize);

        image.format = IMAGE_FORMAT_RGBA;
        image.width = v->width;
        image.height = v->height;
        image.ppPlane[0] = dstFrame->data[0];
        image.pLineSize[0] = image.width * 4;
    }

    NativeImage *renderImage = new NativeImage();
    renderImage->format = image.format;
    renderImage->width = image.width;
    renderImage->height = image.height;
    renderImage->pts = image.pts;
    renderImage->rotation = image.rotation;
    NativeImageUtil::AllocNativeImage(renderImage);
    NativeImageUtil::CopyNativeImage(&image, renderImage);

    if(dstFrame) {
        av_frame_free(&dstFrame);
        dstFrame = nullptr;
    }

    return renderImage;
}

/*void VideoDecoder::setRender(VideoRender *render) {
    //this->nr = render;
}*/

int VideoDecoder::initDecoder(std::string url) {
    std::unique_lock<std::mutex> lock(m_mutex);
    m_decoderState = STATE_UNKNOWN;
    m_decoderPrepared.store(false);
    lock.unlock();

    m_cond.notify_all();

    return video_reader_open(url.c_str());
}

void VideoDecoder::onDecoderReady() {
    std::unique_lock<std::mutex> lock(m_mutex);
    m_decoderState = STATE_PREPARED;
    m_decoderPrepared.store(true);
    lock.unlock();

    m_cond.notify_all();
}

void VideoDecoder::unInitDecoder() {
    std::unique_lock<std::mutex> lock(m_mutex);

    video_reader_close(v.get());

    m_decoderState = STATE_UNKNOWN;
    m_decoderPrepared.store(false);
    lock.unlock();

    m_cond.notify_all();
}

void VideoDecoder::onDecoderDone() {
    std::unique_lock<std::mutex> lock(m_mutex);
    m_decoderState = STATE_DONE;
    m_decoderPrepared.store(false);
    lock.unlock();

    m_cond.notify_all();
}

void VideoDecoder::decodingLoop() {
    for (; /*!decodingLoopCanceled.load()*/;) {
        {
            /*std::unique_lock<std::mutex> lock(seekPtsStackMutex);

            cv.wait(lock, [this] {
                return decodingLoopCanceled.load() || !seekPtsStack.empty();
            });

            if (decodingLoopCanceled.load()) continue;

            int64_t lastSeekPts = INT64_MIN;
            {

                while (seekPtsStack.size() > 7) {
                    seekPtsStack.pop_front();
                }

                lastSeekPts = seekPtsStack.front();

                seekPtsStack.pop_front();
            }

            lock.unlock();

            if (lastSeekPts != INT64_MIN) {
                retrieveFrame(lastSeekPts);
            }*/

            std::unique_lock<std::mutex> lock(m_mutex);
            m_cond.wait(lock, [this] {
               return m_decoderPrepared.load() && (m_decoderState.load() == STATE_DECODING
               || m_decoderState.load() == STATE_STOP || (m_decoderState.load() == STATE_PAUSE && !seekPtsStack.empty()));
            });

            m_seekPts = AV_NOPTS_VALUE;
            {

                while (seekPtsStack.size() > 7) {
                    seekPtsStack.pop_front();
                }

                if(!seekPtsStack.empty()) {
                    m_seekPts = seekPtsStack.front();

                    seekPtsStack.pop_front();
                }
            }

            lock.unlock();

            if(m_startTimestamp == -1)
                m_startTimestamp = GetSysCurrentTime();

            if(m_decoderState.load() == STATE_STOP) {
                break;
            }

            decodeOne();

            //av_usleep(1000*1000);
        }
    }
}

void VideoDecoder::updateCurrentTimestamp() {
    auto &state = v;

    if(v->av_frame->pts != AV_NOPTS_VALUE) {
        m_currentTimestamp = v->av_frame->pts;
        m_currentTimestamp = (int64_t)((m_currentTimestamp * av_q2d(v->av_format_ctx->streams[v->video_stream_index]->time_base)) * 1000);
    } else {
//        m_currentTimestamp = 0;
    }
}

void VideoDecoder::AVSync() {
    long curSysTime = GetSysCurrentTime();
    //基于系统时钟计算从开始播放流逝的时间
    long elapsedTime = curSysTime - m_startTimestamp;

    long delay = 0;

    //向系统时钟同步
    if(m_currentTimestamp > elapsedTime) {
        //休眠时间
        auto sleepTime = static_cast<unsigned int>(m_currentTimestamp - elapsedTime);//ms
        //限制休眠时间不能过长
        sleepTime = sleepTime > 1000 ? 1000 :  sleepTime;
        av_usleep(sleepTime * 1000);
    }
}

void VideoDecoder::setFrameDecodedCallback(void *context, FrameDecodedCallback frameDecodedCallback) {
    this->m_context = context;
    this->frameDecodedCallback = frameDecodedCallback;
}

/*void VideoDecoder::cancelDecodingLoop() {
    {
        std::unique_lock<std::mutex> lock(seekPtsStackMutex);

        decodingLoopCanceled.store(true);

        lock.unlock();

        cv.notify_all();
    }
}*/

/*
void VideoDecoder::updateCurrentTimestamp() {
}*/
