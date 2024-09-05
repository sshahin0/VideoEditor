//
// Created by dev-lock on ১৩/২/২৪.
//

#include "GlTransformFilter.h"
#include "../util/LogUtil.h"
#include "../../render/VideoGLRender.h"
#include <gtc/matrix_transform.hpp>

#define MATH_PI 3.1415926535897932384626433832802

namespace Vivid {
    GlTransformFilter::GlTransformFilter() : mInputHeight{0}, mInputWidth{0},
                                                     GlFilter() {

        setVertexShader(VERTEX_SHADER);
        setFragmentShader(FRAGMENT_SHADER);

        m_othoGraphicMatrix = glm::ortho(-1.0f, 1.0f, -1.0f, 1.0f, 3.0f, 7.0f);
        glm::mat4 View = glm::lookAt(
                glm::vec3(0.0f, 0.0f, 3.0f), // Camera is at (0,0,1), in World Space
                glm::vec3(0.0f, 0.0f, 0.0f), // and looks at the origin
                glm::vec3(0.0f, 1.0f, 0.0f)  // Head is up (set to 0,-1,0 to look upside-down)
        );
        m_othoGraphicMatrix = m_othoGraphicMatrix * View;

        m_transformMatrix = glm::identity<float>();
    }

    GlTransformFilter::~GlTransformFilter() {
        //release();
    }

    void GlTransformFilter::setup() {
        GlFilter::setup();

        resetTransformation();
    }

    void GlTransformFilter::onDraw() {
        GLUtils::setMat4(shader->getProgramId(), "u_MVPMatrix", m_othoGraphicMatrix);
        GLUtils::setMat4(shader->getProgramId(), "transformMatrix", m_transformMatrix);
        GLUtils::setVec4(shader->getProgramId(), "borderColor", glm::vec4(1.0f, 1.0f, 0.0f, 0.0f));
    }

    void GlTransformFilter::release() {
        mInputWidth = 0;
        mInputHeight = 0;

        GlFilter::release();
    }

    void GlTransformFilter::setBorderColor(float r, float g, float b, float a) {
        this->borderColor = glm::vec4(r, g, b, a);
    }

    void GlTransformFilter::setTranslateOffset(float offsetX, float offsetY) {
        glm::mat4 translateM = glm::identity<float>();
        translateM = glm::translate(translateM, glm::vec3(offsetX, -offsetY, 1.0f));

        m_transformMatrix = m_transformMatrix * translateM;
    }

    void GlTransformFilter::setScaleUnit(float scaleX, float scaleY) {
        glm::mat4 scaleM = glm::identity<float>();
        scaleM = glm::scale(scaleM, glm::vec3(scaleX, scaleY, 1.0f));

        m_transformMatrix = m_transformMatrix * scaleM;
    }

    void GlTransformFilter::setRotateInAngle(float angle) {
        glm::mat4 rotationM = glm::identity<float>();
        float radAngle = static_cast<float>(MATH_PI / 180.0f * angle);

        rotationM[0][0] = (float) cos(radAngle);
        rotationM[0][1] = (float) sin(radAngle) * -1.0f;
        rotationM[1][0] = (float) sin(radAngle) * 1.0f;
        rotationM[1][1] = (float) cos(radAngle);

        glm::mat4 scaleM = glm::identity<float>();
        scaleM[0][0] = this->aspectRatio;

        glm::mat4 scaleMInv = glm::identity<float>();
        scaleMInv[0][0] = 1.0f / this->aspectRatio;

        rotationM = scaleMInv * rotationM;
        rotationM = rotationM * scaleM;

        m_transformMatrix = m_transformMatrix * rotationM;
    }

    void GlTransformFilter::resetTransformation() {
        m_transformMatrix = glm::identity<float>();
    }

    void GlTransformFilter::setFrameSize(int width, int height) {
        GlFilter::setFrameSize(width, height);

        if(width > 0 && height > 0) {
            this->aspectRatio = (width * 1.0f) / height;
        }
    }
}