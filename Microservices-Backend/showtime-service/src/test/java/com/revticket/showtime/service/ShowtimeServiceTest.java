package com.revticket.showtime.service;

import com.revticket.showtime.client.BookingServiceClient;
import com.revticket.showtime.client.MovieServiceClient;
import com.revticket.showtime.client.TheaterServiceClient;
import com.revticket.showtime.dto.ShowtimeRequest;
import com.revticket.showtime.dto.ShowtimeResponse;
import com.revticket.showtime.entity.Showtime;
import com.revticket.showtime.repository.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private MovieServiceClient movieServiceClient;

    @Mock
    private TheaterServiceClient theaterServiceClient;

    @Mock
    private BookingServiceClient bookingServiceClient;

    @InjectMocks
    private ShowtimeService showtimeService;

    private Showtime showtime;

    @BeforeEach
    void setUp() {
        showtime = new Showtime();
        showtime.setId("1");
        showtime.setMovieId("movie1");
        showtime.setTheaterId("theater1");
        showtime.setScreen("screen1");
        showtime.setShowDateTime(LocalDateTime.now().plusHours(2));
        showtime.setTotalSeats(100);
        showtime.setAvailableSeats(100);
        showtime.setStatus(Showtime.ShowStatus.ACTIVE);
    }

    @Test
    void testGetShowtimesByMovie_Success() {
        when(showtimeRepository.findByMovieId("movie1")).thenReturn(Collections.singletonList(showtime));

        // Mock client responses to avoid NPE in mapping
        when(movieServiceClient.getMovieById("movie1")).thenReturn(Map.of("id", "movie1", "title", "Inception"));
        when(theaterServiceClient.getTheaterById("theater1"))
                .thenReturn(Map.of("id", "theater1", "name", "Grand Cinema"));
        when(theaterServiceClient.getScreenById("screen1"))
                .thenReturn(Map.of("id", "screen1", "name", "Screen 1", "totalSeats", 100));

        List<ShowtimeResponse> result = showtimeService.getShowtimesByMovie("movie1");

        assertFalse(result.isEmpty());
        assertEquals("movie1", result.get(0).getMovieId());
        assertEquals("Inception", result.get(0).getMovie().getTitle());
    }

    @Test
    void testCreateShowtime_Success() {
        ShowtimeRequest request = new ShowtimeRequest();
        request.setMovieId("movie1");
        request.setTheaterId("theater1");
        request.setScreen("screen1");
        request.setShowDateTime(LocalDateTime.now().plusHours(5));
        request.setTotalSeats(100);

        when(showtimeRepository.save(any(Showtime.class))).thenReturn(showtime);
        lenient().when(bookingServiceClient.initializeSeats(any())).thenReturn(Collections.emptyMap());

        // Mock client responses for mapping
        lenient().when(movieServiceClient.getMovieById("movie1"))
                .thenReturn(Map.of("id", "movie1", "title", "Inception"));
        lenient().when(theaterServiceClient.getTheaterById("theater1"))
                .thenReturn(Map.of("id", "theater1", "name", "Grand Cinema"));
        lenient().when(theaterServiceClient.getScreenById("screen1"))
                .thenReturn(Map.of("id", "screen1", "name", "Screen 1", "totalSeats", 100));

        ShowtimeResponse result = showtimeService.createShowtime(request);

        assertNotNull(result);
        assertEquals("movie1", result.getMovieId());
        verify(showtimeRepository).save(any(Showtime.class));
        verify(bookingServiceClient).initializeSeats(any());
    }

    @Test
    void testCheckShowtimeConflict_NoConflict() {
        when(showtimeRepository.findByScreenAndShowDateTimeBetween(anyString(), any(), any()))
                .thenReturn(Collections.emptyList());

        boolean conflict = showtimeService.checkShowtimeConflict("screen1", LocalDateTime.now(), null);

        assertFalse(conflict);
    }

    @Test
    void testCheckShowtimeConflict_WithConflict() {
        when(showtimeRepository.findByScreenAndShowDateTimeBetween(anyString(), any(), any()))
                .thenReturn(Collections.singletonList(showtime));

        boolean conflict = showtimeService.checkShowtimeConflict("screen1", LocalDateTime.now(), null);

        assertTrue(conflict);
    }
}
