package com.jeffreymcknight.sanfranciscomap.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorAdapter;
import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorViewHolder;
import com.jeffreymcknight.sanfranciscomap.R;

/**
 * Created by jeffmcknight on 4/9/17.
 */

public class StreetCursorAdapter extends RecyclerViewCursorAdapter<StreetCursorAdapter.ViewHolder> {
    /**
     * Constructor.
     *
     * @param context The Context the Adapter is displayed in.
     */
    protected StreetCursorAdapter(Context context) {
        super(context);
        setupCursorAdapter(null, 0, R.layout.street_list_item, false);
    }

    @Override
    public StreetCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(StreetCursorAdapter.ViewHolder holder, int position) {

    }

    /**
     *
     */
    class ViewHolder extends RecyclerViewCursorViewHolder {
        /**
         * Constructor.
         *
         * @param view The root view of the ViewHolder.
         */
        public ViewHolder(View view) {
            super(view);
        }

        @Override
        public void bindCursor(Cursor cursor) {

        }
    }
}
