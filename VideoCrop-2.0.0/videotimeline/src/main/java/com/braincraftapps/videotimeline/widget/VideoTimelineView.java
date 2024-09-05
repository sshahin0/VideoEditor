package com.braincraftapps.videotimeline.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.braincraftapps.videotimeline.FramePosition;
import com.braincraftapps.videotimeline.FrameView;
import com.braincraftapps.videotimeline.R;
import com.braincraftapps.videotimeline.VideoItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VideoTimelineView extends FrameLayout {
    private static final String TAG = VideoTimelineView.class.getSimpleName();

    public static final int SCROLL_STATE_DRAGGING = RecyclerView.SCROLL_STATE_DRAGGING;
    public static final int SCROLL_STATE_SETTLING = RecyclerView.SCROLL_STATE_SETTLING;
    public static final int SCROLL_STATE_IDLE = RecyclerView.SCROLL_STATE_IDLE;

    private List<VideoItem> videoItemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerLayoutManager layoutManager;
    private VideoTimelineViewAdapter videoTimelineViewAdapter;

    private List<ItemJoiner> itemJoiners = new ArrayList<>();
    private int itemJoinerSize;

    private int desiredPixelsPerSecond;
    private int desiredFrameItemPixels;
    private int minPixelsForLastFrameItem;

    private Progress progress;

    /*
     * centerPoint must be > 0f && <= 1f
     */
    private float centerPoint = 0.5f;

    private List<ProgressListener> progressListeners = new ArrayList<>();
    private List<ScrollStateChangeListener> scrollStateChangeListeners = new ArrayList<>();
    private List<ItemJoinerClickListener> itemJoinerClickListeners = new ArrayList<>();
    private List<ItemClickListener> itemClickListeners = new ArrayList<>();
    private TouchListener touchListener;
    private boolean itemJoinerVisibility = true;

    public VideoTimelineView(@NonNull Context context) {
        super(context);
        init();
    }

    public VideoTimelineView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoTimelineView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public VideoTimelineView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        desiredPixelsPerSecond = (int) convertDpToPx(80);
        desiredFrameItemPixels = (int) convertDpToPx(60);
        minPixelsForLastFrameItem = (int) convertDpToPx(10);

        itemJoinerSize = (int) convertDpToPx(25);
        progress = new Progress();

        recyclerView = new RecyclerView(getContext());
        addView(recyclerView);

        layoutManager = new RecyclerLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        videoTimelineViewAdapter = new VideoTimelineViewAdapter(getContext(), videoItemList);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoTimelineViewAdapter);
        recyclerView.addOnScrollListener(scrollStateChangeListener);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int sw = getCenterValue();
        videoTimelineViewAdapter.setStartViewWidth(sw);
        videoTimelineViewAdapter.setEndViewWidth(w - sw);
        notifyDataSetChanged(null);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (touchListener != null) touchListener.onTouch(ev);
        performTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        //drawing the item joiners
        if (itemJoinerVisibility) {
            for (ItemJoiner i : itemJoiners) {
                i.draw(canvas);
            }
        }
    }

    private PointF downTouchPoint;

    private void performTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downTouchPoint = new PointF(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_UP:
                boolean itemJoinerClicked = false;
                for (int i = 0; i < itemJoiners.size(); i++) {
                    ItemJoiner joiner = itemJoiners.get(i);
                    if (joiner.bounds.contains((int) event.getX(), (int) event.getY())) {
                        joiner.performClick();
                        itemJoinerClicked = true;
                        break;
                    }
                }

                if (!itemJoinerClicked) {
                    determineItemClick(event.getX(), event.getY());
                }

                break;
        }
    }

    private RecyclerView.OnScrollListener scrollStateChangeListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            // dragging = 1, settling = 2, idle = 0
            if (newState == SCROLL_STATE_DRAGGING) {
                recyclerView.removeOnScrollListener(scrollListener);
                recyclerView.addOnScrollListener(scrollListener);
            } else if (newState == SCROLL_STATE_IDLE) {
                recyclerView.removeOnScrollListener(scrollListener);
            }

            for (ScrollStateChangeListener listener : scrollStateChangeListeners) {
                listener.onScrollStateChanged(VideoTimelineView.this, newState);
            }
        }
    };

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            progress = new Progress();
            calculateProgress();
            updateAfterProgress();
            delegateProgressListeners();
        }
    };

    private boolean determineItemClick(float touchX, float touchY) {
        final int[] locationOnScreen = getLocationOnScreen();
        touchX = locationOnScreen[0] + touchX;
        touchY = locationOnScreen[1] + touchY;

        final int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
        final int lastItemPosition = layoutManager.findLastVisibleItemPosition();

        for (int i = firstItemPosition; i <= lastItemPosition; i++) {
            FramePosition fp = videoTimelineViewAdapter.getFramePositionForAdapterPosition(i);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);

            if (viewHolder != null && fp != null) {
                final int videoIndex = fp.getVideoIndexInVideoItemList();
                final int frameIndex = fp.getFrameIndexInVideoItem();
                final VideoItem videoItem = videoItemList.get(videoIndex);

                final int[] itemViewLocationOnScreen = getLocationOnScreen(viewHolder.itemView);
                float l = itemViewLocationOnScreen[0];
                float t = itemViewLocationOnScreen[1];
                float r = l + viewHolder.itemView.getWidth();
                float b = t + viewHolder.itemView.getHeight();
                if (new RectF(l, t, r, b).contains(touchX, touchY)) {
                    for (ItemClickListener listener : itemClickListeners) {
                        listener.onItemClicked(videoIndex);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void calculateProgress() {
        final int[] locationOnScreen = getLocationOnScreen();
        final int checkingXOnScreen = locationOnScreen[0] + getCenterValue();

        final int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
        final int lastItemPosition = layoutManager.findLastVisibleItemPosition();

        // determining progress
        for (int i = firstItemPosition; i <= lastItemPosition; i++) {
            FramePosition fp = videoTimelineViewAdapter.getFramePositionForAdapterPosition(i);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);

            if (viewHolder != null && fp != null) {
                final int videoIndex = fp.getVideoIndexInVideoItemList();
                final int frameIndex = fp.getFrameIndexInVideoItem();
                final VideoItem videoItem = videoItemList.get(videoIndex);

                final int[] itemViewLocationOnScreen = getLocationOnScreen(viewHolder.itemView);
                final int itemViewXOnScreen = itemViewLocationOnScreen[0];
                final int itemViewEndXOnScreen = itemViewXOnScreen + viewHolder.itemView.getWidth();

                if (checkingXOnScreen >= itemViewXOnScreen && checkingXOnScreen <= itemViewEndXOnScreen) {

                    final int videoItemScrolledPixels;
                    if (frameIndex == 0) {
                        videoItemScrolledPixels = checkingXOnScreen - itemViewXOnScreen;
                    } else {
                        videoItemScrolledPixels = videoItem.getRequiredPixelsTillFrameItemIndex(frameIndex - 1)
                                + checkingXOnScreen - itemViewXOnScreen;
                    }

                    // determining video progress
                    final long videoItemProgress = videoItem.getUsForPixels(videoItemScrolledPixels);
                    long position = videoItem.getActiveStartPositionUs() + videoItemProgress;
                    position = clampVideoItemCurrentPositionUs(videoItem, position);

                    progress.videoIndex = videoIndex;
                    progress.currentVideoPositionUs = position;

                    break;
                }
            }
        }

        videoTimelineViewAdapter.setSelectedVideoIndex(progress.videoIndex);
    }

    private void updateAfterProgress() {
        final int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
        final int lastItemPosition = layoutManager.findLastVisibleItemPosition();

        final int[] locationOnScreen = getLocationOnScreen();

        // Clearing item joiners
        for (ItemJoiner joiner : itemJoiners) {
            joiner.stopLoadingImage();
        }
        itemJoiners.clear();

        for (int i = firstItemPosition; i <= lastItemPosition; i++) {
            FramePosition fp = videoTimelineViewAdapter.getFramePositionForAdapterPosition(i);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);

            if (viewHolder != null && fp != null) {
                final int videoIndex = fp.getVideoIndexInVideoItemList();
                final int frameIndex = fp.getFrameIndexInVideoItem();

                View itemView = viewHolder.itemView;
                final int[] itemViewLocationOnScreen = getLocationOnScreen(itemView);

                // deciding item joiners
                if (videoIndex != 0 && frameIndex == 0) {
                    int l = itemViewLocationOnScreen[0] - itemJoinerSize / 2;
                    int t = itemViewLocationOnScreen[1] + itemView.getHeight() / 2 - itemJoinerSize / 2;
                    l = l - locationOnScreen[0];
                    t = t - locationOnScreen[1];
                    int r = l + itemJoinerSize;
                    int b = t + itemJoinerSize;

                    VideoItem videoItem = videoItemList.get(videoIndex - 1);
                    ItemJoiner joiner = new ItemJoiner(
                            videoIndex - 1,
                            videoIndex,
                            new Rect(l, t, r, b),
                            videoItem.getJoinerIconResId(),
                            videoItem.getJoinerIconUri());
                    itemJoiners.add(joiner);
                }
                videoTimelineViewAdapter.makeFrameViewSelectedOrUnselected(
                        (VideoTimelineViewAdapter.GeneralViewHolder) viewHolder, videoIndex, frameIndex);
            }
        }

        // Setting item joiners
        int nJoiner = itemJoiners.size();
        for (int i = 0; i < nJoiner; i++) {
            ItemJoiner joiner = itemJoiners.get(i);
            joiner.loadImage();
        }
        invalidate();
    }


    private void delegateProgressListeners() {
        for (ProgressListener listener : progressListeners) {
            listener.onProgress(this, getProgress());
        }
    }

    private void applyChangesToVideoItems() {
        for (VideoItem videoItem : videoItemList) {
            applyChangesToVideoItem(videoItem);
        }
    }

    private void applyChangesToVideoItem(VideoItem videoItem) {
        videoItem.applyChanges(
                desiredPixelsPerSecond,
                desiredFrameItemPixels,
                minPixelsForLastFrameItem);
    }

    public void notifyDataSetChanged() {
        notifyDataSetChanged(null);
    }

    public void notifyDataSetChanged(@Nullable Runnable taskAfterDataSetChanging) {
        for (VideoItem videoItem : videoItemList) {
            if (videoItem.isUpdateNeeded()) {
                applyChangesToVideoItem(videoItem);
            }
        }
        videoTimelineViewAdapter.notifyDataSetChanged();
        progress = new Progress();
        recyclerView.post(() -> {
            calculateProgress();
            updateAfterProgress();
            delegateProgressListeners();
            if (taskAfterDataSetChanging != null) taskAfterDataSetChanging.run();
        });
    }

    public void scrollBy(int x) {
        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.scrollBy(x, 0);
        recyclerView.removeOnScrollListener(scrollListener);
    }

    public void stopScroll() {
        recyclerView.stopScroll();
    }

    public void setItemJoinerVisibility(boolean visibility) {
        this.itemJoinerVisibility = visibility;
    }



    /*.............private utility methods.............*/

    private int getCenterValue() {
        return Math.round(getWidth() * centerPoint);
    }

    private long clampVideoItemCurrentPositionUs(VideoItem item, long currentVideoPositionUs) {
        currentVideoPositionUs = Math.min(currentVideoPositionUs, item.getActiveEndPositionUs());
        currentVideoPositionUs = Math.max(currentVideoPositionUs, item.getActiveStartPositionUs());
        return currentVideoPositionUs;
    }

    private int[] getLocationOnScreen() {
        return getLocationOnScreen(this);
    }

    private int[] getLocationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location;
    }

    private float convertDpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }


    /*..........General public setters and getters...............*/

    /*
     * This method must be called immediately after instance creation
     */
    public void setCenterPoint(float centerPoint) {
        if (centerPoint <= 0f || centerPoint > 1f) {
            throw new IllegalArgumentException("centerPoint must be > 0f && <= 1f.");
        }
        this.centerPoint = centerPoint;
    }

    public void setProgress(int videoIndex) {
        if (videoItemList.size() < 1) return;
        setProgress(videoIndex, videoItemList.get(videoIndex).getActiveStartPositionUs());
    }

    public void setProgress(int videoIndex, long currentVideoPositionUs) {
        if (videoItemList.size() < 1) return;

        VideoItem videoItem = videoItemList.get(videoIndex);
        currentVideoPositionUs = clampVideoItemCurrentPositionUs(videoItem, currentVideoPositionUs);

        progress.videoIndex = videoIndex;
        progress.currentVideoPositionUs = currentVideoPositionUs;
        videoTimelineViewAdapter.setSelectedVideoIndex(progress.videoIndex);

        long progressUs = currentVideoPositionUs - videoItem.getActiveStartPositionUs();
        double progress = progressUs / (double) videoItem.getActiveDurationUs(); // 0.0 to 1.0

        int frameIndex = (int) Math.floor(progress * videoItem.getNumberOfFrameItems()) - 1;
        frameIndex = Math.max(0, Math.min(frameIndex, videoItem.getLastFrameItemIndex()));

        final int a = videoItem.getPixelsForUs(progressUs);
        final int b = frameIndex > 0 ? videoItem.getRequiredPixelsTillFrameItemIndex(frameIndex - 1) : 0;

        final int adapterPosition = videoTimelineViewAdapter.getAdapterPositionForFramePosition(
                new FramePosition(videoIndex, frameIndex));
        final int offset = a - b;

        layoutManager.scrollToPositionWithOffset(adapterPosition, getCenterValue() - offset);
        recyclerView.post(this::updateAfterProgress);
        delegateProgressListeners();
    }

    public void setDesiredPixelsPerSecond(int desiredPixelsPerSecond) {
        this.desiredPixelsPerSecond = desiredPixelsPerSecond;
        applyChangesToVideoItems();
    }

    public void setDesiredFrameItemPixels(int desiredFrameItemPixels) {
        this.desiredFrameItemPixels = desiredFrameItemPixels;
        applyChangesToVideoItems();
    }

    public void setMinPixelsForLastFrameItem(int minPixelsForLastFrameItem) {
        this.minPixelsForLastFrameItem = minPixelsForLastFrameItem;
        applyChangesToVideoItems();
    }

    public void setPixels(int desiredPixelsPerSecond,
                          int desiredFrameItemPixels,
                          int minPixelsForLastFrameItem) {
        this.desiredPixelsPerSecond = desiredPixelsPerSecond;
        this.desiredFrameItemPixels = desiredFrameItemPixels;
        this.minPixelsForLastFrameItem = minPixelsForLastFrameItem;
        applyChangesToVideoItems();
    }

    public void setScrollEnabled(boolean enabled) {
        layoutManager.setScrollEnabled(enabled);
    }


    public float getCenterPoint() {
        return centerPoint;
    }

    public List<VideoItem> getVideoItemList() {
        return videoItemList;
    }

    public Progress getProgress() {
        return progress;
    }

    public int getCurrentScrolledValue() {
        return getTotalRequiredPixelsTill(progress.videoIndex, progress.currentVideoPositionUs);
    }

    public long getFinalVideoProgressUsTill(int videoIndex, long currentVideoPositionUs) {
        if (videoItemList.size() < 1) return 0;

        VideoItem videoItem = videoItemList.get(videoIndex);
        currentVideoPositionUs = clampVideoItemCurrentPositionUs(videoItem, currentVideoPositionUs);
        long progressUs = currentVideoPositionUs - videoItem.getActiveStartPositionUs();

        long totalProgressUs = videoItem.getDurationUsForPositionChangeUs(progressUs);
        for (int i = 0; i < videoIndex; i++) {
            VideoItem item = videoItemList.get(i);
            totalProgressUs += item.getDurationUsForPositionChangeUs(item.getActiveDurationUs());
        }

        return totalProgressUs;
    }

    public long getFinalVideoProgressUs() {
        return getFinalVideoProgressUsTill(getProgress().videoIndex,
                getProgress().currentVideoPositionUs);
    }

    public long getFinalVideoDurationUs() {
        long totalProgressUs = 0;
        for (int i = 0; i < videoItemList.size(); i++) {
            VideoItem item = videoItemList.get(i);
            totalProgressUs += item.getDurationUsForPositionChangeUs(item.getActiveDurationUs());
        }
        return totalProgressUs;
    }

    public int getTotalRequiredPixelsTill(int videoIndex, long currentVideoPositionUs) {
        if (videoItemList.size() < 1) return 0;

        VideoItem videoItem = videoItemList.get(videoIndex);
        currentVideoPositionUs = clampVideoItemCurrentPositionUs(videoItem, currentVideoPositionUs);
        long progressUs = currentVideoPositionUs - videoItem.getActiveStartPositionUs();

        int requiredPixels = videoItem.getPixelsForUs(progressUs);
        for (int i = 0; i < videoIndex; i++) {
            requiredPixels += videoItemList.get(i).getTotalRequiredPixels();
        }
        return requiredPixels;
    }

    public int getTotalRequiredPixels() {
        int totalPixels = 0;
        for (int i = 0; i < videoItemList.size(); i++) {
            VideoItem item = videoItemList.get(i);
            totalPixels += item.getTotalRequiredPixels();
        }
        return totalPixels;
    }

    public FrameView.Params getFrameViewParams() {
        return videoTimelineViewAdapter.getFrameViewParams();
    }

    public int getScrollState() {
        return recyclerView.getScrollState();
    }

    public int getDesiredPixelsPerSecond() {
        return desiredPixelsPerSecond;
    }

    public int getDesiredFrameItemPixels() {
        return desiredFrameItemPixels;
    }

    public int getMinPixelsForLastFrameItem() {
        return minPixelsForLastFrameItem;
    }

    public int getItemJoinerSize() {
        return itemJoinerSize;
    }

    public boolean isScrollEnabled() {
        return layoutManager.isScrollEnabled();
    }


    /*..........Adding/Removing listeners...........*/

    public void addProgressListener(ProgressListener progressListener) {
        if (progressListener != null) {
            progressListeners.add(progressListener);
        }
    }

    public void removeProgressListener(ProgressListener progressListener) {
        if (progressListener != null) {
            progressListeners.remove(progressListener);
        }
    }

    public void removeAllProgressListeners() {
        progressListeners.clear();
    }

    public void addScrollStateChangeListener(ScrollStateChangeListener scrollStateChangeListener) {
        if (scrollStateChangeListener != null) {
            scrollStateChangeListeners.add(scrollStateChangeListener);
        }
    }

    public void removeScrollStateChangeListener(ScrollStateChangeListener scrollStateChangeListener) {
        if (scrollStateChangeListener != null) {
            scrollStateChangeListeners.remove(scrollStateChangeListener);
        }
    }

    public void removeAllScrollStateChangeListeners() {
        scrollStateChangeListeners.clear();
    }

    public void addItemJoinerClickListener(ItemJoinerClickListener itemJoinerClickListener) {
        if (itemJoinerClickListener != null) {
            itemJoinerClickListeners.add(itemJoinerClickListener);
        }
    }

    public void removeItemJoinerClickListener(ItemJoinerClickListener itemJoinerClickListener) {
        if (itemJoinerClickListener != null) {
            itemJoinerClickListeners.remove(itemJoinerClickListener);
        }
    }

    public void removeAllItemJoinerClickListeners() {
        itemJoinerClickListeners.clear();
    }

    public void addItemClickListener(ItemClickListener itemClickListener) {
        if (itemClickListener != null) {
            itemClickListeners.add(itemClickListener);
        }
    }

    public void removeItemClickListener(ItemClickListener itemClickListener) {
        if (itemClickListener != null) {
            itemClickListeners.remove(itemClickListener);
        }
    }

    public void removeAllItemClickListeners() {
        itemClickListeners.clear();
    }

    public void setTouchListener(TouchListener touchListener) {
        this.touchListener = touchListener;
    }

    /*public void removeAllListeners(){
        progressListeners.clear();
        scrollStateChangeListeners.clear();
        itemJoinerClickListeners.clear();
    }*/




    /*......... Inner classes and Interfaces..........*/

    public static class Progress {
        public int videoIndex;
        public long currentVideoPositionUs;

        @Override
        public String toString() {
            return "Progress{" +
                    "videoIndex=" + videoIndex +
                    ", currentVideoPositionUs=" + currentVideoPositionUs +
                    '}';
        }
    }

    public static class PixelProgress {
        //private int videoIndex;
        //private int videoProgressPixels;
        public int totalProgressPixels;
    }

    private class ItemJoiner {
        final int itemPosition1;
        final int itemPosition2;
        final Rect bounds;
        final Integer iconResId;
        final Uri iconUri;
        float roundRectOffset;
        float iconSrcOffset;
        float bgCornerRadius;
        Bitmap iconBitmap;
        CustomTarget<Bitmap> iconTarget;
        Paint paint = new Paint();
        private int bgColor;
        private RectF bgRoundRect;
        private RectF iconDesRect;
        private int tintColor;

        public ItemJoiner(int itemPosition1,
                          int itemPosition2,
                          Rect bounds,
                          Integer iconResId,
                          Uri iconUri) {
            this.itemPosition1 = itemPosition1;
            this.itemPosition2 = itemPosition2;
            this.bounds = bounds;
            this.iconResId = iconResId;
            this.iconUri = iconUri;
            initPaintProperties();
        }

        private void initPaintProperties() {
            roundRectOffset = convertDpToPx(2);
            iconSrcOffset = convertDpToPx(7);
            bgCornerRadius = convertDpToPx(4);

            bgColor = Color.parseColor("#f2f2f2");
            tintColor = Color.parseColor("#808080");

            bgRoundRect = new RectF(
                    bounds.left + roundRectOffset,
                    bounds.top + roundRectOffset,
                    bounds.right - roundRectOffset,
                    bounds.bottom - roundRectOffset
            );
            iconDesRect = new RectF(
                    bounds.left + iconSrcOffset,
                    bounds.top + iconSrcOffset,
                    bounds.right - iconSrcOffset,
                    bounds.bottom - iconSrcOffset
            );
        }

        public void draw(Canvas canvas) {
            if (iconBitmap != null) {
                paint.reset();
                paint.setColor(bgColor);
                canvas.drawRoundRect(bgRoundRect, bgCornerRadius, bgCornerRadius, paint);

                paint.reset();
                paint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(iconBitmap, null, iconDesRect, paint);
            }
        }

        public void performClick() {
            for (ItemJoinerClickListener l : itemJoinerClickListeners) {
                l.onItemJoinerClicked(itemPosition1, itemPosition2);
            }
        }

        void loadImage() {
            iconTarget = new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    iconBitmap = resource;
                    invalidate();
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            };

            Context context = getContext();
            if (isContextValid(context)) {
                Glide.with(context)
                        .asBitmap()
                        .load(iconUri != null ? iconUri :
                                iconResId != null ? iconResId : R.drawable.ic_item_joiner_default)
                        .override(bounds.width(), bounds.height())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(iconTarget);
            }
        }

        void stopLoadingImage() {
            if (iconTarget == null) return;
            Context context = getContext();
            if (isContextValid(context)) {
                Glide.with(context).clear(iconTarget);
            }
        }

        private boolean isContextValid(Context context) {
            if (context == null) {
                return false;
            } else if (context instanceof Activity) {
                final Activity activity = (Activity) context;
                return !activity.isFinishing() && !activity.isDestroyed();
            }
            return true;
        }
    }

    public interface ProgressListener {
        void onProgress(VideoTimelineView videoTimelineView, Progress progress);
    }

    public interface ScrollStateChangeListener {
        void onScrollStateChanged(VideoTimelineView videoTimelineView, int newState);
    }

    public interface ItemJoinerClickListener {
        void onItemJoinerClicked(int itemPosition1, int itemPosition2);
    }

    public interface TouchListener {
        void onTouch(MotionEvent event);
    }

    public interface ItemClickListener {
        void onItemClicked(int itemIndex);
    }
}
