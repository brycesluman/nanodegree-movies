package org.sluman.movies;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;

import org.sluman.movies.data.MoviesContract;

/**
 * Created by bryce on 3/18/16.
 */
public class Utility {
    private static final String POSTER_PATH = "http://image.tmdb.org/t/p/";
    private static final String POSTER_SIZE = "w185";

    public static String getPreferredType(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.pref_key), Context.MODE_PRIVATE);
        return prefs.getString(context.getString(R.string.pref_key),
                context.getString(R.string.popular));
    }

    public static String getPosterPathForResource(String suffix) {
        return POSTER_PATH + POSTER_SIZE + suffix;
    }

    public static void addFavorite(Context context, int id) {
        ContentValues values = new ContentValues();

        values.put(
                MoviesContract.TypeEntry.COLUMN_FOREIGN_KEY,
                id);
        values.put(
                MoviesContract.TypeEntry.COLUMN_TYPE,
                "favorites");

        context.getContentResolver().insert(
                MoviesContract.TypeEntry.CONTENT_URI,
                values
        );
    }

    public static void removeFavorite(Context context, int id) {
        context.getContentResolver().delete(
                MoviesContract.TypeEntry.CONTENT_URI,
                MoviesContract.TypeEntry.COLUMN_FOREIGN_KEY + " = ? AND " +
                        MoviesContract.TypeEntry.COLUMN_TYPE + " = ? ",
                new String[]{
                        Integer.toString(id),
                        "favorites"
                }
        );
    }
}
