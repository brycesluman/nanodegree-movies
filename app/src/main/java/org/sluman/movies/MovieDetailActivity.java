package org.sluman.movies;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Movie;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import org.sluman.movies.data.MoviesContract;

/**
 * An activity representing a single Movie detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MovieListActivity}.
 */
public class MovieDetailActivity extends AppCompatActivity implements
        VideosAdapter.OnInteractionListener,
        MovieDetailFragment.FavoriteStatusListener {
    FloatingActionButton mFab;
    boolean mFavoriteStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);


        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieDetailFragment.ARG_URI,
                    getIntent().getData());
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();

            mFab = (FloatingActionButton) findViewById(R.id.fab_detail);
            if (mFab != null) {
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri inUri = getIntent().getData();
                        if (mFavoriteStatus) {
                            Utility.removeFavorite(getApplicationContext(),
                                    MoviesContract.MovieEntry.getMovieIdFromUri(inUri));
                        } else {
                            Utility.addFavorite(getApplicationContext(), MoviesContract.MovieEntry.getMovieIdFromUri(inUri));
                        }
                    }
                });
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, MovieListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void videoItemHasBeenClicked(View view, int position) {
        VideosAdapter.ViewHolder viewHolder = (VideosAdapter.ViewHolder) view.getTag();
        String key = viewHolder.mVideoId;
        if (key != null) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + key)));
        }
    }

    @Override
    public void favoriteStatus(boolean favorited) {
        mFavoriteStatus = favorited;
        if (mFab == null) {
            return;
        }
        if (favorited) {
            mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_white_24dp, getTheme()));
        } else {
            mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_border_white_24dp, getTheme()));
        }
    }
}
