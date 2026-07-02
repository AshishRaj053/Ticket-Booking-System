package com.Ashish.Booking.Sytem.MovieManagement;

import com.Ashish.Booking.Sytem.exception.MovieNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MovieService {

    private MovieResponseDto convertToDto(Movie m) {
        MovieResponseDto movieResponseDto = new MovieResponseDto();
        movieResponseDto.setId(m.getId());
        movieResponseDto.setName(m.getName());
        movieResponseDto.setDescription(m.getDescription());
        movieResponseDto.setGenre(m.getGenre());
        movieResponseDto.setLanguage(m.getLanguage());
        movieResponseDto.setDuration(m.getDuration());
        movieResponseDto.setReleaseDate(m.getReleaseDate());
        return movieResponseDto;
    }

    @Autowired
    private MovieRepo movieRepo;

    public List<MovieResponseDto> getAllMovies() {
        List<Movie> movies = movieRepo.findAll();
        List<MovieResponseDto> movieResponseDtos = new ArrayList<>();
        for(Movie m : movies){
            MovieResponseDto movieResponseDto = convertToDto(m);
            movieResponseDtos.add(movieResponseDto);
        }
        return movieResponseDtos;
    }

    public MovieResponseDto getById(UUID id) {
        Movie movie = movieRepo.findById(id)
                .orElseThrow(
                        () -> new MovieNotFoundException(
                                "Movie not found"
                        )
                );;

                return convertToDto(movie);
    }

    public Movie getMovieById(UUID id){
        Movie movie = movieRepo.findById(id)
                .orElseThrow(
                        () -> new MovieNotFoundException(
                                "Movie not found"
                        )
                );;

        return movie;
    }

    public MovieResponseDto saveMovie(MovieRequestDto m) {
        Movie movie = new Movie();
        movie.setName(m.getName());
        movie.setDescription(m.getDescription());
        movie.setLanguage(m.getLanguage());
        movie.setGenre(m.getGenre());
        movie.setDuration(m.getDuration());
        movie.setReleaseDate(m.getReleaseDate());
        Movie savedMovie = movieRepo.save(movie);
        MovieResponseDto dto = convertToDto(savedMovie);
        return dto;
    }

    public MovieResponseDto updateMovie(UUID id, MovieRequestDto m) {
        Movie movie = movieRepo.findById(id)
                .orElseThrow(
                        () -> new MovieNotFoundException(
                                "Movie not found"
                        )
                );
        movie.setName(m.getName());
        movie.setDescription(m.getDescription());
        movie.setLanguage(m.getLanguage());
        movie.setGenre(m.getGenre());
        movie.setReleaseDate(m.getReleaseDate());
        movie.setDuration(m.getDuration());
        Movie updatedMovie = movieRepo.save(movie);
        MovieResponseDto dto = convertToDto(updatedMovie);
        return dto;
    }

    public void deleteMovieById(UUID id) {
        Movie movie = movieRepo.findById(id)
                .orElseThrow(
                        () -> new MovieNotFoundException(
                                "Movie not found"
                        )
                );

        movieRepo.deleteById(id);
    }
}
