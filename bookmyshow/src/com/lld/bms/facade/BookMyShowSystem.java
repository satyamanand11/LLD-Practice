package com.lld.bms.facade;

import com.lld.bms.domain.Booking;
import com.lld.bms.domain.City;
import com.lld.bms.domain.Movie;
import com.lld.bms.domain.Show;
import com.lld.bms.domain.ShowSeat;
import com.lld.bms.domain.User;
import com.lld.bms.domain.Screen;
import com.lld.bms.domain.ScreenType;
import com.lld.bms.domain.Seat;
import com.lld.bms.domain.Venue;
import com.lld.bms.service.selection.SeatSelection;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Facade for the BookMyShow subsystem.
 * Provides a small, unified surface over user, catalog, show and booking services.
 */
public interface BookMyShowSystem {

    // ---------- User ----------
    User registerUser(String name, String email);
    User getUser(String userId);

    // ---------- Catalog: city / venue / screen ----------
    City addCity(String name, String state);
    List<City> listCities();

    Venue addVenue(String cityId, String name, String address);
    List<Venue> listVenuesInCity(String cityId);

    Screen addScreen(String venueId, String name, ScreenType type, List<Seat> seats);
    List<Screen> listScreens(String venueId);

    // ---------- Catalog: movie ----------
    Movie addMovie(String title, int durationMinutes, String genre, String language);
    List<Movie> searchMovies(String titleFragment);
    List<Movie> listMovies();

    // ---------- Show ----------
    Show createShow(String movieId, String screenId, LocalDateTime startTime, LocalDateTime endTime);
    Show getShow(String showId);
    List<Show> listShowsForMovie(String movieId);
    List<Show> listShowsAtVenue(String venueId);
    List<ShowSeat> listAvailableSeats(String showId);

    // ---------- Booking ----------
    Booking bookSeats(String userId, String showId, List<String> showSeatIds);
    Booking bookSelections(String userId, String showId, List<SeatSelection> selections);
    Booking cancelBooking(String confirmationId);
    Booking getBooking(String confirmationId);
    List<Booking> listBookingsForUser(String userId);
}
