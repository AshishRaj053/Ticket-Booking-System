package com.Ashish.Booking.Sytem.exception;

public class SeatLockedException extends  RuntimeException{
    public SeatLockedException(String message){
        super(message);
    }
}
