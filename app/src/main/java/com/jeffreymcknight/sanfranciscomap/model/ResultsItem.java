package com.jeffreymcknight.sanfranciscomap.model;

import java.util.Arrays;

/**
 * Author: jeffmcknight
 * Created by: ModelGenerator on 4/7/17
 */
public class ResultsItem {
    public AddressComponentsItem[] addressComponents;
    public String formattedAddress;
    public Geometry geometry;
    public boolean partialMatch;
    public String placeId;
    public String[] types;

    @Override
    public String toString() {
        return "ResultsItem{" +
                "addressComponents=" + Arrays.toString(addressComponents) +
                ", formattedAddress='" + formattedAddress + '\'' +
                ", geometry=" + geometry +
                ", partialMatch=" + partialMatch +
                ", placeId='" + placeId + '\'' +
                ", types=" + Arrays.toString(types) +
                '}';
    }
}