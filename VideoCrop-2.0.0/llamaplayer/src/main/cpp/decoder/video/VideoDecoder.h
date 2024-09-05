//
// Created by dev-lock on ১৯/১১/২৩.
//

#ifndef FFMPEGNDKVIDEO_VIDEODECODER_H
#define FFMPEGNDKVIDEO_VIDEODECODER_H

#define SIZE 25

#include "../../util/LogUtil.h"
#include "../../util/ImageDef.h"

/*extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
#include "libavutil/time.h"
#include "libavutil/dict.h"
#include "libavutil/display.h"
#include "libavutil/eval.h"
#include <inttypes.h>
}*/

#include "vector"
#include "list"
#include "../../olive/core.h"
#include <thread>
#include "atomic"
#include "queue"
#include "../DecoderBase.h"
#include "../VideoReaderState.h"
#include "../opengl/Core.h"

//using namespace std;

typedef void (*FrameDecodedCallback)(void*, NativeImage*);

/*struct VideoReaderState {
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
};*/

class VideoDecoder : public DecoderBase {

public:
    VideoDecoder() {
        //v = new VideoReaderState();
        //v = std::make_shared<VideoReaderState>();

        //LOGCATE("video decoder constructor %p", v.get());
        _native_image = new NativeImage();
    };
    ~VideoDecoder() {
        NativeImageUtil::FreeNativeImage(_native_image);
        delete _native_image;
    };

    // av_err2str returns a temporary array. This doesn't work in gcc.
// This function can be used as a replacement for av_err2str.
    static const char* av_make_error(int errnum) {
        static char str[AV_ERROR_MAX_STRING_SIZE];
        memset(str, 0, sizeof(str));
        return av_make_error_string(str, AV_ERROR_MAX_STRING_SIZE, errnum);
    }

    static AVPixelFormat correct_for_deprecated_pixel_format(AVPixelFormat pix_fmt) {
        // Fix swscaler deprecated pixel format warning
        // (YUVJ has been deprecated, change pixel format to regular YUV)
        switch (pix_fmt) {
            case AV_PIX_FMT_YUVJ420P: return AV_PIX_FMT_YUV420P;
            case AV_PIX_FMT_YUVJ422P: return AV_PIX_FMT_YUV422P;
            case AV_PIX_FMT_YUVJ444P: return AV_PIX_FMT_YUV444P;
            case AV_PIX_FMT_YUVJ440P: return AV_PIX_FMT_YUV440P;
            default:                  return pix_fmt;
        }
        // return pix_fmt;
    }

    void video_reader_close(VideoReaderState* state);
    //void OnFrameAvailable(uint8_t *frame_buffer, VideoReaderState *state, VideoRender* nativeRender, int m_RenderWidth, int m_RenderHeight);
    void seek(void *nativeRender, int64_t timestamp, uint8_t *m_FrameBuffer);

    //void setRender(VideoRender* render);

    void setFrameDecodedCallback(void *context, FrameDecodedCallback frameDecodedCallback);

    //void startDecodingLoop();
    //void cancelDecodingLoop();


    //std::vector<int64_t> seekingTimestamps;
    //int64_t seekPts = INT_MIN;

    std::list<int64_t> seekPtsStack;
    //std::mutex seekPtsStackMutex;

    Vivid::Ref<VideoReaderState> v = std::make_shared<VideoReaderState>();
    //VideoReaderState* v;
    //int64_t pts = 0;
//    VideoRender* nr;
    int rw, rh;
    //bool success = false;
   // std::condition_variable  cv;
   // std::condition_variable cvSeeking;
   // std::mutex vrsMutex;

    //std::atomic<bool> cancelSeek = false;

    std::list<NativeImage*> cached_frames_;
    NativeImage *_native_image = nullptr;

    //std::mutex cached_frames_locker;

   // std::queue<NativeImage*> rendering_frames_;

    //bool cache_at_eof_ = false;
    //bool cache_at_zero_ = false;

    //std::atomic<bool> decodingLoopCanceled = false;
    //std::condition_variable cv;

protected:

    virtual int initDecoder(std::string url);
    virtual void onDecoderReady();
    virtual void decodingLoop();
    virtual void unInitDecoder();
    virtual void onDecoderDone();

private:
    int video_reader_open(const char* filename);

    void seek(int64_t timestamp);
    int getPacket(AVPacket* pkt);
    int getFrame(AVPacket* pkt, AVFrame* frame);
    NativeImage* retrieveFrame(int64_t timestamp, AVPacket *pkt, AVFrame *frame);

    int decodeOne();

    double getRotation(AVStream* avStream);
    void addImageToQueue(NativeImage* image);
    NativeImage* frameToImage(AVFrame* frame);
    NativeImage* getImageFromCache(int64_t target_ts);
    void clearFrameCache();
    void removeFirstFrame();

    void updateCurrentTimestamp();
    void AVSync();

    int64_t m_seekPts = AV_NOPTS_VALUE;

    FrameDecodedCallback frameDecodedCallback = nullptr;

protected:

    //virtual void updateCurrentTimestamp();
    //virtual void AVSync();
};

#endif //FFMPEGNDKVIDEO_VIDEODECODER_H
