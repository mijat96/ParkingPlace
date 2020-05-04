package com.rmj.parking_place.exceptions;

public class InvalidUsernameOrPasswordException extends RuntimeException {

    public InvalidUsernameOrPasswordException() {

    }

    public InvalidUsernameOrPasswordException(String message) {
        super(message);
    }
}
