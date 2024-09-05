//
// Created by dev-lock on ১১/২/২৪.
//

#include "GlFilter.h"
#include "../util/LogUtil.h"
#include "../util/GLUtils.h"

namespace Vivid {
    GlFilter::GlFilter() {
        this->vertexShader = std::string(DEFAULT_VERTEX_SHADER);
        this->fragmentShader = std::string(DEFAULT_FRAGMENT_SHADER);
        this->shader = std::make_shared<Shader>();
    }

    GlFilter::GlFilter(std::string &fragmentShaderSource) {
        this->vertexShader = std::string(DEFAULT_VERTEX_SHADER);
        this->fragmentShader = std::string(fragmentShaderSource);
        this->shader = std::make_shared<Shader>();
    }

    GlFilter::GlFilter(std::string &vertexShaderSource, std::string &fragmentShaderSource) {
        this->vertexShader = std::string(vertexShaderSource);
        this->fragmentShader = std::string(fragmentShaderSource);
        this->shader = std::make_shared<Shader>();
    }

    GlFilter::~GlFilter() {
        //release();
    }

    void GlFilter::setFragmentShader(std::string const &shaderSource) {
        this->fragmentShader = std::string(shaderSource);
    }

    void GlFilter::setVertexShader(std::string const &shaderSource) {
        this->vertexShader = std::string(shaderSource);
    }

    void GlFilter::setup() {
        release();

        shader->setup(this->vertexShader.c_str(), this->fragmentShader.c_str());

        vertexBuffer = std::make_shared<Vivid::VertexBuffer>(DEFAULT_VERTICES_COORDS, sizeof(DEFAULT_VERTICES_COORDS));
        indexBuffer = std::make_shared<Vivid::IndexBuffer>(INDICES, 6);

        vertexBuffer->unBind();
        indexBuffer->unBind();
    }

    void GlFilter::setFrameSize(int width, int height) {
        this->outputWidth = width;
        this->outputHeight = height;
    }

    void GlFilter::draw(int _texName, GlFrameBufferObject &fbo) {
        this->texName = _texName;

        initVertexParams();

        indexBuffer->bind();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texName);
        shader->setInt("sTexture_0", 0);

        onDraw();

        drawGlComponent();
    }

    void GlFilter::onDraw() {
    }

    void GlFilter::initVertexParams() {
        shader->bind();
        vertexBuffer->bind();

        Layout vertexBufferLayout;
        vertexBufferLayout.push(shader->getHandler("aPosition"), 3);
        vertexBufferLayout.push(shader->getHandler("aTextureCoord"), 2);

        int offset = 0;
        for (int i = 0; i < vertexBufferLayout.getElements().size(); i++) {
            const auto &element = vertexBufferLayout.getElements()[i];
            glEnableVertexAttribArray(element.handler);
            glVertexAttribPointer(element.handler, element.count, GLElementType(element.type),
                                  element.normalized, vertexBufferLayout.getStride(), (const void *) offset);

            offset += element.count * sizeofElementType(element.type);
        }
    }

    void GlFilter::drawGlComponent() {
        indexBuffer->bind();
        glDrawElements(GL_TRIANGLE_STRIP, indexBuffer->getCount(), GL_UNSIGNED_INT,
                       (const void *) 0);

        Layout vertexBufferLayout;
        vertexBufferLayout.push(shader->getHandler("aPosition"), 3);
        vertexBufferLayout.push(shader->getHandler("aTextureCoord"), 2);

        for (int i = 0; i < vertexBufferLayout.getElements().size(); i++) {
            const auto &element = vertexBufferLayout.getElements()[i];
            glDisableVertexAttribArray(element.handler);
        }

        glBindTexture(GL_TEXTURE_2D, GL_NONE);
        vertexBuffer->unBind();
        indexBuffer->unBind();
        shader->unBind();
    }

    void GlFilter::release() {
        releaseShader();
        releaseBuffer();
    }
}