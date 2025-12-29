package com.revticket.notification.controller;

import com.revticket.notification.dto.*;
import com.revticket.notification.entity.Notification;
import com.revticket.notification.repository.NotificationRepository;
import com.revticket.notification.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping("/email/send")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody EmailRequest request) {
        emailService.sendEmail(request);
        return ResponseEntity.ok(Map.of("message", "Email sent successfully"));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, String>> sendPasswordReset(@RequestBody PasswordResetRequest request) {
        emailService.sendPasswordResetEmail(request);
        return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
    }

    @PostMapping("/booking-confirmation")
    public ResponseEntity<Map<String, String>> sendBookingConfirmation(@RequestBody BookingNotificationRequest request) {
        // Save notification to MongoDB
        Notification notification = null;
        try {
            notification = new Notification();
            notification.setUserId(request.getBookingId());
            notification.setType("BOOKING_CONFIRMATION");
            notification.setTitle("Booking Confirmed");
            notification.setMessage("Your booking " + request.getTicketNumber() + " has been confirmed.");
            notification.setEmail(request.getCustomerEmail());
            System.out.println("Saving notification to MongoDB: " + notification.getMessage());
            notification = notificationRepository.save(notification);
            System.out.println("Notification saved with ID: " + notification.getId());
        } catch (Exception e) {
            System.err.println("Failed to save notification to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
        
        emailService.sendBookingConfirmation(request);
        
        if (notification != null && notification.getId() != null) {
            try {
                notification.setSent(true);
                notification.setSentAt(java.time.LocalDateTime.now());
                notificationRepository.save(notification);
                System.out.println("Notification marked as sent: " + notification.getId());
            } catch (Exception e) {
                System.err.println("Failed to update notification status: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(Map.of("message", "Booking confirmation sent"));
    }

    @PostMapping("/cancellation-confirmation")
    public ResponseEntity<Map<String, String>> sendCancellationConfirmation(@RequestBody BookingNotificationRequest request) {
        emailService.sendCancellationConfirmation(request);
        return ResponseEntity.ok(Map.of("message", "Cancellation confirmation sent"));
    }

    @PostMapping("/admin/new-user")
    public ResponseEntity<Map<String, String>> notifyAdminNewUser(@RequestBody Map<String, String> request) {
        // Save admin notification to MongoDB
        Notification notification = new Notification();
        notification.setUserId("admin");
        notification.setType("NEW_USER");
        notification.setTitle("New User Registration");
        notification.setMessage("New user " + request.get("userName") + " registered with email " + request.get("userEmail"));
        notification.setEmail("admin@revticket.com");
        notificationRepository.save(notification);
        
        emailService.sendAdminNewUserNotification(request.get("userName"), request.get("userEmail"));
        notification.setSent(true);
        notification.setSentAt(java.time.LocalDateTime.now());
        notificationRepository.save(notification);
        
        return ResponseEntity.ok(Map.of("message", "Admin notified"));
    }

    @PostMapping("/admin/new-booking")
    public ResponseEntity<Map<String, String>> notifyAdminNewBooking(@RequestBody Object requestBody) {
        BookingNotificationRequest request;
        if (requestBody instanceof Map) {
            Map<String, Object> requestMap = (Map<String, Object>) requestBody;
            request = new BookingNotificationRequest();
            request.setBookingId((String) requestMap.get("bookingId"));
            request.setCustomerName((String) requestMap.get("customerName"));
            request.setCustomerEmail((String) requestMap.get("customerEmail"));
            request.setTicketNumber((String) requestMap.get("ticketNumber"));
            request.setMovieTitle((String) requestMap.get("movieTitle"));
            request.setTheaterName((String) requestMap.get("theaterName"));
            request.setScreenName((String) requestMap.get("screenName"));
            request.setShowDateTime((String) requestMap.get("showDateTime"));
            if (requestMap.get("seats") instanceof java.util.List) {
                request.setSeats((java.util.List<String>) requestMap.get("seats"));
            }
            if (requestMap.get("totalAmount") != null) {
                request.setTotalAmount(((Number) requestMap.get("totalAmount")).doubleValue());
            }
        } else {
            request = (BookingNotificationRequest) requestBody;
        }
        
        // Save admin notification to MongoDB
        Notification notification = new Notification();
        notification.setUserId("admin");
        notification.setType("NEW_BOOKING");
        notification.setTitle("New Booking Alert");
        notification.setMessage("New booking " + request.getTicketNumber() + " by " + request.getCustomerName());
        notification.setEmail("admin@revticket.com");
        notificationRepository.save(notification);
        
        emailService.sendAdminNewBookingNotification(request);
        notification.setSent(true);
        notification.setSentAt(java.time.LocalDateTime.now());
        notificationRepository.save(notification);
        
        return ResponseEntity.ok(Map.of("message", "Admin notified"));
    }

    @PostMapping("/admin/cancellation-request")
    public ResponseEntity<Map<String, String>> notifyAdminCancellationRequest(@RequestBody CancellationNotificationRequest request) {
        emailService.sendAdminCancellationRequestNotification(request);
        return ResponseEntity.ok(Map.of("message", "Admin notified"));
    }
    
    @PostMapping("/cancellation-request")
    public ResponseEntity<Map<String, String>> sendCancellationRequest(@RequestBody Map<String, Object> request) {
        CancellationNotificationRequest cancellationRequest = new CancellationNotificationRequest();
        cancellationRequest.setBookingId((String) request.get("bookingId"));
        cancellationRequest.setCustomerName((String) request.get("customerName"));
        cancellationRequest.setCustomerEmail((String) request.get("customerEmail"));
        cancellationRequest.setReason((String) request.get("reason"));
        cancellationRequest.setTicketNumber((String) request.get("ticketNumber"));
        emailService.sendAdminCancellationRequestNotification(cancellationRequest);
        return ResponseEntity.ok(Map.of("message", "Cancellation request notification sent"));
    }
    
    @PostMapping("/booking-cancelled")
    public ResponseEntity<Map<String, String>> sendBookingCancelled(@RequestBody Map<String, Object> request) {
        BookingNotificationRequest bookingRequest = new BookingNotificationRequest();
        bookingRequest.setCustomerEmail((String) request.get("customerEmail"));
        bookingRequest.setCustomerName((String) request.get("customerName"));
        bookingRequest.setTicketNumber((String) request.get("ticketNumber"));
        bookingRequest.setMovieTitle((String) request.get("movieTitle"));
        if (request.get("refundAmount") != null) {
            bookingRequest.setRefundAmount(((Number) request.get("refundAmount")).doubleValue());
        }
        emailService.sendCancellationConfirmation(bookingRequest);
        return ResponseEntity.ok(Map.of("message", "Booking cancellation notification sent"));
    }

    @PostMapping("/review-approved")
    public ResponseEntity<Map<String, String>> notifyReviewApproved(@RequestBody Map<String, String> request) {
        emailService.sendReviewApprovedNotification(request.get("movieTitle"));
        return ResponseEntity.ok(Map.of("message", "Notification sent"));
    }
    
    @GetMapping("/test-mongodb")
    public ResponseEntity<Map<String, Object>> testMongoDB() {
        try {
            long count = notificationRepository.count();
            java.util.List<Notification> notifications = notificationRepository.findAll();
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "connected");
            response.put("totalNotifications", count);
            response.put("notifications", notifications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
