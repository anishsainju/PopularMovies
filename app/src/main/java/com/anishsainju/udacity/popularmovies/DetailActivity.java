package com.anishsainju.udacity.popularmovies;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.anishsainju.udacity.popularmovies.model.Movie;
import com.anishsainju.udacity.popularmovies.databinding.ActivityDetailBinding;
import com.anishsainju.udacity.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    public static final String MOVIE = "movie";
    private Movie movie;

    ActivityDetailBinding activityDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        activityDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        // Obtain the Parcelable Movie object
        movie = intent.getParcelableExtra(MOVIE);
        if (movie == null) {
            closeOnError();
            return;
        }

        populateUI(movie);
        setTitle(movie.getTitle());
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

    private void populateUI(Movie movie) {
        Picasso.with(this)
                .load(NetworkUtils.buildImageURL(movie.getBackdropPath()))
                .placeholder(R.drawable.ic_no_image)
                .into(activityDetailBinding.ivBackdrop);

        Picasso.with(this)
                .load(NetworkUtils.buildImageURL(movie.getPosterPath()))
                .placeholder(R.drawable.ic_no_image)
                .into(activityDetailBinding.ivPoster);

        String releaseDate = movie.getReleaseDate();
        if (releaseDate.isEmpty()) {
            activityDetailBinding.tvReleaseDate.setText(R.string.not_available);
        } else {
            activityDetailBinding.tvReleaseDate.setText(releaseDate);
        }

        String voteAverage = movie.getVoteAverage().toString();
        if (voteAverage.isEmpty()) {
            activityDetailBinding.tvUserRating.setText(R.string.not_available);
        } else {
            activityDetailBinding.tvUserRating.setText(voteAverage);
        }

        String overview = movie.getOverview();
        if (overview.isEmpty()) {
            activityDetailBinding.tvOverview.setText(R.string.overview_unavailable);
        } else {
            activityDetailBinding.tvOverview.setText(overview);
        }
    }
}