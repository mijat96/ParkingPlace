package com.rmj.parking_place.model;


import androidx.annotation.Nullable;

import java.util.Objects;

public class ParkingPlace {
    private Location location;
    private ParkingPlaceStatus status;
    private Zone zone;

    public ParkingPlace() {

    }

    public ParkingPlace(Location location, ParkingPlaceStatus status, Zone zone) {
        this.location = location;
        this.status = status;
        this.zone = zone;
    }

    public ParkingPlace(ParkingPlace parkingPlace) {
        this.location = new Location(parkingPlace.location);
        this.status = parkingPlace.status;
        this.zone = new Zone(parkingPlace.zone);
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

}
