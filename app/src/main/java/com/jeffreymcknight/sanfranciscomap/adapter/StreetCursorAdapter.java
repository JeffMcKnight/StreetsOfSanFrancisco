package com.jeffreymcknight.sanfranciscomap.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorAdapter;
import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorViewHolder;
import com.jeffreymcknight.sanfranciscomap.R;
import com.jeffreymcknight.sanfranciscomap.model.StreetContract;

/**
 * Created by jeffmcknight on 4/9/17.
 */

public class StreetCursorAdapter extends RecyclerViewCursorAdapter<StreetCursorAdapter.ViewHolder> {
    /**
     * Constructor.
     *
     * @param context The Context the Adapter is displayed in.
     */
    public StreetCursorAdapter(Context context) {
        super(context);
        setupCursorAdapter(null, 0, R.layout.street_list_item, false);
    }

    @Override
    public StreetCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent));
    }

    @Override
    public void onBindViewHolder(StreetCursorAdapter.ViewHolder holder, int position) {
        // Move cursor to this position
        mCursorAdapter.getCursor().moveToPosition(position);

        // Set the ViewHolder
        setViewHolder(holder);

        // Bind this view
        mCursorAdapter.bindView(null, mContext, mCursorAdapter.getCursor());
    }

    /**
     *
     */
    class ViewHolder extends RecyclerViewCursorViewHolder {
        private final TextView mStreetNameView;

        /**
         * Constructor.
         *
         * @param view The root view of the ViewHolder.
         */
        public ViewHolder(View view) {
            super(view);
            mStreetNameView = (TextView) view.findViewById(R.id.list_item_textview);
        }

        @Override
        public void bindCursor(Cursor cursor) {
            mStreetNameView.setText(cursor.getString(StreetContract.StreetnameEntry.INDEX_FULLSTREETNAME));
        }
    }
}
