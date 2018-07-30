package com.anishsainju.udacity.popularmovies.utilities;

import android.net.Uri;
import android.util.Log;

import com.anishsainju.udacity.popularmovies.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities methods will be used to communicate with servers via APIs
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    /**
     * The API call looks like this
     *
     * http://api.themoviedb.org/3/movie/popular?api_key=[YOUR_API_KEY]
     * http://api.themoviedb.org/3/movie/top_rated?api_key=[YOUR_API_KEY]
     */

    private static final String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/movie";
    private static final String QUERY_API_KEY = "api_key";
    private static final String MOVIE_IMG_BASE_URL = "http://image.tmdb.org/t/p";
    private static final String MOVIE_IMG_URL_SIZE = "w185";

    private static final String api_key = BuildConfig.THE_MOVIE_DB_API_KEY;

    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
    private static final String QUERY_VIDEO_KEY = "v";

    public static URL buildURL(String endpointURL) {
        Uri builtUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                .appendPath(endpointURL)
                .appendQueryParameter(QUERY_API_KEY, api_key)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Built URI"); // DO NOT print API keys on logs!!!
        return url;
    }

    public static URL buildURL(Integer movieId, String endpointURL) {
        Uri builtUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                .appendPath(movieId.toString())
                .appendPath(endpointURL)
                .appendQueryParameter(QUERY_API_KEY, api_key)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildYoutubeURL(String key) {
        Uri builtUri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_VIDEO_KEY, key)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream inputStream = httpURLConnection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");
            if (scanner.hasNext()) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            httpURLConnection.disconnect();
        }
    }

    public static Uri buildImageURL(String imagePath) {
        String correctedImagePath = imagePath.trim();
        if (correctedImagePath.startsWith("/")){
            // The api returns the image path in this format /sdkjdjfhs3kj34kdkjhkj33ASD.jpg
            // we got to take / out.
            correctedImagePath = correctedImagePath.substring(1);
        }
        return Uri.parse(MOVIE_IMG_BASE_URL).buildUpon()
                .appendPath(MOVIE_IMG_URL_SIZE)
                .appendPath(correctedImagePath)
                .build();
    }
}