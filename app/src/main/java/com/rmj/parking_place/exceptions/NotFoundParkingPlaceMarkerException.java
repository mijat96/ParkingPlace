package com.rmj.parking_place.exceptions;

public class NotFoundParkingPlaceMarkerException extends RuntimeException {

    public NotFoundParkingPlaceMarkerException() {

    }

    public NotFoundParkingPlaceMarkerException(String message) {
        super(message);
    }
}
