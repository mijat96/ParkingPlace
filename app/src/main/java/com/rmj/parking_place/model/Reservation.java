package com.rmj.parking_place.model;

import java.time.LocalDateTime;

public class Reservation {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private ParkingPlace parkingPlace;

    public Reservation() {

    }

    public Reservation(LocalDateTime startDateTime, LocalDateTime endDateTime, ParkingPlace parkingPlace) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.parkingPlace = parkingPlace;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public ParkingPlace getParkingPlace() {
        return parkingPlace;
    }

    public void setParkingPlace(ParkingPlace parkingPlace) {
        this.parkingPlace = parkingPlace;
    }
}
