package com.revticket.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsResponse {
    private Long totalReviews;
    private Long pendingReviews;
    private Long approvedReviews;
    private Double averageRating;
}
