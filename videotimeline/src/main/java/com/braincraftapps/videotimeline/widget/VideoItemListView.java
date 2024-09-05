package com.braincraftapps.videotimeline.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.braincraftapps.videotimeline.R;
import com.braincraftapps.videotimeline.VideoItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoItemListView extends RecyclerView implements VideoItemListViewAdapter.ItemSelectionChangeListener {

    private static final String TAG = VideoItemListView.class.getSimpleName();
    public VideoItemListViewAdapter adapter;
    private RecyclerLayoutManager layoutManager;
    private ItemTouchHelper itemTouchHelper;
    private MyItemAnimator myItemAnimator;

    /*
     * centerPoint must be > 0f && <= 1f
     */
    private float centerPoint = 0.5f;

    private int videoItemWidth;
    private int joinerItemWidth;

    private List<VideoItem> videoItemList = new ArrayList<>();
    private Progress progress = new Progress();
    private int currentScrollValue;
    private boolean updateProgressValue = true;

    private List<ProgressListener> progressListeners = new ArrayList<>();
    private List<ScrollStateChangeListener> scrollStateChangeListeners = new ArrayList<>();
    private List<DragSwapListener> dragSwapListeners = new ArrayList<>();
    private ItemSelectionChangeListener itemSelectionChangeListener;
    private TouchListener touchListener;

    public VideoItemListView(@NonNull Context context) {
        super(context);
        init();
    }

    public VideoItemListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoItemListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        videoItemWidth = getResources().getDimensionPixelSize(R.dimen.video_item_width);
        joinerItemWidth = getResources().getDimensionPixelSize(R.dimen.videoitemlistview_joineritem_width);

        layoutManager = new RecyclerLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        setLayoutManager(layoutManager);

        adapter = new VideoItemListViewAdapter(getContext());
        adapter.setItemSelectionChangeListener(this);
        setAdapter(adapter);

        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(this);

        addOnScrollListener(scrollListener);

        myItemAnimator = new MyItemAnimator();
        setItemAnimator(myItemAnimator);

        setFocusableInTouchMode(true);
        requestFocus();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int sw = getCenterValue();
        adapter.setStartViewWidth(sw);
        adapter.setEndViewWidth(w - sw);
        adapter.notifyDataSetChanged();
    }

    WholeScreenTouchView wholeScreenTouchView;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewGroup root = getContainerActivityRootView();
        if (root != null && wholeScreenTouchView == null) {
            wholeScreenTouchView = new WholeScreenTouchView(getContext());
            root.addView(wholeScreenTouchView);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        ViewGroup root = getContainerActivityRootView();
        if (root != null && wholeScreenTouchView != null) {
            //root.removeView(wholeScreenTouchView);
            wholeScreenTouchView = null;
        }
        super.onDetachedFromWindow();
    }

    private ViewGroup getContainerActivityRootView() {
        Context context = getContext();
        if (context instanceof Activity) {
            View root = ((Activity) context).getWindow().getDecorView().getRootView();
            if (root instanceof ViewGroup) return (ViewGroup) root;
        }
        return null;
    }

    private class WholeScreenTouchView extends View {

        private int color;

        public WholeScreenTouchView(Context context) {
            super(context);
            setAlpha(0f);
            color = context.getResources().getColor(R.color.whole_screen_touch_color);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            View itemListView = VideoItemListView.this;

            int[] itemListViewLocation = new int[2];
            itemListView.getLocationOnScreen(itemListViewLocation);

            int[] location = new int[2];
            getLocationOnScreen(location);

            int l = itemListViewLocation[0] - location[0];
            int t = itemListViewLocation[1] - location[1];
            int r = l + itemListView.getWidth();
            int b = t + itemListView.getHeight();

            Path itemListViewPath = new Path();
            itemListViewPath.addRect(l, t, r, b, Path.Direction.CW);

            Path path = new Path();
            path.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
            path.op(itemListViewPath, Path.Op.DIFFERENCE);

            Paint paint = new Paint();
            paint.setColor(color);

            //canvas.clipRect(l, t, r, b);
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            if (adapter.isVideoItemSelected()) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    boolean handleTouch = true;
                    final int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
                    final int lastItemPosition = layoutManager.findLastVisibleItemPosition();

                    for (int i = firstItemPosition; i <= lastItemPosition; i++) {
                        ViewHolder holder = findViewHolderForAdapterPosition(i);
                        if (holder != null && (adapter.isItemVideo(i) || adapter.isItemJoiner(i))) {
                            View itemView = holder.itemView;

                            int[] location = new int[2];

                            itemView.getLocationOnScreen(location);
                            RectF itemViewRect = new RectF(location[0], location[1],
                                    location[0] + itemView.getWidth(), location[1] + itemView.getHeight());

                            getLocationOnScreen(location);
                            if (itemViewRect.contains(location[0] + event.getX(), location[1] + event.getY())) {
                                handleTouch = false;
                                break;
                            }
                        }
                    }
                    if (handleTouch) return true;

                    /*boolean handleTouch = true;
                    View view = VideoItemListView.this;

                    int[] location = new int[2];

                    view.getLocationOnScreen(location);
                    RectF itemViewRect = new RectF(location[0], location[1],
                            location[0] + view.getWidth(), location[1] + view.getHeight());

                    getLocationOnScreen(location);
                    if(itemViewRect.contains(location[0] + event.getX(), location[1] + event.getY())){
                        handleTouch = false;
                    }

                    if(handleTouch) return true;*/

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    adapter.clearVideoItemSelection();
                    return true;
                }
            }

            return super.dispatchTouchEvent(event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && adapter.isVideoItemSelected()) {
            adapter.clearVideoItemSelection();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (touchListener != null) touchListener.onTouch(e);
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // disabling scrolling while any item is selected
        //if(adapter.isVideoItemSelected() && e.getAction() == MotionEvent.ACTION_MOVE) return true;
        //if(touchListener != null) touchListener.onTouch(e);
        return super.onTouchEvent(e);
    }


    @Override
    public void onItemSelectionChanged(int itemIndex, boolean selected) {
        if (wholeScreenTouchView != null) {
            wholeScreenTouchView.invalidate();
            wholeScreenTouchView.animate().alpha(selected ? 1f : 0f).setDuration(150).start();
        }

        if (itemSelectionChangeListener != null) {
            itemSelectionChangeListener.onItemSelectionChanged(itemIndex, selected);
        }
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            // dragging = 1, settling = 2, idle = 0
            if (newState == SCROLL_STATE_DRAGGING) updateProgressValue = true;
            for (ScrollStateChangeListener listener : scrollStateChangeListeners) {
                listener.onScrollStateChanged(VideoItemListView.this, newState);
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            progressWork();
        }
    };

    private class MyItemAnimator extends DefaultItemAnimator {

        int movePosition = -1;
        ItemAnimator.ItemHolderInfo moveItemHolderInfo;

        @Override
        public boolean animateAppearance(@NonNull ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
            if (viewHolder.getAdapterPosition() == movePosition) {
                if (moveItemHolderInfo != null && (moveItemHolderInfo.left != postLayoutInfo.left
                        || moveItemHolderInfo.top != postLayoutInfo.top)) {
                    return animateMove(viewHolder, moveItemHolderInfo.left, moveItemHolderInfo.top,
                            postLayoutInfo.left, postLayoutInfo.top);
                }
            }
            return super.animateAppearance(viewHolder, preLayoutInfo, postLayoutInfo);
        }

        @Override
        public boolean animateAdd(ViewHolder holder) {
            if (movePosition >= 0) return false;
            else return super.animateAdd(holder);
        }
    }

    private class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

        public ItemTouchHelperCallback() {
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            final int dragFlags;
            if (!adapter.isVideoItemSelected() && adapter.isItemVideo(viewHolder.getAdapterPosition())) {
                dragFlags = ItemTouchHelper.START | ItemTouchHelper.END |
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            } else {
                dragFlags = 0;
            }
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            if (adapter.isItemVideo(toPosition)) {
                int fromIndex = adapter.getVideoItemIndexForAdapterPosition(fromPosition);
                int toIndex = adapter.getVideoItemIndexForAdapterPosition(toPosition);

                for (DragSwapListener l : dragSwapListeners) {
                    l.onSwap(VideoItemListView.this, fromIndex, toIndex);
                }

                ItemAnimator.ItemHolderInfo itemHolderInfo = new ItemAnimator.ItemHolderInfo();
                itemHolderInfo.setFrom(target);
                myItemAnimator.moveItemHolderInfo = itemHolderInfo;
                myItemAnimator.movePosition = fromPosition;

                Collections.swap(videoItemList, fromIndex, toIndex);
                adapter.notifyItemMoved(fromPosition, toPosition);

                for (DragSwapListener l : dragSwapListeners) {
                    l.onSwapped(VideoItemListView.this, fromIndex, toIndex);
                }

                return true;

            } else {
                return false;
            }
        }

        @Override
        public int interpolateOutOfBoundsScroll(@NonNull @NotNull RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
            /*final int direction = (int) Math.signum(viewSizeOutOfBounds);
            return 15 * direction; //22*/
            return super.interpolateOutOfBoundsScroll(recyclerView, viewSize, viewSizeOutOfBounds, totalSize, msSinceStartScroll);
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                int position = viewHolder.getAdapterPosition();
                if (adapter.isItemVideo(position)) {
                    viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                    int itemIndex = adapter.getVideoItemIndexForAdapterPosition(position);
                    for (DragSwapListener l : dragSwapListeners) {
                        l.onDragStarted(VideoItemListView.this, itemIndex);
                    }
                }
            }
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            myItemAnimator.movePosition = -1;
            myItemAnimator.moveItemHolderInfo = null;

            int position = viewHolder.getBindingAdapterPosition();
            if (adapter.isItemVideo(position)) {
                int itemIndex = adapter.getVideoItemIndexForAdapterPosition(position);
                for (DragSwapListener l : dragSwapListeners) {
                    l.onDragReleased(VideoItemListView.this, itemIndex);
                }
            }
        }
    }

    private void update() {
        if (videoItemList.size() < 1) {
            progress = new Progress();
            currentScrollValue = 0;
            return;
        }

        final int[] locationOnScreen = getLocationOnScreen();
        final int checkingXOnScreen = locationOnScreen[0] + getCenterValue();

        final int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
        final int lastItemPosition = layoutManager.findLastVisibleItemPosition();

        for (int i = firstItemPosition; i <= lastItemPosition; i++) {
            RecyclerView.ViewHolder viewHolder = findViewHolderForAdapterPosition(i);
            if (viewHolder == null) continue;

            View itemView = viewHolder.itemView;
            final int[] itemViewLocationOnScreen = getLocationOnScreen(itemView);
            final int itemViewXOnScreen = itemViewLocationOnScreen[0];
            final int itemViewEndXOnScreen = itemViewXOnScreen + itemView.getWidth();

            if (checkingXOnScreen >= itemViewXOnScreen &&
                    checkingXOnScreen <= itemViewEndXOnScreen) {

                final int currentItemPassedPixels = checkingXOnScreen - itemViewXOnScreen;

                if (updateProgressValue) {
                    if (adapter.isItemVideo(i)) {
                        int itemIndex = adapter.getVideoItemIndexForAdapterPosition(i);
                        VideoItem item = videoItemList.get(itemIndex);

                        long progressUs = Math.round((currentItemPassedPixels / (double) videoItemWidth) * item.getActiveDurationUs());
                        long currentPositionUs = item.getActiveStartPositionUs() + progressUs;
                        currentPositionUs = clampVideoItemCurrentPosition(item, currentPositionUs);

                        progress.videoIndex = itemIndex;
                        progress.currentVideoPositionUs = currentPositionUs;

                    } else if (i < 2) {
                        progress.videoIndex = 0;
                        progress.currentVideoPositionUs = videoItemList.get(0).getActiveStartPositionUs();

                    } else if (i >= adapter.getItemCount() - 2) {
                        progress.videoIndex = videoItemList.size() - 1;
                        progress.currentVideoPositionUs = videoItemList.get(progress.videoIndex).getActiveEndPositionUs();

                    } else {
                        if (currentItemPassedPixels > joinerItemWidth / 2) {
                            progress.videoIndex = adapter.getVideoItemIndexForAdapterPosition(i + 1);
                            progress.currentVideoPositionUs = videoItemList.get(progress.videoIndex).getActiveStartPositionUs();
                        } else {
                            progress.videoIndex = adapter.getVideoItemIndexForAdapterPosition(i - 1);
                            progress.currentVideoPositionUs = videoItemList.get(progress.videoIndex).getActiveEndPositionUs();
                        }
                    }
                }

                if (i <= 1) currentScrollValue = 0;
                else
                    currentScrollValue = getScrollValueUntilAdapterPosition(i) + currentItemPassedPixels;

                break;
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void notifyDataSetChanged(@Nullable Runnable taskAfterDataSetChanging) {
        removeOnScrollListener(scrollListener);
        if (getScrollState() != SCROLL_STATE_IDLE) {
            stopScroll();
        }
        adapter.notifyDataSetChanged();
        post(() -> {
            updateProgressValue = true;
            progressWork();
            addOnScrollListener(scrollListener);
            if (taskAfterDataSetChanging != null) taskAfterDataSetChanging.run();
        });
    }


    /*.............private utility methods.............*/

    private int getCenterValue() {
        return Math.round(getWidth() * centerPoint);
    }

    private void progressWork() {
        update();
        //invalidate();
        for (ProgressListener listener : progressListeners) {
            listener.onProgress(VideoItemListView.this, getProgress());
        }
    }

    private int getScrollValueUntilAdapterPosition(int adapterPosition) {
        if (adapterPosition <= 2) return 0;

        int nVideos = adapter.getVideoItemCountTillAdapterPosition(adapterPosition - 1);
        int videoItemsWidth = videoItemWidth * nVideos;

        int nJoiners = adapter.getJoinerItemCountTillAdapterPosition(adapterPosition - 1) - 1;
        int joinerItemsWidth = joinerItemWidth * nJoiners;

        return videoItemsWidth + joinerItemsWidth;
    }

    private long clampVideoItemCurrentPosition(VideoItem item, long currentVideoPositionUs) {
        currentVideoPositionUs = Math.min(currentVideoPositionUs, item.getActiveEndPositionUs());
        currentVideoPositionUs = Math.max(currentVideoPositionUs, item.getActiveStartPositionUs());
        return currentVideoPositionUs;
    }

    private int getXOnScreen() {
        return getXOnScreen(this);
    }

    private int[] getLocationOnScreen() {
        return getLocationOnScreen(this);
    }

    private int getXOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[0];
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


    /*..........General public setters and getters................*/

    /*
     * This method must be called immediately after instance creation
     */
    public void setCenterPoint(float centerPoint) {
        if (centerPoint <= 0f || centerPoint > 1f) {
            throw new IllegalArgumentException("centerPoint must be > 0f && <= 1f.");
        }
        this.centerPoint = centerPoint;
    }

    public void setVideoItemList(List<VideoItem> videoItemList) {
        this.videoItemList = videoItemList;
        adapter.setVideoItemList(this.videoItemList);
    }

    public void setProgress(int videoIndex) {
        setProgress(videoIndex, false);
    }

    public void setProgress(int videoIndex, boolean withAnimation) {
        if (videoItemList.size() < 1) return;
        setProgress(videoIndex, videoItemList.get(videoIndex).getActiveStartPositionUs(), withAnimation);
    }

    /*public void setProgress(int videoIndex, long currentVideoPositionUs){
        if(videoItemList.size() < 1) return;

        currentVideoPositionUs = clampVideoItemCurrentPosition(videoItemList.get(videoIndex), currentVideoPositionUs);

        progress.videoIndex = videoIndex;
        progress.currentVideoPositionUs = currentVideoPositionUs;

        updateProgressValue = false;
        int adapterPosition = adapter.getAdapterPositionForVideoItemIndex(videoIndex);
        int offset = getWidth()/2 - getScrollValue(videoIndex, currentVideoPositionUs);
        layoutManager.scrollToPositionWithOffset(adapterPosition, offset);
        updateProgressValue = true;
    }*/

    /*public void setProgress(int videoIndex, long currentVideoPositionUs){
        if(videoItemList.size() < 1) return;

        currentVideoPositionUs = clampVideoItemCurrentPosition(videoItemList.get(videoIndex), currentVideoPositionUs);

        progress.videoIndex = videoIndex;
        progress.currentVideoPositionUs = currentVideoPositionUs;

        updateProgressValue = false;
        int toScroll = getScrollValueTill(videoIndex, currentVideoPositionUs);
        Log.d(TAG, "setProgress: "+joinerItemWidth+"  "+videoItemWidth+"  "+currentScrollValue+"  "+toScroll);
        scrollBy(toScroll - currentScrollValue, 0);
        Log.d(TAG, "setProgress: xxx"+currentScrollValue);
        updateProgressValue = true;
    }*/

    public void setProgress(int videoIndex, long currentVideoPositionUs) {
        setProgress(videoIndex, currentVideoPositionUs, false);
    }

    public void setProgress(int videoIndex, long currentVideoPositionUs, boolean withAnimation) {
        if (videoItemList.size() < 1) return;

        currentVideoPositionUs = clampVideoItemCurrentPosition(videoItemList.get(videoIndex), currentVideoPositionUs);

        progress.videoIndex = videoIndex;
        progress.currentVideoPositionUs = currentVideoPositionUs;

        updateProgressValue = false;
        int dx = getScrollValueTill(videoIndex, currentVideoPositionUs) - currentScrollValue;
        if (dx != 0) {
            if (withAnimation) smoothScrollBy(dx, 0);
            else scrollBy(dx, 0);

        } else {
            progressWork();
        }
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

    public int getCurrentScrollValue() {
        return currentScrollValue;
    }

    /*public int getScrollValue(int videoIndex, long currentVideoPositionUs){
        if(videoItemList.size() < 1) return 0;

        VideoItem item = videoItemList.get(videoIndex);
        currentVideoPositionUs = clampVideoItemCurrentPosition(item, currentVideoPositionUs);

        double progress = (currentVideoPositionUs - item.getActiveStartPositionUs()) /
                (double) item.getActiveDurationUs();
        progress = Math.max(0f, Math.min(progress, 1f));
        return (int)Math.round(progress * videoItemWidth);
    }*/

    public int getScrollValueTill(int videoIndex, long currentVideoPositionUs) {
        if (videoItemList.size() < 1) return 0;

        VideoItem item = videoItemList.get(videoIndex);
        currentVideoPositionUs = clampVideoItemCurrentPosition(item, currentVideoPositionUs);

        double progress = (currentVideoPositionUs - item.getActiveStartPositionUs()) /
                (double) item.getActiveDurationUs();
        progress = Math.max(0f, Math.min(progress, 1f));

        int adapterPosition = adapter.getAdapterPositionForVideoItemIndex(videoIndex);
        return getScrollValueUntilAdapterPosition(adapterPosition) + (int) Math.round(progress * videoItemWidth);
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

    public void addDragSwapListener(DragSwapListener dragSwapListener) {
        if (dragSwapListener != null) {
            dragSwapListeners.add(dragSwapListener);
        }
    }

    public void removeDragSwapListener(DragSwapListener dragSwapListener) {
        if (dragSwapListener != null) {
            dragSwapListeners.remove(dragSwapListener);
        }
    }

    public void removeAllDragSwapListeners() {
        dragSwapListeners.clear();
    }

    public void setClicksListener(ClicksListener clicksListener) {
        adapter.setClicksListener(clicksListener);
    }

    public void setItemSelectionChangeListener(ItemSelectionChangeListener itemSelectionChangeListener) {
        this.itemSelectionChangeListener = itemSelectionChangeListener;
    }

    public void setTouchListener(TouchListener touchListener) {
        this.touchListener = touchListener;
    }

    /*public void removeAllListeners(){
        progressListeners.clear();
        scrollStateChangeListeners.clear();
        dragSwapListeners.clear();
        itemJoinerClickListeners.clear();
    }*/


    /*......... Public Inner classes and Interfaces..........*/

    public static class Progress {
        public int videoIndex;
        public long currentVideoPositionUs;
    }

    public interface ProgressListener {
        void onProgress(VideoItemListView videoItemListView, Progress progress);
    }

    public interface ScrollStateChangeListener {
        void onScrollStateChanged(VideoItemListView videoItemListView, int newState);
    }

    public interface DragSwapListener {
        void onDragStarted(VideoItemListView videoItemListView, int itemIndex);

        void onSwap(VideoItemListView videoItemListView, int fromIndex, int toIndex);

        void onSwapped(VideoItemListView videoItemListView, int fromIndex, int toIndex);

        void onDragReleased(VideoItemListView videoItemListView, int itemIndex);
    }

    public interface ClicksListener {
        void onLeftAddClicked(int itemIndex);

        void onRightAddClicked(int itemIndex);

        void onTransitionClicked(int firstItemIndex, int secondItemIndex);

        void onEditClicked(int itemIndex);

        void onReplaceClicked(int itemIndex);

        void onDeleteClicked(int itemIndex);
    }

    public interface ItemSelectionChangeListener {
        void onItemSelectionChanged(int itemIndex, boolean selected);
    }

    public interface TouchListener {
        void onTouch(MotionEvent event);
    }

}
