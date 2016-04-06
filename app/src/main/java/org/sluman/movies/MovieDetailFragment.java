package org.sluman.movies;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.sluman.movies.data.MoviesContract;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int MOVIE_LOADER = 0;
    private Uri mUri;
    // For the movie view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIE_COLUMNS = {

            MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH
    };

    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these
    // must change.
    static final int COL_MOVIE_ID = 0;
    static final int COL_TITLE = 1;
    static final int COL_VOTE_AVERAGE = 2;
    static final int COL_OVERVIEW = 3;
    static final int COL_RELEASE_DATE = 4;
    static final int COL_POSTER_PATH = 5;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_URI = "uri";
    CollapsingToolbarLayout mAppBarLayout;

    private ImageView mIconView;
    private TextView mDescription;
    private TextView mVoteAverage;
    private TextView mReleaseDate;
    private TextView mTitle;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_URI)) {
            mUri = getArguments().getParcelable(ARG_URI);
            if(mUri!=null) {
                Log.d("MovieDetailFragment", mUri.toString());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        Activity activity = this.getActivity();
        mAppBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        mIconView = (ImageView) rootView.findViewById(R.id.movie_poster);
        mVoteAverage = (TextView) rootView.findViewById(R.id.movie_vote_average);
        mDescription = (TextView) rootView.findViewById(R.id.movie_description);
        mReleaseDate = (TextView) rootView.findViewById(R.id.movie_year);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MovieDetailFragment.ARG_URI, mUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if(mUri!=null) {
            return new CursorLoader(getActivity(),
                    mUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(cursor!=null && cursor.moveToFirst()) {
            if(mAppBarLayout!=null) {
                mAppBarLayout.setTitle(cursor.getString(COL_TITLE));
            }

            Log.d("MovieDetailFragment", cursor.getString(MovieDetailFragment.COL_TITLE));
            String posterSuffix = cursor.getString(MovieDetailFragment.COL_POSTER_PATH);
            Picasso.with(getActivity()).load(Utility.getPosterPathForResource(posterSuffix)).into(mIconView);
            mDescription.setText(cursor.getString(MovieDetailFragment.COL_OVERVIEW));
            mReleaseDate.setText(cursor.getString(MovieDetailFragment.COL_RELEASE_DATE));
            mVoteAverage.setText(cursor.getString(MovieDetailFragment.COL_VOTE_AVERAGE));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {}
}
