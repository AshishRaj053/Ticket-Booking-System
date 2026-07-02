package com.Ashish.Booking.Sytem.SeatManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/screens")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @GetMapping("/{id}/seats")
    public List<SeatResponseDto> getAllSeatForOneScreen(@PathVariable UUID id){
        return seatService.getAllSeatForOneScreen(id);
    }
}
