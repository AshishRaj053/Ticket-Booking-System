package com.Ashish.Booking.Sytem.TheatreManagement;

import com.Ashish.Booking.Sytem.ShowManagement.ShowService;
import com.Ashish.Booking.Sytem.ShowManagement.TheatreShowResponseDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {


    private final TheatreService theatreService;

    private final ShowService showService;

    public TheatreController(
            TheatreService theatreService,
            ShowService showService){

        this.theatreService = theatreService;
        this.showService = showService;
    }

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

    // business feature
    @GetMapping("/{theatreId}/shows")
    public ResponseEntity<List<TheatreShowResponseDto>>
    getUpcomingShowsByTheatre(
            @PathVariable UUID theatreId){

        return ResponseEntity.ok(
                showService.getUpcomingShowsByTheatre(theatreId)
        );
    }
}
