package com.jeffreymcknight.sanfranciscomap.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorAdapter;
import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorViewHolder;
import com.jeffreymcknight.sanfranciscomap.R;
import com.jeffreymcknight.sanfranciscomap.model.StreetContract;

import java.util.HashSet;
import java.util.Set;

/**
 *  Cursor adapter for a {@link android.support.v7.widget.RecyclerView}
 *
 * Created by jeffmcknight on 4/9/17.
 */

public class StreetCursorAdapter
        extends RecyclerViewCursorAdapter<StreetCursorAdapter.ViewHolder>
        implements ViewHolderListener {
    private static final String TAG = StreetCursorAdapter.class.getSimpleName();
    private View.OnClickListener mListener;
    private Set<Integer> mSelectedItems;

    /**
     * Constructor.
     *
     * @param context The Context the Adapter is displayed in.
     */
    public StreetCursorAdapter(Context context) {
        super(context);
        setupCursorAdapter(null, 0, R.layout.street_list_item, false);
        mSelectedItems = new HashSet<>(2);
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
        holder.setListener(this);
        holder.setSelected(mSelectedItems.contains(position));

        // Bind this view
        mCursorAdapter.bindView(null, mContext, mCursorAdapter.getCursor());
    }


    /**
     * Update the list of selected items, {@link #mSelectedItems}, and notify the listener
     * {@link #mListener}
     * @param view the {@link View} that received the click event
     * @param adapterPosition the adapter position of the {@link View} that received the click event
     */
    public void onItemClick(View view, int adapterPosition) {
        if (view.isSelected()){
            mSelectedItems.add(adapterPosition);
        } else {
            mSelectedItems.remove(adapterPosition);
        }
        if (mListener != null){
            mListener.onClick(view);
        }
    }

    /**
     *
     * @param position
     * @return
     */
    public String getStreetName(Integer position) {
        int restorePosition = mCursorAdapter.getCursor().getPosition();
        boolean isMoveSuccess = mCursorAdapter.getCursor().moveToPosition(position);
        if (!isMoveSuccess){
            Log.w(TAG, "getStreetName()"
                    + " -- failed to moveToPosition(): " + position);
        }
        String streetName = mCursorAdapter.getCursor().getString(StreetContract.StreetnameEntry.INDEX_FULLSTREETNAME);
        boolean isRestoreSuccess = mCursorAdapter.getCursor().moveToPosition(restorePosition);
        if (!isRestoreSuccess){
            Log.w(TAG, "getStreetName()"
                    + " -- failed to moveToPosition(): " + restorePosition);
        }
        return streetName;
    }

    /**
     * Deselect all items in {@link StreetCursorAdapter} by setting all {@link View}s in
     * {@link #mSelectedItems} to unselected and clearing the list
     */
    public void clearSelectedItems() {
        for (Integer eachIndex : mSelectedItems){
//            updateItemSelected(eachIndex, false);
        }
        mSelectedItems.clear();
    }

    public Set<Integer> getSelectedItems() {
        return mSelectedItems;
    }

    public void setListener(View.OnClickListener listener) {
        mListener = listener;
    }

    /**
     *
     */
    static class ViewHolder extends RecyclerViewCursorViewHolder implements View.OnClickListener {
        private final String TAG = ViewHolder.class.getSimpleName();
        private final TextView mStreetNameView;
        private ViewHolderListener mListener;

        /**
         * Constructor.
         *
         * @param view The root view of the ViewHolder.
         */
        public ViewHolder(View view) {
            super(view);
            mStreetNameView = (TextView) view.findViewById(R.id.list_item_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void bindCursor(Cursor cursor) {
            mStreetNameView.setText(cursor.getString(StreetContract.StreetnameEntry.INDEX_FULLSTREETNAME));
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "onClick()"
                    + " -- v.isSelected(): " + v.isSelected()
                    + " -- v.getText(): " + ((TextView) v).getText()
                    + " -- v: " + v
            );
            updateItemSelected(v, !v.isSelected());
            mListener.onItemClick(v, getAdapterPosition());
        }

        public void setListener(ViewHolderListener listener) {
            mListener = listener;
        }

        public void setSelected(boolean isSelected) {
            updateItemSelected(mStreetNameView, isSelected);
        }

        /**
         * TODO: replace this hack with xml
         * @param v
         * @param isSelected
         */
        public void updateItemSelected(View v, boolean isSelected) {
            v.setSelected(isSelected);
            int cyan = v.getResources().getColor(R.color.colorPrimary);
            int backgroundColor = (v.isSelected() ? cyan : Color.WHITE);
            int textColor = (v.isSelected() ? Color.WHITE: Color.BLACK);
            v.setBackgroundColor(backgroundColor);
            if (v instanceof TextView){
                ((TextView) v).setTextColor(textColor);
            }
        }
    }

}
