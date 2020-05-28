package com.rmj.parking_place.dto;

public class ReservingDTO extends DTO {
    private double currentLocationLatitude;
    private double currentLocationLongitude;

    public ReservingDTO() {

    }

    public ReservingDTO(double currentLocationLatitude, double currentLocationLongitude) {
        this.currentLocationLatitude = currentLocationLatitude;
        this.currentLocationLongitude = currentLocationLongitude;
    }

    public ReservingDTO(Long zoneId, Long parkingPlaceId, double currentLocationLatitude, double currentLocationLongitude) {
        super(zoneId, parkingPlaceId);
        this.currentLocationLatitude = currentLocationLatitude;
        this.currentLocationLongitude = currentLocationLongitude;
    }

    public double getCurrentLocationLatitude() {
        return currentLocationLatitude;
    }

    public void setCurrentLocationLatitude(double currentLocationLatitude) {
        this.currentLocationLatitude = currentLocationLatitude;
    }

    public double getCurrentLocationLongitude() {
        return currentLocationLongitude;
    }

    public void setCurrentLocationLongitude(double currentLocationLongitude) {
        this.currentLocationLongitude = currentLocationLongitude;
    }
}
