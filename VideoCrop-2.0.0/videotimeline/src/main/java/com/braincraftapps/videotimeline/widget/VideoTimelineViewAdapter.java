package com.braincraftapps.videotimeline.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.braincraftapps.videotimeline.FrameItem;
import com.braincraftapps.videotimeline.FramePosition;
import com.braincraftapps.videotimeline.FrameView;
import com.braincraftapps.videotimeline.R;
import com.braincraftapps.videotimeline.VideoItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.VideoDecoder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

class VideoTimelineViewAdapter extends RecyclerView.Adapter<VideoTimelineViewAdapter.ViewHolder> {
    private static final String TAG = VideoTimelineViewAdapter.class.getSimpleName();
    private static final String PAYLOAD = "payload_title";

    public static final int VIEW_TYPE_GENERAL = 0;
    public static final int VIEW_TYPE_START = 1;
    public static final int VIEW_TYPE_END = 2;


    private List<VideoItem> videoItemList;
    private FrameView.Params frameViewParams;
    private int startViewWidth = ViewGroup.LayoutParams.MATCH_PARENT;
    private int endViewWidth = ViewGroup.LayoutParams.MATCH_PARENT;
    private int selectedVideoIndex;

    VideoTimelineViewAdapter(Context context, List<VideoItem> videoItemList){
        frameViewParams = new FrameView.Params(context);
        this.videoItemList = videoItemList;
    }

    public void setStartViewWidth(int startViewWidth) {
        this.startViewWidth = startViewWidth;
    }

    public void setEndViewWidth(int endViewWidth) {
        this.endViewWidth = endViewWidth;
    }

    public void setSelectedVideoIndex(int selectedVideoIndex) {
        this.selectedVideoIndex = selectedVideoIndex;
    }

    /*public void setSelectedVideoIndex(int selectedVideoIndex, int firstVisiblePosition, int lastVisiblePosition) {
        if(selectedVideoIndex != this.selectedVideoIndex){
            this.selectedVideoIndex = selectedVideoIndex;

            if(firstVisiblePosition >= 0 && lastVisiblePosition >= 0){
                notifyItemRangeChanged(firstVisiblePosition, lastVisiblePosition - firstVisiblePosition + 1, PAYLOAD);
            }
        }
    }*/

    public FrameView.Params getFrameViewParams() {
        return frameViewParams;
    }

    public int getSelectedVideoIndex() {
        return selectedVideoIndex;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for(VideoItem videoItem: videoItemList){
            count += videoItem.getNumberOfFrameItems();
        }
        return count + 2; // adding 2 for the start and ending views
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){ // first view
            return VIEW_TYPE_START;
        }else if(position == getItemCount() - 1){ // last view
            return VIEW_TYPE_END;
        }else {
            return VIEW_TYPE_GENERAL;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType == VIEW_TYPE_START){
            return new StartViewHolder(inflater.
                    inflate(R.layout.timeline_item_startview, parent, false));
        }else if(viewType == VIEW_TYPE_END){
            return new EndViewHolder(inflater.
                    inflate(R.layout.timeline_item_endview, parent, false));
        }else {
            return new GeneralViewHolder(inflater.
                    inflate(R.layout.timeline_item_generalview, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == 0){// first view
            holder.itemView.getLayoutParams().width = startViewWidth;

        }else if(position == getItemCount()-1){// last view
            holder.itemView.getLayoutParams().width = endViewWidth;

        }else{
            FramePosition fp = getFramePositionForAdapterPosition(position);
            final int videoIndex = fp.getVideoIndexInVideoItemList();
            final int frameIndex = fp.getFrameIndexInVideoItem();

            VideoItem videoItem = videoItemList.get(videoIndex);
            FrameItem frameItem = videoItem.getFrameItem(frameIndex);

            GeneralViewHolder viewHolder = (GeneralViewHolder) holder;
            viewHolder.startLoadingFrame(videoItem.getVideoUri(), frameItem.getVideoFramePositionUs(),
                    (int)viewHolder.itemView.getContext().getResources().getDimension(R.dimen.frame_image_desired_size));

            viewHolder.itemView.getLayoutParams().width = frameItem.getRequiredPixels();

            makeFrameViewSelectedOrUnselected(viewHolder, videoIndex, frameIndex);
        }
    }

    /*@Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if(position != 0 && position != getItemCount()-1){ // not first/last view
            for (Object payload : payloads) {
                if (payload.equals(PAYLOAD)) {
                    FramePosition fp = getFramePositionForAdapterPosition(position);
                    final int videoIndex = fp.getVideoIndexInVideoItemList();
                    final int frameIndex = fp.getFrameIndexInVideoItem();
                    makeFrameViewSelectedOrUnselected((GeneralViewHolder) holder, videoIndex, frameIndex);
                }
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }*/

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if(holder instanceof GeneralViewHolder){
            ((GeneralViewHolder)holder).stopLoadingFrame();
        }
    }

