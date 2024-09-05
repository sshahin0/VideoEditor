//
// Created by dev-lock on ৬/২/২৪.
//

#ifndef OPENGLESNATIVE_GLFRAMEBUFFEROBJECT_H
#define OPENGLESNATIVE_GLFRAMEBUFFEROBJECT_H

#include <GLES2/gl2.h>

namespace Vivid {
    class GlFrameBufferObject {
    public:
        GlFrameBufferObject();

        ~GlFrameBufferObject();

        void setUp(GLuint width, GLuint height);

        void setUp(GLuint width, GLuint height, int mag, int min);

        void release();

        void enable();

        void disable();

        inline GLuint getWidth() const {
            return this->width;
        }

        inline GLuint getHeight() const {
            return this->height;
        }

        inline GLuint getFrameBufferName() const {
            return this->frameBufferId;
        }

        inline GLuint getRenderBufferName() const {
            return this->renderBufferId;
        }

        inline GLuint getTexName() const {
            return this->texId;
        }

    private:
        GLuint width = GL_NONE, height = GL_NONE;
        GLuint frameBufferId = GL_NONE, renderBufferId = GL_NONE;
        GLuint texId = GL_NONE;
    };
}

#endif //OPENGLESNATIVE_GLFRAMEBUFFEROBJECT_H
