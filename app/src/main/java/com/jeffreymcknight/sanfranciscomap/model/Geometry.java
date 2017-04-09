package com.jeffreymcknight.sanfranciscomap.model;

/**
 * Author: jeffmcknight
 * Created by: ModelGenerator on 4/7/17
 */
public class Geometry {
    public Location location;
    public String locationType;
    public Viewport viewport;

    @Override
    public String toString() {
        return "Geometry{" +
                "location=" + location +
                ", locationType='" + locationType + '\'' +
                ", viewport=" + viewport +
                '}';
    }
}