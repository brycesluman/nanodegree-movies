package org.sluman.movies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by bryce on 3/18/16.
 */
public class MovieAdapter
        extends CursorRecyclerViewAdapter<MovieAdapter.ViewHolder> {
    private static final int VIEW_TYPE_POSTER = 0;

    private OnInteractionListener mListener;

    Context mContext;
    public MovieAdapter(Context context, Cursor c) {
        super(context,c);
        Log.d("MovieAdapter", "init");
        mContext = context;

        if (context instanceof OnInteractionListener) {
            mListener = (OnInteractionListener) context;
        }

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_POSTER: {
                layoutId = R.layout.movie_list_content;
                break;
            }
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) holder.mView.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_POSTER: {
                break;
            }
        }
        Log.d("MovieAdapter", cursor.getInt(MovieListActivity.COL_MOVIE_ID)+ "");
        String posterSuffix = cursor.getString(MovieListActivity.COL_POSTER_PATH);
        Picasso.with(mContext).load(Utility.getPosterPathForResource(posterSuffix)).into(viewHolder.mIconView);
        viewHolder.mMovieId = cursor.getInt(MovieListActivity.COL_MOVIE_ID);
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

    public interface OnInteractionListener {
        void listItemHasBeenClicked(View view, int position);
    }
}