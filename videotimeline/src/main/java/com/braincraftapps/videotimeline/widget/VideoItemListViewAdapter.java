package com.braincraftapps.videotimeline.widget;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.braincraftapps.videotimeline.R;
import com.braincraftapps.videotimeline.Util;
import com.braincraftapps.videotimeline.VideoItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

class VideoItemListViewAdapter extends RecyclerView.Adapter<VideoItemListViewAdapter.ViewHolder>{
    private static final String TAG = VideoItemListViewAdapter.class.getSimpleName();

    public static final int INVALID_INDEX = -1;

    public static final int VIEW_TYPE_VIDEO = 0;
    public static final int VIEW_TYPE_JOINER = 1;
    public static final int VIEW_TYPE_START = 2;
    public static final int VIEW_TYPE_END = 3;

    private Context context;
    private List<VideoItem> videoItemList = new ArrayList<>();
    private int startViewWidth = ViewGroup.LayoutParams.MATCH_PARENT;
    private int endViewWidth = ViewGroup.LayoutParams.MATCH_PARENT;

    private int selectedItemIndex = INVALID_INDEX;

    private VideoItemListView.ClicksListener clicksListener;
    private ItemSelectionChangeListener itemSelectionChangeListener;

    VideoItemListViewAdapter(Context context) {
        this.context = context;
    }

    public void setVideoItemList(List<VideoItem> videoItemList) {
        this.videoItemList = videoItemList;
    }

    public void setStartViewWidth(int startViewWidth) {
        this.startViewWidth = startViewWidth;
    }

    public void setEndViewWidth(int endViewWidth) {
        this.endViewWidth = endViewWidth;
    }

    public void setClicksListener(VideoItemListView.ClicksListener clicksListener) {
        this.clicksListener = clicksListener;
    }

    public void setItemSelectionChangeListener(ItemSelectionChangeListener itemSelectionChangeListener) {
        this.itemSelectionChangeListener = itemSelectionChangeListener;
    }

    public int getSelectedVideoItemIndex() {
        return selectedItemIndex;
    }

    public boolean isVideoItemSelected(){
        return selectedItemIndex != INVALID_INDEX;
    }

