//
// Created by dev-lock on ২৮/৫/২৪.
//

#include "GlFrameInputFilter.h"

namespace Vivid {
    GlFrameInputFilter::GlFrameInputFilter(): GlTransformFilter() {

        GLfloat coord[20] = {
                // X, Y, Z, U, V
                -1.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                -1.0f, -1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, -1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f, 0.0f
        };

        memcpy(DEFAULT_VERTICES_COORDS, coord, sizeof(coord));

        setFragmentShader(FRAGMENT_SHADER);

        for(auto & i : texture) {
            i = Texture();
        }
    }

    GlFrameInputFilter::~GlFrameInputFilter() {
    }

    void GlFrameInputFilter::setInput(NativeImage *pImage) {

        {

            //std::lock_guard<std::mutex> lock(*m_mutex);

            if (pImage == nullptr || pImage->ppPlane[0] == nullptr)
                return;

            int oldWidth = mInputWidth;
            int oldHeight = mInputHeight;

            mInputWidth = pImage->width;
            mInputHeight = pImage->height;

            textureParams.width = mInputWidth;
            textureParams.height = mInputHeight;
            textureParams.format = pImage->format;

            if (pImage->width != ni.width || pImage->height != ni.height) {
                NativeImageUtil::FreeNativeImage(&ni);

                memset(&ni, 0, sizeof(NativeImage));
                ni.format = pImage->format;
                ni.width = pImage->width;
                ni.height = pImage->height;
                ni.pts = pImage->pts;
                ni.rotation = pImage->rotation;
                NativeImageUtil::AllocNativeImage(&ni);
            }

            NativeImageUtil::CopyNativeImage(pImage, &ni);
        }
    }

    void GlFrameInputFilter::freeInput() {
        NativeImageUtil::FreeNativeImage(&ni);

        mInputWidth = 0;
        mInputHeight = 0;
    }

    void GlFrameInputFilter::setFrameSize(int width, int height) {
        GlTransformFilter::setFrameSize(width, height);
        this->aspectRatio = 1.0f;

        _fbo.setUp(width, height);
    }

    void GlFrameInputFilter::setup() {
        GlTransformFilter::setup();

        for (int i = 0; i < TEXTURE_NUM; i++) {
            texture[i].setup();
        }
    }

    void GlFrameInputFilter::draw(int texName, GlFrameBufferObject &fbo) {
        if (ni.ppPlane[0] == nullptr) return;

        _fbo.enable();

        glClear(GL_COLOR_BUFFER_BIT);

        {
            //std::lock_guard<std::mutex> lock(*m_mutex);

            texture[0].bind(0);
            textureParams.width = ni.width;
            textureParams.height = ni.height;
            texture[0].loadFrame(textureParams, ni.ppPlane[0]);
            texture[0].unbind();

            if (textureParams.format == IMAGE_FORMAT_NV12 || textureParams.format == IMAGE_FORMAT_NV21) {
                textureParams.width = mInputWidth >> 1;
                textureParams.height = mInputHeight >> 1;
                texture[1].bind(1);
                texture[1].loadFrame(textureParams, ni.ppPlane[1]);
                texture[1].unbind();
            } else if (textureParams.format == IMAGE_FORMAT_I420) {

                textureParams.width = ni.width >> 1;
                textureParams.height = ni.height >> 1;
                texture[1].bind(1);
                texture[1].loadFrame(textureParams, ni.ppPlane[1]);
                texture[1].unbind();

                textureParams.width = ni.width >> 1;
                textureParams.height = ni.height >> 1;
                texture[2].bind(2);
                texture[2].loadFrame(textureParams, ni.ppPlane[2]);
                texture[2].unbind();
            }

            resetTransformation();

            setScaleUnit(1.0f, 1.0f);
            setRotateInAngle(ni.rotation);
            setTranslateOffset(0.0f, 0.0f);
        }

        _fbo.disable();

        fbo.enable();
        glClear(GL_COLOR_BUFFER_BIT);
        GlTransformFilter::draw(_fbo.getTexName(), fbo);
        fbo.disable();
    }

    void GlFrameInputFilter::onDraw() {
        GlTransformFilter::onDraw();

        for (int i = 0; i < TEXTURE_NUM; i++) {
            texture[i].bind(i);
            char samplerName[64] = {0};
            sprintf(samplerName, "s_texture%d", i);
            shader->setInt(samplerName, i);
        }
        shader->setInt("u_nImgType", textureParams.format);
    }

    void GlFrameInputFilter::release() {

        for (int i = 0; i < TEXTURE_NUM; i++) texture[i].release();

        freeInput();

        GlTransformFilter::release();
    }
}