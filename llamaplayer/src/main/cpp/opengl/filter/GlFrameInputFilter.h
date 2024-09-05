//
// Created by dev-lock on ২৮/৫/২৪.
//

#ifndef VIDEOCROP_GLFRAMEINPUTFILTER_H
#define VIDEOCROP_GLFRAMEINPUTFILTER_H

#include "../texture/Texture.h"
#include "../util/ImageDef.h"
#include "../texture/TextureParams.h"

#include "thread"
#include "GlTransformFilter.h"

#include "../Core.h"

#define TEXTURE_NUM 3

namespace Vivid {
    class GlFrameInputFilter : public GlTransformFilter {

    protected:

        Texture texture[TEXTURE_NUM];
        TextureParams textureParams;

        GlFrameBufferObject _fbo;

        NativeImage ni;

        static inline std::string const FRAGMENT_SHADER =
                                                          R"(precision mediump float;
                                 varying highp vec2 vTextureCoord;
                                 uniform sampler2D s_texture0;
                                 uniform sampler2D s_texture1;
                                 uniform sampler2D s_texture2;
                                 uniform mat4 transformMatrix;
                                 uniform vec4 borderColor;
                                 uniform int u_nImgType; // 1:RGBA, 2:NV21, 3:NV12, 4:I420

                                 vec4 getClampToBorderColor(vec2 uv, vec4 textureColor, vec4 borderColor) {
                                     float d = (step(1.0, uv.x) < 1.0 && step(uv.x, 0.0) < 1.0 && step(1.0, uv.y) < 1.0 && step(uv.y, 0.0) < 1.0)
                                     ? 1.0 : 0.0;
                                     return mix(borderColor, textureColor, d);
                                 }

                                 void main()
                                 {
                                     vec4 uv4 = vec4(vTextureCoord.xy, 0., 1.);
                                     uv4 -= vec4(0.5, 0.5, 0.0, 0.0);
                                     uv4 = (transformMatrix * uv4);
                                     uv4 += vec4(0.5, 0.5, 0.0, 0.0);
                                     vec2 uv = uv4.xy;
                                     if (u_nImgType == 1) //RGBA
                                     {
                                         gl_FragColor = getClampToBorderColor(uv, texture2D(s_texture0, uv), borderColor);
                                     }
                                     else if (u_nImgType == 2) //NV21
                                     {
                                         vec3 yuv;
                                         yuv.x = texture2D(s_texture0, uv).r;
                                         yuv.y = texture2D(s_texture1, uv).a - 0.5;
                                         yuv.z = texture2D(s_texture1, uv).r - 0.5;
                                         highp vec3 rgb = mat3(1.0, 1.0, 1.0,
                                                               0.0, -0.344, 1.770,
                                                               1.403, -0.714, 0.0) * yuv;

                                         gl_FragColor = getClampToBorderColor(uv, vec4(rgb, 1.0), borderColor);
                                     }
                                     else if (u_nImgType == 3) //NV12
                                     {
                                         vec3 yuv;
                                         yuv.x = texture2D(s_texture0, uv).r;
                                         yuv.y = texture2D(s_texture1, uv).r - 0.5;
                                         yuv.z = texture2D(s_texture1, uv).a - 0.5;
                                         highp vec3 rgb = mat3(1.0, 1.0, 1.0,
                                                               0.0, -0.344, 1.770,
                                                               1.403, -0.714, 0.0) * yuv;

                                         gl_FragColor = getClampToBorderColor(uv, vec4(rgb, 1.0), borderColor);
                                     }
                                     else if (u_nImgType == 4) //I420
                                     {
                                         vec3 yuv;
                                         yuv.x = texture2D(s_texture0, uv).r;
                                         yuv.y = texture2D(s_texture1, uv).r - 0.5;
                                         yuv.z = texture2D(s_texture2, uv).r - 0.5;
                                         highp vec3 rgb = mat3(1.0, 1.0, 1.0,
                                                               0.0, -0.344, 1.770,
                                                               1.403, -0.714, 0.0) * yuv;

                                         gl_FragColor = getClampToBorderColor(uv, vec4(rgb, 1.0), borderColor);
                                     }
                                     else
                                     {
                                         gl_FragColor = borderColor;
                                     }
                                 })";

    public:

        GlFrameInputFilter();

        ~GlFrameInputFilter();

        void setInput(NativeImage *nativeImage);

        void freeInput();

        void setup() override;

        void setFrameSize(int width, int height) override;

        void draw(int texName, GlFrameBufferObject &fbo) override;

        void onDraw() override;

        void release() override;
    };
}

#endif //VIDEOCROP_GLFRAMEINPUTFILTER_H
