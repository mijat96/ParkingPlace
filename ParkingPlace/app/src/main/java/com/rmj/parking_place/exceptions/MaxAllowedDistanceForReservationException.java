package com.rmj.parking_place.exceptions;

public class MaxAllowedDistanceForReservationException extends RuntimeException {

    public MaxAllowedDistanceForReservationException() {

    }

    public MaxAllowedDistanceForReservationException(String message) {
        super(message);
    }
}
