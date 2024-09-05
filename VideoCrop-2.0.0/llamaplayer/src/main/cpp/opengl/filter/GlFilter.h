//
// Created by dev-lock on ১১/২/২৪.
//

#ifndef OPENGLESNATIVE_GLFILTER_H
#define OPENGLESNATIVE_GLFILTER_H

#include "../buffer/IndexBuffer.h"
#include "../buffer/VertexBuffer.h"
#include "../buffer/Layout.h"
#include "../shader/Shader.h"
#include "../fbo/GlFrameBufferObject.h"
#include "../Core.h"

namespace Vivid {
    class GlFilter {
    protected:
        unsigned int outputWidth;
        unsigned int outputHeight;

        std::string vertexShader;
        std::string fragmentShader;

        int texName;

        Vivid::Ref<Shader> shader;
        Vivid::Ref<IndexBuffer> indexBuffer;
        Vivid::Ref<VertexBuffer> vertexBuffer;

        static inline std::string const DEFAULT_VERTEX_SHADER = R"(
                                                                    attribute highp vec4 aTextureCoord;
                                                                    attribute highp vec4 aPosition;
                                                                    varying highp vec2 vTextureCoord;
                                                                    void main() {
                                                                        gl_Position = aPosition;
                                                                        vTextureCoord = aTextureCoord.xy;
                                                                    }
                                                                )";

        static inline std::string const DEFAULT_FRAGMENT_SHADER = R"(
                                                                    precision mediump float;
                                                                    varying highp vec2 vTextureCoord;
                                                                    uniform lowp sampler2D s_texture0;
                                                                    void main() {
                                                                        gl_FragColor = texture2D(s_texture0, vTextureCoord);
                                                                    }
                                                                  )";

        GLfloat DEFAULT_VERTICES_COORDS[20] = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 0.0f, 0.0f, 1.0f
        };

        GLuint INDICES[6] = {0, 1, 2, 0, 2, 3};

        void releaseShader() {
            shader->release();
        }

        void releaseBuffer() {
            if(vertexBuffer) {
                vertexBuffer->unBind();
            }

            if(indexBuffer) {
                indexBuffer->unBind();
            }
        }

    public:
        GlFilter(std::string &fragmentShaderSource);

        GlFilter(std::string &vertexShaderSource, std::string &fragmentShaderSource);

        GlFilter();

        ~GlFilter();

        virtual void setup();

        /*virtual*/ void setVertexShader(std::string const &shaderSource);

        /*virtual*/ void setFragmentShader(std::string const &shaderSource);

        virtual void setFrameSize(int width, int height);

        virtual void draw(int _texName, GlFrameBufferObject &fbo);

        virtual void onDraw();

        virtual void initVertexParams();

        virtual void drawGlComponent();

        virtual void release();

        inline int getTexName() const {
            return texName;
        }

        inline int getOutputWidth() const {
            return outputWidth;
        }

        inline int getOutputHeight() const {
            return outputHeight;
        }
    };
}

#endif //OPENGLESNATIVE_GLFILTER_H
