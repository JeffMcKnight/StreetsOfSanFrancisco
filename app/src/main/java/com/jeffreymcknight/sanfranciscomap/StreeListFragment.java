package com.jeffreymcknight.sanfranciscomap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.jeffreymcknight.sanfranciscomap.adapter.StreetCursorAdapter;
import com.jeffreymcknight.sanfranciscomap.api.ApiClient;
import com.jeffreymcknight.sanfranciscomap.model.StreetBean;
import com.jeffreymcknight.sanfranciscomap.model.StreetContract;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays a scrollable list of San Francisco streets
 *
 * Created by jeffmcknight on 4/6/17.
 */

public class StreeListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = StreeListFragment.class.getSimpleName();
    private final int LOADER_ID = this.hashCode();
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private StreeListFragment.Listener mListener;
    private View.OnClickListener mClickListener;
    private StreetCursorAdapter mCursorAdapter;
    public static final String[] PLACEHOLDER_STREET_NAMES = new String[]{
            "1st St",
            "Hampshire Way",
            "Kearny St",
            "Lincoln Ave",
            "Market St",
            "Mariposa St",
            "Potrero Ave"
    };

    /**
     *
     * @return
     */
    public static Fragment newInstance() {
        StreeListFragment fragment = new StreeListFragment();
        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener){
            mListener = (Listener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_street_list, container, false);
        /** Set up {@link RecyclerView} */
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.street_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleListItemClick(view);
            }
        };
        mAdapter = new StreetListAdapter(PLACEHOLDER_STREET_NAMES, mClickListener);
        mCursorAdapter = new StreetCursorAdapter(getContext());
        mRecyclerView.setAdapter(mCursorAdapter);

        updateStreetNames();
        return rootView;
    }

    /**
     * Start the Cursor Loader
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.streetlist_fragment, menu);
    }

    /**
     * Handle action bar item clicks. For now we just a menu item to update the list of street names
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected()"
                + " -- item: " + item);
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateStreetNames();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param view
     */
    private void handleListItemClick(View view) {
        Log.d(TAG, "handleListItemClick()"
                + " -- view: " + view
                + " -- view.getText(): " + ((view instanceof TextView) ? ((TextView) view).getText() : "no text!")
        );
        view.setSelected(!view.isSelected());
        if (mListener != null){
            mListener.onItemClick(view);
        }
    }

    /**
     * Retrieve street names from the SfGov.org API, and update {@link #mAdapter}, and the database
     * TODO: remove {@link #mAdapter} once {@link com.jeffreymcknight.sanfranciscomap.adapter.StreetCursorAdapter}
     * is implemented
     */
    private void updateStreetNames() {
        int limit = 3000;
        int offset = 0;
        ApiClient.getInstance().getStreets(limit, offset, new Callback<List<StreetBean>>() {
            @Override
            public void onResponse(Call<List<StreetBean>> call, Response<List<StreetBean>> response) {
                Log.d(TAG, "onResponse()"
                        + " -- call: " + call
                        + " -- response: " + response
                        + "\n -- response.body().toString(): " + response.body().toString()
                );
                String[] streetNames = new String[response.body().size()];
                ContentValues[] contentValues = new ContentValues[streetNames.length];
                for (int i=0; i<streetNames.length; i++){
                    streetNames[i] = response.body().get(i).fullstreetname;
                    contentValues[i] = StreetContract.StreetnameEntry.buildContentValues(
                            response.body().get(i).fullstreetname);
                    Log.d(TAG, "onResponse()"
                            + " -- streetNames["+i+"]: " + streetNames[i]);
                }
//                RecyclerView.Adapter adapter = new StreetListAdapter(streetNames, mClickListener);
//                mRecyclerView.swapAdapter(adapter, true);
                int rowsInserted = getContext().getContentResolver().bulkInsert(
                        StreetContract.StreetnameEntry.buildAllStreetsUri(),
                        contentValues);
                Log.i(TAG, "onResponse()"
                        + " -- streetNames.length: " + streetNames.length
                        + " -- rowsInserted: " + rowsInserted
                );
            }

            @Override
            public void onFailure(Call<List<StreetBean>> call, Throwable t) {

            }
        });
    }

    /**
     * Create a {@link CursorLoader} for use in the {@link #mCursorAdapter}
     * This is the implementation of a {@link LoaderManager.LoaderCallbacks} method
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != LOADER_ID) {
            Log.w(TAG, "onCreateLoader() -- UNRECOGNIZED id: " + id);
        }
        CursorLoader cursorLoader;
        Uri uri = StreetContract.StreetnameEntry.buildAllStreetsUri();
        // Sort Cursor by street name in ascending order
        String sortOrder = StreetContract.StreetnameEntry.COLUMN_FULLSTREETNAME + " " + StreetContract.SORT_ORDER_ASCENDING;
        cursorLoader = new CursorLoader(getActivity(), uri, null, null, null, sortOrder);
        Log.d(TAG, "onCreateLoader()"
                + "\n\t -- id: " + id
                + "\n\t -- uri: " + uri
                + "\n\t -- cursorLoader: " + cursorLoader
        );
        return cursorLoader;
    }

    /**
     * Swap the new Cursor Loader created by {@link #onCreateLoader(int, Bundle)} into the {@link #mCursorAdapter}
     * This is the implementation of a {@link LoaderManager.LoaderCallbacks} method
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(TAG, "onLoadFinished()"
                + "\n\t -- loader.getId(): " + loader.getId()
                + "\n\t -- loader: " + loader
        );
        mCursorAdapter.swapCursor(data);
    }

    /**
     * Release the Cursor {@link Loader} if it is reset
     * This is the implementation of a {@link LoaderManager.LoaderCallbacks} method
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(TAG, "onLoaderReset()"
                + "\n\t -- loader.getId(): " + loader.getId()
                + "\n\t -- loader: " + loader
        );
        mCursorAdapter.swapCursor(null);
    }

    public interface Listener {
        public void onItemClick(View view);
    }


    /**
     * Adapter to tell {@link RecyclerView} what to display
     */
    public static class StreetListAdapter extends RecyclerView.Adapter<StreetListAdapter.ViewHolder> {
        private String[] mStreetNames;

        private View.OnClickListener mListener;

        public StreetListAdapter(String[] streetNames, View.OnClickListener listener) {
            mStreetNames = streetNames;
            mListener = listener;
        }

        public StreetListAdapter(String[] streetNames) {
            this(streetNames, null);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView streetName = (TextView) LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.street_list_item, parent, false);
            return new ViewHolder(streetName);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder()"
                    + " -- holder: " + holder
                    + " -- position: " + position);
            holder.getStreetNameView().setText(mStreetNames[position]);
            holder.getStreetNameView().setOnClickListener(mListener);
        }

        @Override
        public int getItemCount() {
            return mStreetNames.length;
        }

        /**
         * TODO: implement method
         * @param streets
         */
        public void addItems(List<StreetBean> streets){

        }


        /**
         * Inner inner class to hold {@link View}s that are displayed in the {@link RecyclerView} list items
         */
        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mStreetNameView;

            public ViewHolder(TextView itemView) {
                super(itemView);
                mStreetNameView = itemView;
            }

            public TextView getStreetNameView() {
                return mStreetNameView;
            }
        }

    }

}
