//
// Created by dev-lock on ৮/২/২৪.
//

#ifndef OPENGLESNATIVE_INDEXBUFFER_H
#define OPENGLESNATIVE_INDEXBUFFER_H

#include "Buffer.h"

namespace Vivid {
    class IndexBuffer : public Buffer {
    private:
        unsigned int m_count;
    public:
        IndexBuffer(const unsigned *data, unsigned int count);

        inline unsigned int getCount() const {
            return m_count;
        }
    };
}


#endif //OPENGLESNATIVE_INDEXBUFFER_H
