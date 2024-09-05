//
// Created by dev-lock on ১২/২/২৪.
//

#include "Buffer.h"
#include "../util/GLUtils.h"

namespace Vivid {
    Buffer::Buffer(BufferType bufferType) : mType{bufferType} {
        glGenBuffers(1, &mId);
        glBindBuffer(GLBufferType(bufferType), mId);
    }

    Buffer::~Buffer() {
        glBindBuffer(GLBufferType(mType), GL_NONE);
        glDeleteBuffers(1, &mId);
    }

    void Buffer::bind() const {
        glBindBuffer(GLBufferType(mType), mId);
    }

    void Buffer::unBind() const {
        glBindBuffer(GLBufferType(mType), GL_NONE);
    }
}