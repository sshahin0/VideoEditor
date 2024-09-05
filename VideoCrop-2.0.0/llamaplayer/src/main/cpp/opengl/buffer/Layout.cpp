//
// Created by dev-lock on ১১/২/২৪.
//

#include "Layout.h"

namespace Vivid {
    void Layout::push(unsigned int handler, unsigned int count) {
        push(ElementType::Float, handler, count);
    }

    void Layout::push(ElementType type, unsigned int handler, unsigned int count) {
        m_Elements.push_back({
                                     type, count, handler, false
                             });

        m_Stride += sizeofElementType(type) * count;
    }
}