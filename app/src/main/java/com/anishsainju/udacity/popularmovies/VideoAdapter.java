package com.anishsainju.udacity.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anishsainju.udacity.popularmovies.model.Video;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoAdapterViewHolder> {

    private final VideoAdapterOnClickHandler mClickHandler;
    private List<Video> mVideosData = new ArrayList<>();

    public interface VideoAdapterOnClickHandler {
        void onClick(int position);
    }

    public VideoAdapter(Context context, VideoAdapterOnClickHandler clickHandler) {
        Context context1 = context;
        this.mClickHandler = clickHandler;
    }

    public class VideoAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Each item display only contains an ImageView, if more added in future, add here
        final TextView mTextView;

        VideoAdapterViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.txv_title);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int videoPosition = getAdapterPosition();
            mClickHandler.onClick(videoPosition);
        }
    }

    public VideoAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        int layoutIdForListItem = R.layout.video_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        boolean shouldAttachToParentImmediately = false;
        View view = layoutInflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        return new VideoAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoAdapterViewHolder holder, int position) {
        Video selectedVideo = mVideosData.get(position);
        holder.mTextView.setText(selectedVideo.getName());
    }

    @Override
    public int getItemCount() {
        if (null == mVideosData) return 0;
        return mVideosData.size();
    }

    public void setVideoData(List<Video> VideosData) {
        mVideosData.clear();
        mVideosData.addAll(VideosData);
        notifyDataSetChanged();
    }
}
