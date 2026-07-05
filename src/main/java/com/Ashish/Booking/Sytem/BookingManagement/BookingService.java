package com.Ashish.Booking.Sytem.BookingManagement;

import com.Ashish.Booking.Sytem.event.BookingCancelledEvent;
import com.Ashish.Booking.Sytem.event.BookingCreatedEvent;
import com.Ashish.Booking.Sytem.producer.BookingCancelledProducer;
import com.Ashish.Booking.Sytem.producer.BookingEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.Ashish.Booking.Sytem.RedisManagement.LockResult;
import com.Ashish.Booking.Sytem.RedisManagement.RedisLockService;
import com.Ashish.Booking.Sytem.BookedSeatManagement.BookedSeat;
import com.Ashish.Booking.Sytem.BookedSeatManagement.BookedSeatRepo;
import com.Ashish.Booking.Sytem.ScreenManagement.ScreenService;
import com.Ashish.Booking.Sytem.SeatManagement.Seat;
import com.Ashish.Booking.Sytem.SeatManagement.SeatService;
import com.Ashish.Booking.Sytem.ShowManagement.ShowService;
import com.Ashish.Booking.Sytem.ShowSeatManagement.ShowSeatService;
import com.Ashish.Booking.Sytem.UserManagement.User;
import com.Ashish.Booking.Sytem.UserManagement.UserService;
import com.Ashish.Booking.Sytem.exception.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class BookingService {
    private static final Logger log =
            LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private ShowService showService;

    @Autowired
    private UserService userService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private ScreenService screenService;

    @Autowired
    private BookedSeatRepo bookedSeatRepo;

    @Autowired
    private ShowSeatService showSeatService;

    @Autowired
    private RedisLockService redisLockService;

    @Autowired
    private BookingEventProducer bookingEventProducer;

    @Autowired
    private BookingCancelledProducer bookingCancelledProducer;

    private User getLoggedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        return user;
    }

    private Booking convertRequestToBooking(BookingRequestDto dto){
        Booking booking = new Booking();
        UUID id = dto.getShowId();
        booking.setUser(getLoggedUser());
        booking.setBookingTime(LocalDateTime.now());
        booking.setShow(showService.getShowById(id));
        booking.setStatus(BookingStatus.PENDING);
        return booking;
    }

    private BookingResponseDto convertBookingToResponse(Booking booking){
        BookingResponseDto bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setBookingId(booking.getId());
        bookingResponseDto.setBookingTime(booking.getBookingTime());
        bookingResponseDto.setStatus(booking.getStatus());
        bookingResponseDto.setShowDate(booking.getShow().getShowDate());
        bookingResponseDto.setStartTime(booking.getShow().getStartTime());
        bookingResponseDto.setScreenName(booking.getShow().getScreen().getName());
        bookingResponseDto.setTheatreName(booking.getShow().getScreen().getTheatre().getName());
        bookingResponseDto.setMovieTitle(booking.getShow().getMovie().getName());

        // get all the seats for the particular booking id as given
        UUID bookingId = booking.getId();

        List<BookedSeat> bookedSeats = bookedSeatRepo.findAllByBookingId(bookingId);

        List<Seat> seats = bookedSeats.stream()
                .map(BookedSeat::getSeat)
                .toList();
        // return string of seats
        List<String> stringSeats = new ArrayList<>();
        for(Seat seat : seats){
            stringSeats.add(seat.getRowName() + seat.getSeatNumber());
        }

        bookingResponseDto.setSeats(stringSeats);
        return bookingResponseDto;
    }

    // validate show
    // validate seat
    // validate seat must belong to shows screen
    // Show Must Not Have Started
    // Show Must Not Be Finished
    // Duplicate Seat IDs In Same Request


    private void validateBooking(Booking booking,BookingRequestDto dto){

       // 1) validate show
        // check show start date < booking date and show start time < booking time
        LocalDate showDate = booking.getShow().getShowDate();
        LocalTime showTime = booking.getShow().getStartTime();

        LocalDate bookingDate = booking.getBookingTime().toLocalDate();
        LocalTime bookingTime = booking.getBookingTime().toLocalTime();

        if (showDate.isBefore(bookingDate)
                || (showDate.equals(bookingDate) && showTime.isBefore(bookingTime))) {

            throw new InvalidBookingException(
                    "Cannot book a show that has already started"
            );
        }

        // 2) validate all seats bcz booking request dto might have multiple seats
        List<UUID> seatIds = dto.getSeatIds();
        for(UUID id : seatIds){
            // this will throw exception of seat now found
            Optional<Seat> seat  = seatService.getSeatById(id);
            if(seat.isEmpty()){
                // throw exception
                throw new  InvalidBookingException("this seat is not valid");
            }
        }

        // 3) validate seat must belong to shows screen
        // seatid ->  seat and seat ->  screen and screen -> screenid
        // show-> screen and screen -> screen id

        UUID showScreenId = booking.getShow().getScreen().getId();
        for(UUID id : seatIds){
            // this will throw exception of seat now found
            Optional<Seat> seat  = seatService.getSeatById(id);
            UUID seatScreenId = seat.get().getScreen().getId();
            if(!seatScreenId.equals(showScreenId)) {
                // throw related exception here
                throw new InvalidBookingException("this seat does not belong to the same screen");
            }
        }
        // checking for duplicate seats
    Set<UUID> setSeatIds = new HashSet<>();
        for(UUID id : seatIds){
            if(setSeatIds.contains(id)){
                throw new InvalidBookingException("duplicate seat not allowed");
            }

            setSeatIds.add(id);
        }
        if(booking.getStatus()==BookingStatus.CANCELLED){
            throw new InvalidBookingException(
                    "Booking already cancelled"
            );
        }

    }

    private void releaseLocks(Map<String,String> locks){
        for(Map.Entry<String, String> entry : locks.entrySet()){
            String key = entry.getKey();
            String token = entry.getValue();
            redisLockService.unlock(key,token);
        }
    }

    private Map<String,String> acquireLocks(UUID showId,List<UUID> seatIds){
        Map<String,String> mapKeyToToken = new HashMap<>();
        for(UUID id : seatIds){
            String key = "Show:" + showId.toString()+ ":Seat:" + id.toString();
            LockResult lockResult = redisLockService.lock(key);
            if(!lockResult.getAcquired()){
                // unlock all the seats here only
                releaseLocks(mapKeyToToken);
                throw new SeatLockedException("seat already locked");
            }
            mapKeyToToken.put(key,lockResult.getToken());
        }
        return mapKeyToToken;
    }

    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto dto) {
        UUID showId = dto.getShowId();
        Booking booking = convertRequestToBooking(dto);
        // validate show seat and all
        validateBooking(booking,dto);
        // get all seats
        List<UUID> seatIds = dto.getSeatIds();
        // acquire lock here
        // key = showId+seatId
        // map-> key -> token


        Map<String,String> mapKeyToToken = new HashMap<>();
        Booking savedBooking = null;

        try{
            // acquire locks
            mapKeyToToken = acquireLocks(showId, seatIds);
            // save the booking

            savedBooking = bookingRepo.save(booking);




            // save bookedseats
            for(UUID id : seatIds){
                // this will throw exception of seat now found
                showSeatService.bookSeats(showId,id);
                BookedSeat bookedSeat = new BookedSeat();
                bookedSeat.setBooking(savedBooking);
                bookedSeat.setSeat(seatService.getSeatById(id).get());
                bookedSeatRepo.save(bookedSeat);
            }
            BookingCreatedEvent event = new BookingCreatedEvent();
            event.setBookingId(savedBooking.getId());
            event.setUserId(savedBooking.getUser().getId());
            event.setShowId(savedBooking.getShow().getId());
            bookingEventProducer.publishBookingCreated(event);
        }
        finally{
            try{
                releaseLocks(mapKeyToToken);
            }
            catch(Exception e){
                // log the error
                log.error("Failed to release Redis locks", e);
            }
        }

        // return booking response
        BookingResponseDto resp = convertBookingToResponse(savedBooking);
        return resp;
    }
    @Transactional
    public void confirmBooking(UUID bookingId){

      Booking booking = bookingRepo.findById(bookingId).orElseThrow(()->new BookingNotFoundException("booking not found"));
        booking.setStatus(BookingStatus.CONFIRMED);

        bookingRepo.save(booking);
        log.info(
                "Booking {} confirmed successfully",
                bookingId
        );
    }

    public List<BookingResponseDto> getAllBookingOfOneUser() {
        List<Booking> bookings = bookingRepo.findAllByUserId(getLoggedUser().getId());
        List<BookingResponseDto> resp = new ArrayList<>();

        for(Booking booking : bookings){
            BookingResponseDto dto = convertBookingToResponse(booking);
            resp.add(dto);
        }
        return resp;
    }

    public BookingResponseDto getBookingById(UUID id) {

        Booking booking = bookingRepo.findById(id).orElseThrow(()->new BookingNotFoundException("booking not found"));
        if(!booking.getUser().getId()
                .equals(getLoggedUser().getId())){
            throw new UserNotAllowedException("Not authorised to get this");
        }
        BookingResponseDto dto = convertBookingToResponse(booking);
        return dto;
    }

    @Transactional
    public void cancelBookingById(UUID id) {
        Booking booking = bookingRepo.findById(id).orElseThrow(()->new BookingNotFoundException("booking not found"));
        if(!booking.getUser().getId()
                .equals(getLoggedUser().getId())){
            throw new UserNotAllowedException("Not authorised to delete this");
        }
        if(booking.getStatus() == BookingStatus.CANCELLED){
            throw new InvalidBookingException(
                    "Booking already cancelled"
            );
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(booking);
        // we got the booking id
        // we need showId and seatIds
        UUID showId = booking.getShow().getId();
        List<BookedSeat> bookedSeats = bookedSeatRepo.findAllByBookingId(id);
        List<UUID> seatIds = new ArrayList<>();

        for (BookedSeat bookedSeat : bookedSeats) {
            seatIds.add(bookedSeat.getSeat().getId());
        }
        for(UUID seatId : seatIds){
            showSeatService.updateBookedToAvailable(showId,seatId);
        }

        // publish cancellation to the kafka
        BookingCancelledEvent event =
                new BookingCancelledEvent(
                        booking.getId(),
                        booking.getUser().getId(),
                        booking.getShow().getId()
                );

        bookingCancelledProducer.publishBookingCancelled(event);

    }
}
