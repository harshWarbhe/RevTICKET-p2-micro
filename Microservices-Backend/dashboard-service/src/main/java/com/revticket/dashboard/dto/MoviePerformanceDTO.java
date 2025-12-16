package com.revticket.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoviePerformanceDTO {
    private String movieId;
    private String movieTitle;
    private Long totalBookings;
    private Double totalRevenue;
    private Double averageRating;
    private Long totalReviews;
    private Double occupancyRate;
}
