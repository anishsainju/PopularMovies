package com.anishsainju.udacity.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.anishsainju.udacity.popularmovies.model.Movie;
import com.anishsainju.udacity.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private Context context;
    private final MovieAdapterOnClickHandler mClickHandler;
    private List<Movie> mMoviesData = new ArrayList<>();

    public interface MovieAdapterOnClickHandler {
        void onClick(int position);
    }

    public MovieAdapter(Context context, MovieAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.mClickHandler = clickHandler;
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Each item display only contains an ImageView, if more added in future, add here
        final ImageView mImageView;

        MovieAdapterViewHolder(View view) {
            super(view);
            mImageView = view.findViewById(R.id.movie_image);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int moviePosition = getAdapterPosition();
            mClickHandler.onClick(moviePosition);
        }
    }

    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        int layoutIdForListItem = R.layout.movie_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        boolean shouldAttachToParentImmediately = false;
        View view = layoutInflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        Movie selectedMovie = mMoviesData.get(position);
        Picasso.with(context)
                .load(NetworkUtils.buildImageURL(selectedMovie.getPosterPath()))
                .placeholder(R.drawable.ic_no_image)
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        if (null == mMoviesData) return 0;
        return mMoviesData.size();
    }

    public void setMovieData(List<Movie> moviesData) {
        mMoviesData.clear();
        mMoviesData.addAll(moviesData);
        notifyDataSetChanged();
    }
}