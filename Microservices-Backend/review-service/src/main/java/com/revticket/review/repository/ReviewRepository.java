package com.revticket.review.repository;

import com.revticket.review.entity.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByMovieIdAndApprovedTrueOrderByCreatedAtDesc(String movieId);
    List<Review> findByMovieIdOrderByCreatedAtDesc(String movieId);
    Optional<Review> findByUserIdAndMovieId(String userId, String movieId);
    List<Review> findByApprovedFalseOrderByCreatedAtDesc();
    
    @Query(value = "{ 'movieId': ?0, 'approved': true }", count = true)
    long countApprovedByMovieId(String movieId);
    
    long countByApproved(boolean approved);
    
    long countByApprovedTrue();
    
    long countByApprovedFalse();
}
