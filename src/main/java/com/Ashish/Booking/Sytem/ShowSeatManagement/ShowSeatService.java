package com.Ashish.Booking.Sytem.ShowSeatManagement;

import com.Ashish.Booking.Sytem.SeatManagement.Seat;
import com.Ashish.Booking.Sytem.SeatManagement.SeatResponseDto;
import com.Ashish.Booking.Sytem.SeatManagement.SeatService;
import com.Ashish.Booking.Sytem.ShowManagement.Show;
import com.Ashish.Booking.Sytem.exception.ShowSeatNotAvailableException;
import com.Ashish.Booking.Sytem.exception.ShowSeatNotFoundException;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShowSeatService {
    @Autowired
    private ShowSeatRepo showSeatRepo;

    @Autowired
    private SeatService seatService;

    private ShowSeatResponseDto convertShowSeatToResponse(ShowSeat showSeat){
        ShowSeatResponseDto showSeatResponseDto = new ShowSeatResponseDto();
        String row = showSeat.getSeat().getRowName();
        int seatNo = showSeat.getSeat().getSeatNumber();
        showSeatResponseDto.setLabel(row + seatNo);
        showSeatResponseDto.setStatus(showSeat.getStatus());
        return showSeatResponseDto;
    }

//    Get seat map
    public List<ShowSeatResponseDto> getAllSeatForAShow(UUID showId) {
        List<ShowSeat> showSeats = showSeatRepo.findByShowId(showId);
        List<ShowSeatResponseDto> resp = new ArrayList<>();

        for(ShowSeat showSeat : showSeats){
            ShowSeatResponseDto dto = convertShowSeatToResponse(showSeat);
            resp.add(dto);
        }
        return resp;
    }
    // Generate ShowSeats
    public void generateShowSeats(Show show){
        UUID screenId = show.getScreen().getId();
        List<Seat> seats = seatService.getAllSeat(screenId);

        for(Seat seat : seats){
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShow(show);
            showSeat.setSeat(seat);
            showSeat.setStatus(SeatStatus.AVAILABLE);
            showSeatRepo.save(showSeat);
        }

    }

    // check if showSeat available

    public ShowSeat getShowSeat(UUID showId, UUID seatId){
        ShowSeat showSeat = showSeatRepo.findByShowIdAndSeatId(showId,seatId).orElseThrow(()->
                new ShowSeatNotFoundException("show seat not found"));
        return showSeat;
    }

    public boolean isAvailable(UUID showId, UUID seatId){
        ShowSeat showSeat = getShowSeat(showId,seatId);
        if(showSeat.getStatus().equals(SeatStatus.AVAILABLE)){
            return true;
        }
        return false;
    }

    //Update seat status
    public void updateBookedToAvailable(UUID showId, UUID seatId){
        ShowSeat showSeat = getShowSeat(showId,seatId);
        showSeat.setStatus(SeatStatus.AVAILABLE);
        showSeatRepo.save(showSeat);
    }
    public void updateAvailableToBooked(UUID showId, UUID seatId){
        ShowSeat showSeat = getShowSeat(showId,seatId);
        showSeat.setStatus(SeatStatus.BOOKED);
        showSeatRepo.save(showSeat);
    }

    public void bookSeats(UUID showId,UUID seatId){
        ShowSeat showSeat = getShowSeat(showId,seatId);
        if(!showSeat.getStatus().equals(SeatStatus.AVAILABLE)){
            throw new ShowSeatNotAvailableException("show seat is not available");
        }
        updateAvailableToBooked(showId,seatId);
    }
}
