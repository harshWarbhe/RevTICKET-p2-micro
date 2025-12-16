package com.revticket.dashboard.controller;

import com.revticket.dashboard.dto.*;
import com.revticket.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<SystemOverviewDTO> getOverview() {
        return ResponseEntity.ok(dashboardService.getSystemOverview());
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueStatsDTO> getRevenue() {
        return ResponseEntity.ok(dashboardService.getRevenueStats());
    }

    @GetMapping("/bookings")
    public ResponseEntity<BookingStatsDTO> getBookings() {
        return ResponseEntity.ok(dashboardService.getBookingStats());
    }

    @GetMapping("/movies")
    public ResponseEntity<Map<String, Object>> getMovies() {
        return ResponseEntity.ok(dashboardService.getMovieStats());
    }

    @GetMapping("/theaters")
    public ResponseEntity<TheaterStatsDTO> getTheaters() {
        return ResponseEntity.ok(dashboardService.getTheaterStats());
    }

    @GetMapping("/showtimes")
    public ResponseEntity<ShowtimeStatsDTO> getShowtimes() {
        return ResponseEntity.ok(dashboardService.getShowtimeStats());
    }

    @GetMapping("/reviews")
    public ResponseEntity<Map<String, Object>> getReviews() {
        return ResponseEntity.ok(dashboardService.getReviewStats());
    }

    @GetMapping("/full-report")
    public ResponseEntity<AnalyticsResponse> getFullReport() {
        return ResponseEntity.ok(dashboardService.getFullReport());
    }
}
