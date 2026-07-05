package com.Ashish.Booking.Sytem.MovieManagement;

import com.Ashish.Booking.Sytem.Config.RedisCacheNames;
import com.Ashish.Booking.Sytem.MovieManagement.search.MovieSearchCriteria;
import com.Ashish.Booking.Sytem.MovieManagement.search.MovieSpecification;
import com.Ashish.Booking.Sytem.common.PageRequestDto;
import com.Ashish.Booking.Sytem.common.PageResponseDto;
import com.Ashish.Booking.Sytem.exception.MovieNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    private Movie convertRequestToMovie(MovieRequestDto m){
        Movie movie = new Movie();
        movie.setName(m.getName());
        movie.setDescription(m.getDescription());
        movie.setLanguage(m.getLanguage());
        movie.setGenre(m.getGenre());
        movie.setDuration(m.getDuration());
        movie.setReleaseDate(m.getReleaseDate());
        return  movie;
    }

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private MovieSpecification movieSpecification;

    public List<MovieResponseDto> getAllMovies() {
        List<Movie> movies = movieRepo.findAll();
        List<MovieResponseDto> movieResponseDtos = new ArrayList<>();
        for(Movie m : movies){
            MovieResponseDto movieResponseDto = convertToDto(m);
            movieResponseDtos.add(movieResponseDto);
        }
        return movieResponseDtos;
    }

    @Cacheable(
            value = RedisCacheNames.MOVIES,
            key = "#id"
    )
    public MovieResponseDto getById(UUID id) {
        System.out.println("Fetching movie from Database...");
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
    @Caching(evict = {

            @CacheEvict(
                    value = RedisCacheNames.TRENDING_MOVIES,
                    allEntries = true
            ),

            @CacheEvict(
                    value = RedisCacheNames.UPCOMING_MOVIES,
                    allEntries = true
            ),

            @CacheEvict(
                    value = RedisCacheNames.NOW_SHOWING_MOVIES,
                    allEntries = true
            )

    })
    public MovieResponseDto saveMovie(MovieRequestDto m) {
        Movie movie = convertRequestToMovie(m);
        Movie savedMovie = movieRepo.save(movie);
        MovieResponseDto dto = convertToDto(savedMovie);
        return dto;
    }

    @Caching(evict = {

            @CacheEvict(
                    value = RedisCacheNames.MOVIES,
                    key = "#id"
            ),

            @CacheEvict(
                    value = RedisCacheNames.UPCOMING_MOVIES,
                    allEntries = true
            ),

            @CacheEvict(
                    value = RedisCacheNames.NOW_SHOWING_MOVIES,
                    allEntries = true
            ),

            @CacheEvict(
                    value = RedisCacheNames.TRENDING_MOVIES,
                    allEntries = true
            ),

            @CacheEvict(
                    value = RedisCacheNames.MOVIE_SHOWS,
                    key = "#id"
            ),

            @CacheEvict(
                    value = RedisCacheNames.THEATRE_SHOWS,
                    allEntries = true
            ),

            @CacheEvict(
                    value = RedisCacheNames.SHOWS,
                    allEntries = true
            )

    })
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


    @Caching(evict = {

            @CacheEvict(
                    value = RedisCacheNames.MOVIES,
                    key = "#id"
            ),

            @CacheEvict(
                    value = RedisCacheNames.UPCOMING_MOVIES,
                    allEntries = true
            ),

            @CacheEvict(
                    value = RedisCacheNames.NOW_SHOWING_MOVIES,
                    allEntries = true
            ),

            @CacheEvict(
                    value = RedisCacheNames.TRENDING_MOVIES,
                    allEntries = true
            ),

            @CacheEvict(
                    value = RedisCacheNames.MOVIE_SHOWS,
                    key = "#id"
            ),

            @CacheEvict(
                    value = RedisCacheNames.THEATRE_SHOWS,
                    allEntries = true
            ),

            @CacheEvict(
                    value = RedisCacheNames.SHOWS,
                    allEntries = true
            )

    })
    public void deleteMovieById(UUID id) {
        Movie movie = movieRepo.findById(id)
                .orElseThrow(
                        () -> new MovieNotFoundException(
                                "Movie not found"
                        )
                );

        movieRepo.deleteById(id);
    }
    /// business feature
    public  List<MovieResponseDto> searchMovies(
            MovieSearchCriteria criteria){
        Specification<Movie> specification = MovieSpecification.search(criteria);
        List<Movie> movies = movieRepo.findAll(specification);
        List<MovieResponseDto> resp = new ArrayList<>();
        for(Movie movie : movies){
            MovieResponseDto dto = convertToDto(movie);
            resp.add(dto);
        }
    return resp;
    }

    public PageResponseDto<MovieResponseDto> getAllMoviesPaged(
            PageRequestDto request){

        Page<Movie> page = movieRepo.findAll(request.toPageable());

        List<MovieResponseDto> movies =
                page.getContent()
                        .stream()
                        .map(this::convertToDto)
                        .toList();

        PageResponseDto<MovieResponseDto> response = new PageResponseDto<>();

        response.setContent(movies);

        response.setPage(page.getNumber());

        response.setSize(page.getSize());

        response.setTotalElements(
                page.getTotalElements()
        );

        response.setTotalPages(
                page.getTotalPages()
        );

        response.setLast(
                page.isLast()
        );

        return response;
    }

    @Cacheable(
            value = RedisCacheNames.UPCOMING_MOVIES,
            key = "#request.page + '-' + #request.size + '-' + #request.sortBy + '-' + #request.direction"
    )
    public PageResponseDto<MovieResponseDto> getUpcomingMovies(PageRequestDto request) {
        System.out.println("upcoming service reached");
        Page<Movie> page = movieRepo.findByReleaseDateAfter(LocalDate.now(),request.toPageable());
        List<MovieResponseDto> movies =
                page.getContent()
                        .stream()
                        .map(this::convertToDto)
                        .toList();

        PageResponseDto<MovieResponseDto> response = new PageResponseDto<>();

        response.setContent(movies);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());

        return response;
    }


    @Cacheable(
            value = RedisCacheNames.NOW_SHOWING_MOVIES,
            key = "#request.page + '-' + #request.size + '-' + #request.sortBy + '-' + #request.direction"
    )
    public PageResponseDto<MovieResponseDto> getNowShowingMovies(PageRequestDto request) {

        Page<Movie> page = movieRepo.findNowShowing(LocalDate.now(), request.toPageable());
        List<MovieResponseDto> movies =
                page.getContent()
                        .stream()
                        .map(this::convertToDto)
                        .toList();

        PageResponseDto<MovieResponseDto> response = new PageResponseDto<>();

        response.setContent(movies);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());

        return response;
    }

    @Cacheable(
            value = RedisCacheNames.TRENDING_MOVIES,
            key = "#request.page + '-' + #request.size"
    )
    public PageResponseDto<MovieResponseDto> getTrendingMovies(PageRequestDto request) {

        System.out.println("service reached");

        Page<Movie> page = movieRepo.findTrendingMovies(request.toPageableWithoutSort());
        List<MovieResponseDto> movies =
                page.getContent()
                        .stream()
                        .map(this::convertToDto)
                        .toList();

        PageResponseDto<MovieResponseDto> response = new PageResponseDto<>();

        response.setContent(movies);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());

        return response;
    }
}
