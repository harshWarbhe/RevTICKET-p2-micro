package com.revticket.booking.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingRequest {
    private String movieId;
    private String theaterId;
    private String showtimeId;
    private String userId;
    private List<String> seats;
    private List<String> seatLabels;
    private Double totalAmount;
    private LocalDateTime showtime;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
}
