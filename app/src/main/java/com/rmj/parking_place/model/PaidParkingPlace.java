package com.rmj.parking_place.model;


import java.util.Date;

public class PaidParkingPlace {
    private ParkingPlace parkingPlace;
    private Date startDateTime;
    private TicketType ticketType;
    private boolean arrogantUser;

    public PaidParkingPlace() {

    }

    public PaidParkingPlace(ParkingPlace parkingPlace) {
        this.parkingPlace = new ParkingPlace(parkingPlace);
        this.startDateTime = new Date();
        this.ticketType = TicketType.REGULAR;
        this.arrogantUser = false;
    }

    public PaidParkingPlace(ParkingPlace parkingPlace, Date startDateTime,
                            TicketType ticketType, boolean arrogantUser) {
        this.parkingPlace = new ParkingPlace(parkingPlace);
        this.startDateTime = startDateTime;
        this.ticketType = ticketType;
        this.arrogantUser = arrogantUser;
    }

    public Date getEndDateTime() {
        Zone zone = this.parkingPlace.getZone();
        TicketPrice ticketPrice = zone.getTicketPrice(this.ticketType);
        long duration = ticketPrice.getDuration() * 3600000; // form hours to milliseconds
        return new Date(this.startDateTime.getTime() + duration);
    }

    public ParkingPlace getParkingPlace() {
        return parkingPlace;
    }

    public void setParkingPlace(ParkingPlace parkingPlace) {
        this.parkingPlace = parkingPlace;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
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
