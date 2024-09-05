//
// Created by dev-lock on ১৩/২/২৪.
//

#ifndef OPENGLESNATIVE_INPUTGLFILTER_H
#define OPENGLESNATIVE_INPUTGLFILTER_H

#include "GlFilter.h"
/*#include "../texture/Texture.h"
#include "../util/ImageDef.h"
#include "../texture/TextureParams.h"
#include "thread"*/
/*#include "../Core.h"*/

/*#define TEXTURE_NUM 3*/

namespace Vivid {
    class GlTransformFilter : public GlFilter {
    protected:
        unsigned int mInputWidth;
        unsigned int mInputHeight;

        float aspectRatio = 1.0f;
        glm::mat4 m_othoGraphicMatrix;
        glm::mat4 m_transformMatrix;

        glm::vec4 borderColor = glm::vec4(0.0f);

        static inline std::string const VERTEX_SHADER =
                                                        "attribute highp vec4 aPosition;\n"
                                                        "attribute highp vec4 aTextureCoord;\n"
                                                        "varying highp vec2 vTextureCoord;\n"
                                                        "uniform mat4 u_MVPMatrix;\n"
                                                        "void main() {\n"
                                                        "    gl_Position = u_MVPMatrix * aPosition;\n"
                                                        "    vTextureCoord = aTextureCoord.xy;\n"
                                                        "}";

        static inline std::string const FRAGMENT_SHADER =
                                                          "precision mediump float;\n"
                                                          "varying highp vec2 vTextureCoord;\n"
                                                          "uniform sampler2D s_texture0;\n"
                                                          "uniform mat4 transformMatrix;\n"
                                                          "uniform vec4 borderColor;\n"
                                                          "\n"
                                                          "vec4 getClampToBorderColor(vec2 uv, vec4 textureColor, vec4 borderColor) {\n"
                                                          "    float d = (step(1.0, uv.x) < 1.0 && step(uv.x, 0.0) < 1.0 && step(1.0, uv.y) < 1.0 && step(uv.y, 0.0) < 1.0)\n"
                                                          "    ? 1.0 : 0.0;\n"
                                                          "    return mix(borderColor, textureColor, d);\n"
                                                          "}\n"
                                                          "\n"
                                                          "void main()\n"
                                                          "{\n"
                                                          "    vec4 uv4 = vec4(vTextureCoord.xy, 0., 1.);\n"
                                                          "    uv4 -= vec4(0.5, 0.5, 0.0, 0.0);\n"
                                                          "    uv4 = (transformMatrix * uv4);\n"
                                                          "    uv4 += vec4(0.5, 0.5, 0.0, 0.0);\n"
                                                          "    vec2 uv = uv4.xy;\n"
                                                          "        gl_FragColor = getClampToBorderColor(uv, texture2D(s_texture0, uv), borderColor);\n"
                                                          "}";


    public:
        GlTransformFilter();

        ~GlTransformFilter();

        void setBorderColor(float r, float g, float b, float a);

        void setTranslateOffset(float offsetX, float offsetY);

        void setRotateInAngle(float angle);

        void setScaleUnit(float scaleX, float scaleY);

        void resetTransformation();

        // overriden method

        void setup() override;

        void setFrameSize(int width, int height) override;

        // void draw(int texName, GlFrameBufferObject &fbo) override;

        void onDraw() override;

        void release() override;
    };
}

#endif //OPENGLESNATIVE_INPUTGLFILTER_H
