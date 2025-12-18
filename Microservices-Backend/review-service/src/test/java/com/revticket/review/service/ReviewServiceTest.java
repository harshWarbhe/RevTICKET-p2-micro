package com.revticket.review.service;

import com.revticket.review.client.BookingServiceClient;
import com.revticket.review.client.MovieServiceClient;
import com.revticket.review.client.UserServiceClient;
import com.revticket.review.dto.ReviewRequest;
import com.revticket.review.dto.ReviewResponse;
import com.revticket.review.entity.Review;
import com.revticket.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private MovieServiceClient movieServiceClient;

    @Mock
    private BookingServiceClient bookingServiceClient;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;

    @BeforeEach
    void setUp() {
        review = new Review();
        review.setId("1");
        review.setUserId("user1");
        review.setUserName("Test User");
        review.setMovieId("movie1");
        review.setMovieTitle("Inception");
        review.setRating(5);
        review.setComment("Great movie!");
        review.setApproved(true);
        review.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetMovieReviews_Success() {
        when(reviewRepository.findByMovieIdAndApprovedTrueOrderByCreatedAtDesc("movie1"))
                .thenReturn(Collections.singletonList(review));

        List<ReviewResponse> result = reviewService.getMovieReviews("movie1");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Great movie!", result.get(0).getComment());
    }

    @Test
    void testAddReview_Success() {
        ReviewRequest request = new ReviewRequest();
        request.setMovieId("movie1");
        request.setRating(5);
        request.setComment("Loved it");

        String userId = "user1";
        String token = "jwt-token";

        when(userServiceClient.getUserProfile(token)).thenReturn(Map.of("name", "Test User"));
        when(movieServiceClient.getMovieById("movie1")).thenReturn(Map.of("title", "Inception"));
        when(reviewRepository.findByUserIdAndMovieId(userId, "movie1")).thenReturn(Optional.empty());

        Map<String, Object> showtime = new HashMap<>();
        showtime.put("showDateTime", LocalDateTime.now().minusDays(1).toString());
        showtime.put("movie", Map.of("id", "movie1"));

        Map<String, Object> booking = new HashMap<>();
        booking.put("status", "CONFIRMED");
        booking.put("showtime", showtime);

        when(bookingServiceClient.getUserBookings(userId, token)).thenReturn(Collections.singletonList(booking));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.addReview(userId, request, token);

        assertNotNull(response);
        assertEquals("Test User", response.getUserName());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void testAddReview_NotWatched() {
        ReviewRequest request = new ReviewRequest();
        request.setMovieId("movie1");

        String userId = "user1";
        String token = "jwt-token";

        when(userServiceClient.getUserProfile(token)).thenReturn(Map.of("name", "Test User"));
        when(movieServiceClient.getMovieById("movie1")).thenReturn(Map.of("title", "Inception"));
        when(reviewRepository.findByUserIdAndMovieId(userId, "movie1")).thenReturn(Optional.empty());
        when(bookingServiceClient.getUserBookings(userId, token)).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () -> reviewService.addReview(userId, request, token));
    }
}
