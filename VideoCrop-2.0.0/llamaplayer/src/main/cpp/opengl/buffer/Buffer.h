//
// Created by dev-lock on ১২/২/২৪.
//

#ifndef OPENGLESNATIVE_BUFFER_H
#define OPENGLESNATIVE_BUFFER_H

#include <GLES2/gl2.h>

namespace Vivid {

    enum class BufferType : unsigned int {
        VERTEX_BUFFER = GL_ARRAY_BUFFER,
        INDEX_BUFFER = GL_ELEMENT_ARRAY_BUFFER
    };
}

unsigned int inline GLBufferType(const Vivid::BufferType t) {
    return static_cast<unsigned int>(t);
}

namespace Vivid {

    class Buffer {
    private:
        unsigned int mId;
        BufferType mType;

    public:
        Buffer(BufferType bufferType);

        ~Buffer();

        void bind() const;

        void unBind() const;
    };
}


#endif //OPENGLESNATIVE_BUFFER_H
