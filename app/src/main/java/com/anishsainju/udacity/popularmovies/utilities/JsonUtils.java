package com.anishsainju.udacity.popularmovies.utilities;

import com.anishsainju.udacity.popularmovies.model.Movie;
import com.anishsainju.udacity.popularmovies.model.Review;
import com.anishsainju.udacity.popularmovies.model.Video;

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
    private static final String RESULTS = "results";
    private static final String VOTE_COUNT = "vote_count";
    private static final String ID = "id";
    private static final String VIDEO = "video";
    private static final String VOTE_AVERAGE = "vote_average";
    private static final String TITLE = "title";
    private static final String POPULARITY = "popularity";
    private static final String POSTER_PATH = "poster_path";
    private static final String ORIGINAL_LANGUAGE = "original_language";
    private static final String ORIGINAL_TITLE = "original_title";
    private static final String GENRE_IDS = "genre_ids";
    private static final String BACKDROP_PATH = "backdrop_path";
    private static final String ADULT = "adult";
    private static final String OVERVIEW = "overview";
    private static final String RELEASE_DATE = "release_date";

    private static final String VIDEO_KEY = "key";
    private static final String VIDEO_NAME = "name";
    private static final String VIDEO_SITE = "site";
    private static final String VIDEO_SIZE = "size";
    private static final String VIDEO_TYPE = "type";

    private static final String REVIEW_AUTHOR = "author";
    private static final String REVIEW_CONTENT = "content";
    private static final String REVIEW_URL = "url";

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
            String backdropPath = movie.getString(BACKDROP_PATH);
            Boolean adult = movie.getBoolean(ADULT);
            String overview = movie.getString(OVERVIEW);
            String releaseDate = movie.getString(RELEASE_DATE);

            Movie thisMovie = new Movie(
                    id,
                    voteCount,
                    video,
                    voteAverage,
                    title,
                    popularity,
                    posterPath,
                    originalLanguage,
                    originalTitle,
                    backdropPath,
                    adult,
                    overview,
                    releaseDate);
            movies.add(thisMovie);
        }
        return movies;
    }


    /*
    {
        "id": "5973c4f79251415836004bc3",
        "iso_639_1": "en",
        "iso_3166_1": "US",
        "key": "dtwpjnuaVTE",
        "name": "Ready Player One - SDCC Teaser [HD]",
        "site": "YouTube",
        "size": 1080,
        "type": "Teaser"
        },
        {
        "id": "5a2d8dbf0e0a264cd0153de9",
        "iso_639_1": "en",
        "iso_3166_1": "US",
        "key": "cSp1dM2Vj48",
        "name": "READY PLAYER ONE - Official Trailer 1 [HD]",
        "site": "YouTube",
        "size": 1080,
        "type": "Trailer"
        },
        {
        "id": "5aa0b7f40e0a2629520002e0",
        "iso_639_1": "en",
        "iso_3166_1": "US",
        "key": "D_eZxSYRhco",
        "name": "READY PLAYER ONE - Come With Me",
        "site": "YouTube",
        "size": 1080,
        "type": "Trailer"
        },
        {
        "id": "5aa0b83c9251414463000328",
        "iso_639_1": "en",
        "iso_3166_1": "US",
        "key": "DlU4ZSU2xzg",
        "name": "READY PLAYER ONE - Change the World",
        "site": "YouTube",
        "size": 1080,
        "type": "Teaser"
    }
    * */
    public static List<Video> parseVideosJson(String jsonVideosResponse) throws JSONException {

        JSONObject videoData = new JSONObject(jsonVideosResponse);
        JSONArray videosList = videoData.getJSONArray(RESULTS);

        List<Video> videos = new ArrayList<>();
        for (int i = 0; i < videosList.length(); i++) {
            JSONObject video = videosList.getJSONObject(i);

            String id = video.getString(ID);
            String key = video.getString(VIDEO_KEY);
            String name = video.getString(VIDEO_NAME);
            String site = video.getString(VIDEO_SITE);
            String size = video.getString(VIDEO_SIZE);
            String type = video.getString(VIDEO_TYPE);

            Video thisVideo = new Video(id, key, name, site, size, type);
            videos.add(thisVideo);
        }
        return videos;
    }

    /**
     * Sample format
     *
     "author": "Columbusbuck",
     "content": "There's a moment in this movie when the central character says "I love her". His friend hastily replies, "Slow down bro... she could be a 300 lb man living in his mom's basement." In the very brief beat between the two lines, I thought FINALLY a movie about falling in love completely independent of what a person's genetics may be. And then the crushing cynicism in a dystopian world where there is little to live for and even less to hope for: even in a nightmare, a fat man is undateable.

     Don't worry though. If you have the skills you may end up on top. Just like Ben Mendelsohn's slave-owning antagonist almost did. Because in this story, it's not a world where anyone feels compassion or empathy. It's a world where everyone only thinks of themselves. A sociopath's dream. And that's what earns the top prize.

     I love looking at Tye Sheridan. But not enough to sit through this again.",
     "id": "5ac333d092514126c302d333",
     "url": "https://www.themoviedb.org/review/5ac333d092514126c302d333"
     */
    public static List<Review> parseReviewsJson(String jsonReviewsResponse) throws JSONException {

        JSONObject reviewData = new JSONObject(jsonReviewsResponse);
        JSONArray reviewsList = reviewData.getJSONArray(RESULTS);

        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < reviewsList.length(); i++) {
            JSONObject review = reviewsList.getJSONObject(i);

            String id = review.getString(ID);
            String author = review.getString(REVIEW_AUTHOR);
            String content = review.getString(REVIEW_CONTENT);
            String url = review.getString(REVIEW_URL);

            Review thisReview = new Review(author, content, id, url);
            reviews.add(thisReview);
        }
        return reviews;
    }
}