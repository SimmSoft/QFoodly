package com.example.qfoodly.model;

import com.google.android.gms.maps.model.LatLng;

public class ReturnPoint {
    private String id;
    private String name;
    private String address;
    private LatLng location;
    private String hours;
    private double rating;

    public ReturnPoint(String id, String name, String address, LatLng location, String hours, double rating) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.location = location;
        this.hours = hours;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getHours() {
        return hours;
    }

    public double getRating() {
        return rating;
    }
}