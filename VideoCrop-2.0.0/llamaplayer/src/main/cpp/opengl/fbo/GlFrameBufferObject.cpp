//
// Created by dev-lock on ৬/২/২৪.
//

#include "GlFrameBufferObject.h"
#include "../util/GLUtils.h"
#include "../util/LogUtil.h"

namespace Vivid {
    GlFrameBufferObject::GlFrameBufferObject() {
    }

    GlFrameBufferObject::~GlFrameBufferObject() {
        release();
    }

    void GlFrameBufferObject::setUp(GLuint width, GLuint height) {
        this->setUp(width, height, GL_LINEAR, GL_LINEAR);
    }

    void GlFrameBufferObject::setUp(GLuint width, GLuint height, int mag, int min) {
        GLint args;

        glGetIntegerv(GL_MAX_TEXTURE_SIZE, &args);

        //LOGCATE("texture size max %d", args);

        if (width > args || height > args) {
            // throw exception from native layer
            return;
        }

        glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE, &args);

        //LOGCATE("render buffer size max %d", args);

        if (width > args || height > args) {
            // throw exception from native layer
            return;
        }

        glGetIntegerv(GL_FRAMEBUFFER_BINDING, &args);
        GLint savedFrameBuffer = args;

        glGetIntegerv(GL_RENDERBUFFER_BINDING, &args);
        GLint savedRenderBuffer = args;

        glGetIntegerv(GL_TEXTURE_BINDING_2D, &args);
        GLint savedTexture = args;

        release();

        this->width = width;
        this->height = height;

        glGenFramebuffers(1, &frameBufferId);
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId);

        glGenRenderbuffers(1, &renderBufferId);
        glBindRenderbuffer(GL_RENDERBUFFER, renderBufferId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, this->width, this->height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER,
                                  renderBufferId);

        glGenTextures(1, &texId);
        glBindTexture(GL_TEXTURE_2D, texId);

        GLUtils::setupSampler(GL_TEXTURE_2D, mag, min);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this->width, this
                ->height, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texId, 0);

        const int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            this->release();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, savedFrameBuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, savedRenderBuffer);
        glBindTexture(GL_TEXTURE_2D, savedTexture);
    }

    void GlFrameBufferObject::release() {
        glDeleteTextures(1, &this->texId);
        this->texId = GL_NONE;

        glDeleteRenderbuffers(1, &this->renderBufferId);
        this->renderBufferId = GL_NONE;

        glDeleteFramebuffers(1, &this->frameBufferId);
        this->frameBufferId = GL_NONE;
    }

    void GlFrameBufferObject::enable() {
        glBindFramebuffer(GL_FRAMEBUFFER, this->frameBufferId);
    }

    void GlFrameBufferObject::disable() {
        glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);
    }
}