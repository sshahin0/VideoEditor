package com.braincraftapps.videotimeline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

public class FrameView extends View {

    public FrameView(Context context) {
        super(context);
        init();
    }

    public FrameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FrameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FrameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();

        final int frameBackgroundColor = params.frameBackgroundColor;
        final float cornerRadius = params.cornerRadius;
        final int borderColor = params.borderColor;
        final float borderWidth = params.borderWidth;
        final int overlayColor = params.overlayColor;
        final float overlayAlpha = params.overlayAlpha;

        // clipping rounded shape
        float[] radii = new float[8];
        if(firstView){
            // top-left
            radii[0] = cornerRadius;
            radii[1] = cornerRadius;

            // bottom-left
            radii[6] = cornerRadius;
            radii[7] = cornerRadius;
        }

        if(lastView){
            // top-right
            radii[2] = cornerRadius;
            radii[3] = cornerRadius;

            // bottom-right
            radii[4] = cornerRadius;
            radii[5] = cornerRadius;
        }
        path.reset();
        path.addRoundRect(0, 0, width, height, radii, Path.Direction.CW);
        canvas.clipPath(path);


        // drawing frame background
        canvas.drawColor(frameBackgroundColor);


        // drawing frame
        if (frame != null && !frame.isRecycled()){
            final float viewRatio = (float) width / (float) height;
            final float frameRatio = (float) frame.getWidth() / (float) frame.getHeight();

            final float w, h;
            if (viewRatio < frameRatio) {
                h = height;
                w = frame.getWidth() * (h / (float)frame.getHeight());
            } else {
                w = width;
                h = frame.getHeight() * (w / (float)frame.getWidth());
            }

            float l = width/2.0f - w/2.0f;
            float t = height/2.0f - h/2.0f;
            float r = l + w;
            float b = t + h;
            canvas.drawBitmap(frame, null, new RectF(l, t, r, b), null);
        }


        if(!selectedFrame) return;

        // drawing overlay
        paint.reset();
        paint.setColor(overlayColor);
        paint.setAlpha((int)(overlayAlpha * 255));
        canvas.drawRect(0, 0, width, height, paint);


        // drawing border
        path.reset();
        float borderHalf = borderWidth/2.0f;
        if(firstView && !lastView){
            path.moveTo(width, 0 + borderHalf);
            path.lineTo(0 + borderHalf, 0 + borderHalf);
            path.lineTo(0 + borderHalf, height - borderHalf);
            path.lineTo(width, height - borderHalf);

        }else if (lastView && !firstView){
            path.moveTo(0, 0 + borderHalf);
            path.lineTo(width - borderHalf, 0 + borderHalf);
            path.lineTo(width - borderHalf, height - borderHalf);
            path.lineTo(0, height - borderHalf);

        }else if(firstView && lastView){
            path.moveTo(0 + borderHalf, 0 + borderHalf);
            path.lineTo(width - borderHalf, 0 + borderHalf);
            path.lineTo(width - borderHalf, height - borderHalf);
            path.lineTo(0 + borderHalf, height - borderHalf);
            path.close();

        }else{
            path.moveTo(0, 0 + borderHalf);
            path.lineTo(width, 0 + borderHalf);
            path.moveTo(0, height - borderHalf);
            path.lineTo(width, height - borderHalf);
        }
        paint.reset();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(borderColor);
        paint.setStrokeWidth(borderWidth);
        paint.setPathEffect(new CornerPathEffect(cornerRadius));
        canvas.drawPath(path, paint);
    }

    private void init(){
        frame = null;
        firstView = false;
        lastView = false;
        selectedFrame = false;

        params = new Params(getContext());

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        path = new Path();
    }

    private Bitmap frame;
    private boolean firstView;
    private boolean lastView;
    private boolean selectedFrame;

    private Params params;

    private Paint paint;
    private Path path;

    /*................ Setters & Getters.................*/
    public void setFrame(Bitmap frame) {
        this.frame = frame;
        invalidate();
    }

    public void setParams(Params params) {
        this.params = params;
        invalidate();
    }

    public void setFirstView(boolean firstView) {
        this.firstView = firstView;
        invalidate();
    }

    public void setLastView(boolean lastView) {
        this.lastView = lastView;
        invalidate();
    }

    public void setSelectedFrame(boolean selectedFrame) {
        this.selectedFrame = selectedFrame;
        invalidate();
    }

    public Bitmap getFrame() {
        return frame;
    }

    public Params getParams() {
        return params;
    }

    public boolean isFirstView() {
        return firstView;
    }

    public boolean isLastView() {
        return lastView;
    }

    public boolean isSelectedFrame() {
        return selectedFrame;
    }


    /*................ Inner classes .................*/
    public static class Params {

        public int frameBackgroundColor;
        public float cornerRadius;
        public int borderColor;
        public float borderWidth;
        public int overlayColor;
        public float overlayAlpha;

        public Params(Context context){
            frameBackgroundColor = Color.TRANSPARENT;
            cornerRadius = convertDpToPx(context, 4f);
            borderColor = Color.WHITE;
            borderWidth = convertDpToPx(context, 1.5f);
            overlayColor = Color.BLACK;
            overlayAlpha = 0.3f;
        }

        private float convertDpToPx(Context context, float dp){
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    context.getResources().getDisplayMetrics()
            );
        }

        @Override
        public String toString() {
            return "Params{" +
                    "frameBackgroundColor=" + frameBackgroundColor +
                    ", cornerRadius=" + cornerRadius +
                    ", borderColor=" + borderColor +
                    ", borderWidth=" + borderWidth +
                    ", overlayColor=" + overlayColor +
                    ", overlayAlpha=" + overlayAlpha +
                    '}';
        }
    }
}
