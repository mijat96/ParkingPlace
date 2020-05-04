package com.rmj.parking_place.exceptions;

public class NotFoundParkingPlaceException extends RuntimeException {

    public  NotFoundParkingPlaceException() {

    }

    public NotFoundParkingPlaceException(String message) {
        super(message);
    }
}
