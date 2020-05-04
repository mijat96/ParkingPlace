package com.rmj.parking_place.exceptions;

public class AlreadyReservedParkingPlaceException extends RuntimeException {

    public AlreadyReservedParkingPlaceException() {

    }

    public AlreadyReservedParkingPlaceException(String message) {
        super(message);
    }
}
