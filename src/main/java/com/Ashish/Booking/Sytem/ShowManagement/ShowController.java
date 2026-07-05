package com.Ashish.Booking.Sytem.ShowManagement;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shows")
public class ShowController {
    @Autowired
    private ShowService showService;
    @GetMapping
    public List<ShowResponseDto> getAll(){
        return showService.getAllShows();
    }

    @GetMapping("/{id}")
    public ShowResponseDto getById(@PathVariable UUID id){
       return showService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ShowResponseDto createShow(@Valid @RequestBody ShowRequestDto dto){
        return showService.createShow(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ShowResponseDto updateShow(@PathVariable UUID id, @Valid @RequestBody ShowRequestDto dto){
        return showService.updateShow(id,dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteShow(@PathVariable UUID id){
        showService.deleteShow(id);
    }

}
