package com.anishsainju.udacity.popularmovies.utilities;

import com.anishsainju.udacity.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    /**
     * Sample json output for
     * http://api.themoviedb.org/3/movie/popular?api_key=[API_KEY]
     * <p>
     * <p>
     * {
     * "page": 1,
     * "total_results": 19872,
     * "total_pages": 994,
     * "results": [
     * {
     * "vote_count": 3581,
     * "id": 299536,
     * "video": false,
     * "vote_average": 8.5,
     * "title": "Avengers: Infinity War",
     * "popularity": 544.063527,
     * "poster_path": "/7WsyChQLEftFiDOVTGkv3hFpyyt.jpg",
     * "original_language": "en",
     * "original_title": "Avengers: Infinity War",
     * "genre_ids": [
     * 12,
     * 878,
     * 14,
     * 28
     * ],
     * "backdrop_path": "/bOGkgRGdhrBYJSLpXaxhXVstddV.jpg",
     * "adult": false,
     * "overview": "As the Avengers and their allies have continued to protect the world from threats too large for any one hero to handle, a new danger has emerged from the cosmic shadows: Thanos. A despot of intergalactic infamy, his goal is to collect all six Infinity Stones, artifacts of unimaginable power, and use them to inflict his twisted will on all of reality. Everything the Avengers have fought for has led up to this moment - the fate of Earth and existence itself has never been more uncertain.",
     * "release_date": "2018-04-25"
     * },
     * {
     * "vote_count": 1823,
     * "id": 337167,
     * "video": false,
     * "vote_average": 6,
     * "title": "Fifty Shades Freed",
     * "popularity": 543.182383,
     * "poster_path": "/jjPJ4s3DWZZvI4vw8Xfi4Vqa1Q8.jpg",
     * "original_language": "en",
     * "original_title": "Fifty Shades Freed",
     * "genre_ids": [
     * 18,
     * 10749
     * ],
     * "backdrop_path": "/9ywA15OAiwjSTvg3cBs9B7kOCBF.jpg",
     * "adult": false,
     * "overview": "Believing they have left behind shadowy figures from their past, newlyweds Christian and Ana fully embrace an inextricable connection and shared life of luxury. But just as she steps into her role as Mrs. Grey and he relaxes into an unfamiliar stability, new threats could jeopardize their happy ending before it even begins.",
     * "release_date": "2018-02-07"
     * },
     * ...
     */
    public static final String RESULTS = "results";
    public static final String VOTE_COUNT = "vote_count";
    public static final String ID = "id";
    public static final String VIDEO = "video";
    public static final String VOTE_AVERAGE = "vote_average";
    public static final String TITLE = "title";
    public static final String POPULARITY = "popularity";
    public static final String POSTER_PATH = "poster_path";
    public static final String ORIGINAL_LANGUAGE = "original_language";
    public static final String ORIGINAL_TITLE = "original_title";
    public static final String GENRE_IDS = "genre_ids";
    public static final String BACKDROP_PATH = "backdrop_path";
    public static final String ADULT = "adult";
    public static final String OVERVIEW = "overview";
    public static final String RELEASE_DATE = "release_date";


    public static List<Movie> parseMoviesJson(String jsonMoviesResponse) throws JSONException {

        JSONObject moviesData = new JSONObject(jsonMoviesResponse);
        JSONArray moviesList = moviesData.getJSONArray(RESULTS);

        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < moviesList.length(); i++) {
            JSONObject movie = moviesList.getJSONObject(i);

            Integer voteCount = movie.getInt(VOTE_COUNT);
            Integer id = movie.getInt(ID);
            Boolean video = movie.getBoolean(VIDEO);
            Double voteAverage = movie.getDouble(VOTE_AVERAGE);
            String title = movie.getString(TITLE);
            Double popularity = movie.getDouble(POPULARITY);
            String posterPath = movie.getString(POSTER_PATH);
            String originalLanguage = movie.getString(ORIGINAL_LANGUAGE);
            String originalTitle = movie.getString(ORIGINAL_TITLE);

            JSONArray genreIdsArray = movie.getJSONArray(GENRE_IDS);
            int genreIdsSize = genreIdsArray.length();
            int[] genreIds = new int [genreIdsSize];
            for (int j = 0; j < genreIdsSize; j++) {
                genreIds[j] = genreIdsArray.getInt(j);
            }

            String backdropPath = movie.getString(BACKDROP_PATH);
            Boolean adult = movie.getBoolean(ADULT);
            String overview = movie.getString(OVERVIEW);
            String releaseDate = movie.getString(RELEASE_DATE);

            Movie thisMovie = new Movie(
                    voteCount,
                    id,
                    video,
                    voteAverage,
                    title,
                    popularity,
                    posterPath,
                    originalLanguage,
                    originalTitle,
                    genreIds,
                    backdropPath,
                    adult,
                    overview,
                    releaseDate);
            movies.add(thisMovie);
        }
        return movies;
    }
}