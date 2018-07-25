package com.anishsainju.udacity.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anishsainju.udacity.popularmovies.model.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private List<Review> mReviewsData = new ArrayList<>();

    public ReviewAdapter(Context context) {
        Context context1 = context;
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {
        final TextView mTxvContent;
        final TextView mTxvAuthor;

        ReviewAdapterViewHolder(View view) {
            super(view);
            mTxvContent = view.findViewById(R.id.txv_content);
            mTxvAuthor = view.findViewById(R.id.txv_author);
        }
    }

    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        int layoutIdForListItem = R.layout.review_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        boolean shouldAttachToParentImmediately = false;
        View view = layoutInflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapterViewHolder holder, int position) {
        Review selectedReview = mReviewsData.get(position);
        holder.mTxvContent.setText(selectedReview.getContent());
        holder.mTxvAuthor.setText(selectedReview.getAuthor());
    }

    @Override
    public int getItemCount() {
        if (null == mReviewsData) return 0;
        return mReviewsData.size();
    }

    public void setReviewData(List<Review> reviewsData) {
        mReviewsData.clear();
        mReviewsData.addAll(reviewsData);
        notifyDataSetChanged();
    }
}
