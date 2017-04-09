package com.jeffreymcknight.sanfranciscomap;

import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeffreymcknight.sanfranciscomap.api.ApiClient;
import com.jeffreymcknight.sanfranciscomap.model.GeocoderResult;
import com.jeffreymcknight.sanfranciscomap.model.StreetBean;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements StreeListFragment.Listener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String mIntersectedStreet;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mIntersectedStreet = "";
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view) {
        Log.d(TAG, "onItemClick()"
                + " -- view: " + view);
        if ((view instanceof TextView))
            Log.d(TAG, "onItemClick()"
                    + " -- view.getText(): " + ((TextView) view).getText()
                    + " -- view.`isSelected(): " + view.isSelected()
            );
        String crossStreet = (String) ((TextView) view).getText();
        if (view.isSelected()) {
            if (mIntersectedStreet.isEmpty()) {
                mIntersectedStreet = crossStreet;
            } else {
                findIntersection(mIntersectedStreet, crossStreet);
                mViewPager.setCurrentItem(1, true);
                mIntersectedStreet = "";
                view.setSelected(false);
            }
        } else if (crossStreet.equals(mIntersectedStreet)){
            mIntersectedStreet = "";
        }
    }

    /**
     *
     * @param streetName1
     * @param streetName2
     */
    private void findIntersection(String streetName1, String streetName2) {
        Log.d(TAG, "findIntersection()");
        Callback<GeocoderResult> callback = new Callback<GeocoderResult>() {
            @Override
            public void onResponse(Call<GeocoderResult> call, Response<GeocoderResult> response) {
                handleResponse(response);
            }

            @Override
            public void onFailure(Call<GeocoderResult> call, Throwable t) {
                Log.w(TAG, "onFailure()"
                        + " -- call: " + call
                        + " -- t: " + t);
            }
        };
        ApiClient.getInstance().getIntersection(streetName1, streetName2, callback);
    }

    /**
     *
     * @param response
     */
    private void handleResponse(Response<GeocoderResult> response) {
        Log.d(TAG, "onResponse()"
//                        + " -- call: " + call
//                        + " -- response: " + response
                        + "\n -- response.body(): " + response.body()
//                        + "\n -- lat: " + response.body().results[0].geometry.location.lat
//                        + " -- lng: " + response.body().results[0].geometry.location.lng
        );
        if (response.body().results.length > 0){
            LatLng latLng = new LatLng(
                    response.body().results[0].geometry.location.lat,
                    response.body().results[0].geometry.location.lng);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            mMap.animateCamera(cameraUpdate);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            mMap.addMarker(markerOptions);
        } else {
            Snackbar.make(mViewPager, R.string.msg_no_intersection_found, Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
        }
    }

    /**
     * Get a reference to the {@link GoogleMap} when it is ready.
     * @param googleMap
     */
    private void handleMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static final String TAG = PlaceholderFragment.class.getSimpleName();
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleHelloClick();
                }
            });
            return rootView;
        }

        private void handleHelloClick() {
            ApiClient.getInstance().getStreets(10, 1, new Callback<List<StreetBean>>() {
                @Override
                public void onResponse(Call<List<StreetBean>> call, Response<List<StreetBean>> response) {
                    Log.d(TAG, "onResponse()"
                            + " -- call: " + call
                            + " -- response: " + response
                            + "\n -- response.body().toString(): " + response.body().toString()
                    );
                }

                @Override
                public void onFailure(Call<List<StreetBean>> call, Throwable t) {

                }
            });
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final String TAG = SectionsPagerAdapter.class.getSimpleName();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0){
                return StreeListFragment.newInstance();
            } else if (position == 1){
                GoogleMapOptions mapOptions;
                mapOptions = buildCameraPositionOption(37.7749F, -122.4194F, 16.0F);
                SupportMapFragment supportMapFragment = SupportMapFragment.newInstance(mapOptions);
                OnMapReadyCallback callback = new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        handleMapReady(googleMap);
                    }
                };
                supportMapFragment.getMapAsync(callback);
                return supportMapFragment;
            } else {
                Log.w(TAG, "getItem()"
                        + " -- invalid position: " + position);
                return PlaceholderFragment.newInstance(1);
            }
        }

        @NonNull
        private GoogleMapOptions buildCameraPositionOption(double lat, double lng, float zoomLevel) {
            GoogleMapOptions mapOptions;
            mapOptions = new GoogleMapOptions();
            LatLng latlng = new LatLng(lat, lng);
            CameraPosition cameraPosition
                    = new CameraPosition.Builder()
                    .target(latlng)
                    .zoom(zoomLevel)
                    .build();
            mapOptions.camera(cameraPosition);
            return mapOptions;
        }

        @Override
        public int getCount() {
            // Show 2 tabs.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_title_street_list);
                case 1:
                    return getString(R.string.tab_title_map);
            }
            return null;
        }
    }

}
