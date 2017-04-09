package com.jeffreymcknight.sanfranciscomap.model;

/**
 * Author: jeffmcknight
 * Created by: ModelGenerator on 4/7/17
 */
public class Viewport {
    public Northeast northeast;
    public Southwest southwest;

    @Override
    public String toString() {
        return "Viewport{" +
                "northeast=" + northeast +
                ", southwest=" + southwest +
                '}';
    }
}