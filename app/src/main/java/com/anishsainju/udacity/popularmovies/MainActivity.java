package com.anishsainju.udacity.popularmovies;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.anishsainju.udacity.popularmovies.model.Movie;
import com.anishsainju.udacity.popularmovies.utilities.Endpoint;
import com.anishsainju.udacity.popularmovies.utilities.JsonUtils;
import com.anishsainju.udacity.popularmovies.utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity class
 */
public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler, LoaderManager.LoaderCallbacks<String>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // a constant int (chosen arbitrarily) to uniquely identify your loader.
    private static final int MOVIES_DATA_LOADER_ID = 143;

    private static final String ENDPOINT_INDEX = "sortOrderIndex";
    private static final int DEFAULT_ENDPOINT_INDEX = 0; // index based on Endpoint constants

    // UI components
    private RecyclerView mRecyclerViewMovies;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    // Adapter for RecyclerView
    private MovieAdapter mMovieAdapter;

    private List<Movie> moviesList = new ArrayList<>();
    private List<Movie> favoriteMoviesList = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerViewMovies = findViewById(R.id.rv_movies);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);

        /*
         * We will use GridLayoutManager to view the movie images in grid style in the recyclerView
         * numberOfColumns is set to 2 for Portrait orientation, and 3 for Landscape
         */
        int numberOfColumns = getColumnsNumByOrientation();
        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns);
        mRecyclerViewMovies.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerViewMovies.setHasFixedSize(true);

        /*
         * The MovieAdapter is responsible for linking our movies data with the Views that
         * will end up displaying our movie data.
         * Passing itself as clickHandler as this MainActivity implements
         * MovieAdapter.MovieAdapterOnClickHandler
         */
        mMovieAdapter = new MovieAdapter(this, this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerViewMovies.setAdapter(mMovieAdapter);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Register MainActivity as a OnSharedPreferenceChangedListener in onCreate
        /*
         * Register MainActivity as an OnPreferenceChangedListener to receive a callback when a
         * SharedPreference has changed. Please note that we must unregister MainActivity as an
         * OnSharedPreferenceChanged listener in onDestroy to avoid any memory leaks.
         */
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        favoriteViewModelInit();
    }

    private void favoriteViewModelInit() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                Log.d(TAG, "Updating list of favorite movies from LiveData in ViewModel");
                favoriteMoviesList = movies;
                int sortByIndex = sharedPreferences.getInt(ENDPOINT_INDEX, DEFAULT_ENDPOINT_INDEX);
                if (sortByIndex == Endpoint.FAVORITE_INDEX) {
                    moviesList = favoriteMoviesList;
                    showMovies(moviesList);
                } else {
                    getSupportLoaderManager().restartLoader(MOVIES_DATA_LOADER_ID, null, MainActivity.this);
                }
            }
        });
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            // Create a String member variable called mMoviesJson that will store the raw JSON
            /* This String will contain the raw JSON from the results of API call */
            String mMoviesJson;

            @Override
            protected void onStartLoading() {
                // If mMoviesJson is not null, deliver that result. Otherwise, force a load
                /*
                 * If we already have cached results, just deliver them now. If we don't have any
                 * cached results, force a load.
                 */
                if (mMoviesJson != null) {
                    Log.v(TAG, "loaded cached movies data");
                    deliverResult(mMoviesJson);
                } else {
                    Log.v(TAG, "force loading movies data");
                    /*
                     * When we initially begin loading in the background, we want to display the
                     * loading indicator to the user
                     */
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {
                // Get the String for our URL from the bundle passed to onCreateLoader
                int sortByIndex = sharedPreferences.getInt(ENDPOINT_INDEX, DEFAULT_ENDPOINT_INDEX);
                String moviesUrlString = Endpoint.ENDPOINT_URLS[sortByIndex];

                // If the URL is null or empty, return null
                if (moviesUrlString == null || TextUtils.isEmpty(moviesUrlString)) {
                    return null;
                }
                URL moviesURL = NetworkUtils.buildURL(moviesUrlString);
                /* Parse the URL from the passed in String and perform the search */
                try {
                    Log.v(TAG, "loadInBackground: loading movies data from API");
                    return NetworkUtils.getResponseFromHttpUrl(moviesURL);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            // Override deliverResult and store the data in mGithubJson
            // Call super.deliverResult after storing the data
            @Override
            public void deliverResult(String moviesJson) {
                mMoviesJson = moviesJson;
                super.deliverResult(moviesJson);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        // Hide the loading indicator
        /* When we finish loading, we want to hide the loading indicator from the user. */
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        /*
         * If the results are null, we assume an error has occurred. There are much more robust
         * methods for checking errors, but we wanted to keep this particular example simple.
         */
        if (null == data) {
            showErrorMessage();
        } else {
            parseJsonAndShow(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        /*
         * We aren't using this method in our example application, but we are required to Override
         * it to implement the LoaderCallbacks<String> interface
         */
    }

    // Override onDestroy and unregister MainActivity as a SharedPreferenceChangedListener
    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* Unregister MainActivity as an OnPreferenceChangedListener to avoid any memory leaks. */
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Returns 3 for Landscape Orientation.
     * Returns 2 for otherwise (Portrait Orientation)
     *
     * @return int
     */
    private int getColumnsNumByOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return 3;
        } else //if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            return 2;
    }


    private void showErrorMessage() {
        mRecyclerViewMovies.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void showMoviesDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerViewMovies.setVisibility(View.VISIBLE);
    }

    private void parseJsonAndShow(String jsonMoviesResponse) {
        try {
            moviesList = JsonUtils.parseMoviesJson(jsonMoviesResponse);
            showMovies(moviesList);
        } catch (JSONException e) {
            e.printStackTrace();
            showErrorMessage();
        }
    }

    private void showMovies(List<Movie> moviesToShow) {
        if (moviesToShow.isEmpty()) {
            mErrorMessageDisplay.setText(R.string.msg_no_movies);
            mErrorMessageDisplay.setVisibility(View.VISIBLE);
        } else {
            showMoviesDataView();
        }
        mMovieAdapter.setMovieData(moviesToShow);
    }

    @Override
    public void onClick(int position) {
        launchDetailActivity(position);
    }

    /**
     * Starts DetailActivity
     * Sends along with selected Parcelable Movie object
     *
     * @param position
     */
    private void launchDetailActivity(int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        Movie mv = moviesList.get(position);
        intent.putExtra(DetailActivity.MOVIE, mv);
        intent.putExtra(DetailActivity.IS_MOVIE_FAVORITE, isMovieFavorite(mv));
        startActivity(intent);
    }

    private boolean isMovieFavorite(Movie mv) {
        return favoriteMoviesList.contains(mv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.item_sort_by:
                showAlertDialogSortBy().show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Dialog showAlertDialogSortBy() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.dialog_title);

        // build choice items
        builder.setSingleChoiceItems(Endpoint.ENDPOINT_DISPLAY_NAMES, sharedPreferences.getInt(ENDPOINT_INDEX, DEFAULT_ENDPOINT_INDEX), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int newClickedIndex) {
                int oldIndex = sharedPreferences.getInt(ENDPOINT_INDEX, DEFAULT_ENDPOINT_INDEX);
                // If same already selected option is selected, do nothing
                // Otherwise
                if (oldIndex != newClickedIndex) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(ENDPOINT_INDEX, newClickedIndex);
                    editor.apply();
                }
                dialogInterface.dismiss();
            }
        });
        // Get the AlertDialog from create()
        return builder.create();
    }

    //Override onSharedPreferenceChanged to set the preferences flag to true
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(ENDPOINT_INDEX)) {
            int selectedIndex = sharedPreferences.getInt(ENDPOINT_INDEX, DEFAULT_ENDPOINT_INDEX);
            if (selectedIndex == Endpoint.FAVORITE_INDEX) {
                moviesList = favoriteMoviesList;
                showMovies(moviesList);
            } else {
                getSupportLoaderManager().restartLoader(MOVIES_DATA_LOADER_ID, null, this);
            }
        }
    }
}