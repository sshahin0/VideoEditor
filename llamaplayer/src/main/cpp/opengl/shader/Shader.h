//
// Created by dev-lock on ১১/২/২৪.
//

#ifndef OPENGLESNATIVE_SHADER_H
#define OPENGLESNATIVE_SHADER_H

#include "../util/GLUtils.h"
#include "../util/LogUtil.h"
#include <unordered_map>

namespace Vivid {
    class Shader {
    private:
        unsigned int mRenderId;

    protected:
        std::unordered_map<std::string, int> handlerList;

    public:
        Shader() : mRenderId(0) {
        }

        virtual ~Shader() {
            //release();
        }

        void release() {
            //unBind();
            glDeleteProgram(mRenderId);
            GLUtils::CheckGLError("delete program");
        }

        void setup(const char *pVertexShaderSource, const char *pFragShaderSource);

        int getHandler(const std::string name);

        void bind() const;

        void unBind() const;

        inline unsigned int getProgramId() const {
            return mRenderId;
        }

        void setBool(const std::string &name, bool value) {
            glUniform1i(getHandler(name), (int) value);
        }

        void setInt(const std::string &name, int value) {
            glUniform1i(getHandler(name), value);
        }

        void setFloat(const std::string &name, float value) {
            glUniform1f(getHandler(name), value);
        }

        void setVec2(const std::string &name, const glm::vec2 &value) {
            glUniform2fv(getHandler(name), 1, &value[0]);
        }

        void setVec2(const std::string &name, float x, float y) {
            glUniform2f(getHandler(name), x, y);
        }

        void setVec3(const std::string &name, const glm::vec3 &value) {
            glUniform3fv(getHandler(name), 1, &value[0]);
        }

        void setVec3(const std::string &name, float x, float y, float z) {
            glUniform3f(getHandler(name), x, y, z);
        }

        void setVec4(const std::string &name, const glm::vec4 &value) {
            glUniform4fv(getHandler(name), 1, &value[0]);
        }

        void setVec4(const std::string &name, float x, float y, float z, float w) {
            glUniform4f(getHandler(name), x, y, z, w);
        }

        void setMat2(const std::string &name, const glm::mat2 &mat) {
            glUniformMatrix2fv(getHandler(name), 1, GL_FALSE, &mat[0][0]);
        }

        void setMat3(const std::string &name, const glm::mat3 &mat) {
            glUniformMatrix3fv(getHandler(name), 1, GL_FALSE, &mat[0][0]);
        }

        void setMat4(const std::string &name, const glm::mat4 &mat) {
            glUniformMatrix4fv(getHandler(name), 1, GL_FALSE, &mat[0][0]);
        }

        inline int getHandler(GLuint programId, const std::string &name) {
            int location = -1;

            location = glGetAttribLocation(programId, name.c_str());

            if (location == -1) {
                location = glGetUniformLocation(programId, name.c_str());
            }

            if (location == -1) {
                // throw exception
            }

            return location;
        }
    };
}


#endif //OPENGLESNATIVE_SHADER_H
