package com.Ashish.Booking.Sytem.ShowSeatManagement;

import com.Ashish.Booking.Sytem.SeatManagement.Seat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shows")
public class ShowSeatController {
    @Autowired
    private ShowSeatService showSeatService;

    @GetMapping("/{showId}/seats")
    public List<ShowSeatResponseDto> getAllSeatForAShow(@PathVariable UUID showId){
        return showSeatService.getAllSeatForAShow(showId);
    }
}
