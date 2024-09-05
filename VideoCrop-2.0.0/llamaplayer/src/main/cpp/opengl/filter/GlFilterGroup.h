//
// Created by dev-lock on ২০/২/২৪.
//

#ifndef OPENGLESNATIVE_GLFILTERGROUP_H
#define OPENGLESNATIVE_GLFILTERGROUP_H

#include "GlFilter.h"
#include "../fbo/GlFrameBufferObject.h"
#include "../Core.h"
#include <vector>

namespace Vivid {
    class GlFilterGroup : public GlFilter {
    protected:
        std::vector<Vivid::Ref<GlFilter>> filterList;
        std::vector<Vivid::Ref<GlFilter>> mergedList;

        int activeIndex;
        GlFrameBufferObject fboObj[2];

        int prevTexName = GL_NONE;

    public:
        GlFilterGroup();

        virtual ~GlFilterGroup();

        virtual void addGlFilter(const Vivid::Ref<GlFilter> filter);

        void setup() override;

        void setFrameSize(int width, int height) override;

        void draw(int texName, GlFrameBufferObject &fbo) override;

        void release() override;

    };
}

#endif //OPENGLESNATIVE_GLFILTERGROUP_H
