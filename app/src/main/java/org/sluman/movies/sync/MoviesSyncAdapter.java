package org.sluman.movies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sluman.movies.BuildConfig;
import org.sluman.movies.R;
import org.sluman.movies.Utility;
import org.sluman.movies.data.MoviesContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Vector;

/**
 * Created by bryce on 3/17/16.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MoviesSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    ContentResolver mContentResolver;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");
        String movieType = Utility.getPreferredType(getContext());

        if (movieType.isEmpty()) {
            return;
        }
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            final String MOVIEDB_BASE_URL =
                    "https://api.themoviedb.org/3/movie/";
            final String TYPE_PATH = movieType;
            final String APPID_PARAM = "api_key";
            Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                    .appendPath(TYPE_PATH)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                    .build();
            Log.d(LOG_TAG, builtUri.toString());
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            getMovieDataFromJson(buffer.toString(), movieType);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
//             If the code didn't successfully get the weather data, there's no point in attempting
//             to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }
    public void getMovieDataFromJson(String forecastJsonStr,
                                       String movieType)
            throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // movie information
        final String OWM_POSTER_PATH = "poster_path";
        final String OWM_OVERVIEW = "overview";
        final String OWM_RELEASE_DATE = "release_date";
        final String OWM_MOVIE_ID = "id";
        final String OWM_TITLE = "title";
        final String OWM_VOTE_AVERAGE = "vote_average";


        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_RESULTS = "results";
        final String OWM_PAGE = "page";


        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            int pageNumber = forecastJson.getInt(OWM_PAGE);
            JSONArray movieArray = forecastJson.getJSONArray(OWM_RESULTS);


            // Insert the new movie information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());



            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            for (int i = 0; i < movieArray.length(); i++) {
                // These are the values that will be collected.

                long dateTime;
                String posterPath;
                String overview;
                String releaseDate;
                int movieId;
                String title;
                double voteAverage;

                // Get the JSON object representing the day
                JSONObject movieItem = movieArray.getJSONObject(i);

                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);

                posterPath = movieItem.getString(OWM_POSTER_PATH);
                overview = movieItem.getString(OWM_OVERVIEW);
                releaseDate = movieItem.getString(OWM_RELEASE_DATE);
                movieId = movieItem.getInt(OWM_MOVIE_ID);
                title = movieItem.getString(OWM_TITLE);
                voteAverage = movieItem.getDouble(OWM_VOTE_AVERAGE);

                ContentValues movieValues = new ContentValues();

                movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_DATE, dateTime);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_ID, movieId);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_TYPE, movieType);

                cVVector.add(movieValues);
            }

            int inserted = 0;
            int deleted = 0;
            // add to database
            if (cVVector.size() > 0) {
//              delete old records
                Calendar rightNow = Calendar.getInstance();
                rightNow.add(Calendar.DAY_OF_MONTH, -1);
                String where = MoviesContract.MovieEntry.COLUMN_DATE + " < ?";
                String[] args = new String[] { rightNow.getTimeInMillis()+"" };
                deleted = mContentResolver.delete(MoviesContract.MovieEntry.CONTENT_URI, where, args);
                //insert the new ones
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContentResolver.bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, cvArray);
            }
            Log.d(LOG_TAG, "getMovieDataFromJson Complete. " + deleted + " Deleted");
            Log.d(LOG_TAG, "getMovieDataFromJson Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
