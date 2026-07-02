package com.Ashish.Booking.Sytem.ScreenManagement;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/screens")
public class ScreenController {

    @Autowired
    private ScreenService screenService;



    @GetMapping
    public List<ScreenResponseDto> getAll(){
        return screenService.getAll();
    }

    @GetMapping("/{id}")
    public ScreenResponseDto getScreenById(@PathVariable UUID id){
        return screenService.getById(id);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ScreenResponseDto createScreen(@Valid @RequestBody ScreenRequestDto dto){
       return screenService.createScreen(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ScreenResponseDto updateScreen(@PathVariable UUID id,@Valid @RequestBody ScreenRequestDto dto){
        return screenService.updateScreen(id,dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteScreen(@PathVariable UUID id){
         screenService.deleteById(id);
    }

}
