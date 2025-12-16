package com.revticket.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private SystemOverviewDTO overview;
    private RevenueStatsDTO revenue;
    private BookingStatsDTO bookings;
    private TheaterStatsDTO theaters;
    private ShowtimeStatsDTO showtimes;
}
