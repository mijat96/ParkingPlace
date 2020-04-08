package com.rmj.parking_place.model;


public class ParkingPlace {
    private Location location;
    private ParkingPlaceStatus status;
    private Zone zone;

    public ParkingPlace() {

    }

    public ParkingPlace(Location location, ParkingPlaceStatus status, Zone zone) {
        this.status = status;
        this.zone = zone;
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
}
