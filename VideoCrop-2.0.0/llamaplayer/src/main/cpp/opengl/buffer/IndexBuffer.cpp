//
// Created by dev-lock on ৮/২/২৪.
//

#include "IndexBuffer.h"
#include "../util/GLUtils.h"

namespace Vivid {
    IndexBuffer::IndexBuffer(const unsigned int *data, unsigned int count) : Buffer(
            BufferType::INDEX_BUFFER), m_count{count} {
        glBufferData(GLBufferType(BufferType::INDEX_BUFFER), count * sizeof(unsigned int), data,
                     GL_STATIC_DRAW);
    }
}
