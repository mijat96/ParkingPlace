package com.rmj.parking_place.exceptions;

public class InvalidNumberOfParamsException extends RuntimeException {

    public InvalidNumberOfParamsException() {

    }

    public InvalidNumberOfParamsException(String message) {
        super(message);
    }
}
