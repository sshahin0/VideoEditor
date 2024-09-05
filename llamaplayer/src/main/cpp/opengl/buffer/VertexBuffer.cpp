//
// Created by dev-lock on ৮/২/২৪.
//

#include "VertexBuffer.h"
#include "../util/GLUtils.h"

namespace Vivid {
    VertexBuffer::VertexBuffer(const void *data, unsigned int size) : Buffer(
            BufferType::VERTEX_BUFFER) {
        glBufferData(GLBufferType(BufferType::VERTEX_BUFFER), size, data, GL_STATIC_DRAW);
    }
}
