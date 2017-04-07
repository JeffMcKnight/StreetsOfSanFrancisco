package com.jeffreymcknight.sanfranciscomap.api;

/**
 * Created by jeffmcknight on 4/6/17.
 */

import com.jeffreymcknight.sanfranciscomap.model.StreetBean;

import java.io.IOException;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton to make API calls
 * Created by jeffmcknight on 11/17/16.
 */
public class ApiClient {
    private static ApiClient sApiClient;
    private SfGovApi mmSfGovApi;

    public void setSfGovApi(SfGovApi mmSfGovApi) {
        this.mmSfGovApi = mmSfGovApi;
    }

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
                    .baseUrl(SfGovApi.BASE_HTTP_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            SfGovApi apiService = retrofit.create(SfGovApi.class);
            sApiClient.setSfGovApi(apiService);
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
        mmSfGovApi.getStreetsPaginated(limit, offset).enqueue(callback);
    }

    /**
     * Synchronously retrieves list of San Francisco streets from sfgov.org
     * @param limit
     * @param offset
     * @return the list of SF streets
     */
    public Response<List<StreetBean>> getStreets(Integer limit, Integer offset) {
        try {
            return mmSfGovApi.getStreetsPaginated(limit, offset).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
