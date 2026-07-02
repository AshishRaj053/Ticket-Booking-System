package com.Ashish.Booking.Sytem.BookingManagement;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(@Valid @RequestBody BookingRequestDto dto){
       return bookingService.createBooking(dto);
    }


    @GetMapping
    public List<BookingResponseDto> getAllBooking(){
        return bookingService.getAllBookingOfOneUser();
    }

    @GetMapping("/{id}")
    public BookingResponseDto getBookingById(@PathVariable UUID id){
        return bookingService.getBookingById(id);
    }

    @DeleteMapping("/{id}")
    public void cancelBooking(@PathVariable UUID id){
        bookingService.cancelBookingById(id);
    }
}
