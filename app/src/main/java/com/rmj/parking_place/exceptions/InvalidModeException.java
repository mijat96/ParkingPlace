package com.rmj.parking_place.exceptions;

public class InvalidModeException extends RuntimeException {

    public InvalidModeException() {

    }

    public InvalidModeException(String message) {
        super(message);
    }
}