    public void clearVideoItemSelection(){
        if(isVideoItemSelected()){
            selectedItemIndex = INVALID_INDEX;
            if(itemSelectionChangeListener != null){
                itemSelectionChangeListener.onItemSelectionChanged(selectedItemIndex, false);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        int count = videoItemList.size();
        if(count > 0) count += count + 1; // adding joiner views
        count += 2;  // adding 2 for the start and ending views
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) { // first view
            return VIEW_TYPE_START;
        } else if (position == getItemCount() - 1) { // last view
            return VIEW_TYPE_END;
        } else if(isItemVideo(position)){
            return VIEW_TYPE_VIDEO;
        }else{
            return VIEW_TYPE_JOINER;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_START) {
            return new StartViewHolder(inflater.
                    inflate(R.layout.timeline_item_startview, parent, false));

        } else if (viewType == VIEW_TYPE_END) {
            return new EndViewHolder(inflater.
                    inflate(R.layout.timeline_item_endview, parent, false));

        } else if (viewType == VIEW_TYPE_JOINER){
            return new JoinerViewHolder(inflater.
                    inflate(R.layout.videoitemlistview_item_joiner, parent, false));

        }else{
            return new VideoViewHolder(inflater.
                    inflate(R.layout.videoitemlistview_item_video, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int itemCount = getItemCount();
        final int joinerItemWidth = context.getResources().getDimensionPixelSize(R.dimen.videoitemlistview_joineritem_width);

        if (position == 0) {// first view
            holder.itemView.getLayoutParams().width = startViewWidth - joinerItemWidth;

        } else if (position == itemCount - 1) {// last view
            holder.itemView.getLayoutParams().width = endViewWidth - joinerItemWidth;

        } else {
            if(isItemVideo(position)){
                final int itemIndex = getVideoItemIndexForAdapterPosition(position);
                VideoItem videoItem = videoItemList.get(itemIndex);
                VideoViewHolder viewHolder = (VideoViewHolder) holder;

                viewHolder.durationText.setText(Util.convertTimeFromUsToHhMmSs(videoItem.getFinalVideoDurationUs()));
                viewHolder.loadTypeIcon(R.drawable.ic_itemtype_video);
                int imageSize = context.getResources().getDimensionPixelSize(R.dimen.video_item_width);
                viewHolder.loadThumb(videoItem.getVideoUri(), imageSize);

                if(itemIndex == selectedItemIndex){
                    viewHolder.addEditOptionsView();
                }else{
                    viewHolder.removeEditOptionsView();
                }

            }else{ // Joiner Item

                JoinerViewHolder viewHolder = (JoinerViewHolder) holder;
                boolean visible = true;

                if(isAddItem(position)){
                    viewHolder.loadIcon(R.drawable.item_add_icon);
                }else{
                    if(position == 1 || position == itemCount - 2) visible = false;
                    else{
                        VideoItem item = videoItemList.get(getVideoItemIndexForAdapterPosition(position - 1));
                        viewHolder.loadIcon(item.getJoinerIconResId());
                    }
                }
                viewHolder.itemView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof VideoViewHolder) {
            ((VideoViewHolder) holder).stopLoadingAll();
        }

        if (holder instanceof JoinerViewHolder) {
            ((JoinerViewHolder) holder).stopLoadingIcon();
        }
    }

    public boolean isItemVideo(int adapterPosition){
        int itemCount = getItemCount();
        if (adapterPosition == 0 || adapterPosition == itemCount - 1) return false;
        return adapterPosition % 2 == 0;
    }

    public int getVideoItemIndexForAdapterPosition(int adapterPosition){
        return isItemVideo(adapterPosition) ? adapterPosition / 2 - 1 : INVALID_INDEX;
    }

    public int getAdapterPositionForVideoItemIndex(int itemIndex){
        return itemIndex * 2 + 2;
    }

    public boolean isAddItem(int adapterPosition){
        if(!isVideoItemSelected()) return false;
        int selectedItemAdapterPosition = getAdapterPositionForVideoItemIndex(selectedItemIndex);
        return adapterPosition == selectedItemAdapterPosition - 1 || adapterPosition == selectedItemAdapterPosition + 1;
    }

    public boolean isItemJoiner(int adapterPosition){
        return adapterPosition != 0 && adapterPosition != getItemCount() -1 && !isItemVideo(adapterPosition);
    }

    public int getVideoItemCountTillAdapterPosition(int adapterPosition){
        if(adapterPosition >= 2){
            int count = adapterPosition/2;
            if(count > videoItemList.size()) count = videoItemList.size();
            return count;
        }
        else return 0;
    }

    public int getJoinerItemCountTillAdapterPosition(int adapterPosition){
        return (int)(adapterPosition/2f + 0.5f);
    }


    /*...................Inner classes...................*/

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        protected void loadImage(ImageView imageView, Integer resId, Uri uri, Integer desiredSize){
            imageView.setImageDrawable(null);

            RequestOptions options = new RequestOptions();
            if(desiredSize != null) options.override(desiredSize);
            options.diskCacheStrategy(DiskCacheStrategy.ALL);

            Context context = imageView.getContext();
            if(isContextValid(context)){
                Glide.with(context)
                        .asBitmap()
                        .apply(options)
                        .load(resId != null ? resId : uri)
                        .into(imageView);
            }
        }

        protected void stopLoadingImage(ImageView imageView){
            Context context = imageView.getContext();
            if(isContextValid(context)){
                Glide.with(context).clear(imageView);
            }
        }

        private boolean isContextValid(Context context){
            if (context == null) {
                return false;
            }
            else if (context instanceof Activity) {
                final Activity activity = (Activity) context;
                return !activity.isFinishing() && !activity.isDestroyed();
            }
            return true;
        }
    }

    public class VideoViewHolder extends ViewHolder {
        ImageView thumbImage;
        ImageView typeIcon;
        TextView durationText;

        View editOptionsView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbImage = itemView.findViewById(R.id.thumb_image);
            typeIcon = itemView.findViewById(R.id.type_icon);
            durationText = itemView.findViewById(R.id.duration_text);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                int itemIndex = getVideoItemIndexForAdapterPosition(position);

                if(position < 0 || itemIndex < 0) return;

                if(selectedItemIndex != itemIndex){
                    selectedItemIndex = itemIndex;
                    if(itemSelectionChangeListener != null){
                        itemSelectionChangeListener.onItemSelectionChanged(selectedItemIndex, true);
                    }
                }
                notifyDataSetChanged();
            });
        }

        public void addEditOptionsView(){
            if(editOptionsView == null){

                editOptionsView = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.videoitemlistview_itemeditoptions, (ViewGroup) itemView, false);
                ((ViewGroup) itemView).addView(editOptionsView);

                editOptionsView.findViewById(R.id.edit).setOnClickListener(v ->{
                    if(clicksListener != null){
                        clicksListener.onEditClicked(getVideoItemIndexForAdapterPosition(getBindingAdapterPosition()));
                    }
                    clearVideoItemSelection();
                });

                editOptionsView.findViewById(R.id.replace).setOnClickListener(v ->{
                    if(clicksListener != null){
                        clicksListener.onReplaceClicked(getVideoItemIndexForAdapterPosition(getBindingAdapterPosition()));
                    }
                    clearVideoItemSelection();
                });

                editOptionsView.findViewById(R.id.delete).setOnClickListener(v ->{
                    if(clicksListener != null){
                        clicksListener.onDeleteClicked(getVideoItemIndexForAdapterPosition(getBindingAdapterPosition()));
                    }
                    clearVideoItemSelection();
                });
            }
        }

        public void removeEditOptionsView(){
            if(editOptionsView != null){
                ((ViewGroup) itemView).removeView(editOptionsView);
                editOptionsView = null;
            }
        }


        public void loadThumb(Uri uri, int desiredSize) {
            loadImage(thumbImage, null, uri, desiredSize);
        }

        public void loadTypeIcon(int resId){
            loadImage(typeIcon, resId, null, null);
        }

        public void stopLoadingAll() {
            stopLoadingImage(thumbImage);
            stopLoadingImage(typeIcon);
        }
    }

