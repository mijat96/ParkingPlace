package com.rmj.parking_place.dto;

import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.TicketType;

public class PaidParkingPlaceDTO {
    private Long id;
    private String startDateTimeAndroid;
    private String startDateTimeServer;
    private TicketType ticketType;
    private boolean arrogantUser;
    private ParkingPlace parkingPlace;
    private Long zoneId;

    public PaidParkingPlaceDTO() {

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

    public void setStartDateTimeServer(String startDateTimeAndroidServer) {
        this.startDateTimeServer = startDateTimeServer;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    public boolean isArrogantUser() {
        return arrogantUser;
    }

    public void setArrogantUser(boolean arrogantUser) {
        this.arrogantUser = arrogantUser;
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
