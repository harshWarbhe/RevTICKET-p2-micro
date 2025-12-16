package com.revticket.review.service;

import com.revticket.review.client.BookingServiceClient;
import com.revticket.review.client.MovieServiceClient;
import com.revticket.review.client.UserServiceClient;
import com.revticket.review.dto.ReviewRequest;
import com.revticket.review.dto.ReviewResponse;
import com.revticket.review.entity.Review;
import com.revticket.review.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private MovieServiceClient movieServiceClient;

    @Autowired
    private BookingServiceClient bookingServiceClient;

    public ReviewResponse addReview(String userId, ReviewRequest request, String token) {
        Map<String, Object> user = userServiceClient.getUserProfile(token);
        Map<String, Object> movie = movieServiceClient.getMovieById(request.getMovieId());

        if (reviewRepository.findByUserIdAndMovieId(userId, request.getMovieId()).isPresent()) {
            throw new RuntimeException("You have already reviewed this movie");
        }

        List<Map<String, Object>> bookings = bookingServiceClient.getUserBookings(userId, token);
        boolean hasWatchedMovie = bookings.stream()
                .anyMatch(booking -> {
                    String status = (String) booking.get("status");
                    Map<String, Object> showtime = (Map<String, Object>) booking.get("showtime");
                    if (showtime != null) {
                        Map<String, Object> showtimeMovie = (Map<String, Object>) showtime.get("movie");
                        String showtimeDateTime = (String) showtime.get("showDateTime");
                        return "CONFIRMED".equals(status) &&
                                showtimeMovie != null &&
                                request.getMovieId().equals(showtimeMovie.get("id")) &&
                                LocalDateTime.parse(showtimeDateTime).isBefore(LocalDateTime.now());
                    }
                    return false;
                });

        if (!hasWatchedMovie) {
            throw new RuntimeException("You can only review movies you have watched");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setUserName((String) user.get("name"));
        review.setMovieId(request.getMovieId());
        review.setMovieTitle((String) movie.get("title"));
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());
        review.setApproved(false);

        review = reviewRepository.save(review);

        return new ReviewResponse(
                review.getId(),
                review.getUserName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.isApproved());
    }

    public List<ReviewResponse> getMovieReviews(String movieId) {
        return reviewRepository.findByMovieIdAndApprovedTrueOrderByCreatedAtDesc(movieId)
                .stream()
                .map(review -> new ReviewResponse(
                        review.getId(),
                        review.getUserName(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt(),
                        review.isApproved()))
                .collect(Collectors.toList());
    }

    public Double getAverageRating(String movieId) {
        List<Review> reviews = reviewRepository.findByMovieIdAndApprovedTrueOrderByCreatedAtDesc(movieId);
        return reviews.isEmpty() ? 0.0
                : reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);
    }

    public List<ReviewResponse> getAllPendingReviews() {
        return reviewRepository.findByApprovedFalseOrderByCreatedAtDesc()
                .stream()
                .map(review -> new ReviewResponse(
                        review.getId(),
                        review.getUserName(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt(),
                        review.isApproved(),
                        review.getMovieId(),
                        review.getMovieTitle()))
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getAllReviewsForMovie(String movieId) {
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId)
                .stream()
                .map(review -> new ReviewResponse(
                        review.getId(),
                        review.getUserName(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt(),
                        review.isApproved()))
                .collect(Collectors.toList());
    }

    public ReviewResponse approveReview(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setApproved(true);
        review = reviewRepository.save(review);
        return new ReviewResponse(
                review.getId(),
                review.getUserName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.isApproved(),
                review.getMovieId(),
                review.getMovieTitle());
    }

    public void deleteReview(String reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(review -> new ReviewResponse(
                        review.getId(),
                        review.getUserName(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt(),
                        review.isApproved(),
                        review.getMovieId(),
                        review.getMovieTitle()))
                .collect(Collectors.toList());
    }

    public boolean canUserReviewMovie(String userId, String movieId, String token) {
        System.out.println("Checking if user can review movie - userId: " + userId + ", movieId: " + movieId);
        
        if (reviewRepository.findByUserIdAndMovieId(userId, movieId).isPresent()) {
            System.out.println("User has already reviewed this movie");
            return false;
        }

        try {
            List<Map<String, Object>> bookings = bookingServiceClient.getUserBookings(userId, token);
            System.out.println("Found " + bookings.size() + " bookings for user");
            
            return bookings.stream()
                    .anyMatch(booking -> {
                        String status = (String) booking.get("status");
                        Map<String, Object> showtime = (Map<String, Object>) booking.get("showtime");
                        if (showtime != null) {
                            Map<String, Object> movie = (Map<String, Object>) showtime.get("movie");
                            String showtimeDateTime = (String) showtime.get("showDateTime");
                            return "CONFIRMED".equals(status) &&
                                    movie != null &&
                                    movieId.equals(movie.get("id")) &&
                                    LocalDateTime.parse(showtimeDateTime).isBefore(LocalDateTime.now());
                        }
                        return false;
                    });
        } catch (Exception e) {
            System.out.println("Error fetching user bookings: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getReviewStats() {
        Long totalReviews = reviewRepository.count();
        Long approvedReviews = reviewRepository.countByApprovedTrue();
        Long pendingReviews = reviewRepository.countByApprovedFalse();
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalReviews", totalReviews);
        stats.put("approvedReviews", approvedReviews);
        stats.put("pendingReviews", pendingReviews);
        return stats;
    }
}
