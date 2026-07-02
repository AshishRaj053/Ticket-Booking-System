package com.Ashish.Booking.Sytem.TheatreManagement;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    @Autowired
    private TheatreService theatreService;


    @GetMapping
    public List<TheatreResponseDto> getAllTheatre(){
        return theatreService.getAll();
    }

    @GetMapping("/{id}")
    public TheatreResponseDto getTheatreById(@PathVariable UUID id){
        return theatreService.getTheatreById(id);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public TheatreResponseDto createTheatre(@Valid @RequestBody TheatreRequestDto dto){
        return theatreService.create(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public TheatreResponseDto updateTheatre(@PathVariable UUID id,@Valid @RequestBody TheatreRequestDto dto){
        return theatreService.updateTheatre(id,dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteTheatreById(@PathVariable UUID id){
        theatreService.deleteTheatreById(id);
    }
}
