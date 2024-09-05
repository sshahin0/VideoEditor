//
// Created by dev-lock on ১১/২/২৪.
//

#include "Shader.h"

namespace Vivid {
    void Shader::setup(const char *pVertexShaderSource, const char *pFragShaderSource) {
        release();

        mRenderId = GLUtils::CreateProgram(pVertexShaderSource, pFragShaderSource);
    }

    int Shader::getHandler(const std::string name) {
        if (handlerList.find(name) == handlerList.end()) {
            // not found
            // assign in list

            int location = getHandler(getProgramId(), name);
            if (location != -1) {
                handlerList[name] = location;
            }

            return location;
        } else {
            return handlerList[name];
        }
    }

    void Shader::bind() const {
        glUseProgram(mRenderId);
    }

    void Shader::unBind() const {
        glUseProgram(GL_NONE);
    }
}