package org.sluman.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.sluman.movies.data.MoviesContract.MovieEntry;

/**
 * Created by bryce on 3/17/16.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("MoviesDbHelper", "Create Tables");
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_ID + " INTEGER UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_TYPE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " BLOB NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                " UNIQUE (" + MovieEntry.COLUMN_ID + ") ON CONFLICT IGNORE);";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);

        final String SQL_CREATE_VIDEOS_TABLE = "CREATE TABLE " + MoviesContract.VideoEntry.TABLE_NAME + " (" +
                MoviesContract.VideoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesContract.VideoEntry.COLUMN_FOREIGN_KEY + " INTEGER NOT NULL, " +
                MoviesContract.VideoEntry.COLUMN_VIDEO_ID + " INTEGER UNIQUE NOT NULL, " +
                MoviesContract.VideoEntry.COLUMN_MOVIEDB_ID + " INTEGER NOT NULL, " +
                MoviesContract.VideoEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                MoviesContract.VideoEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                MoviesContract.VideoEntry.COLUMN_SITE + " TEXT NOT NULL, " +
                MoviesContract.VideoEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                MoviesContract.VideoEntry.COLUMN_SIZE + " REAL NOT NULL, " +
                " FOREIGN KEY (" + MoviesContract.VideoEntry.COLUMN_FOREIGN_KEY + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "), " +
                " UNIQUE (" + MoviesContract.VideoEntry.COLUMN_VIDEO_ID + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_VIDEOS_TABLE);

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + MoviesContract.ReviewEntry.TABLE_NAME + " (" +
                MoviesContract.ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesContract.ReviewEntry.COLUMN_FOREIGN_KEY + " INTEGER NOT NULL, " +
                MoviesContract.ReviewEntry.COLUMN_REVIEW_ID + " INTEGER UNIQUE NOT NULL, " +
                MoviesContract.ReviewEntry.COLUMN_MOVIEDB_ID + " INTEGER NOT NULL, " +
                MoviesContract.ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                MoviesContract.ReviewEntry.COLUMN_CONTENT + " BLOB NOT NULL, " +
                MoviesContract.ReviewEntry.COLUMN_URL + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + MoviesContract.ReviewEntry.COLUMN_FOREIGN_KEY + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "), " +
                " UNIQUE (" + MoviesContract.ReviewEntry.COLUMN_REVIEW_ID + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
        final String SQL_CREATE_TYPES_TABLE = "CREATE TABLE " + MoviesContract.TypeEntry.TABLE_NAME + " (" +
                MoviesContract.TypeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesContract.TypeEntry.COLUMN_FOREIGN_KEY + " INTEGER NOT NULL, " +
                MoviesContract.TypeEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + MoviesContract.TypeEntry.COLUMN_FOREIGN_KEY + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "), " +
                " UNIQUE (" + MoviesContract.TypeEntry.COLUMN_FOREIGN_KEY +
                ", " + MoviesContract.TypeEntry.COLUMN_TYPE + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_TYPES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d("MoviesDbHelper", "Upgrade Tables");
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.VideoEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.ReviewEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.TypeEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
