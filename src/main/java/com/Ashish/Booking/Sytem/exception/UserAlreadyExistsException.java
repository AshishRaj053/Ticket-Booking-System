package com.Ashish.Booking.Sytem.exception;

public class UserAlreadyExistsException extends RuntimeException{

    public UserAlreadyExistsException(String message){
            super(message);
    }
}
