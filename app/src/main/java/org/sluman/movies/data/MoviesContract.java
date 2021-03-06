package org.sluman.movies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by bryce on 3/17/16.
 */
public class MoviesContract {
    public static final String CONTENT_AUTHORITY = "org.sluman.movies.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";
    public static final String PATH_VIDEOS = "videos";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_TYPES = "types";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        //columns
        public static final String COLUMN_ID = "movie_id";
        public static final String COLUMN_DATE = "download_date";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_MOVIE_TYPE = "content_type";

        public static Uri buildMoviesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMoviesWithType(String type) {
            return CONTENT_URI.buildUpon().appendPath(type).build();
        }

        public static int getMovieIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static String getMoviesTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class VideoEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEOS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;

        public static final String TABLE_NAME = "videos";


        public static final String COLUMN_VIDEO_ID = "id";
        public static final String COLUMN_FOREIGN_KEY = "foreign_key";
        public static final String COLUMN_MOVIEDB_ID = "movie_id";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_TYPE = "type";


        public static Uri buildVideosUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static int getVideoIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

    public static final class ReviewEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        public static final String TABLE_NAME = "reviews";
//        {
//            "id": "56f4f0bd9251417a440017bd",
//                "author": "Rahul Gupta",
//                "content": "Awesome moview. Best Action sequence.\r\n\r\n**Slow in the first half**",
//                "url": "https://www.themoviedb.org/review/56f4f0bd9251417a440017bd"
//        }

        public static final String COLUMN_MOVIEDB_ID = "moviedb_id";
        public static final String COLUMN_FOREIGN_KEY = "foreign_key";
        public static final String COLUMN_REVIEW_ID = "id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";

        public static Uri buildReviewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static int getReviewIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

    public static final class TypeEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TYPES).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TYPES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TYPES;

        public static final String TABLE_NAME = "types";

        public static final String COLUMN_FOREIGN_KEY = "foreign_key";

        public static final String COLUMN_TYPE = "type";

        public static Uri buildTypeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static int getTypeIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(2));
        }

        public static String getTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildTypesWithTypeAndId(String type, int id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(type)
                    .appendPath(Integer.toString(id))
                    .build();
        }

        public static Uri buildTypesWithType(String type) {
            return CONTENT_URI.buildUpon()
                    .appendPath(type)
                    .build();
        }
    }
}
