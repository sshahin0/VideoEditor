//
// Created by dev-lock on ১১/২/২৪.
//

#include "Texture.h"
#include "../util/GLUtils.h"
#include "../util/LogUtil.h"

namespace Vivid {
    Texture::Texture(): mType(TextureType::_2D) {
    }

    Texture::Texture(TextureType textureType) : mType{textureType} {
    }

    Texture::~Texture() {
        release();
    }

    void Texture::setup() {
        release();

        glGenTextures(1, &m_renderId);
        glBindTexture(GLTextureType(mType), m_renderId);
        GLUtils::setupSampler(GLTextureType(mType), GL_LINEAR, GL_LINEAR);
        glBindTexture(GLTextureType(mType), GL_NONE);
    }

    void Texture::loadFrame(TextureParams &params, void *pixels) {
        this->mWidth = params.width;
        this->mHeight = params.height;

        glTexImage2D(GLTextureType(mType), 0, TextureParamsUtil::getInternalFormat(params.format)
                     , this->mWidth, this->mHeight, 0
                     , TextureParamsUtil::getPixelFormat(params.format)
                     , TextureParamsUtil::getPixelType(params.format),
                     pixels);
    }

    void Texture::bind(unsigned int slot) {
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GLTextureType(mType), m_renderId);
    }

    void Texture::unbind() {
        glBindTexture(GLTextureType(mType), GL_NONE);
    }

    void Texture::release() {
        unbind();
        glDeleteTextures(1, &m_renderId);
    }
}