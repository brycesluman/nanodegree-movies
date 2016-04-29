package org.sluman.movies;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.sluman.movies.data.MoviesContract;
import org.sluman.movies.data.MoviesProvider;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private boolean mFavorited = false;
    private FavoriteStatusListener mListener;

    private static final int MOVIE_LOADER = 0;
    private static final int VIDEO_LOADER = 1;
    private static final int REVIEW_LOADER = 2;
    private static final int TYPE_LOADER = 4;
    private Uri mUri;
    // For the movie view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIE_COLUMNS = {

            MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH
    };

    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these
    // must change.
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIEDB_ID = 1;
    static final int COL_TITLE = 2;
    static final int COL_VOTE_AVERAGE = 3;
    static final int COL_OVERVIEW = 4;
    static final int COL_RELEASE_DATE = 5;
    static final int COL_POSTER_PATH = 6;

    private static final String[] VIDEO_COLUMNS = {
            MoviesContract.VideoEntry._ID,
            MoviesContract.VideoEntry.COLUMN_KEY,
            MoviesContract.VideoEntry.COLUMN_NAME,
            MoviesContract.VideoEntry.COLUMN_SITE
    };

    static final int COL_VIDEO_ID = 0;
    static final int COL_VIDEO_KEY = 1;
    static final int COL_VIDEO_NAME = 2;
    static final int COL_VIDEO_SITE = 3;

    private static final String[] REVIEW_COLUMNS = {
            MoviesContract.ReviewEntry._ID,
            MoviesContract.ReviewEntry.COLUMN_AUTHOR,
            MoviesContract.ReviewEntry.COLUMN_CONTENT,
            MoviesContract.ReviewEntry.COLUMN_URL
    };

    static final int COL_REVIEW_ID = 0;
    static final int COL_REVIEW_AUTHOR = 1;
    static final int COL_REVIEW_CONTENT = 2;
    static final int COL_REVIEW_URL = 3;

    private static final String[] TYPE_COLUMNS = {
            MoviesContract.TypeEntry._ID,
            MoviesContract.TypeEntry.COLUMN_FOREIGN_KEY
    };

    static final int COL_TYPE_ID = 0;
    static final int COL_TYPE_FK = 1;

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
    private VideosAdapter mVideosAdapter;
    private ReviewsAdapter mReviewsAdapter;
    private int mId;
    private LinearLayoutManager mLayoutManager;
    private FloatingActionButton mFab;
    ShareActionProvider mShareActionProvider;
    private String mShareKey;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        } else {
            Log.d("MovieDetailFragment", "Share Action Provider is null?");
        }

    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        Log.d("MovieDetailFragment", "http://www.youtube.com/watch?v=" + mShareKey);
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "http://www.youtube.com/watch?v=" + mShareKey);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_share) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mVideosAdapter = new VideosAdapter(getContext(), null);
        mReviewsAdapter = new ReviewsAdapter(getContext(), null);
        if (getArguments().containsKey(ARG_URI)) {
            mUri = getArguments().getParcelable(ARG_URI);

            if (mUri != null) {
                Log.d("MovieDetailFragment", mUri.toString());
            }
        }
        if (getContext() instanceof FavoriteStatusListener) {
            mListener = (FavoriteStatusListener) getContext();
        }
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        Activity activity = this.getActivity();
        mAppBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        mIconView = (ImageView) rootView.findViewById(R.id.movie_poster);
        mVoteAverage = (TextView) rootView.findViewById(R.id.movie_vote_average);
        mDescription = (TextView) rootView.findViewById(R.id.movie_description);
        mReleaseDate = (TextView) rootView.findViewById(R.id.movie_year);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab_detail);
        if (mFab != null) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mFavorited) {
                        Utility.removeFavorite(getContext(),
                                MoviesContract.MovieEntry.getMovieIdFromUri(mUri));
                    } else {
                        Utility.addFavorite(getContext(), MoviesContract.MovieEntry.getMovieIdFromUri(mUri));
                    }
                }
            });
        }
        if (mUri != null) {
            mId = MoviesContract.MovieEntry.getMovieIdFromUri(mUri);
            FetchVideosTask fvTask = new FetchVideosTask(getContext());
            fvTask.execute(mId);
            FetchReviewsTask frTask = new FetchReviewsTask(getContext());
            frTask.execute(mId);

            RecyclerView videoRecyclerView = (RecyclerView) rootView.findViewById(R.id.video_list);
            setupRecyclerView(videoRecyclerView, mVideosAdapter);

            RecyclerView reviewRecyclerView = (RecyclerView) rootView.findViewById(R.id.review_list);
            setupRecyclerView(reviewRecyclerView, mReviewsAdapter);

        }
        return rootView;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, CursorRecyclerViewAdapter adapter) {
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getActivity().getSupportLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        getActivity().getSupportLoaderManager().restartLoader(VIDEO_LOADER, null, this);
        getActivity().getSupportLoaderManager().restartLoader(REVIEW_LOADER, null, this);
        getActivity().getSupportLoaderManager().restartLoader(TYPE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MovieDetailFragment.ARG_URI, mUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case MOVIE_LOADER:
                if (mUri != null) {
                    return new CursorLoader(getActivity(),
                            mUri,
                            MOVIE_COLUMNS,
                            null,
                            null,
                            null);
                }
                break;

            case VIDEO_LOADER:
                if (mUri != null) {
                    return new CursorLoader(getActivity(),
                            MoviesContract.VideoEntry.buildVideosUri(mId),
                            VIDEO_COLUMNS,
                            null,
                            null,
                            null);
                }
                break;
            case REVIEW_LOADER:
                if (mUri != null) {
                    return new CursorLoader(getActivity(),
                            MoviesContract.ReviewEntry.buildReviewsUri(mId),
                            REVIEW_COLUMNS,
                            null,
                            null,
                            null);
                }
                break;
            case TYPE_LOADER:
                if (mUri != null) {
                    return new CursorLoader(getActivity(),
                            MoviesContract.TypeEntry.buildTypesWithTypeAndId("favorites", mId),
                            TYPE_COLUMNS,
                            null,
                            null,
                            null);
                }
                break;
        }
        return null;
    }

    private void setFavoriteStatus(boolean isFavorite) {
        mFavorited = isFavorite;
        if (mListener != null) {
            mListener.favoriteStatus(isFavorite);
        }
        if (mFab == null) {
            return;
        }
        if (mFavorited) {
            mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_white_24dp, getActivity().getTheme()));
        } else {
            mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_border_white_24dp, getActivity().getTheme()));
        }

    }

    private boolean getFavoriteStatus() {
        return mFavorited;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case MOVIE_LOADER:
                if (cursor != null && cursor.moveToFirst()) {
                    if (mAppBarLayout != null) {
                        mAppBarLayout.setTitle(cursor.getString(COL_TITLE));
                    }

                    String posterSuffix = cursor.getString(MovieDetailFragment.COL_POSTER_PATH);
                    Picasso.with(getActivity()).load(Utility.getPosterPathForResource(posterSuffix)).into(mIconView);
                    mDescription.setText(cursor.getString(MovieDetailFragment.COL_OVERVIEW));
                    mReleaseDate.setText(cursor.getString(MovieDetailFragment.COL_RELEASE_DATE));
                    mVoteAverage.setText(cursor.getString(MovieDetailFragment.COL_VOTE_AVERAGE));
                }
                break;
            case VIDEO_LOADER:
                if (cursor != null && cursor.moveToFirst()) {
                    mShareKey = cursor.getString(MovieDetailFragment.COL_VIDEO_KEY);
                    mVideosAdapter.swapCursor(cursor);
                }
                break;
            case REVIEW_LOADER:
                if (cursor != null && cursor.moveToFirst()) {
                    mReviewsAdapter.swapCursor(cursor);
                }
                break;
            case TYPE_LOADER:
                if (cursor != null && cursor.moveToFirst()) {
                    setFavoriteStatus(true);
                    break;
                }
                setFavoriteStatus(false);
                break;
            default:
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case MOVIE_LOADER:
                break;
            case VIDEO_LOADER:
                mVideosAdapter.swapCursor(null);
                break;
            case REVIEW_LOADER:
                mReviewsAdapter.swapCursor(null);
                break;
            case TYPE_LOADER:
            default:
                break;
        }
    }

    public interface FavoriteStatusListener {
        void favoriteStatus(boolean favorited);
    }

}