    public void makeFrameViewSelectedOrUnselected(GeneralViewHolder viewHolder, int videoIndex, int frameIndex){
        VideoItem videoItem = videoItemList.get(videoIndex);
        FrameView frameView = viewHolder.frameView;
        frameView.setParams(frameViewParams);
        frameView.setSelectedFrame(videoIndex == selectedVideoIndex);
        frameView.setFirstView(frameIndex == 0);
        frameView.setLastView(frameIndex == videoItem.getLastFrameItemIndex());
    }

    public FramePosition getFramePositionForAdapterPosition(int adapterPosition){
        if(videoItemList.size() < 1 || adapterPosition == 0 || adapterPosition == getItemCount()-1) return null;
        int videoItemPosition = -1;
        int framePosition = adapterPosition - 1;
        for(int i = 0; i < videoItemList.size(); i++){
            videoItemPosition = i;
            int numberOfFrames = videoItemList.get(i).getNumberOfFrameItems();
            if(framePosition < numberOfFrames) break;
            else framePosition -= numberOfFrames;
        }
        return new FramePosition(videoItemPosition, framePosition);
    }

    public int getAdapterPositionForFramePosition(FramePosition framePosition){
        if(videoItemList.size() < 1) return 0;

        int videoIndex = framePosition.getVideoIndexInVideoItemList();
        int frameIndex = framePosition.getFrameIndexInVideoItem();

        int position = 0;
        for(int i = 0; i <= videoIndex; i++){
            if(i == videoIndex){
                position += frameIndex + 1;
                break;
            }else{
                position += videoItemList.get(i).getNumberOfFrameItems();
            }
        }
        return position;
    }



    /*...................Inner classes...................*/

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class GeneralViewHolder extends ViewHolder{

        FrameView frameView;
        CustomTarget<Bitmap> glideTarget;

        public GeneralViewHolder(@NonNull View itemView) {
            super(itemView);
            frameView = itemView.findViewById(R.id.frame_view);
        }

        public void startLoadingFrame(Uri videoUri, long framePositionUs, int desiredSize) {
            glideTarget = new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource,
                                            @Nullable Transition<? super Bitmap> transition) {
                    frameView.setFrame(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) { }
            };

            RequestOptions options = new RequestOptions()
                    .frame(framePositionUs)
                    .override(desiredSize)
                    .set(VideoDecoder.TARGET_FRAME, framePositionUs)
                    .set(VideoDecoder.FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    //.priority(Priority.HIGH)
                    .dontAnimate();

            /*GlideApp.with(frameView)
                    .setDefaultRequestOptions(options)
                    .asBitmap()
                    .load(videoUri)
                    .into(glideTarget);*/

            Context context = frameView.getContext();
            if(isContextValid(context)){
                Glide.with(context)
                        .asBitmap()
                        .apply(options)
                        .load(videoUri)
                        .into(glideTarget);
            }
        }

        public void stopLoadingFrame(){
            Context context = frameView.getContext();
            if(isContextValid(context)){
                Glide.with(context).clear(glideTarget);
            }
            glideTarget = null;
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

    public static class StartViewHolder extends ViewHolder{
        public StartViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class EndViewHolder extends ViewHolder{
        public EndViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
