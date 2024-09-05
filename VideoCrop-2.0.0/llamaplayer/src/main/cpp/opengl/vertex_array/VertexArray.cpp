//
// Created by dev-lock on ১১/২/২৪.
//

#include "VertexArray.h"
#include "../util/GLUtils.h"
#include "../util/LogUtil.h"

namespace Vivid {
    VertexArray::VertexArray() {
    }

    VertexArray::~VertexArray() {
        release();
    }

    void VertexArray::addBuffer(const VertexBuffer &vb, const Layout &layout) {
        bind();

        vb.bind();

        int offset = 0;
        for (int i = 0; i < layout.getElements().size(); i++) {
            const auto &element = layout.getElements()[i];
            glEnableVertexAttribArray(element.handler);
            glVertexAttribPointer(element.handler, element.count, GLElementType(element.type),
                                  element.normalized, layout.getStride(), (const void *) offset);

            offset += element.count * sizeofElementType(element.type);
        }
    }

    void VertexArray::bind() const {
        //glBindVertexArray(m_RenderId);

        GLUtils::CheckGLError("vertex array bind");
    }

    void VertexArray::unBind() const {
        //glBindVertexArray(GL_NONE);

        GLUtils::CheckGLError("vertex array unbind");
    }

    void VertexArray::setup() {
        release();

        //glGenVertexArrays(1, &m_RenderId);

        GLUtils::CheckGLError("vertex array setup");
    }

    void VertexArray::release() {
        unBind();
        //glDeleteVertexArrays(1, &m_RenderId);
    }
}