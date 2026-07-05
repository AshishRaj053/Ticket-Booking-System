package com.Ashish.Booking.Sytem.MovieManagement;

import com.Ashish.Booking.Sytem.MovieManagement.search.MovieSearchCriteria;
import com.Ashish.Booking.Sytem.ShowManagement.MovieShowResponseDto;
import com.Ashish.Booking.Sytem.ShowManagement.ShowService;
import com.Ashish.Booking.Sytem.common.PageRequestDto;
import com.Ashish.Booking.Sytem.common.PageResponseDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final ShowService showService;
    private  final MovieService movieService;
    public MovieController(
            MovieService movieService,
            ShowService showService){

        this.movieService = movieService;
        this.showService = showService;
    }


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

    // business controller
    @PostMapping("/search")
    public ResponseEntity<List<MovieResponseDto>> searchMovies(
            @RequestBody MovieSearchCriteria criteria) {

        return ResponseEntity.ok(
                movieService.searchMovies(criteria)
        );
    }
        @PostMapping("/paged")
        public ResponseEntity<PageResponseDto<MovieResponseDto>> getAllMoviesPaged(@RequestBody PageRequestDto request){

            return ResponseEntity.ok(
                    movieService.getAllMoviesPaged(request)
            );
        }

    @PostMapping("/upcoming")
    public ResponseEntity<PageResponseDto<MovieResponseDto>> getUpcomingMovies(
            @RequestBody PageRequestDto request) {
        System.out.println("upcoming controller reached");
        return ResponseEntity.ok(
                movieService.getUpcomingMovies(request)
        );
    }

    @PostMapping("/now-showing")
    public ResponseEntity<PageResponseDto<MovieResponseDto>>
    getNowShowingMovies(
            @RequestBody PageRequestDto request){

        return ResponseEntity.ok(
                movieService.getNowShowingMovies(request)
        );
    }

    @PostMapping("/trending")
    public ResponseEntity<PageResponseDto<MovieResponseDto>>
    getTrendingMovies(
            @RequestBody PageRequestDto request) {
        System.out.println("controller reached");
        return ResponseEntity.ok(
                movieService.getTrendingMovies(request));

    }

    @GetMapping("/{movieId}/shows")
    public ResponseEntity<List<MovieShowResponseDto>>
    getUpcomingShowsByMovie(
            @PathVariable UUID movieId){

        return ResponseEntity.ok(
                showService.getUpcomingShowsByMovie(movieId)
        );
    }


}
