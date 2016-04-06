package org.sluman.movies;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by bryce on 3/18/16.
 */
public class Utility {
    private static final String POSTER_PATH = "http://image.tmdb.org/t/p/";
    private static final String POSTER_SIZE = "w185";

    public static String getPreferredType(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.pref_key), Context.MODE_PRIVATE);
        //PreferenceManager.getDefaultSharedPreferences(context);
        Log.d("Utility", prefs.getString(context.getString(R.string.pref_key),
                context.getString(R.string.popular)));
        return prefs.getString(context.getString(R.string.pref_key),
                context.getString(R.string.popular));
    }

    public static String getPosterPathForResource(String suffix) {
        return POSTER_PATH + POSTER_SIZE + suffix;
    }
}
