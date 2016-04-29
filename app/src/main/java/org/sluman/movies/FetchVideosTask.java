package org.sluman.movies;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sluman.movies.data.MoviesContract;
import org.sluman.movies.data.MoviesProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;


public class FetchVideosTask extends AsyncTask<Integer, Void, Void> {

    private final String LOG_TAG = FetchVideosTask.class.getSimpleName();

    private final Context mContext;

    public FetchVideosTask(Context context) {
        mContext = context;
    }

    private boolean DEBUG = true;

    private long getMovieDbId(int id) {
        long videoId = 0;
        // First, check if the location with this city name exists in the db
        Cursor videoCursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.buildMoviesUri(id),
                new String[]{MoviesContract.MovieEntry.COLUMN_ID},
                null,
                null,
                null);
        if (videoCursor.moveToFirst()) {
            int videoIdIndex = videoCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_ID);
            videoId = videoCursor.getLong(videoIdIndex);
        }

        videoCursor.close();
        return videoId;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public Void getVideosDataFromJson(String videosJsonStr,
                                      int id)
            throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_MDB_ID = "id";
        final String OWM_LIST = "results";
        final String OWM_ID = "id";
        final String OWM_KEY = "key";
        final String OWM_NAME = "name";
        final String OWM_SITE = "site";
        final String OWM_SIZE = "size";
        final String OWM_TYPE = "type";


        try {
            int moviedb_id;
            JSONObject videosJson = new JSONObject(videosJsonStr);
            JSONArray videosArray = videosJson.getJSONArray(OWM_LIST);

            moviedb_id = videosJson.getInt(OWM_MDB_ID);
            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(videosArray.length());


            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            for (int i = 0; i < videosArray.length(); i++) {
                // These are the values that will be collected.
                long dateTime;
                String video_id;
                String key;
                String name;
                String site;
                int size;
                String type;

                // Get the JSON object representing the day
                JSONObject videoObject = videosArray.getJSONObject(i);

                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);

                video_id = videoObject.getString(OWM_ID);
                key = videoObject.getString(OWM_KEY);
                name = videoObject.getString(OWM_NAME);
                site = videoObject.getString(OWM_SITE);
                size = videoObject.getInt(OWM_SIZE);
                type = videoObject.getString(OWM_TYPE);

                ContentValues videoValues = new ContentValues();

                videoValues.put(MoviesContract.VideoEntry.COLUMN_FOREIGN_KEY, id);
                videoValues.put(MoviesContract.VideoEntry.COLUMN_MOVIEDB_ID, moviedb_id);
                videoValues.put(MoviesContract.VideoEntry.COLUMN_VIDEO_ID, video_id);
                videoValues.put(MoviesContract.VideoEntry.COLUMN_KEY, key);
                videoValues.put(MoviesContract.VideoEntry.COLUMN_NAME, name);
                videoValues.put(MoviesContract.VideoEntry.COLUMN_SIZE, size);
                videoValues.put(MoviesContract.VideoEntry.COLUMN_TYPE, type);
                videoValues.put(MoviesContract.VideoEntry.COLUMN_SITE, site);

                cVVector.add(videoValues);
            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MoviesContract.VideoEntry.CONTENT_URI, cvArray);
            }
            Log.d(LOG_TAG, "FetchVideosTask Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Void doInBackground(Integer... params) {
        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }
        int MOVIE_ID = params[0];

        long MOVIEDB_ID = getMovieDbId(MOVIE_ID);
        Log.d("FetchVideosTask", "movieDbId: " + MOVIEDB_ID);
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String videosJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String VIDEOS_BASE_URL =
                    "https://api.themoviedb.org/3/movie/";
            final String APPID_PARAM = "api_key";
            Uri builtUri = Uri.parse(VIDEOS_BASE_URL).buildUpon()
                    .appendPath(String.valueOf(MOVIEDB_ID))
                    .appendPath("videos")
                    .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                    .build();
            Log.d(LOG_TAG, builtUri.toString());


            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
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
                return null;
            }
            videosJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
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

        try {
            return getVideosDataFromJson(videosJsonStr, MOVIE_ID);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }
}
