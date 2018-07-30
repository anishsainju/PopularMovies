package com.anishsainju.udacity.popularmovies;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anishsainju.udacity.popularmovies.database.AppDatabase;
import com.anishsainju.udacity.popularmovies.databinding.ActivityDetailBinding;
import com.anishsainju.udacity.popularmovies.model.Movie;
import com.anishsainju.udacity.popularmovies.model.Review;
import com.anishsainju.udacity.popularmovies.model.Video;
import com.anishsainju.udacity.popularmovies.utilities.AppExecutors;
import com.anishsainju.udacity.popularmovies.utilities.Endpoint;
import com.anishsainju.udacity.popularmovies.utilities.JsonUtils;
import com.anishsainju.udacity.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements VideoAdapter.VideoAdapterOnClickHandler{

    private static final String TAG = DetailActivity.class.getSimpleName();
    public static final String MOVIE = "movie";
    private static final int VIDEOS_LOADER_ID = 0;
    private static final int REVIEWS_LOADER_ID = 1;
    private static final String MOVIE_ID = "movie_id";

    private ActivityDetailBinding activityDetailBinding;
    // UI components
    private RecyclerView mRecyclerViewVideos;
    private RecyclerView mRecyclerViewReviews;
    private TextView mErrorMsgVideosDisplay;
    private TextView mErrorMsgReviewsDisplay;
    private ProgressBar mVideosLoadingIndicator;
    private ProgressBar mReviewsLoadingIndicator;
    private FloatingActionButton mFavoriteFAB;

    // Adapter for RecyclerView
    private VideoAdapter mVideoAdapter;
    private ReviewAdapter mReviewAdapter;

    private List<Video> mVideosList = new ArrayList<>();

    private Movie movie;
    private AppDatabase mDb;
    private static boolean isFavoriteMovie;

    private LoaderManager.LoaderCallbacks<String> videosLoaderCallbacks = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader<String> onCreateLoader(int id, final Bundle bundle) {
            return new AsyncTaskLoader<String>(DetailActivity.this) {

                /* This String array will hold and help cache our weather data */
                String mVideosData = null;

                /**
                 * Subclasses of AsyncTaskLoader must implement this to take care of loading their data.
                 */
                @Override
                protected void onStartLoading() {
                    /* If no arguments were passed, we don't have a query to perform. Simply return. */
                    if (bundle == null) {
                        return;
                    }
                    if (mVideosData != null) {
                        deliverResult(mVideosData);
                    } else {
                        mVideosLoadingIndicator.setVisibility(View.VISIBLE);
                        forceLoad();
                    }
                }

                @Override
                public String loadInBackground() {
                    URL videosUrl = NetworkUtils.buildURL(bundle.getInt(MOVIE_ID), Endpoint.VIDEOS_URL);
                    try {
                        Log.v(TAG, "loadInBackground: loading movies' trailers data from API");
                        return NetworkUtils.getResponseFromHttpUrl(videosUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                /**
                 * Sends the result of the load to the registered listener.
                 *
                 * @param data The result of the load
                 */
                public void deliverResult(String data) {
                    mVideosData = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            mVideosLoadingIndicator.setVisibility(View.INVISIBLE);
            if (null == data) {
                showErrorMessageVideos();
            } else {
                parseVideosJsonAndShow(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<String> reviewsLoaderCallbacks = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader onCreateLoader(int id, final Bundle bundle) {
            return new AsyncTaskLoader<String>(DetailActivity.this) {
                String mReviewsData = null;
                @Override
                protected void onStartLoading() {
                    if (bundle == null) {
                        return;
                    }
                    if (mReviewsData != null) {
                        deliverResult(mReviewsData);
                    } else {
                        mReviewsLoadingIndicator.setVisibility(View.VISIBLE);
                        forceLoad();
                    }
                }

                @Override
                public String loadInBackground() {
                    URL reviewsUrl = NetworkUtils.buildURL(bundle.getInt(MOVIE_ID), Endpoint.REVIEWS_URL);
                    try {
                        Log.v(TAG, "loadInBackground: loading movies' reviews data from API");
                        return NetworkUtils.getResponseFromHttpUrl(reviewsUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                public void deliverResult(String data) {
                    mReviewsData = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader loader, String data) {
            mReviewsLoadingIndicator.setVisibility(View.INVISIBLE);
            if (null == data) {
                showErrorMessageReviews();
            } else {
                parseReviewsJsonAndShow(data);
            }
        }

        @Override
        public void onLoaderReset(Loader loader) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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

        Bundle loadMovieDataBundle = new Bundle();
        loadMovieDataBundle.putInt(MOVIE_ID, movie.getId());
        getSupportLoaderManager().initLoader(VIDEOS_LOADER_ID, loadMovieDataBundle, videosLoaderCallbacks);
        getSupportLoaderManager().initLoader(REVIEWS_LOADER_ID, loadMovieDataBundle,reviewsLoaderCallbacks);

        mRecyclerViewVideos = findViewById(R.id.rv_videos);
        mRecyclerViewReviews = findViewById(R.id.rv_reviews);

        mErrorMsgVideosDisplay = findViewById(R.id.tv_error_msg_video_display);
        mErrorMsgReviewsDisplay = findViewById(R.id.tv_error_msg_review_display);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewVideos.setLayoutManager(layoutManager);
        mRecyclerViewReviews.setLayoutManager(layoutManager2);

        mRecyclerViewVideos.setHasFixedSize(true);
        mRecyclerViewReviews.setHasFixedSize(true);

        mVideoAdapter = new VideoAdapter(this, this);
        mReviewAdapter = new ReviewAdapter(this);

        mRecyclerViewVideos.setAdapter(mVideoAdapter);
        mRecyclerViewReviews.setAdapter(mReviewAdapter);

        mVideosLoadingIndicator = findViewById(R.id.pb_video_loading_indicator);
        mReviewsLoadingIndicator = findViewById(R.id.pb_review_loading_indicator);

        // Set Default
        populateUI(movie);
        setTitle(movie.getTitle());

        mDb = AppDatabase.getInstance(getApplicationContext());

        mFavoriteFAB = findViewById(R.id.fab_favorite);

        favoriteMovieDBInit(movie.getId());

        mFavoriteFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if already checked, uncheck and delete from database
                if (isFavoriteMovie) {
                    isFavoriteMovie = false;
                    setFABFavorite(false);
                    final Movie movieToDelete = movie;
                    AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.movieDao().deleteMovie(movieToDelete);
                        }
                    });
                } else {// if unchecked, check it and add movie to database
                    isFavoriteMovie = true;
                    setFABFavorite(true);
                    final Movie movieToAddToDB = movie;
                    AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.movieDao().insertMovie(movieToAddToDB);
                        }
                    });
                }
            }
        });
    }

    private void favoriteMovieDBInit(int movieId) {
        final LiveData<Integer> isFavMovie = mDb.movieDao().doesMovieExists(movieId);
        isFavMovie.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null || !integer.equals(1))
                    isFavoriteMovie = false;
                else
                    isFavoriteMovie = true;
                setFABFavorite(isFavoriteMovie);
            }
        });
    }
    private void setFABFavorite(boolean favorite) {
        if (favorite) {
            mFavoriteFAB.setImageResource(R.drawable.ic_heart);
        } else {
            mFavoriteFAB.setImageResource(R.drawable.ic_heart_outline);
        }
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

    private void populateUI(Movie movie) {
        activityDetailBinding.tvTitle.setText(movie.getTitle());
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

        String voteAverage = String.valueOf(movie.getVoteAverage());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showErrorMessageVideos() {
        mRecyclerViewVideos.setVisibility(View.INVISIBLE);
        mErrorMsgVideosDisplay.setVisibility(View.VISIBLE);
    }

    private void showErrorMessageReviews() {
        mRecyclerViewReviews.setVisibility(View.INVISIBLE);
        mErrorMsgReviewsDisplay.setVisibility(View.VISIBLE);
    }

    private void showVideosDataView() {
        mErrorMsgVideosDisplay.setVisibility(View.INVISIBLE);
        mRecyclerViewVideos.setVisibility(View.VISIBLE);
    }

    private void showReviewsDataView() {
        mErrorMsgReviewsDisplay.setVisibility(View.INVISIBLE);
        mRecyclerViewReviews.setVisibility(View.VISIBLE);
    }

    private void parseVideosJsonAndShow(String jsonMoviesResponse) {
        try {
            mVideosList = JsonUtils.parseVideosJson(jsonMoviesResponse);
            showVideos(mVideosList);
        } catch (JSONException e) {
            e.printStackTrace();
            showErrorMessageVideos();
        }
    }

    private void showVideos(List<Video> videos) {
        if (videos.isEmpty()) {
            mErrorMsgVideosDisplay.setText(R.string.msg_no_videos);
            mErrorMsgVideosDisplay.setVisibility(View.VISIBLE);
        } else {
            showVideosDataView();
            mVideoAdapter.setVideoData(mVideosList);
        }
    }

    private void parseReviewsJsonAndShow(String jsonMoviesResponse) {
        try {
            List<Review> mReviewsList = JsonUtils.parseReviewsJson(jsonMoviesResponse);
            showReviews(mReviewsList);
        } catch (JSONException e) {
            e.printStackTrace();
            showErrorMessageReviews();
        }
    }

    private void showReviews(List<Review> reviews) {
        if (reviews.isEmpty()) {
            mErrorMsgReviewsDisplay.setText(R.string.msg_no_reviews);
            mErrorMsgReviewsDisplay.setVisibility(View.VISIBLE);
        } else {
            showReviewsDataView();
            mReviewAdapter.setReviewData(reviews);
        }
    }

    @Override
    public void onClick(int position) {
        // Get the video from position
        Video clickedVideo = mVideosList.get(position);
        // Get the key of youtube video link
        URL youtubeURL = NetworkUtils.buildYoutubeURL(clickedVideo.getKey());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeURL.toString()));

        // Verify it resolves
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (isIntentSafe) {
            startActivity(intent);
        }
    }
}