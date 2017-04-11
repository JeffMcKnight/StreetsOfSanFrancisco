package com.jeffreymcknight.sanfranciscomap;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements StreetListFragment.Listener {

    private static final String TAG = MainActivity.class.getSimpleName();

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

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onIntersectionSelected(String street, String crossStreet) {
        Log.d(TAG, "onIntersectionSelected()"
                + " -- street: " + street
                + " -- crossStreet: " + crossStreet
        );
        findIntersection(street, crossStreet);
        mViewPager.setCurrentItem(SectionsPagerAdapter.PAGER_INDEX_MAP, true);
    }

    /**
     *
     * @param street
     * @param crossStreet
     */
    private void findIntersection(String street, String crossStreet) {
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
        ApiClient.getInstance().getIntersection(street, crossStreet, callback);
    }

    /**
     *
     * @param response
     */
    private void handleResponse(Response<GeocoderResult> response) {
        Log.d(TAG, "onResponse()"
                        + "\n -- response.body(): " + response.body()
        );
        if (response.body().results.length > 0){
            LatLng latLng = new LatLng(
                    response.body().results[0].geometry.location.lat,
                    response.body().results[0].geometry.location.lng);
            // TODO: check whether CameraUpdateFactory is initialized; can throw NPE
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
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final String TAG = SectionsPagerAdapter.class.getSimpleName();
        static final int PAGER_INDEX_STREETLIST = 0;
        static final int PAGER_INDEX_MAP = 1;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * instantiate the fragment for the page at the {@code position}
         * @param position
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            if (position == PAGER_INDEX_STREETLIST){
                return StreetListFragment.newInstance();
            } else if (position == PAGER_INDEX_MAP){
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
                return null;
            }
        }

        /**
         * Set the initial location and zoom level of the camera
         *
         * @param lat
         * @param lng
         * @param zoomLevel
         * @return
         */
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

        /**
         * Tells {@link #mViewPager} how many tabs it has
         * @return
         */
        @Override
        public int getCount() {
            return 2;
        }

        /**
         * Tells {@link #mViewPager} how to label its tabs
         * @param position
         * @return
         */
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
