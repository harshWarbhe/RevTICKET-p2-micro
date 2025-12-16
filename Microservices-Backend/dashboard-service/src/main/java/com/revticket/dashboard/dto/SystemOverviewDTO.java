package com.revticket.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemOverviewDTO {
    private Long totalUsers;
    private Long totalBookings;
    private Long totalMovies;
    private Long totalTheaters;
    private Long totalShowtimes;
    private Long totalReviews;
    private Double totalRevenue;
    private Long activeBookings;
    private Long cancelledBookings;
}
