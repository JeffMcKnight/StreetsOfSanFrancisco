package com.jeffreymcknight.sanfranciscomap.model;

import java.util.Arrays;

/**
 * Author: jeffmcknight
 * Created by: ModelGenerator on 4/7/17
 */
public class GeocoderResult {
    public ResultsItem[] results;
    public String status;

    @Override
    public String toString() {
        return "GeocoderResult{" +
                "results=" + Arrays.toString(results) +
                ", status='" + status + '\'' +
                '}';
    }
}