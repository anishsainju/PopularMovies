package com.anishsainju.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridView mGridView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private List<Movie> moviesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridView = (GridView) findViewById(R.id.gridview_movies);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mMovieAdapter = new MovieAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mGridView.setAdapter(mMovieAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                launchDetailActivity(position);
            }
        });

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /* Once all of our views are setup, we can load the weather data. */
        loadMoviesData();
    }

    /**
     *
     */
    private void loadMoviesData() {
        showMoviesDataView();

        //TODO get user selected Endpoint
        new FetchMoviesDataTask().execute(NetworkUtils.Endpoint.TOPRATED);
    }
    private void showMoviesDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movies data is visible */
        mGridView.setVisibility(View.VISIBLE);
    }

    public void launchDetailActivity(int position) {
        Context context = this;
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.MOVIE, moviesList.get(position));
        startActivity(intent);
    }

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
                try {
                    moviesList = JsonUtils.parseMoviesJson(jsonMoviesResponse);
                    showMoviesDataView();
                    mMovieAdapter.setMovieData(moviesList);
                } catch (JSONException e) {
                    e.printStackTrace();
                    showErrorMessage();
                }

            }
        }
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
        mGridView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }
}
