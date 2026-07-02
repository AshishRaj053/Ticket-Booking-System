package com.Ashish.Booking.Sytem.exception;

import com.Ashish.Booking.Sytem.ShowSeatManagement.ShowSeat;

public class ShowSeatNotFoundException extends RuntimeException{
    public ShowSeatNotFoundException(String message){
        super(message);
    }
}
