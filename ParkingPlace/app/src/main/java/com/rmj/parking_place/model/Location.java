package com.rmj.parking_place.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.rmj.parking_place.database.LocationDb;

import java.util.Objects;

public class Location implements Parcelable {
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

    protected Location(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        address = in.readString();
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public Location(LocationDb locationDb) {
        this.latitude = locationDb.latitude;
        this.longitude = locationDb.longitude;
        this.address = locationDb.address;
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
                Double.compare(location.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(address);
    }
}