    public class JoinerViewHolder extends ViewHolder {
        ImageView joinerIcon;

        public JoinerViewHolder(@NonNull View itemView) {
            super(itemView);
            joinerIcon = itemView.findViewById(R.id.joiner_icon);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();

                if(isVideoItemSelected()){

                    int selectedItemAdapterPosition = getAdapterPositionForVideoItemIndex(getSelectedVideoItemIndex());
                    if(position == selectedItemAdapterPosition - 1){
                        if(clicksListener != null){
                            clicksListener.onLeftAddClicked(getSelectedVideoItemIndex());
                        }
                        clearVideoItemSelection();
                    }else if(position == selectedItemAdapterPosition + 1){
                        if(clicksListener != null){
                            clicksListener.onRightAddClicked(getSelectedVideoItemIndex());
                        }
                        clearVideoItemSelection();
                    }

                }else{

                    if(clicksListener != null){
                        int firstIndex = getVideoItemIndexForAdapterPosition(position - 1);
                        int secondIndex = getVideoItemIndexForAdapterPosition(position + 1);
                        clicksListener.onTransitionClicked(firstIndex, secondIndex);
                    }

                }

            });
        }

        public void loadIcon(int resId){
            loadImage(joinerIcon, resId, null, null);
        }

        public void stopLoadingIcon(){
            stopLoadingImage(joinerIcon);
        }
    }

    public static class StartViewHolder extends ViewHolder {
        public StartViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class EndViewHolder extends ViewHolder {
        public EndViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface ItemSelectionChangeListener{
        void onItemSelectionChanged(int itemIndex, boolean selected);
    }
}
