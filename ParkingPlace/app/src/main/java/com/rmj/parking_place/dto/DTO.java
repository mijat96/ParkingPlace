package com.rmj.parking_place.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DTO {
    private Long zoneId;
    private Long parkingPlaceId;
    private String dateTimeAndroid;
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public DTO() {

    }

    public DTO(Long zoneId, Long parkingPlaceId) {
        this.zoneId = zoneId;
        this.parkingPlaceId = parkingPlaceId;
        this.dateTimeAndroid = sdf.format(new Date());
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
