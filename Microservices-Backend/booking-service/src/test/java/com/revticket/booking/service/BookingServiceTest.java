package com.revticket.booking.service;

import com.revticket.booking.client.MovieServiceClient;
import com.revticket.booking.client.NotificationServiceClient;
import com.revticket.booking.client.ShowtimeServiceClient;
import com.revticket.booking.client.TheaterServiceClient;
import com.revticket.booking.dto.BookingRequest;
import com.revticket.booking.dto.BookingResponse;
import com.revticket.booking.entity.Booking;
import com.revticket.booking.entity.Seat;
import com.revticket.booking.repository.BookingRepository;
import com.revticket.booking.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ShowtimeServiceClient showtimeServiceClient;

    @Mock
    private MovieServiceClient movieServiceClient;

    @Mock
    private TheaterServiceClient theaterServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private BookingService bookingService;

    private io.jsonwebtoken.security.Keys keys; // not used directly but good to have context if needed

    @BeforeEach
    void setUp() {
        // No specific setup needed for basic tests
    }

    @Test
    void testCreateBooking_Success() {
        String userId = "user123";
        BookingRequest request = new BookingRequest();
        request.setShowtimeId("show1");
        request.setSeats(Arrays.asList("A1"));
        request.setSeatLabels(Arrays.asList("A1"));
        request.setTotalAmount(100.0);
        request.setCustomerEmail("test@example.com");

        Seat seat = new Seat();
        seat.setId("A1");
        seat.setRow("A");
        seat.setNumber(1);
        seat.setIsBooked(false);
        seat.setIsHeld(false);

        when(seatRepository.findByShowtimeId("show1")).thenReturn(Collections.singletonList(seat));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId("booking1");
            return b;
        });

        // Mock notification client to avoid NPE if called
        doNothing().when(notificationServiceClient).sendBookingConfirmation(any());
        doNothing().when(notificationServiceClient).sendAdminNewBooking(any());

        // Lenient stubs for external services used in mapToResponse
        lenient().when(showtimeServiceClient.getShowtimeById(anyString())).thenReturn(null);

        BookingResponse response = bookingService.createBooking(userId, request);

        assertNotNull(response);
        assertEquals("booking1", response.getId());
        assertEquals("CONFIRMED", response.getStatus().name());
        verify(seatRepository, atLeastOnce()).save(any(Seat.class)); // Verifying seat status update
    }

    @Test
    void testCreateBooking_SeatAlreadyBooked() {
        String userId = "user123";
        BookingRequest request = new BookingRequest();
        request.setShowtimeId("show1");
        request.setSeats(Arrays.asList("A1"));

        Seat seat = new Seat();
        seat.setId("A1");
        seat.setRow("A");
        seat.setNumber(1);
        seat.setIsBooked(true); // Already booked

        when(seatRepository.findByShowtimeId("show1")).thenReturn(Collections.singletonList(seat));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(userId, request);
        });

        assertTrue(exception.getMessage().contains("is already booked"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
