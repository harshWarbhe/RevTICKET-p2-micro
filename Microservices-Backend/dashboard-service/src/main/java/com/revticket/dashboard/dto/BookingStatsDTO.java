package com.revticket.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatsDTO {
    private Long totalBookings;
    private Long confirmedBookings;
    private Long cancelledBookings;
    private Long pendingBookings;
    private Double averageBookingValue;
    private Long totalSeatsBooked;
}
