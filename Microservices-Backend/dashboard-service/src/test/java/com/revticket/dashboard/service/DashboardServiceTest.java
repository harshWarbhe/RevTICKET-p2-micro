package com.revticket.dashboard.service;

import com.revticket.dashboard.client.BookingServiceClient;
import com.revticket.dashboard.client.MovieServiceClient;
import com.revticket.dashboard.client.PaymentServiceClient;
import com.revticket.dashboard.client.ReviewServiceClient;
import com.revticket.dashboard.client.ShowtimeServiceClient;
import com.revticket.dashboard.client.TheaterServiceClient;
import com.revticket.dashboard.client.UserServiceClient;
import com.revticket.dashboard.dto.SystemOverviewDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private BookingServiceClient bookingServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private MovieServiceClient movieServiceClient;

    @Mock
    private TheaterServiceClient theaterServiceClient;

    @Mock
    private ShowtimeServiceClient showtimeServiceClient;

    @Mock
    private ReviewServiceClient reviewServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void testGetSystemOverview_Success() {
        when(userServiceClient.getUserStats()).thenReturn(Map.of("totalUsers", 100));
        when(bookingServiceClient.getBookingStats())
                .thenReturn(Map.of("totalBookings", 50, "confirmedBookings", 40, "cancelledBookings", 10));
        when(movieServiceClient.getMovieStats()).thenReturn(Map.of("totalMovies", 20));
        when(theaterServiceClient.getTheaterStats()).thenReturn(Map.of("totalTheaters", 5));
        when(showtimeServiceClient.getShowtimeStats()).thenReturn(Map.of("totalShowtimes", 200));
        when(reviewServiceClient.getReviewStats()).thenReturn(Map.of("totalReviews", 30));
        when(paymentServiceClient.getPaymentStats()).thenReturn(Map.of("totalRevenue", 5000.0));

        SystemOverviewDTO overview = dashboardService.getSystemOverview();

        assertNotNull(overview);
        assertEquals(100L, overview.getTotalUsers());
        assertEquals(50L, overview.getTotalBookings());
        assertEquals(20L, overview.getTotalMovies());
        assertEquals(5000.0, overview.getTotalRevenue());
    }

    @Test
    void testGetSystemOverview_PartialFailure() {
        // Simulate failure for user service, others succeed
        when(userServiceClient.getUserStats()).thenThrow(new RuntimeException("Service down"));
        when(bookingServiceClient.getBookingStats())
                .thenReturn(Map.of("totalBookings", 50, "confirmedBookings", 40, "cancelledBookings", 10));
        when(movieServiceClient.getMovieStats()).thenReturn(Map.of("totalMovies", 20));

        // Mock remaining services to avoid NPE if logic expects returns, although
        // service handles exceptions
        when(theaterServiceClient.getTheaterStats()).thenThrow(new RuntimeException("Service down"));
        when(showtimeServiceClient.getShowtimeStats()).thenThrow(new RuntimeException("Service down"));
        when(reviewServiceClient.getReviewStats()).thenThrow(new RuntimeException("Service down"));
        when(paymentServiceClient.getPaymentStats()).thenThrow(new RuntimeException("Service down"));

        SystemOverviewDTO overview = dashboardService.getSystemOverview();

        assertNotNull(overview);
        assertEquals(0L, overview.getTotalUsers()); // Should fallback to 0
        assertEquals(50L, overview.getTotalBookings());
        assertEquals(20L, overview.getTotalMovies());
    }
}
