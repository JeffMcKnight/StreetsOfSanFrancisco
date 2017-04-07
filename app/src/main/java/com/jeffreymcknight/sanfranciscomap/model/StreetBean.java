package com.jeffreymcknight.sanfranciscomap.model;

/**
 * Created by jeffmcknight on 4/6/17.
 */

public class StreetBean {
    public String fullstreetname;
    public String streetname;
    public String streettype;

    @Override
    public String toString() {
        return "StreetBean{" +
                "fullstreetname='" + fullstreetname + '\'' +
                ", streetname='" + streetname + '\'' +
                ", streettype='" + streettype + '\'' +
                '}';
    }

}
