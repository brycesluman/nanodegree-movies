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
 * Created by bryce on 4/12/16.
 */
public class VideosAdapter extends CursorRecyclerViewAdapter<VideosAdapter.ViewHolder> {
    private static final int VIEW_TYPE_VIDEO = 0;

    private OnInteractionListener mListener;

    Context mContext;

    public VideosAdapter(Context context, Cursor c) {
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
            case VIEW_TYPE_VIDEO: {
                layoutId = R.layout.video_list_content;
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
            case VIEW_TYPE_VIDEO: {
                break;
            }
        }
        viewHolder.mVideoId = cursor.getString(MovieDetailFragment.COL_VIDEO_KEY);
        viewHolder.mDescription.setText(cursor.getString(MovieDetailFragment.COL_VIDEO_NAME));
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public String mVideoId;
        public TextView mDescription;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDescription = (TextView) view.findViewById(R.id.video_description);
            view.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mVideoId + "'";
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.videoItemHasBeenClicked(v, getAdapterPosition());
            }
        }
    }

    public interface OnInteractionListener {
        void videoItemHasBeenClicked(View view, int position);
    }
}
