package com.rmj.parking_place.dto;

import com.rmj.parking_place.model.ParkingPlace;

import java.util.List;

public class ParkingPlacesInitialDTO {
    private Long zoneId;
    private Long version;
    private List<ParkingPlace> parkingPlaces;


    public ParkingPlacesInitialDTO() {

    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<ParkingPlace> getParkingPlaces() {
        return parkingPlaces;
    }

    public void setParkingPlaces(List<ParkingPlace> parkingPlaces) {
        this.parkingPlaces = parkingPlaces;
    }
}
