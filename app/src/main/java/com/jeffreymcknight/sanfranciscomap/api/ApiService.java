package com.jeffreymcknight.sanfranciscomap.api;

/**
 * Created by jeffmcknight on 4/6/17.
 */

import com.jeffreymcknight.sanfranciscomap.BuildConfig;
import com.jeffreymcknight.sanfranciscomap.model.GeocoderResult;
import com.jeffreymcknight.sanfranciscomap.model.StreetBean;

import java.util.List;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by jeffmcknight on 11/17/16.
 */

public interface ApiService {
    public static final String PATH_STREET_LIST = "/resource/fuen-w6ja.json";
    public static final String QUERY_PARAM_LIMIT = "$limit";
    public static final String QUERY_PARAM_OFFSET = "$offset";
    public static final String QUERY_PARAM_ADDRESS = "address";
    public static final String QUERY_PARAM_SENSOR = "sensor";
    public static final String HEADER_APP_TOKEN = "X-App-Token: " + BuildConfig.API_KEY_SF_GOV;

    /**
     * Get a subset of all street names to be used in a paginated list
     *
     * @param limit the maximum number of results for the API to return
     * @param offset the index of the first result for the API to return
     * @return
     */
    @Headers(HEADER_APP_TOKEN)
    @GET (PATH_STREET_LIST)
    Call<List<StreetBean>> getStreetsPaginated(
            @Query(QUERY_PARAM_LIMIT) Integer limit,
            @Query(QUERY_PARAM_OFFSET) Integer offset);

    @GET
    Call<GeocoderResult> getIntersection(
            @Url String url,
            @Query(QUERY_PARAM_ADDRESS) String address,
            @Query(QUERY_PARAM_SENSOR) Boolean isSensor);
}
