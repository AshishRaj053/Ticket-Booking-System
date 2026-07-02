package com.Ashish.Booking.Sytem.SeatManagement;

import com.Ashish.Booking.Sytem.ScreenManagement.Screen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SeatService {

    @Autowired
    private SeatRepo seatRepo;

    private SeatResponseDto convertSeatToResponse(Seat seat){
        SeatResponseDto dto = new SeatResponseDto();
        dto.setId(seat.getId());
        dto.setRowName(seat.getRowName());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setLabel(seat.getRowName()+seat.getSeatNumber());
        return dto;
    }

    public List<SeatResponseDto> getAllSeatForOneScreen(UUID id) {
        List<Seat> seats = seatRepo.findByScreenId(id);
        List<SeatResponseDto> resp = new ArrayList<>();
        for(Seat seat : seats){
            SeatResponseDto dto = convertSeatToResponse(seat);
            resp.add(dto);
        }
        return resp;
    }

    public List<Seat> getAllSeat(UUID id) {
        List<Seat> seats = seatRepo.findByScreenId(id);
      return seats;
    }

    public void createSeat(Seat seat){
        seatRepo.save(seat);
    }
    public Optional<Seat> getSeatById(UUID id){
        Optional<Seat> seat = seatRepo.findById(id);
        return seat;
    }


}
