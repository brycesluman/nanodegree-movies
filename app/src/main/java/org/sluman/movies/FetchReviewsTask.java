package org.sluman.movies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sluman.movies.data.MoviesContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by bryce on 4/8/16.
 */
public class FetchReviewsTask extends AsyncTask<Integer, Void, Void> {

    private final String LOG_TAG = FetchVideosTask.class.getSimpleName();

    private final Context mContext;

    public FetchReviewsTask(Context context) {
        mContext = context;
    }

    private boolean DEBUG = true;

    private long getMovieDbId(int id) {
        long videoId = 0;

        // First, check if the location with this city name exists in the db
        Cursor reviewCursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.buildMoviesUri(id),
                new String[]{MoviesContract.MovieEntry.COLUMN_ID},
                null,
                null,
                null);

        if (reviewCursor.moveToFirst()) {
            int videoIdIndex = reviewCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_ID);
            videoId = reviewCursor.getLong(videoIdIndex);
        }

        reviewCursor.close();
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
        Log.d("FetchVideosTask", "MovieId: " + id);
        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

//        {
//            "id": 209112,
//                "page": 1,
//                "results": [
//            {
//                "id":"56f4f0bd9251417a440017bd",
//                    "author":"Rahul Gupta",
//                    "content":
//                "Awesome moview. Best Action sequence.\r\n\r\n**Slow in the first half**",
//                        "url":"https://www.themoviedb.org/review/56f4f0bd9251417a440017bd"
//            }
//        }
        // These are the names of the JSON objects that need to be extracted.
        final String OWM_MDB_ID = "id";
        final String OWM_LIST = "results";
        final String OWM_ID = "id";
        final String OWM_AUTHOR = "author";
        final String OWM_CONTENT = "content";
        final String OWM_URL = "url";

        try {
            int moviedb_id;
            JSONObject reviewsJson = new JSONObject(videosJsonStr);
            JSONArray reviewsArray = reviewsJson.getJSONArray(OWM_LIST);


            moviedb_id = reviewsJson.getInt(OWM_MDB_ID);
            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(reviewsArray.length());


            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            for (int i = 0; i < reviewsArray.length(); i++) {
                // These are the values that will be collected.
                long dateTime;
                String review_id;
                String author;
                String content;
                String url;

                // Get the JSON object representing the day
                JSONObject reviewObject = reviewsArray.getJSONObject(i);

                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);


                review_id = reviewObject.getString(OWM_ID);
                author = reviewObject.getString(OWM_AUTHOR);
                content = reviewObject.getString(OWM_CONTENT);
                url = reviewObject.getString(OWM_URL);

                ContentValues reviewValues = new ContentValues();

                reviewValues.put(MoviesContract.ReviewEntry.COLUMN_FOREIGN_KEY, id);
                reviewValues.put(MoviesContract.ReviewEntry.COLUMN_MOVIEDB_ID, moviedb_id);
                reviewValues.put(MoviesContract.ReviewEntry.COLUMN_REVIEW_ID, review_id);
                reviewValues.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, author);
                reviewValues.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, content);
                reviewValues.put(MoviesContract.ReviewEntry.COLUMN_URL, url);
                cVVector.add(reviewValues);
            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchReviewsTask Complete. " + inserted + " Inserted");

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
        Log.d("FetchReviewsTask", "MovieDbId: " + MOVIEDB_ID);
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String reviewsJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String BASE_URL =
                    "https://api.themoviedb.org/3/movie/";
            final String APPID_PARAM = "api_key";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(String.valueOf(MOVIEDB_ID))
                    .appendPath("reviews")
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
            reviewsJsonStr = buffer.toString();
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
            return getVideosDataFromJson(reviewsJsonStr, MOVIE_ID);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

}
