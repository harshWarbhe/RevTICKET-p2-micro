package com.revticket.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingNotificationRequest {
    private String bookingId;
    private String customerName;
    private String customerEmail;
    private String ticketNumber;
    private String movieTitle;
    private String theaterName;
    private String screenName;
    private String showDateTime;
    private List<String> seats;
    private Double totalAmount;
    private Double refundAmount;
}
