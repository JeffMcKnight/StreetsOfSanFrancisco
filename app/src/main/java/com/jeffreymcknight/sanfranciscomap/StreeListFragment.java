package com.jeffreymcknight.sanfranciscomap;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jeffreymcknight.sanfranciscomap.api.ApiClient;
import com.jeffreymcknight.sanfranciscomap.model.StreetBean;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays a scrollable list of San Francisco streets
 *
 * Created by jeffmcknight on 4/6/17.
 */

public class StreeListFragment extends Fragment {
    private static final String TAG = StreeListFragment.class.getSimpleName();
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private StreeListFragment.Listener mListener;
    private View.OnClickListener mClickListener;

    public static Fragment newInstance(int i) {
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
        View rootView = inflater.inflate(R.layout.fragment_street_list, container, false);
        /** Set up {@link RecyclerView} */
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.street_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        String[] streetNames = {"Lincoln Ave", "Shafter Ave", "Hampshire Way", "Stafford Dr"};
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleListItemClick(view);
            }
        };
        mAdapter = new StreetListAdapter(streetNames, mClickListener);
        mRecyclerView.setAdapter(mAdapter);

        TextView textView = (TextView) rootView.findViewById(R.id.title_text);
        textView.setText("Update Streets");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTitleClick();
            }
        });
        return rootView;
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
        if (mListener != null){
            mListener.onItemClick(view);
        }
    }

    private void handleTitleClick() {
        ApiClient.getInstance().getStreets(100, 100, new Callback<List<StreetBean>>() {
            @Override
            public void onResponse(Call<List<StreetBean>> call, Response<List<StreetBean>> response) {
                Log.d(TAG, "onResponse()"
                        + " -- call: " + call
                        + " -- response: " + response
                        + "\n -- response.body().toString(): " + response.body().toString()
                );
                String[] streetNames = new String[response.body().size()];
                for (int i=0; i<streetNames.length; i++){
                    streetNames[i] = response.body().get(i).fullstreetname;
                    Log.d(TAG, "onResponse()"
                            + " -- streetNames["+i+"]: " + streetNames[i]);
                }
                // TODO: create/call method mAdapter.addItems(List<StreetBean>)
                mAdapter = new StreetListAdapter(streetNames, mClickListener);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onFailure(Call<List<StreetBean>> call, Throwable t) {

            }
        });
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
