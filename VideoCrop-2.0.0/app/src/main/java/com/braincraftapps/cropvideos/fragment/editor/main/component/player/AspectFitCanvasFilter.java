/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: AspectFitCanvasFilter.java
 * @modified: Aug 21, 2024, 03:50 PM
 */

package com.braincraftapps.cropvideos.fragment.editor.main.component.player;

import android.graphics.PointF;

import com.braincraftapps.droid.mp4composition.filter.GlFilter;
import com.braincraftapps.droid.mp4composition.filter.GlTransformFilter;
import com.braincraftapps.droid.mp4composition.gl.GlFramebufferObject;

/**
 * This filter is responsible to fit the source data into currently active surface by preserving its (i.e. source) aspects.
 * Surface can be filled by tweaking a minimum units of scaling multiplier.
 * <p>
 * Created by TAPOS DATTA on 15,September,2020
 */

class AspectFitCanvasFilter extends GlFilter {

    private static final float TOLERANCE = 0.01f;

    private GlTransformFilter transformFilter;
    private float inputRatio = 0f;  // video or src ratio
    private int rotation = 0;
    private float zoomAmount = 0;
    private int outputWidth;
    private int outputHeight;
    private float surfaceRatio = 0f;
    private float changeZoomUnits = 1f;

    public AspectFitCanvasFilter() {
        transformFilter = new GlTransformFilter();
    }

    @Override
    public void setup() {
        transformFilter.setup();
        transformFilter.resetTransformation();
    }

    @Override
    public void release() {
        transformFilter.release();
    }

    /**
     * only usages for GPU rendering frame size
     *
     * @param width  active canvas with
     * @param height active canvas height
     */
    @Override
    public void setFrameSize(int width, int height) {
        this.outputHeight = height;
        this.outputWidth = width;
        this.surfaceRatio = (float) width / height;
        transformFilter.setFrameSize(width, height);
    }

    /**
     * @param inputRatio input ratio of source
     * @param rotation   rotation unit if needed
     */
    public void setInputRatio(float inputRatio, int rotation) {
        this.inputRatio = inputRatio;
        this.rotation = rotation;
    }

    public boolean isIdenticalAspectRatio() {
        return (Math.abs(surfaceRatio - inputRatio) <= TOLERANCE);
    }

    /**
     * @param zoomAmount amount should be in range from 0.0 to 1.0
     */
    public void setZoomAmount(float zoomAmount) {
        this.zoomAmount = zoomAmount;
    }

    @Override
    public void draw(int texName, GlFramebufferObject fbo) {
        updateScaleUnits();
        transformFilter.draw(texName, fbo);
    }

    private void updateScaleUnits() {
        PointF scale = getScaleWithAspect(outputWidth, outputHeight, inputRatio, rotation);
        changeZoomUnits = getFitRatio(scale, zoomAmount);
        transformFilter.resetTransformation();
        transformFilter.setScaleUnit(scale.x * changeZoomUnits, scale.y * changeZoomUnits);
    }

    /**
     * Calculate the minimum unit of scaling multiplier for which full canvas will be fitted.
     * If canvas ratio and video aspect ratio is same, is considered here as special case.
     *
     * @param scale      scaling parameters for aspect fill
     * @param zoomAmount zoom amount
     * @return minimum unit of scaling multiplier
     */
    private float getFitRatio(PointF scale, float zoomAmount) {
        float newLowerLimit = Math.min(scale.x, scale.y);
        float newUpperLimit = Math.max(scale.x, scale.y);
        float fitRatio = 1f;
        float valueInNewRange = 0;
        // check for the special case where surface and video ratio are equal
        newUpperLimit = isIdenticalAspectRatio() ? newUpperLimit * 2f : newUpperLimit;

        valueInNewRange = zoomAmount * (newUpperLimit - newLowerLimit) + newLowerLimit;
        valueInNewRange = newUpperLimit - valueInNewRange + 1;
        fitRatio = valueInNewRange / newUpperLimit;

        return fitRatio;
    }

    private PointF getScaleWithAspect(int widthOut, int heightOut, float inputRatio, int angle) {
        PointF scale = new PointF(1f, 1f);

        if (isIdenticalAspectRatio()) {
            return scale;
        }
        if (angle == 90 || angle == 270) {
            inputRatio = 1.0f / inputRatio;
        }
        float aspectRatioIn = inputRatio;  //frame aspect ratio
        int heightOutCalculated = (int) (widthOut / aspectRatioIn);

        if (heightOutCalculated <= heightOut) {
            scale.y = heightOut / (float) heightOutCalculated;
        } else {
            scale.x = widthOut / (heightOut * aspectRatioIn);
        }
        return scale;
    }
}
