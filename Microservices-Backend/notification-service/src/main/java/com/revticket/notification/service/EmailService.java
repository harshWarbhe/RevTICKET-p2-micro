package com.revticket.notification.service;

import com.revticket.notification.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.admin.email}")
    private String adminEmail;

    public void sendEmail(EmailRequest request) {
        int maxRetries = 1;
        int retryDelay = 5000; // 5 seconds

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(request.getTo());
                message.setSubject(request.getSubject());
                message.setText(request.getBody());
                mailSender.send(message);
                System.out.println("Email sent successfully to: " + request.getTo());
                return; // Success, exit retry loop
            } catch (Exception e) {
                System.err.println(
                        "Attempt " + attempt + " failed to send email to " + request.getTo() + ": " + e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    System.err.println("All " + maxRetries + " attempts failed. Email details - To: " + request.getTo()
                            + ", Subject: " + request.getSubject());
                }
            }
        }
    }

    public void sendPasswordResetEmail(PasswordResetRequest request) {
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + request.getResetToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(request.getEmail());
        message.setSubject("RevTicket - Password Reset Request");
        message.setText(buildResetEmailBody(resetUrl));

        mailSender.send(message);
    }

    private String buildResetEmailBody(String resetUrl) {
        return "Hello,\n\n" +
                "You have requested to reset your password for your RevTicket account.\n\n" +
                "Please click the link below to reset your password:\n" +
                resetUrl + "\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "RevTicket Team";
    }

    public void sendBookingConfirmation(BookingNotificationRequest request) {
        System.out.println("=== BOOKING CONFIRMATION EMAIL DEBUG ===");
        System.out.println("Customer: " + request.getCustomerName());
        System.out.println("Email: " + request.getCustomerEmail());
        System.out.println("Movie: " + request.getMovieTitle());
        System.out.println("Theater: " + request.getTheaterName());
        System.out.println("Screen: " + request.getScreenName());
        System.out.println("Showtime: " + request.getShowDateTime());
        System.out.println("Seats: " + request.getSeats());
        System.out.println("Amount: " + request.getTotalAmount());
        
        int maxRetries = 3;
        int retryDelay = 5000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(request.getCustomerEmail());
                message.setSubject("Booking Confirmed - " + (request.getMovieTitle() != null ? request.getMovieTitle() : "RevTicket"));
                message.setText(buildBookingConfirmationBody(request));
                mailSender.send(message);
                System.out.println("Booking confirmation email sent to: " + request.getCustomerEmail());
                return;
            } catch (Exception e) {
                System.err.println("Attempt " + attempt + " failed to send booking confirmation: " + e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    System.err.println("All attempts failed. Booking details - Customer: " + request.getCustomerEmail()
                            + ", Ticket: " + request.getTicketNumber());
                }
            }
        }
    }

    public void sendCancellationConfirmation(BookingNotificationRequest request) {
        System.out.println("=== CANCELLATION EMAIL DEBUG ===");
        System.out.println("Movie Title: " + request.getMovieTitle());
        System.out.println("Refund Amount: " + request.getRefundAmount());
        System.out.println("Customer: " + request.getCustomerName());
        System.out.println("Ticket: " + request.getTicketNumber());
        
        String movieTitle = request.getMovieTitle() != null ? request.getMovieTitle() : "RevTicket";
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(request.getCustomerEmail());
        message.setSubject("Booking Cancelled - " + movieTitle);
        message.setText(buildCancellationBody(request));
        mailSender.send(message);
    }

    private String buildBookingConfirmationBody(BookingNotificationRequest request) {
        String seats = "Not specified";
        if (request.getSeats() != null && !request.getSeats().isEmpty()) {
            // Convert UUID seat IDs to readable format if needed
            List<String> readableSeats = new ArrayList<>();
            for (String seat : request.getSeats()) {
                if (seat.length() > 10 && seat.contains("-")) {
                    // This looks like a UUID, convert to readable format
                    readableSeats.add("Seat-" + seat.substring(0, 4));
                } else {
                    // This is already a readable seat label
                    readableSeats.add(seat);
                }
            }
            seats = String.join(", ", readableSeats);
        }
        String movieTitle = request.getMovieTitle() != null ? request.getMovieTitle() : "Movie details will be updated";
        String theaterName = request.getTheaterName() != null ? request.getTheaterName() : "Theater details will be updated";
        String screenName = request.getScreenName() != null ? request.getScreenName() : "Screen details will be updated";
        String showDateTime = request.getShowDateTime() != null ? request.getShowDateTime() : "Showtime will be updated";
        String totalAmount = request.getTotalAmount() != null ? String.format("%.2f", request.getTotalAmount()) : "0.00";

        return "Dear " + (request.getCustomerName() != null ? request.getCustomerName() : "Customer") + ",\n\n" +
                "Your booking has been confirmed!\n\n" +
                "Booking Details:\n" +
                "Ticket Number: " + request.getTicketNumber() + "\n" +
                "Movie: " + movieTitle + "\n" +
                "Theater: " + theaterName + "\n" +
                "Screen: " + screenName + "\n" +
                "Showtime: " + showDateTime + "\n" +
                "Seats: " + seats + "\n" +
                "Total Amount: ₹" + totalAmount + "\n\n" +
                "Please arrive 30 minutes before showtime.\n\n" +
                "View your ticket: " + frontendUrl + "/user/my-bookings\n\n" +
                "Enjoy your movie!\n\n" +
                "Best regards,\n" +
                "RevTicket Team";
    }

    private String buildCancellationBody(BookingNotificationRequest request) {
        String movieTitle = request.getMovieTitle() != null ? request.getMovieTitle() : "Movie Info Unavailable";
        Double refundAmount = request.getRefundAmount() != null ? request.getRefundAmount() : 0.0;
        
        return "Dear " + request.getCustomerName() + ",\n\n" +
                "Your booking has been cancelled.\n\n" +
                "Booking Details:\n" +
                "Ticket Number: " + request.getTicketNumber() + "\n" +
                "Movie: " + movieTitle + "\n" +
                "Refund Amount: ₹" + String.format("%.1f", refundAmount) + "\n\n" +
                "The refund will be processed within 5-7 business days.\n\n" +
                "Best regards,\n" +
                "RevTicket Team";
    }

    public void sendAdminNewUserNotification(String userName, String userEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(adminEmail);
        message.setSubject("New User Registration - RevTicket");
        message.setText("New user registered:\n\n" +
                "Name: " + userName + "\n" +
                "Email: " + userEmail + "\n\n" +
                "Login to admin panel to view details.");
        mailSender.send(message);
    }

    public void sendAdminNewBookingNotification(BookingNotificationRequest request) {
        String seats = request.getSeats() != null ? String.join(", ", request.getSeats()) : "";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(adminEmail);
        message.setSubject("New Booking - " + request.getMovieTitle());
        message.setText("New booking received:\n\n" +
                "Ticket Number: " + request.getTicketNumber() + "\n" +
                "Customer: " + request.getCustomerName() + " (" + request.getCustomerEmail() + ")\n" +
                "Movie: " + request.getMovieTitle() + "\n" +
                "Theater: " + request.getTheaterName() + "\n" +
                "Screen: " + request.getScreenName() + "\n" +
                "Showtime: " + request.getShowDateTime() + "\n" +
                "Seats: " + seats + "\n" +
                "Amount: ₹" + request.getTotalAmount() + "\n\n" +
                "Login to admin panel to view details.");
        mailSender.send(message);
    }

    public void sendAdminCancellationRequestNotification(CancellationNotificationRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(adminEmail);
        message.setSubject("Cancellation Request - " + request.getTicketNumber());
        message.setText("Cancellation request received:\n\n" +
                "Ticket Number: " + request.getTicketNumber() + "\n" +
                "Customer: " + request.getCustomerName() + " (" + request.getCustomerEmail() + ")\n" +
                "Movie: " + request.getMovieTitle() + "\n" +
                "Reason: " + request.getReason() + "\n\n" +
                "Login to admin panel to approve/reject.");
        mailSender.send(message);
    }

    public void sendReviewApprovedNotification(String movieTitle) {
        // This could be sent to users who submitted reviews
        // For now, just log it
        System.out.println("Review approved for movie: " + movieTitle);
    }
}
