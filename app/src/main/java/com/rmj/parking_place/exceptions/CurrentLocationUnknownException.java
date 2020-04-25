package com.rmj.parking_place.exceptions;

public class CurrentLocationUnknownException extends RuntimeException {

    public CurrentLocationUnknownException() {

    }

    public CurrentLocationUnknownException(String message) {
        super(message);
    }
}
