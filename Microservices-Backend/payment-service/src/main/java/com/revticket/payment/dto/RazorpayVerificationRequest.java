package com.revticket.payment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RazorpayVerificationRequest {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String movieId;
    private String theaterId;
    private String showtimeId;
    private LocalDateTime showtime;
    private List<String> seats;
    private List<String> seatLabels;
    private Double totalAmount;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
}
