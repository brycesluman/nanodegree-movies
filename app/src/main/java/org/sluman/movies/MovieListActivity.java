package org.sluman.movies;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.sluman.movies.data.MoviesContract;
import org.sluman.movies.sync.MoviesSyncAdapter;

import java.util.ArrayList;


/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MovieAdapter.OnInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        VideosAdapter.OnInteractionListener,
        MovieDetailFragment.FavoriteStatusListener {
    private int mPosition;

    private static final int MOVIE_LOADER = 2010;
    private static final int TYPE_LOADER = 2011;

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    // For the movie view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIE_COLUMNS = {
            MoviesContract.MovieEntry.TABLE_NAME + "." +
                    MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH
    };

    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these
    // must change.
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_POSTER_PATH = 2;

    private static final String[] TYPE_COLUMNS = {
            MoviesContract.TypeEntry._ID,
            MoviesContract.TypeEntry.COLUMN_FOREIGN_KEY
    };

    static final int COL_TYPE_ID = 0;
    static final int COL_TYPE_FK = 1;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private MovieAdapter mMovieAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SharedPreferences mPrefs;
    private static String[] mSelectItems;
    private static String[] mSelectValues;
    Toolbar mToolbar;

    private final String POPULAR = "popular";
    private final String TOP_RATED = "top_rated";
    private final String FAVORITES = "favorites";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        Resources res = getResources();
        mSelectItems = res.getStringArray(R.array.pref_options);
        mSelectValues = res.getStringArray(R.array.pref_values);

        mMovieAdapter = new MovieAdapter(this, null);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        updateTitle();


        mRecyclerView = (RecyclerView) findViewById(R.id.movie_list);

        assert mRecyclerView != null;
        setupRecyclerView(mRecyclerView);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            FragmentEmptyDetail fragment = new FragmentEmptyDetail();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG);
            ft.addToBackStack(null);
            ft.commit();
        }
        if (savedInstanceState == null) {
            MoviesSyncAdapter.initializeSyncAdapter(this);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        mPrefs = getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE);
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getSupportActionBar().getThemedContext());
                    builder.setTitle(R.string.pref_label)
                            .setItems(mSelectItems,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            SharedPreferences.Editor editor = mPrefs.edit();
                                            editor.putString(getString(R.string.pref_key), mSelectValues[which]);
                                            Log.d("MovieListActivity", mSelectValues[which]);
                                            if (!mSelectValues[which].equals(Utility.getPreferredType(getApplicationContext()))) {
                                                editor.apply();
                                            }
                                        }
                                    });
                    AlertDialog alertDialog = builder.create();

                    // show it
                    alertDialog.show();
                }
            });
        }
        setupLoader(MOVIE_LOADER);
        setupLoader(TYPE_LOADER);
    }

    private void setupLoader(int loaderId) {
        if (getLoaderManager().getLoader(loaderId) == null) {
            getSupportLoaderManager().initLoader(loaderId, null, this);
        } else {
            getSupportLoaderManager().restartLoader(loaderId, null, this);
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setAdapter(mMovieAdapter);
    }

    @Override
    protected void onPause() {
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case MOVIE_LOADER:
                String typeSetting = Utility.getPreferredType(this);
                Uri moviesForTypeUri = MoviesContract.MovieEntry.buildMoviesWithType(typeSetting);
                return new CursorLoader(this,
                        moviesForTypeUri,
                        MOVIE_COLUMNS,
                        null,
                        null,
                        null);
            case TYPE_LOADER:
                Uri typeForTypeUri = MoviesContract.TypeEntry.buildTypesWithType("favorites");
                return new CursorLoader(this,
                        typeForTypeUri,
                        TYPE_COLUMNS,
                        null,
                        null,
                        null);
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case MOVIE_LOADER:
                mMovieAdapter.swapCursor(cursor);
                break;
            case TYPE_LOADER:
                if (cursor.moveToFirst()) {
                    updateTypeList();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mMovieAdapter.swapCursor(null);
    }

    private void updateTypeList() {
        if (mSelectItems.length != 3) {
            String[] newArray = new String[3];
            newArray[0] = mSelectItems[0];
            newArray[1] = mSelectItems[1];
            newArray[2] = getString(R.string.favorites_label);
            mSelectItems = newArray;
            String[] newValArray = new String[3];
            newValArray[0] = mSelectValues[0];
            newValArray[1] = mSelectValues[1];
            newValArray[2] = getString(R.string.favorites_key);
            mSelectValues = newValArray;
        }
    }

    @Override
    public void listItemHasBeenClicked(View view, int position) {
        MovieAdapter.ViewHolder viewHolder = (MovieAdapter.ViewHolder) view.getTag();
        Uri uri = MoviesContract.MovieEntry.buildMoviesUri(viewHolder.mMovieId);
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieDetailFragment.ARG_URI, uri);
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG);
            ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.setData(uri);

            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_key))) {
            updateTitle();
            if (!getString(R.string.favorites_key).equals(Utility.getPreferredType(this))) {
                MoviesSyncAdapter.syncImmediately(this);
            }
            getSupportLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        }
    }

    private void updateTitle() {
        if (mToolbar != null) {
            switch (Utility.getPreferredType(this)) {
                case FAVORITES:
                    mToolbar.setTitle(getString(R.string.title_favorites));
                    break;
                case POPULAR:
                    mToolbar.setTitle(getString(R.string.title_popular));
                    break;
                case TOP_RATED:
                    mToolbar.setTitle(getString(R.string.title_top_rated));
                    break;
                default:
                    break;
            }
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

    }
}
