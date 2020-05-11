package com.rmj.parking_place.dto;

public class DTO {
    private Long zoneId;
    private Long parkingPlaceId;


    public DTO() {

    }

    public DTO(Long zoneId, Long parkingPlaceId) {
        this.zoneId = zoneId;
        this.parkingPlaceId = parkingPlaceId;
    }


    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public Long getParkingPlaceId() {
        return parkingPlaceId;
    }

    public void setParkingPlaceId(Long parkingPlaceId) {
        this.parkingPlaceId = parkingPlaceId;
    }
}
