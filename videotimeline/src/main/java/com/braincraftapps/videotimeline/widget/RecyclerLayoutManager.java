package com.braincraftapps.videotimeline.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

public class RecyclerLayoutManager extends LinearLayoutManager {

    private boolean scrollEnabled = true;

    public RecyclerLayoutManager(Context context) {
        super(context);
    }

    public RecyclerLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public RecyclerLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        this.scrollEnabled = scrollEnabled;
    }

    public boolean isScrollEnabled() {
        return scrollEnabled;
    }

    @Override
    public boolean canScrollVertically() {
        return scrollEnabled && super.canScrollVertically();
    }

    @Override
    public boolean canScrollHorizontally() {
        return scrollEnabled && super.canScrollHorizontally();
    }
}
