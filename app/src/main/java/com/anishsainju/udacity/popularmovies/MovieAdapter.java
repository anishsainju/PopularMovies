package com.anishsainju.udacity.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.anishsainju.udacity.popularmovies.model.Movie;
import com.anishsainju.udacity.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends ArrayAdapter<Movie>{

    private Context context;

    public MovieAdapter(Activity context) {
        super(context, 0);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Gets the AndroidFlavor object from the ArrayAdapter at the appropriate position
        Movie movie = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.movie_item, parent, false);
        }

        ImageView iconView = (ImageView) convertView.findViewById(R.id.movie_image);
        Picasso.with(context)
                .load(NetworkUtils.buildImageURL(movie.getPosterPath()))
                .placeholder(R.drawable.ic_no_image)
                .into(iconView);
        return convertView;
    }

    public void setMovieData(List<Movie> movieData) {
        clear();
        addAll(movieData);
        notifyDataSetChanged();
    }
}
