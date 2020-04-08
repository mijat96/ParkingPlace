package com.rmj.parking_place.model;

import java.time.LocalDateTime;

public class PaidParkingPlace {
    private ParkingPlace parkingPlace;
    private LocalDateTime startDateTime;
    private TicketType ticketType;
    private boolean arrogantUser;

    public PaidParkingPlace() {

    }

    public PaidParkingPlace(ParkingPlace parkingPlace, LocalDateTime startDateTime,
                            TicketType ticketType, boolean arrogantUser) {
        this.parkingPlace = parkingPlace;
        this.startDateTime = startDateTime;
        this.ticketType = ticketType;
        this.arrogantUser = arrogantUser;
    }

    public ParkingPlace getParkingPlace() {
        return parkingPlace;
    }

    public void setParkingPlace(ParkingPlace parkingPlace) {
        this.parkingPlace = parkingPlace;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
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
}
