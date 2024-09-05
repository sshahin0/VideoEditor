//
// Created by dev-lock on ১১/২/২৪.
//

#ifndef OPENGLESNATIVE_LAYOUT_H
#define OPENGLESNATIVE_LAYOUT_H

#include <vector>
#include <map>
#include <GLES2/gl2.h>

namespace Vivid {
// OpenGL supported element types
    enum class ElementType : unsigned int {
        Byte = GL_BYTE,
        UByte = GL_UNSIGNED_BYTE,
        Short = GL_SHORT,
        UShort = GL_UNSIGNED_SHORT,
        Int = GL_INT,
        UInt = GL_UNSIGNED_INT,
        Float = GL_FLOAT
    };
}

static std::map<Vivid::ElementType,unsigned int> ElementTypeSizes = {
        { Vivid::ElementType::Byte,   sizeof(GLchar) },
        { Vivid::ElementType::UByte,  sizeof(GLbyte) },
        { Vivid::ElementType::Short,  sizeof(GLshort) },
        { Vivid::ElementType::UShort, sizeof(GLushort) },
        { Vivid::ElementType::Int,    sizeof(GLint) },
        { Vivid::ElementType::UInt,   sizeof(GLuint) },
        { Vivid::ElementType::Float,  sizeof(GLfloat) },
};

unsigned int inline sizeofElementType(const Vivid::ElementType e) {
    return ElementTypeSizes[e];
}

unsigned int inline GLElementType(const Vivid::ElementType e) {
    return static_cast<unsigned int>(e);
}

namespace Vivid {
    struct VertexBufferElement {
        ElementType type;
        unsigned int count;
        unsigned int handler;
        bool normalized;
    };
}

namespace Vivid {
    class Layout {
    private:
        std::vector<VertexBufferElement> m_Elements;
        unsigned int m_Stride;

    public:
        Layout() : m_Stride(0) {
        }

        ~Layout() = default;

        void push(unsigned int handler, unsigned int count);

        void push(ElementType type, unsigned int handler, unsigned int count);

        /*template<>
        void push<float>(unsigned int handler, unsigned int count) {
            m_Elements.push_back({
                GL_FLOAT, count, handler, false
            });

            m_Stride += VertexBufferElement::getSizeOfType(GL_FLOAT) * count;
        }

        template<>
        void push<unsigned int>(unsigned int handler, unsigned int count) {
            m_Elements.push_back({
                                         GL_UNSIGNED_INT, count, handler, false
                                 });

            m_Stride += VertexBufferElement::getSizeOfType(GL_UNSIGNED_INT) * count;
        }

        template<>
        void push<unsigned char>(unsigned int handler, unsigned int count) {
            m_Elements.push_back({
                                         GL_UNSIGNED_BYTE, count, handler,true
                                 });

            m_Stride += VertexBufferElement::getSizeOfType(GL_UNSIGNED_BYTE) * count;
        }*/

        inline const std::vector<VertexBufferElement> &getElements() const {
            return m_Elements;
        }

        inline unsigned int getStride() const {
            return m_Stride;
        }
    };
}


#endif //OPENGLESNATIVE_LAYOUT_H
