//
// Created by dev-lock on ১১/২/২৪.
//

#ifndef OPENGLESNATIVE_TEXTURE_H
#define OPENGLESNATIVE_TEXTURE_H

#include <GLES2/gl2.h>
#include "TextureParams.h"

namespace Vivid {
    enum class TextureType : unsigned int {
        _2D = GL_TEXTURE_2D,
        //_3D = GL_TEXTURE_3D,
    };
}

inline unsigned int GLTextureType(const Vivid::TextureType t) {
    return static_cast<unsigned int>(t);
}

namespace Vivid {
    class Texture {
    private:
        unsigned int m_renderId;
        TextureType mType;
        int mWidth, mHeight;

    public:
        Texture();

        Texture(TextureType textureType);

        ~Texture();

        void setup();

        void loadFrame(TextureParams &params, void *pixels);

        void bind(unsigned int slot = 0);

        void unbind();

        void release();

        inline int getWidth() const { return mWidth; }

        inline int getHeight() const { return mHeight; }

        inline unsigned int getTexName() {
            return m_renderId;
        }
    };
}

#endif //OPENGLESNATIVE_TEXTURE_H
