package com.rmj.parking_place.model;


import java.util.Calendar;
import java.util.Date;

public class Reservation {
    private Date startDateTime;
    private Date endDateTime;
    private ParkingPlace parkingPlace;

    private static final long ONE_MINUTE_IN_MILLISECONDS = 60000;
    private static final long DURATION_OF_RESERVATION = 1; // 10; // min

    public Reservation() {

    }

    public Reservation(ParkingPlace parkingPlace) {
        this.startDateTime = new Date();

        long startDateTimeInMillis = this.startDateTime.getTime();
        this.endDateTime = new Date(startDateTimeInMillis + (DURATION_OF_RESERVATION * ONE_MINUTE_IN_MILLISECONDS));
        this.parkingPlace = new ParkingPlace(parkingPlace);
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public ParkingPlace getParkingPlace() {
        return parkingPlace;
    }

    public void setParkingPlace(ParkingPlace parkingPlace) {
        this.parkingPlace = parkingPlace;
    }
}
