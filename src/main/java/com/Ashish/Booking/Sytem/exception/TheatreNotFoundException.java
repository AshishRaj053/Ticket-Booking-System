package com.Ashish.Booking.Sytem.exception;


public class TheatreNotFoundException extends  RuntimeException{
    public TheatreNotFoundException(String message){
        super(message);
    }
}
