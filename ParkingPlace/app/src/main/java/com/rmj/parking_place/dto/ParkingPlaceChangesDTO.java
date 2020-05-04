package com.rmj.parking_place.dto;

import java.util.List;

public class ParkingPlaceChangesDTO {
    private Long zoneId;
    private Long version;
    private List<ParkingPlaceDTO> parkingPlaceChanges;


    public ParkingPlaceChangesDTO() {

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

    public List<ParkingPlaceDTO> getParkingPlaceChanges() {
        return parkingPlaceChanges;
    }

    public void setParkingPlaceChanges(List<ParkingPlaceDTO> parkingPlaceChanges) {
        this.parkingPlaceChanges = parkingPlaceChanges;
    }
}
