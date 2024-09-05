//
// Created by dev-lock on ২০/২/২৪.
//

#include "GlFilterGroup.h"
#include "../util/LogUtil.h"

namespace Vivid {
    GlFilterGroup::GlFilterGroup() : activeIndex(0) {
        fboObj[activeIndex] = GlFrameBufferObject();
    }

    GlFilterGroup::~GlFilterGroup() {
    }

    void GlFilterGroup::addGlFilter(const Vivid::Ref<GlFilter> filter) {
        filterList.push_back(filter);
    }

    void GlFilterGroup::setup() {
        release();

        GlFilter::setup();

        activeIndex = 0;
        fboObj[activeIndex].release();

        for (auto &filter: filterList) {
            mergedList.push_back(filter);
        }

        for (auto &filter: mergedList) {
            filter->setup();
        }
    }

    void GlFilterGroup::setFrameSize(int width, int height) {
        fboObj[0].setUp(width, height);

        for (auto filter: mergedList) {
            filter->setFrameSize(width, height);
        }

        GlFilter::setFrameSize(width, height);
    }

    void GlFilterGroup::draw(int texName, GlFrameBufferObject &fbo) {
        activeIndex = 1;
        fboObj[activeIndex] = fbo;

        prevTexName = texName;

        for (auto &filter: mergedList) {
            fboObj[activeIndex].enable();
            glClear(GL_COLOR_BUFFER_BIT);
            filter->draw(prevTexName, fboObj[activeIndex]);
            fboObj[activeIndex].disable();

            prevTexName = fboObj[activeIndex].getTexName();
            activeIndex = (activeIndex + 1) % 2; // 0, 1, 0
        }
        // rendering buffer check for output renderBuffer
        if (fboObj[activeIndex].getTexName() == fbo.getTexName()) {
            fbo.enable();
            glClear(GL_COLOR_BUFFER_BIT);
            GlFilter::draw(prevTexName, fbo);
        }
    }

    void GlFilterGroup::release() {

        for (auto filter: mergedList) {
            filter->release();
        }

        mergedList.clear();

        fboObj[0].release();

        GlFilter::release();
    }
}