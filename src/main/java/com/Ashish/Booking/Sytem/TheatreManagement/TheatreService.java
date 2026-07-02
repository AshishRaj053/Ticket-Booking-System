package com.Ashish.Booking.Sytem.TheatreManagement;


import com.Ashish.Booking.Sytem.exception.TheatreNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TheatreService {

    @Autowired
    private TheatreRepo theatreRepo;

    private TheatreResponseDto convertTheatreToResponse(Theatre t){

        TheatreResponseDto dto = new TheatreResponseDto();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setLocation(t.getLocation());
        return dto;
    }

    private Theatre convertRequestToTheatre(TheatreRequestDto dto){
        Theatre theatre = new Theatre();
        theatre.setName(dto.getName());
        theatre.setLocation(dto.getLocation());

        return theatre;
    }

    public List<TheatreResponseDto> getAll() {
        List<Theatre> theatres = theatreRepo.findAll();
        List<TheatreResponseDto> dtos = new ArrayList<>();
        for(Theatre theatre : theatres){
            TheatreResponseDto resp  = convertTheatreToResponse(theatre);
            dtos.add(resp);
        }
        return dtos;
    }

    public TheatreResponseDto getTheatreById(UUID id) {
        Theatre theatre = theatreRepo.findById(id)
                .orElseThrow(
                        () -> new TheatreNotFoundException(
                                "Theatre not found"
                        )
                );

        TheatreResponseDto resp = convertTheatreToResponse(theatre);
        return resp;
    }

    public Theatre getTheatre(UUID id) {
        Theatre theatre = theatreRepo.findById(id)
                .orElseThrow(
                        () -> new TheatreNotFoundException(
                                "Theatre not found"
                        )
                );

        return theatre;
    }

    public TheatreResponseDto create(TheatreRequestDto dto) {
        Theatre theatre = convertRequestToTheatre(dto);
        Theatre savedTheatre = theatreRepo.save(theatre);
        TheatreResponseDto savedResp = convertTheatreToResponse(savedTheatre);
        return savedResp;
    }

    public TheatreResponseDto updateTheatre(UUID id,TheatreRequestDto dto) {
        Theatre theatre = theatreRepo.findById(id)
                .orElseThrow(()->new TheatreNotFoundException("Theatre Not Found"));
        theatre.setName(dto.getName());
        theatre.setLocation(dto.getLocation());

        Theatre savedTheatre = theatreRepo.save(theatre);
        TheatreResponseDto resp = convertTheatreToResponse(savedTheatre);
        return resp;
    }

    public void deleteTheatreById(UUID id) {
        Theatre theatre = theatreRepo.findById(id)
                .orElseThrow(
                        () -> new TheatreNotFoundException(
                                "Theatre not found"
                        )
                );

        theatreRepo.deleteById(id);
    }

}
