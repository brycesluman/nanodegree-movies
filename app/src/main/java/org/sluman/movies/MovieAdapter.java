package org.sluman.movies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by bryce on 3/18/16.
 */
public class MovieAdapter
        extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private static final int VIEW_TYPE_POSTER = 0;

    //private final List<DummyContent.DummyItem> mValues;
    static CursorAdapter mCursorAdapter;
    private OnInteractionListener mListener;

    Context mContext;
    public MovieAdapter(Context context, Cursor c) {

        mContext = context;

        if (context instanceof OnInteractionListener) {
            mListener = (OnInteractionListener) context;
        }

        mCursorAdapter = new CursorAdapter(mContext, c, 0) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                int viewType = getItemViewType(cursor.getPosition());
                int layoutId = -1;
                switch (viewType) {
                    case VIEW_TYPE_POSTER: {
                        layoutId = R.layout.movie_list_content;
                        break;
                    }
                }
                View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

                ViewHolder viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
                return view;
            }

            @Override
            public void bindView(View view, Context context, final Cursor cursor) {
                final ViewHolder viewHolder = (ViewHolder) view.getTag();

                int viewType = getItemViewType(cursor.getPosition());
                switch (viewType) {
                    case VIEW_TYPE_POSTER: {
                        break;
                    }
                }

                String posterSuffix = cursor.getString(MovieListActivity.COL_POSTER_PATH);
                Picasso.with(context).load(Utility.getPosterPathForResource(posterSuffix)).into(viewHolder.mIconView);
                viewHolder.mMovieId = cursor.getInt(MovieListActivity.COL_MOVIE_ID);
            }
        };
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.mView, mContext, mCursorAdapter.getCursor());

    }

    @Override
    public int getItemCount() {
        if(mCursorAdapter!=null && mCursorAdapter.getCursor()!=null) {
            return mCursorAdapter.getCursor().getCount();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public int mMovieId;
        public final ImageView mIconView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIconView = (ImageView) view.findViewById(R.id.posterImage);
            view.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mIconView.toString() + "'";
        }

        @Override
        public void onClick(View v) {
            if(mListener!=null) {
                mListener.listItemHasBeenClicked(v, getAdapterPosition());
            }
        }
    }


    public void swapCursor(Cursor newCursor) {
        mCursorAdapter.swapCursor(newCursor);
    }

    public interface OnInteractionListener {
        void listItemHasBeenClicked(View view, int position);
    }
}