package com.rmj.parking_place.exceptions;

public class AlreadyTakenParkingPlaceException extends RuntimeException {

    public AlreadyTakenParkingPlaceException() {

    }

    public AlreadyTakenParkingPlaceException(String message) {
        super(message);
    }
}
