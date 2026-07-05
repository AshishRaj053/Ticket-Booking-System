package com.Ashish.Booking.Sytem.ShowManagement;

import com.Ashish.Booking.Sytem.Config.RedisCacheNames;
import com.Ashish.Booking.Sytem.MovieManagement.MovieService;
import com.Ashish.Booking.Sytem.ScreenManagement.ScreenService;
import com.Ashish.Booking.Sytem.ShowSeatManagement.ShowSeatService;
import com.Ashish.Booking.Sytem.TheatreManagement.TheatreService;
import com.Ashish.Booking.Sytem.exception.InvalidShowScheduleException;
import com.Ashish.Booking.Sytem.exception.ShowNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ShowService {
    @Autowired
    private ShowRepo showRepo;

    @Autowired
    private ScreenService screenService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private ShowSeatService showSeatService;

    private ShowResponseDto convertShowToResponse(Show show){
        ShowResponseDto resp = new ShowResponseDto();
        resp.setId(show.getId());
        resp.setShowDate(show.getShowDate());
        resp.setMovieTitle(show.getMovie().getName());
        resp.setScreenName(show.getScreen().getName());
        resp.setTheatreName(show.getScreen().getTheatre().getName());
        resp.setStartTime(show.getStartTime());
        resp.setEndTime(show.getStartTime().plusMinutes(show.getMovie().getDuration()));
        return resp;
    }

    private Show convertRequestToShow(ShowRequestDto dto){
        Show show = new Show();
        show.setShowDate(dto.getShowDate());
        show.setEndTime(dto.getStartTime().plusMinutes(movieService.getMovieById(dto.getMovieId()).getDuration()));
        show.setStartTime(dto.getStartTime());
        show.setScreen(screenService.getScreenById(dto.getScreenId()));
        show.setMovie(movieService.getMovieById(dto.getMovieId()));
        return show;
    }

    private TheatreShowResponseDto convertToTheatreShowResponse(
            Show show){

        TheatreShowResponseDto dto =
                new TheatreShowResponseDto();

        dto.setShowId(show.getId());

        dto.setMovieId(
                show.getMovie().getId()
        );

        dto.setMovieName(
                show.getMovie().getName()
        );

        dto.setScreenId(
                show.getScreen().getId()
        );

        dto.setScreenName(
                show.getScreen().getName()
        );

        dto.setShowDate(
                show.getShowDate()
        );

        dto.setStartTime(
                show.getStartTime()
        );

        return dto;
    }

    private void validateShowScheduleForCreate(ShowRequestDto dto){
        //        1. Today's show time cannot be in past
        LocalDate showDate = dto.getShowDate();
        LocalDate today = LocalDate.now();
        LocalTime showTime = dto.getStartTime();
        LocalTime currentTime = LocalTime.now();
        if(showDate.equals(today)
                && showTime.isBefore(LocalTime.now())) {
            throw new InvalidShowScheduleException(
                    "Show time cannot be in the past"
            );
        }

//        2. No overlapping shows on same screen and date
        UUID screenId = dto.getScreenId();
        LocalTime showEndTime = dto.getStartTime().plusMinutes(movieService.getMovieById(dto.getMovieId()).getDuration());
        List<Show> existingShows = showRepo.findByScreenIdAndShowDate(screenId, showDate);
        for(Show show : existingShows){
            LocalTime existingStart = show.getStartTime();
            LocalTime existingEnd = show.getEndTime();

            boolean overlap =
                    showTime.isBefore(existingEnd)
                            && showEndTime.isAfter(existingStart);

            if (overlap) {
                throw new InvalidShowScheduleException(
                        "Show overlaps with an existing show on this screen"
                );
            }
        }

    }
    private void validateShowScheduleForUpdate(ShowRequestDto dto,UUID showId){
        //        1. Today's show time cannot be in past
        LocalDate showDate = dto.getShowDate();
        LocalDate today = LocalDate.now();
        LocalTime showTime = dto.getStartTime();
        LocalTime currentTime = LocalTime.now();
        if(showDate.equals(today)
                && showTime.isBefore(LocalTime.now())) {
            throw new InvalidShowScheduleException(
                    "Show time cannot be in the past"
            );
        }

//        2. No overlapping shows on same screen and date
        UUID screenId = dto.getScreenId();
        LocalTime showEndTime = dto.getStartTime().plusMinutes(movieService.getMovieById(dto.getMovieId()).getDuration());
        List<Show> existingShows = showRepo.findByScreenIdAndShowDate(screenId, showDate);
        for(Show show : existingShows){
            if(show.getId().equals(showId)) continue;
            LocalTime existingStart = show.getStartTime();
            LocalTime existingEnd = show.getEndTime();

            boolean overlap =
                    showTime.isBefore(existingEnd)
                            && showEndTime.isAfter(existingStart);

            if (overlap) {
                throw new InvalidShowScheduleException(
                        "Show overlaps with an existing show on this screen"
                );
            }
        }

    }

    public List<ShowResponseDto> getAllShows() {
        List<Show> shows = showRepo.findAll();
        List<ShowResponseDto> resp = new ArrayList<>();

        for(Show show : shows){
            ShowResponseDto dto = convertShowToResponse(show);
            resp.add(dto);
        }
        return resp;
    }

    @Cacheable(
            value = RedisCacheNames.SHOWS,
            key = "#id"
    )
    public ShowResponseDto getById(UUID id) {
        Show show = showRepo.findById(id).orElseThrow(()-> new ShowNotFoundException("show not found"));
        ShowResponseDto dto = convertShowToResponse(show);
        return dto;
    }
    public Show getShowById(UUID id) {
        Show show = showRepo.findById(id).orElseThrow(()-> new ShowNotFoundException("show not found"));
        return show;
    }

    public ShowResponseDto createShow(@Valid ShowRequestDto dto) {
        // validate show first
       validateShowScheduleForCreate(dto);
       // then save
        Show show = convertRequestToShow(dto);
        Show savedShow = showRepo.save(show);
        showSeatService.generateShowSeats(savedShow);
        ShowResponseDto resp  = convertShowToResponse(savedShow);
        return resp;
    }

    public ShowResponseDto updateShow(UUID id, @Valid ShowRequestDto dto) {

        // validate show first before update
        validateShowScheduleForUpdate(dto,id);

        Show show = showRepo.findById(id).orElseThrow(()-> new ShowNotFoundException("show not found"));

        show.setShowDate(dto.getShowDate());
        show.setMovie(movieService.getMovieById(dto.getMovieId()));
        show.setScreen(screenService.getScreenById(dto.getScreenId()));
        show.setStartTime(dto.getStartTime());
        show.setEndTime(dto.getStartTime().plusMinutes(movieService.getMovieById(dto.getMovieId()).getDuration()));
        Show updatedShow = showRepo.save(show);
        ShowResponseDto resp = convertShowToResponse(updatedShow);
        return resp;
    }

    public void deleteShow(UUID id) {
        Show show = showRepo.findById(id).orElseThrow(()-> new ShowNotFoundException("show not found"));
        showRepo.delete(show);
    }

    @Cacheable(
            value = RedisCacheNames.MOVIE_SHOWS,
            key = "#movieId"
    )
    public List<MovieShowResponseDto> getUpcomingShowsByMovie(UUID movieId){

        List<Show> shows =
                showRepo.findUpcomingShowsByMovie(
                        movieId,
                        LocalDate.now(),
                        LocalTime.now()
                );

        List<MovieShowResponseDto> response =
                new ArrayList<>();

        for(Show show : shows){

            MovieShowResponseDto dto =
                    new MovieShowResponseDto();

            dto.setShowId(show.getId());

            dto.setTheatreId(
                    show.getScreen()
                            .getTheatre()
                            .getId()
            );

            dto.setTheatreName(
                    show.getScreen()
                            .getTheatre()
                            .getName()
            );

            dto.setScreenId(
                    show.getScreen()
                            .getId()
            );

            dto.setScreenName(
                    show.getScreen()
                            .getName()
            );

            dto.setShowDate(
                    show.getShowDate()
            );

            dto.setStartTime(
                    show.getStartTime()
            );

            response.add(dto);
        }

        return response;
    }

    @Cacheable(
            value = RedisCacheNames.THEATRE_SHOWS,
            key = "#theatreId"
    )
    public List<TheatreShowResponseDto> getUpcomingShowsByTheatre(
            UUID theatreId){

        List<Show> shows =
                showRepo.findUpcomingShowsByTheatre(
                        theatreId,
                        LocalDate.now(),
                        LocalTime.now()
                );

        List<TheatreShowResponseDto> response =
                new ArrayList<>();

        for(Show show : shows){

            response.add(
                    convertToTheatreShowResponse(show)
            );

        }

        return response;
    }


}
