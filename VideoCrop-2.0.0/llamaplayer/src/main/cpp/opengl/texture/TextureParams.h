//
// Created by dev-lock on ২৩/৫/২৪.
//

#ifndef FFMPEGNDKVIDEO_TEXTUREPARAMS_H
#define FFMPEGNDKVIDEO_TEXTUREPARAMS_H

#include "../util/ImageDef.h"
#include <GLES2/gl2.h>

typedef struct _tag_TextureParams {
    int width = 0;
    int height = 0;
    int channels = 1;
    int format = IMAGE_FORMAT_RGBA;
} TextureParams;

class TextureParamsUtil {
public:
    static int getPixelType(int imageFormat) {
        return GL_UNSIGNED_BYTE;
    }

    static int getPixelFormat(int imageFormat) {
        if(imageFormat == IMAGE_FORMAT_RGBA) {
            return GL_RGBA;
        } else if(imageFormat == IMAGE_FORMAT_I420) {
            return GL_LUMINANCE;
        }
    }

    static int getInternalFormat(int imageFormat) {
        if(imageFormat == IMAGE_FORMAT_RGBA) {
            return GL_RGBA;
        } else if(imageFormat == IMAGE_FORMAT_I420) {
            return GL_LUMINANCE;
        }
    }
};

#endif //FFMPEGNDKVIDEO_TEXTUREPARAMS_H