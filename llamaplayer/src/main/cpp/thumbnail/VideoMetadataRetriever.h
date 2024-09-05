//
// Created by dev-lock on ৯/৬/২৪.
//

#ifndef VIDEOCROP_VIDEOMETADATARETRIEVER_H
#define VIDEOCROP_VIDEOMETADATARETRIEVER_H

#include "../decoder/VideoReaderState.h"
#include "../util/ImageDef.h"
#include "../opengl/Core.h"
#include "memory"

class VideoMetadataRetriever {

public:

    VideoMetadataRetriever() = default ;

    ~VideoMetadataRetriever()= default;

    Vivid::Ref<VideoReaderState> v = std::make_shared<VideoReaderState>();

    NativeImage* seekFrame(int64_t timestamp, int reqW, int reqH);
    void video_reader_open(const char* filename);
    int video_reader_close();

    bool isOpened();
private:

    void seek(int64_t timestamp);
    int getPacket(AVPacket* pkt);
    int getFrame(AVPacket* pkt, AVFrame* frame);
    NativeImage* retrieveFrame(int64_t timestamp, AVPacket *pkt, AVFrame *frame, int reqW, int reqH);
    NativeImage* frameToImage(AVFrame* frame, int reqW, int reqH);

    double getRotation(AVStream* avStream);

    bool _isOpened = false;

    int current_req_width = 0;
    int current_req_height = 0;
};

#endif //VIDEOCROP_VIDEOMETADATARETRIEVER_H
