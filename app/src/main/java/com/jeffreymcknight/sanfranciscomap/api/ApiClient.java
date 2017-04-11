package com.jeffreymcknight.sanfranciscomap.api;


import android.util.Log;

import com.jeffreymcknight.sanfranciscomap.model.GeocoderResult;
import com.jeffreymcknight.sanfranciscomap.model.StreetBean;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton to make API calls
 * Created by jeffmcknight on 4/6/17.
 */
public class ApiClient {
    // https://data.sfgov.org/resource/fuen-w6ja.json
    private static final HttpUrl BASE_HTTP_URL = HttpUrl.parse("https://data.sfgov.org");

    // http://maps.googleapis.com/maps/api/geocode/json?address=market+and+4th,+san+francisco&sensor=false
    private static final String URL_STRING_GOOGLE_GEOCODER = "http://maps.googleapis.com/maps/api/geocode/json";
    private static final HttpUrl HTTP_URL_GOOGLE_GEOCODER = HttpUrl.parse(URL_STRING_GOOGLE_GEOCODER);
    private static final String TAG = ApiClient.class.getSimpleName();
    private static ApiClient sApiClient;
    private ApiService mApiService;

    private ApiClient() {
    }

    /**
     *
     * @return
     */
    public static ApiClient getInstance() {
        if (sApiClient == null) {
            sApiClient = new ApiClient();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_HTTP_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiService apiService = retrofit.create(ApiService.class);
            sApiClient.setApiService(apiService);

        }
        return sApiClient;
    }

    /**
     * Asynchronously retrieves list of San Francisco streets from sfgov.org
     * @param limit
     * @param offset
     * @param callback
     */
    public void getStreets(Integer limit, Integer offset, Callback<List<StreetBean>> callback) {
        mApiService.getStreetsPaginated(limit, offset).enqueue(callback);
    }

    /**
     * Synchronously retrieves list of San Francisco streets from sfgov.org
     * @param limit
     * @param offset
     * @return the list of SF streets
     */
    public Response<List<StreetBean>> getStreets(Integer limit, Integer offset) {
        try {
            return mApiService.getStreetsPaginated(limit, offset).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param streetName1
     * @param streetName2
     * @param callback
     */
    public void getIntersection(String streetName1, String streetName2, Callback<GeocoderResult> callback){
        String address = buildAddressParam(streetName1, streetName2);
        mApiService.getIntersection(URL_STRING_GOOGLE_GEOCODER, address, false).enqueue(callback);
    }

    /**
     * @param streetName1
     * @param streetName2
     */
    public Response<GeocoderResult> getIntersection(String streetName1, String streetName2){
        String address = buildAddressParam(streetName1, streetName2);
        try {
            return mApiService.getIntersection(URL_STRING_GOOGLE_GEOCODER, address, false).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param streetName1
     * @param streetName2
     * @return
     */
    private String buildAddressParam(String streetName1, String streetName2) {
        String address = streetName1.replaceAll(" ", "+") + "+" + streetName2.replaceAll(" ", "+") + ",+san+francisco";
        Log.i(TAG, "buildAddressParam()"
                + " -- address: " + address
                + " -- streetName1: " + streetName1
                + " -- streetName2: " + streetName2
        );
        return address;
    }

    public void setApiService(ApiService apiService) {
        mApiService = apiService;
    }

}
