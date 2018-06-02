package com.anishsainju.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anishsainju.udacity.popularmovies.model.Movie;
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
public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Constant for user's sort selection, used in savedInstanceState
    private static final String USER_SORT_SELECTION = "user_sort_selection";

    // UI components
    private RecyclerView mRecyclerViewMovies;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    // Adapter for RecyclerView
    private MovieAdapter mMovieAdapter;

    private List<Movie> moviesList = new ArrayList<>();

    // Enum for user's sort selection (either Top Rated or Popular)
    private NetworkUtils.Endpoint endpointUserSelection;
    // Default is Popular
    private static final NetworkUtils.Endpoint DEFAULT_ENDPOINT = NetworkUtils.Endpoint.POPULAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerViewMovies = (RecyclerView) findViewById(R.id.rv_movies);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        /*
         * We will use GridLayoutManager to view the movie images in grid style in the recylerView
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
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // Set Default
        endpointUserSelection = DEFAULT_ENDPOINT;

        /* Once all of our views are setup, we can load the weather data. */
        loadMoviesData(endpointUserSelection);
    }

    /**
     * Returns 3 for Landscape Orientation.
     * Returns 2 for otherwise (Portrait Orientation)
     *
     * @return
     */
    private int getColumnsNumByOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return 3;
        } else //if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            return 2;
    }

    /**
     * Saves the user's sort selection
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(USER_SORT_SELECTION, endpointUserSelection.toString());
    }

    /**
     * Retrives the user's sort selection and uses it correctly show selection in UI as well as
     * loads the correct data as per user sort selection.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String userSortSelection = savedInstanceState.getString(USER_SORT_SELECTION);
        endpointUserSelection = NetworkUtils.Endpoint.valueOf(userSortSelection);
        loadMoviesData(endpointUserSelection);
    }

    /**
     * Load movies data with the provided endpoint as per user's sort selection
     *
     * @param endpoint
     */
    private void loadMoviesData(NetworkUtils.Endpoint endpoint) {
        showMoviesDataView();
        new FetchMoviesDataTask().execute(endpoint);
    }

    private void showMoviesDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movies data is visible */
        mRecyclerViewMovies.setVisibility(View.VISIBLE);
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
        Context context = this;
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.MOVIE, moviesList.get(position));
        startActivity(intent);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerViewMovies.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * AsyncTask to run in background to fetch movies data from provided URL
     */
    public class FetchMoviesDataTask extends AsyncTask<NetworkUtils.Endpoint, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(NetworkUtils.Endpoint... endpoints) {

            if (endpoints.length == 0) {
                return null;
            }

            NetworkUtils.Endpoint endpoint = endpoints[0];
            URL movieRequestURL = NetworkUtils.buildURL(endpoint);

            try {
                String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestURL);
                return jsonMoviesResponse;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String jsonMoviesResponse) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (jsonMoviesResponse == null) {
                showErrorMessage();
            } else {
                Log.v(TAG, "onPostExecute: successfully loaded movies data");
                parseJsonAndShow(jsonMoviesResponse);
            }
        }
    }

    private void parseJsonAndShow(String jsonMoviesResponse) {
        try {
            moviesList = JsonUtils.parseMoviesJson(jsonMoviesResponse);
            showMoviesDataView();
            mMovieAdapter.setMovieData(moviesList);
        } catch (JSONException e) {
            e.printStackTrace();
            showErrorMessage();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort, menu);

        // Get the menu item to set it checked as default sort order
        MenuItem sortByPopular = menu.findItem(R.id.sort_by_popular);
        MenuItem sortByTopRated = menu.findItem(R.id.sort_by_top_rated);
        if (endpointUserSelection == NetworkUtils.Endpoint.POPULAR) {
            sortByPopular.setChecked(true);
        } else if (endpointUserSelection == NetworkUtils.Endpoint.TOPRATED) {
            sortByTopRated.setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_popular:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    NetworkUtils.Endpoint endpoint = NetworkUtils.Endpoint.POPULAR;
                    // note the user's selection to save in savedInstanceState
                    endpointUserSelection = endpoint;
                    loadMoviesData(endpoint);
                }
                return true;
            case R.id.sort_by_top_rated:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    NetworkUtils.Endpoint endpoint = NetworkUtils.Endpoint.TOPRATED;
                    // note the user's selection to save in savedInstanceState
                    endpointUserSelection = endpoint;
                    loadMoviesData(endpoint);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}