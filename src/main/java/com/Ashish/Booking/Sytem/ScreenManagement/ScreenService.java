package com.Ashish.Booking.Sytem.ScreenManagement;

import com.Ashish.Booking.Sytem.SeatManagement.Seat;
import com.Ashish.Booking.Sytem.SeatManagement.SeatService;
import com.Ashish.Booking.Sytem.TheatreManagement.TheatreService;
import com.Ashish.Booking.Sytem.exception.ScreenNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ScreenService {
    @Autowired
    private ScreenRepo screenRepo;

    @Autowired
    private TheatreService theatreService;

    @Autowired
    private SeatService seatService;

    /// mapping from requestdto to screen

    private Screen convertRequestToScreen(ScreenRequestDto dto){
        Screen screen  = new Screen();
        screen.setName(dto.getName());
        screen.setTheatre(theatreService.getTheatre(dto.getTheatreId()));
        screen.setTotalRows(dto.getTotalRows());
        screen.setSeatsPerRow(dto.getSeatsPerRow());
        return screen;
    }

    /// mapping from screen to screen response
    private ScreenResponseDto convertScreenToResponse(Screen screen){
        ScreenResponseDto dto = new ScreenResponseDto();
        dto.setId(screen.getId());
        dto.setName(screen.getName());
        dto.setTheatreId(screen.getTheatre().getId());
        dto.setTheatreName(screen.getTheatre().getName());
        dto.setTotalRows(screen.getTotalRows());
        dto.setSeatsPerRow(screen.getSeatsPerRow());
        return dto;
    }


    public List<ScreenResponseDto> getAll() {
        List<Screen> screens = screenRepo.findAll();
        List<ScreenResponseDto> resp = new ArrayList<>();

        for(Screen screen : screens){
            ScreenResponseDto dto = convertScreenToResponse(screen);
            resp.add(dto);
        }
        return resp;
    }

    public ScreenResponseDto getById(UUID id) {
        Screen screen = screenRepo.findById(id).orElseThrow(()-> new ScreenNotFoundException("screen not present"));

        ScreenResponseDto dto = convertScreenToResponse(screen);
        return dto;
    }

    public Screen getScreenById(UUID id){
        Screen screen = screenRepo.findById(id).orElseThrow(()-> new ScreenNotFoundException("screen not present"));
        return screen;
    }

    @Transactional
    public ScreenResponseDto  createScreen(ScreenRequestDto dto) {
        Screen screen = convertRequestToScreen(dto);
        Screen savedScreen = screenRepo.save(screen);

        // create seats here and store them in db
        int totalRows = dto.getTotalRows();
        int totalSeatsPerRow = dto.getSeatsPerRow();
        for(int i=0;i<totalRows;i++){
            for(int j=1;j<=totalSeatsPerRow;j++){
                Seat seat = new Seat();
                // do all set operation here
                // i--> row
                // j --> seat number in that row
                seat.setRowName(String.valueOf((char) ('A' + i)));
                seat.setSeatNumber(j);
                seat.setScreen(savedScreen);
                seatService.createSeat(seat);
            }
        }

        return convertScreenToResponse(savedScreen);
    }

    public ScreenResponseDto updateScreen(UUID id,ScreenRequestDto dto) {
        Screen screen = screenRepo.findById(id).orElseThrow(()-> new ScreenNotFoundException("screen not present"));
        screen.setName(dto.getName());
        screen.setTheatre(theatreService.getTheatre(dto.getTheatreId()));
        screen.setTotalRows(dto.getTotalRows());
        screen.setSeatsPerRow(dto.getSeatsPerRow());
        Screen updatedScreen = screenRepo.save(screen);
        return convertScreenToResponse(updatedScreen);
    }

    public void deleteById(UUID id) {
        Screen screen = screenRepo.findById(id).orElseThrow(()-> new ScreenNotFoundException("screen not present"));
        screenRepo.delete(screen);
    }
}
