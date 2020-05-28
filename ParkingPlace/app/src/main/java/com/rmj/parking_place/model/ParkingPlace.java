package com.rmj.parking_place.model;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.Objects;

public class ParkingPlace implements Parcelable {
    private Long id;
    private Location location;
    private ParkingPlaceStatus status;
    private Zone zone;

    public ParkingPlace() {

    }

    public ParkingPlace(Long id, Location location, ParkingPlaceStatus status, Zone zone) {
        this.id = id;
        this.location = location;
        this.status = status;
        this.zone = zone;
    }

    public ParkingPlace(ParkingPlace parkingPlace) {
        this.id = parkingPlace.id;
        this.location = new Location(parkingPlace.location);
        this.status = parkingPlace.status;
        this.zone = new Zone(parkingPlace.zone);
    }

    protected ParkingPlace(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        location = in.readParcelable(Location.class.getClassLoader());
        zone = in.readParcelable(Zone.class.getClassLoader());
    }

    public static final Creator<ParkingPlace> CREATOR = new Creator<ParkingPlace>() {
        @Override
        public ParkingPlace createFromParcel(Parcel in) {
            return new ParkingPlace(in);
        }

        @Override
        public ParkingPlace[] newArray(int size) {
            return new ParkingPlace[size];
        }
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ParkingPlaceStatus getStatus() {
        return status;
    }

    public void setStatus(ParkingPlaceStatus status) {
        this.status = status;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean hasCoordinates(double latitude, double longitude) {
        return this.location.getLatitude() == latitude && this.location.getLongitude() == longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingPlace that = (ParkingPlace) o;
        return location.equals(that.location) &&
                status == that.status &&
                zone.equals(that.zone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, status, zone);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeParcelable(location, flags);
        dest.writeParcelable(zone, flags);
    }

    public static void writeToParcelWithoutZone(Parcel dest, int flags, ParkingPlace parkingPlace) {
        if (parkingPlace.id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(parkingPlace.id);
        }
        dest.writeParcelable(parkingPlace.location, flags);
    }

    public static ParkingPlace readToParcelWithoutZone(Parcel in) {
        ParkingPlace parkingPlace = new ParkingPlace();
        if (in.readByte() == 0) {
            parkingPlace.id = null;
        } else {
            parkingPlace.id = in.readLong();
        }
        parkingPlace.location = in.readParcelable(Location.class.getClassLoader());
        return parkingPlace;
    }
}
