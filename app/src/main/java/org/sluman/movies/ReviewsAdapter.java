package org.sluman.movies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by bryce on 4/13/16.
 */
public class ReviewsAdapter extends CursorRecyclerViewAdapter<ReviewsAdapter.ViewHolder> {
    private static final int VIEW_TYPE_REVIEW = 0;

    private OnInteractionListener mListener;

    Context mContext;

    public ReviewsAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;

        if (context instanceof OnInteractionListener) {
            mListener = (OnInteractionListener) context;
        }

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_REVIEW: {
                layoutId = R.layout.review_list_content;
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
            case VIEW_TYPE_REVIEW: {
                break;
            }
        }
        viewHolder.mAuthor.setText(cursor.getString(MovieDetailFragment.COL_REVIEW_AUTHOR));
        viewHolder.mContent.setText(cursor.getString(MovieDetailFragment.COL_REVIEW_CONTENT));
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public TextView mAuthor;
        public TextView mContent;
        public String mUrl;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mAuthor = (TextView) view.findViewById(R.id.list_author);
            mContent = (TextView) view.findViewById(R.id.list_content);
            view.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAuthor.toString() + "'";
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.listItemHasBeenClicked(v, getAdapterPosition());
            }
        }
    }

    public interface OnInteractionListener {
        void listItemHasBeenClicked(View view, int position);
    }
}
