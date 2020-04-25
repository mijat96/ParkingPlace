package com.rmj.parking_place.model;

import java.util.Objects;

public class Location {
    private double latitude;
    private double longitude;
    private String address;

    public Location() {

    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = null;
    }

    public Location(double latitude, double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public Location(Location location) {
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        if (location.address == null) {
            this.address = null;
        }
        else {
            this.address = location.address;
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.latitude, latitude) == 0 &&
                Double.compare(location.longitude, longitude) == 0 &&
                Objects.equals(address, location.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, address);
    }
}
