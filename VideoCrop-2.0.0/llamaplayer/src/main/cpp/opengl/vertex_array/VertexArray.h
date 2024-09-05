//
// Created by dev-lock on ১১/২/২৪.
//

#ifndef OPENGLESNATIVE_VERTEXARRAY_H
#define OPENGLESNATIVE_VERTEXARRAY_H

#include "../buffer/VertexBuffer.h"
#include "../buffer/Layout.h"


namespace Vivid {
    class VertexArray {
    private:
        //unsigned int m_RenderId;

    public:
        VertexArray();

        ~VertexArray();

        void addBuffer(const Vivid::VertexBuffer &vb, const Vivid::Layout &layout);

        void bind() const;

        void unBind() const;

        void setup();

        void release();
    };
}

#endif //OPENGLESNATIVE_VERTEXARRAY_H
