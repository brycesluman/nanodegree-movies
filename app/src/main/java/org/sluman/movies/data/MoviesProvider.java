package org.sluman.movies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by bryce on 3/17/16.
 */
public class MoviesProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;
    static final int MOVIES = 100;
    static final int MOVIE_BY_ID = 101;
    static final int MOVIES_BY_TYPE = 102;
    static final int VIDEOS = 103;
    static final int REVIEWS = 104;
    static final int VIDEOS_BY_ID = 105;
    static final int REVIEWS_BY_ID = 106;
    static final int TYPES = 109;
    static final int TYPES_BY_ID = 110;
    static final int TYPES_BY_TYPE_AND_ID = 111;
    static final int TYPES_BY_TYPE = 112;

    private static final SQLiteQueryBuilder sMoviesSettingQueryBuilder;
    private static final SQLiteQueryBuilder sVideosSettingQueryBuilder;
    private static final SQLiteQueryBuilder sReviewsSettingQueryBuilder;
    private static final SQLiteQueryBuilder sMoviesByTypeSettingQueryBuilder;
    private static final SQLiteQueryBuilder sTypesSettingQueryBuilder;

    static {
        sMoviesSettingQueryBuilder = new SQLiteQueryBuilder();

        sMoviesSettingQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME);

        sMoviesByTypeSettingQueryBuilder = new SQLiteQueryBuilder();

        sMoviesByTypeSettingQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME + " INNER JOIN " +
                        MoviesContract.TypeEntry.TABLE_NAME +
                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
                        "." + MoviesContract.MovieEntry._ID +
                        " = " + MoviesContract.TypeEntry.TABLE_NAME +
                        "." + MoviesContract.TypeEntry.COLUMN_FOREIGN_KEY);

        sVideosSettingQueryBuilder = new SQLiteQueryBuilder();

        sVideosSettingQueryBuilder.setTables(
                MoviesContract.VideoEntry.TABLE_NAME);

        sReviewsSettingQueryBuilder = new SQLiteQueryBuilder();

        sReviewsSettingQueryBuilder.setTables(
                MoviesContract.ReviewEntry.TABLE_NAME);

        sTypesSettingQueryBuilder = new SQLiteQueryBuilder();

        sTypesSettingQueryBuilder.setTables(
                MoviesContract.TypeEntry.TABLE_NAME);
    }


    //movies.movie_id = ?
    private static final String sMovieIdSelection =
            MoviesContract.MovieEntry.TABLE_NAME +
                    "." + MoviesContract.MovieEntry._ID + " = ? ";


    //movies.movie_type = ?
    private static final String sMovieTypeSelection =
            MoviesContract.TypeEntry.TABLE_NAME +
                    "." + MoviesContract.TypeEntry.COLUMN_TYPE + " = ? ";

    //videos.movie_id = ?
    private static final String sVideoIdSelection =
            MoviesContract.VideoEntry.TABLE_NAME +
                    "." + MoviesContract.VideoEntry.COLUMN_FOREIGN_KEY + " = ? ";
    //reviews.movie_id = ?
    private static final String sReviewIdSelection =
            MoviesContract.ReviewEntry.TABLE_NAME +
                    "." + MoviesContract.ReviewEntry.COLUMN_FOREIGN_KEY + " = ? ";

    //types.movie_id = ?
    private static final String sTypesIdSelection =
            MoviesContract.TypeEntry.TABLE_NAME +
                    "." + MoviesContract.TypeEntry.COLUMN_FOREIGN_KEY + " = ? ";
    //types.type = ?
    private static final String sTypesByTypeSelection =
            MoviesContract.TypeEntry.TABLE_NAME +
                    "." + MoviesContract.TypeEntry.COLUMN_TYPE + " = ? ";

    //types.movie_id = ? & types.type = ?
    private static final String sTypesIdByTypeSelection =
            MoviesContract.TypeEntry.TABLE_NAME +
                    "." + MoviesContract.TypeEntry.COLUMN_FOREIGN_KEY + " = ? AND " +
                    MoviesContract.TypeEntry.TABLE_NAME +
                    "." + MoviesContract.TypeEntry.COLUMN_TYPE + " = ? ";

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.

        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/#", MOVIE_BY_ID);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/*/", MOVIES_BY_TYPE);

        matcher.addURI(authority, MoviesContract.PATH_VIDEOS, VIDEOS);
        matcher.addURI(authority, MoviesContract.PATH_VIDEOS + "/#", VIDEOS_BY_ID);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS, REVIEWS);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS + "/#", REVIEWS_BY_ID);

        matcher.addURI(authority, MoviesContract.PATH_TYPES, TYPES);
        matcher.addURI(authority, MoviesContract.PATH_TYPES + "/#", TYPES_BY_ID);
        matcher.addURI(authority, MoviesContract.PATH_TYPES + "/*", TYPES_BY_TYPE);
        matcher.addURI(authority, MoviesContract.PATH_TYPES + "/*/#", TYPES_BY_TYPE_AND_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movies"
            case MOVIES: {
                retCursor = getMovies(uri, projection, sortOrder);
                break;
            }
            // "movies/#"
            case MOVIE_BY_ID: {
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            }

            // "movies/*/"
            case MOVIES_BY_TYPE: {
                retCursor = getMoviesByType(uri, projection, sortOrder);
                break;
            }

            // "videos/#"
            case VIDEOS_BY_ID: {
                retCursor = getVideosById(uri, projection, sortOrder);
                break;
            }

            // "reviews/#"
            case REVIEWS_BY_ID: {
                retCursor = getReviewsById(uri, projection, sortOrder);
                break;
            }

            // "types/#"
            case TYPES_BY_ID: {
                retCursor = getTypesById(uri, projection, sortOrder);
                break;
            }
            // "types/*"
            case TYPES_BY_TYPE: {
                retCursor = getTypesByType(uri, projection, sortOrder);
                break;
            }
            // "types/*/#"
            case TYPES_BY_TYPE_AND_ID: {
                retCursor = getTypesByTypeAndId(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getMovies(Uri uri, String[] projection, String sortOrder) {
        return sMoviesSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    public Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        int movieId = MoviesContract.MovieEntry.getMovieIdFromUri(uri);
        String[] selectionArgs;
        String selection;

        selection = sMovieIdSelection;
        selectionArgs = new String[]{String.valueOf(movieId)};


        return sMoviesSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getVideosById(Uri uri, String[] projection, String sortOrder) {
        Log.d("MoviesProvider", "Videos URI: " + uri);
        int movieId = MoviesContract.VideoEntry.getVideoIdFromUri(uri);
        String[] selectionArgs;
        String selection;

        selection = sVideoIdSelection;
        selectionArgs = new String[]{String.valueOf(movieId)};


        return sVideosSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getReviewsById(Uri uri, String[] projection, String sortOrder) {
        int reviewId = MoviesContract.ReviewEntry.getReviewIdFromUri(uri);
        String[] selectionArgs;
        String selection;

        selection = sReviewIdSelection;
        selectionArgs = new String[]{String.valueOf(reviewId)};


        return sReviewsSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTypesById(Uri uri, String[] projection, String sortOrder) {
        int typeId = MoviesContract.TypeEntry.getTypeIdFromUri(uri);
        String[] selectionArgs;
        String selection;

        selection = sTypesIdSelection;
        selectionArgs = new String[]{String.valueOf(typeId)};


        return sTypesSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTypesByType(Uri uri, String[] projection, String sortOrder) {
        String type = MoviesContract.TypeEntry.getTypeFromUri(uri);
        String[] selectionArgs;
        String selection;

        selection = sTypesByTypeSelection;
        selectionArgs = new String[]{type};


        return sTypesSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTypesByTypeAndId(Uri uri, String[] projection, String sortOrder) {
        int typeId = MoviesContract.TypeEntry.getTypeIdFromUri(uri);
        String type = MoviesContract.TypeEntry.getTypeFromUri(uri);
        String[] selectionArgs;
        String selection;

        selection = sTypesIdByTypeSelection;
        selectionArgs = new String[]{String.valueOf(typeId), type};


        return sTypesSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMoviesByType(Uri uri, String[] projection, String sortOrder) {
        String movieType = MoviesContract.MovieEntry.getMoviesTypeFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sMovieTypeSelection;
        selectionArgs = new String[]{movieType};

        return sMoviesByTypeSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.MovieEntry.buildMoviesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case VIDEOS: {
                long _id = db.insert(MoviesContract.VideoEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.VideoEntry.buildVideosUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS: {
                long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.ReviewEntry.buildReviewsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TYPES: {
                long _id = db.insert(MoviesContract.TypeEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.TypeEntry.buildTypeUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case VIDEOS:
                rowsDeleted = db.delete(
                        MoviesContract.VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEWS:
                rowsDeleted = db.delete(
                        MoviesContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TYPES:
                rowsDeleted = db.delete(
                        MoviesContract.TypeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case VIDEOS:
                rowsUpdated = db.update(MoviesContract.VideoEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REVIEWS:
                rowsUpdated = db.update(MoviesContract.ReviewEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TYPES:
                rowsUpdated = db.update(MoviesContract.TypeEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                returnCount = 0;
                String selection = MoviesContract.MovieEntry.COLUMN_ID + "=?";
                String[] selectionArgs;
                try {
                    for (ContentValues value : values) {
                        //doing 'upsert' to preserve foreign key
                        selectionArgs = new String[] {value.getAsString(MoviesContract.MovieEntry.COLUMN_ID)};
                        int affected = db.update(MoviesContract.MovieEntry.TABLE_NAME,
                                value, selection, selectionArgs);
                        if (affected == 0) {
                            long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, value);
                            //add entry to pivot table
                            ContentValues movieValues = new ContentValues();
                            movieValues.put(MoviesContract.TypeEntry.COLUMN_FOREIGN_KEY, _id);
                            movieValues.put(MoviesContract.TypeEntry.COLUMN_TYPE,
                                    value.getAsString(MoviesContract.MovieEntry.COLUMN_MOVIE_TYPE));
                            db.insert(MoviesContract.TypeEntry.TABLE_NAME, null, movieValues);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case VIDEOS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.VideoEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case REVIEWS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case TYPES:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.TypeEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // Method to assist the testing
    // framework in running smoothly. Read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
