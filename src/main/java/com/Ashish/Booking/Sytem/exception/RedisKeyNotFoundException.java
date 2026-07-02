package com.Ashish.Booking.Sytem.exception;

class RedisKeyNotFoundException extends RuntimeException {

    public RedisKeyNotFoundException(String message) {
        super(message);
    }
}