package com.revticket.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancellationNotificationRequest {
    private String bookingId;
    private String customerName;
    private String customerEmail;
    private String ticketNumber;
    private String movieTitle;
    private String reason;
}
