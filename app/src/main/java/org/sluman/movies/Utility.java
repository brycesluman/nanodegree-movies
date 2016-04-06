package org.sluman.movies;

import android.content.Context;
import android.content.SharedPreferences;

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
}
