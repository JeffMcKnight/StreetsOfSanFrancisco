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

public class StreetListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = StreetListFragment.class.getSimpleName();
    private final int LOADER_ID = this.hashCode();
    private RecyclerView mRecyclerView;
    private StreetListFragment.Listener mListener;
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
        StreetListFragment fragment = new StreetListFragment();
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
        Listener intersectionListener = new Listener() {
            @Override
            public void onIntersectionSelected(String street, String crossStreet) {
                handleIntersectionSelected(street, crossStreet);
            }
        };
        mCursorAdapter = new StreetCursorAdapter(getContext());
        mCursorAdapter.setIntersectionListener(intersectionListener);
        mRecyclerView.setAdapter(mCursorAdapter);

        updateStreetNames();
        return rootView;
    }

    /**
     * Notify the listener if we have selected a street intersection.  After notifying, clear the
     * selected items from {@link #mRecyclerView}
     */
    private void handleIntersectionSelected(String street, String crossStreet) {
        notifyIntersectionSelected(street, crossStreet);
        clearSelectedItems();
    }

    /**
     * Clear selected list item views
     * TODO: clear the selected items in a less hacky way; or maybe don't clear the selections, and
     * figure out what to do if user selects a third item (maybe replace the oldest selection and
     * explain with a SnackBar?)
     */
    private void clearSelectedItems() {
        for (Integer eachIndex : mCursorAdapter.getSelectedItems()){
            StreetCursorAdapter.ViewHolder streetViewHolder
                    = (StreetCursorAdapter.ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(eachIndex);
            if (streetViewHolder != null){
                streetViewHolder.setSelected(false);
            }
        }
        mCursorAdapter.clearSelectedItems();
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
     * @param street
     * @param crossStreet
     */
    private void notifyIntersectionSelected(CharSequence street, CharSequence crossStreet) {
        Log.d(TAG, "notifyIntersectionSelected()"
                + " -- street: " + street
                + " -- crossStreet: " + crossStreet
        );
        if (mListener != null){
            mListener.onIntersectionSelected((String) street, (String) crossStreet);
        }
    }

    /**
     * Retrieve street names from the SfGov.org API, and update the database
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
                }
                int rowsInserted = getContext().getContentResolver().bulkInsert(
                        StreetContract.StreetnameEntry.buildAllStreetsUri(),
                        contentValues);
                Log.d(TAG, "onResponse()"
                        + " -- streetNames.length: " + streetNames.length
                        + " -- rowsInserted: " + rowsInserted
                );
            }

            @Override
            public void onFailure(Call<List<StreetBean>> call, Throwable t) {
                Log.w(TAG, "onFailure()"
                        + " -- call: " + call
                        + " -- t: " + t);
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

    /**
     * Notify when user has selected an intersection
     */
    public interface Listener {
        public void onIntersectionSelected(String street, String crossStreet);
    }

}
