package com.revticket.booking.controller;

import com.revticket.booking.dto.BookingRequest;
import com.revticket.booking.dto.BookingResponse;
import com.revticket.booking.dto.BookingStatsResponse;
import com.revticket.booking.dto.CancellationRequest;
import com.revticket.booking.service.BookingService;
import com.revticket.booking.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Booking Controller - Matches Monolithic Implementation
 * Uses SecurityUtil to extract userId from JWT (no SecurityConfig headers
 * needed)
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        // Try to get userId from JWT token (for API Gateway calls)
        String userId = securityUtil.getCurrentUserId();

        // Fallback to userId in request body (for internal payment service calls)
        if (userId == null || userId.isEmpty()) {
            userId = request.getUserId();
        }

        // Final fallback
        if (userId == null || userId.isEmpty()) {
            userId = "default-user";
        }

        return ResponseEntity.ok(bookingService.createBooking(userId, request));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        String userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable String userId) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable("id") String id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/request-cancellation")
    public ResponseEntity<BookingResponse> requestCancellation(
            @PathVariable("id") String id,
            @RequestBody(required = false) CancellationRequest request) {
        String reason = request != null && request.getReason() != null ? request.getReason() : "";
        return ResponseEntity.ok(bookingService.requestCancellation(id, reason));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable("id") String id,
            @RequestBody(required = false) String reason) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, reason));
    }

    @GetMapping("/cancellation-requests")
    public ResponseEntity<List<BookingResponse>> getCancellationRequests() {
        return ResponseEntity.ok(bookingService.getCancellationRequests());
    }

    @GetMapping("/all")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable("id") String id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/scan")
    public ResponseEntity<BookingResponse> scanBooking(@PathVariable("id") String id) {
        return ResponseEntity.ok(bookingService.scanBooking(id));
    }

    @PostMapping("/{id}/resign")
    public ResponseEntity<BookingResponse> resignBooking(
            @PathVariable("id") String id,
            @RequestBody List<String> newSeats) {
        return ResponseEntity.ok(bookingService.resignBooking(id, newSeats));
    }

    @GetMapping("/verify/{id}")
    public ResponseEntity<?> verifyTicket(@PathVariable("id") String id) {
        return bookingService.getBookingById(id)
                .map(booking -> {
                    if (booking.getStatus().toString().equals("CANCELLED")) {
                        return ResponseEntity.badRequest().body("Ticket has been cancelled");
                    }
                    return ResponseEntity.ok(booking);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/confirm-payment")
    public ResponseEntity<BookingResponse> confirmPayment(
            @PathVariable("id") String id,
            @RequestParam String transactionId) {
        return ResponseEntity.ok(bookingService.confirmPayment(id, transactionId));
    }

    @GetMapping("/admin/bookings/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBookingStats() {
        BookingStatsResponse stats = bookingService.getBookingStats();
        Map<String, Object> response = new HashMap<>();
        response.put("totalBookings", stats.getTotalBookings());
        response.put("confirmedBookings", stats.getTotalBookings() - stats.getCancelledBookings());
        response.put("cancelledBookings", stats.getCancelledBookings());
        response.put("pendingBookings", 0L);
        response.put("averageBookingValue", 0.0);
        response.put("totalSeatsBooked", stats.getTotalSeatsBooked());
        return ResponseEntity.ok(response);
    }
}
