package com.rmj.parking_place.dto;

import com.rmj.parking_place.model.ParkingPlace;

public class ReservationDTO {
    private Long id;
    private String startDateTimeAndroid;
    private String startDateTimeServer;
    private ParkingPlace parkingPlace;
    private Long zoneId;

    public ReservationDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartDateTimeAndroid() {
        return startDateTimeAndroid;
    }

    public void setStartDateTimeAndroid(String startDateTimeAndroid) {
        this.startDateTimeAndroid = startDateTimeAndroid;
    }

    public String getStartDateTimeServer() {
        return startDateTimeServer;
    }

    public void setStartDateTimeServer(String startDateTimeServer) {
        this.startDateTimeServer = startDateTimeServer;
    }

    public ParkingPlace getParkingPlace() {
        return parkingPlace;
    }

    public void setParkingPlace(ParkingPlace parkingPlace) {
        this.parkingPlace = parkingPlace;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }
}
