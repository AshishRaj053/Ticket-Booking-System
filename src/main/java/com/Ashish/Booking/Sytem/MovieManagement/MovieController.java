package com.Ashish.Booking.Sytem.MovieManagement;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;


    // anyone logged in can use them
    @GetMapping
    public List<MovieResponseDto> getAllMovis(){
       return movieService.getAllMovies();
    }

    @GetMapping("/{id}")
    public MovieResponseDto getMovieById(@PathVariable UUID id){
        return movieService.getById(id);
    }

    // admin only can access this endpoint
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public MovieResponseDto createMovie(@Valid @RequestBody MovieRequestDto movie){
        return movieService.saveMovie(movie);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public MovieResponseDto updateMovie(@PathVariable UUID id ,@Valid @RequestBody MovieRequestDto movie){
        return movieService.updateMovie(id,movie);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteMovie(@PathVariable UUID id){
        movieService.deleteMovieById(id);
    }
}
