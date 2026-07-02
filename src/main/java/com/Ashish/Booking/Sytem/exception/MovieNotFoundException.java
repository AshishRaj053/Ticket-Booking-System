package com.Ashish.Booking.Sytem.exception;

public class MovieNotFoundException extends  RuntimeException{
    public MovieNotFoundException(String message){
        super(message);
    }
}
