//
// Created by dev-lock on ৮/২/২৪.
//

#ifndef OPENGLESNATIVE_VERTEXBUFFER_H
#define OPENGLESNATIVE_VERTEXBUFFER_H

#include "Buffer.h"

namespace Vivid {
    class VertexBuffer : public Buffer {
    private:
    public:
        VertexBuffer(const void *data, unsigned int size);
    };
}


#endif //OPENGLESNATIVE_VERTEXBUFFER_H
