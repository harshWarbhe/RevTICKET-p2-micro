package com.revticket.booking.service;

import com.revticket.booking.client.MovieServiceClient;
import com.revticket.booking.client.NotificationServiceClient;
import com.revticket.booking.client.ShowtimeServiceClient;
import com.revticket.booking.client.TheaterServiceClient;
import com.revticket.booking.dto.*;
import com.revticket.booking.entity.Booking;
import com.revticket.booking.entity.Seat;
import com.revticket.booking.repository.BookingRepository;
import com.revticket.booking.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowtimeServiceClient showtimeServiceClient;

    @Autowired
    private MovieServiceClient movieServiceClient;

    @Autowired
    private TheaterServiceClient theaterServiceClient;

    @Autowired
    private NotificationServiceClient notificationServiceClient;

    // private static final int MAX_SEATS_PER_BOOKING = 10;
    // private static final int CANCELLATION_WINDOW_HOURS = 2;

    @Transactional
    public BookingResponse createBooking(String userId, BookingRequest request) {
        System.out.println("=== BOOKING CREATION START ===");
        System.out.println("UserId: " + userId);
        System.out.println("ShowtimeId: " + request.getShowtimeId());
        System.out.println("Seats: " + request.getSeats());

        if (request.getSeats() == null || request.getSeats().isEmpty()) {
            throw new RuntimeException("No seats selected");
        }

        // Validate and block seats first
        List<Seat> showtimeSeats = seatRepository.findByShowtimeId(request.getShowtimeId());
        System.out.println("Found " + showtimeSeats.size() + " seats for showtime");

        if (showtimeSeats.isEmpty()) {
            throw new RuntimeException("Seats not initialized for showtime " + request.getShowtimeId()
                    + ". Please refresh and select seats again.");
        }

        for (String seatId : request.getSeats()) {
            System.out.println("Looking for seat: " + seatId);
            Seat seat = showtimeSeats.stream()
                    .filter(s -> seatId.equals(s.getId()) ||
                            seatId.equals(s.getRow() + s.getNumber()))
                    .findFirst()
                    .orElseThrow(() -> {
                        System.out.println("Available seats: "
                                + showtimeSeats.stream().map(s -> s.getId() + "(" + s.getRow() + s.getNumber() + ")")
                                        .collect(java.util.stream.Collectors.toList()));
                        return new RuntimeException("Seat not found: " + seatId);
                    });

            System.out.println("Found seat: " + seat.getId() + " (" + seat.getRow() + seat.getNumber() + ") - Booked: "
                    + seat.getIsBooked());

            if (seat.getIsBooked()) {
                throw new RuntimeException("Seat " + seat.getRow() + seat.getNumber() + " is already booked");
            }

            if (seat.getIsHeld() && seat.getHoldExpiry() != null && seat.getHoldExpiry().isAfter(LocalDateTime.now())) {
                throw new RuntimeException(
                        "Seat " + seat.getRow() + seat.getNumber() + " is currently held by another user");
            }
        }

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowtimeId(request.getShowtimeId());
        booking.setSeats(request.getSeats());
        if (request.getSeatLabels() != null && !request.getSeatLabels().isEmpty()) {
            booking.setSeatLabels(request.getSeatLabels());
        }
        booking.setTotalAmount(request.getTotalAmount());
        booking.setPaymentMethod("ONLINE");
        booking.setCustomerName(Objects.requireNonNullElse(request.getCustomerName(), ""));
        booking.setCustomerEmail(Objects.requireNonNullElse(request.getCustomerEmail(), ""));
        booking.setCustomerPhone(Objects.requireNonNullElse(request.getCustomerPhone(), ""));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setTicketNumber("TKT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setQrCode("QR_" + UUID.randomUUID().toString());

        booking = bookingRepository.save(booking);

        // Send booking confirmation notification
        try {
            sendBookingConfirmationNotification(booking);
            sendAdminNewBookingNotification(booking);
        } catch (Exception e) {
            System.out.println("Failed to send booking confirmation notification: " + e.getMessage());
        }

        // Block the seats
        for (String seatId : request.getSeats()) {
            Seat seat = showtimeSeats.stream()
                    .filter(s -> seatId.equals(s.getId()) ||
                            seatId.equals(s.getRow() + s.getNumber()))
                    .findFirst()
                    .orElse(null);
            if (seat != null) {
                seat.setIsBooked(true);
                seat.setIsHeld(false);
                seat.setHoldExpiry(null);
                seat.setSessionId(null);
                seatRepository.save(seat);
            }
        }

        return mapToResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(String userId) {
        return bookingRepository.findByUserId(Objects.requireNonNullElse(userId, ""))
                .stream()
                .sorted((b1, b2) -> b2.getBookingDate().compareTo(b1.getBookingDate()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<BookingResponse> getBookingById(String id) {
        return bookingRepository.findById(Objects.requireNonNullElse(id, ""))
                .map(this::mapToResponse);
    }

    @Transactional
    public BookingResponse requestCancellation(String id, String reason) {
        Booking booking = bookingRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Only confirmed bookings can request cancellation");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLATION_PENDING);
        booking.setCancellationReason(Objects.requireNonNullElse(reason, ""));
        booking.setCancellationRequestedAt(LocalDateTime.now());

        booking = bookingRepository.save(booking);

        // Send cancellation request notification to admin
        try {
            sendCancellationRequestNotification(booking);
        } catch (Exception e) {
            System.out.println("Failed to send cancellation request notification: " + e.getMessage());
        }

        return mapToResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getCancellationRequests() {
        return bookingRepository.findAll()
                .stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CANCELLATION_PENDING)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse cancelBooking(String id, String reason) {
        Booking booking = bookingRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        if (reason != null && !reason.isEmpty()) {
            booking.setCancellationReason(Objects.requireNonNullElse(reason, ""));
        }

        List<Seat> showtimeSeats = seatRepository.findByShowtimeId(booking.getShowtimeId());
        for (String seatId : booking.getSeats()) {
            Seat seat = showtimeSeats.stream()
                    .filter(s -> seatId.equals(s.getId()))
                    .findFirst()
                    .orElse(null);
            if (seat != null) {
                seat.setIsBooked(false);
                seat.setIsHeld(false);
                seatRepository.save(seat);
            }
        }

        booking.setRefundAmount(booking.getTotalAmount() * 0.9);
        booking.setRefundDate(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        // Send booking cancellation notification
        try {
            sendBookingCancelledNotification(savedBooking);
        } catch (Exception e) {
            System.out.println("Failed to send booking cancellation notification: " + e.getMessage());
        }

        return mapToResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBooking(String id) {
        Booking booking = bookingRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        List<Seat> showtimeSeats = seatRepository.findByShowtimeId(booking.getShowtimeId());
        for (String seatId : booking.getSeats()) {
            Seat seat = showtimeSeats.stream()
                    .filter(s -> seatId.equals(s.getId()) ||
                            seatId.equals(s.getRow() + s.getNumber()))
                    .findFirst()
                    .orElse(null);
            if (seat != null) {
                seat.setIsBooked(false);
                seat.setIsHeld(false);
                seatRepository.save(seat);
            }
        }

        bookingRepository.delete(booking);
    }

    @Transactional
    public BookingResponse scanBooking(String id) {
        Booking booking = bookingRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot scan cancelled booking");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse resignBooking(String id, List<String> newSeats) {
        Booking booking = bookingRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot reassign seats for cancelled booking");
        }

        List<Seat> showtimeSeats = seatRepository.findByShowtimeId(booking.getShowtimeId());

        for (String seatId : booking.getSeats()) {
            Seat seat = showtimeSeats.stream()
                    .filter(s -> seatId.equals(s.getId()) ||
                            seatId.equals(s.getRow() + s.getNumber()))
                    .findFirst()
                    .orElse(null);
            if (seat != null) {
                seat.setIsBooked(false);
                seatRepository.save(seat);
            }
        }

        for (String seatId : newSeats) {
            Seat seat = showtimeSeats.stream()
                    .filter(s -> seatId.equals(s.getId()) ||
                            seatId.equals(s.getRow() + s.getNumber()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Seat not found: " + seatId));
            if (seat.getIsBooked()) {
                throw new RuntimeException("Seat " + seat.getRow() + seat.getNumber() + " is already booked");
            }
        }

        booking.setSeats(newSeats);
        for (String seatId : newSeats) {
            Seat seat = showtimeSeats.stream()
                    .filter(s -> seatId.equals(s.getId()) ||
                            seatId.equals(s.getRow() + s.getNumber()))
                    .findFirst()
                    .orElse(null);
            if (seat != null) {
                seat.setIsBooked(true);
                seatRepository.save(seat);
            }
        }

        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse confirmPayment(String bookingId, String transactionId) {
        Booking booking = bookingRepository.findById(Objects.requireNonNullElse(bookingId, ""))
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setPaymentId(transactionId);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        booking = bookingRepository.save(booking);
        return mapToResponse(booking);
    }

    public BookingStatsResponse getBookingStats() {
        Long totalBookings = bookingRepository.count();
        Long cancelledBookings = bookingRepository.countByStatus(Booking.BookingStatus.CANCELLED);
        LocalDateTime now = LocalDateTime.now();
        Long bookingsLast7Days = bookingRepository.countByDateRange(now.minusDays(7), now);
        Long bookingsLast30Days = bookingRepository.countByDateRange(now.minusDays(30), now);
        Long totalSeatsBooked = bookingRepository.findAll().stream()
                .mapToLong(b -> b.getSeats() != null ? b.getSeats().size() : 0)
                .sum();
        return new BookingStatsResponse(totalBookings, cancelledBookings, bookingsLast7Days, bookingsLast30Days,
                totalSeatsBooked);
    }

    private BookingResponse mapToResponse(Booking booking) {
        String movieId = "";
        String movieTitle = "";
        String moviePosterUrl = "";
        String theaterId = "";
        String theaterName = "";
        String theaterLocation = "";
        LocalDateTime showtime = null;
        String screen = booking.getScreenName();
        Double ticketPrice = booking.getTicketPriceSnapshot();

        try {
            ShowtimeDTO showtimeDTO = showtimeServiceClient.getShowtimeById(booking.getShowtimeId());
            if (showtimeDTO != null) {
                movieId = Objects.requireNonNullElse(showtimeDTO.getMovieId(), "");
                theaterId = Objects.requireNonNullElse(showtimeDTO.getTheaterId(), "");
                showtime = showtimeDTO.getShowDateTime();
                if (ticketPrice == null) {
                    ticketPrice = showtimeDTO.getTicketPrice();
                }

                // Get screen name from theater service
                if (screen == null || screen.isEmpty()) {
                    String screenId = showtimeDTO.getScreen();
                    if (screenId != null && !screenId.isEmpty()) {
                        try {
                            Map<String, Object> screenData = theaterServiceClient.getScreenById(screenId);
                            if (screenData != null && screenData.containsKey("name")) {
                                screen = (String) screenData.get("name");
                            } else {
                                screen = "Screen " + screenId.substring(0, Math.min(8, screenId.length()));
                            }
                        } catch (Exception e) {
                            screen = "Screen Info Unavailable";
                        }
                    }
                }

                if (!movieId.isEmpty()) {
                    try {
                        MovieDTO movieDTO = movieServiceClient.getMovieById(movieId);
                        if (movieDTO != null) {
                            movieTitle = Objects.requireNonNullElse(movieDTO.getTitle(), "");
                            moviePosterUrl = Objects.requireNonNullElse(movieDTO.getPosterUrl(), "");
                        }
                    } catch (Exception e) {
                        movieTitle = "Movie Info Unavailable";
                    }
                }

                if (!theaterId.isEmpty()) {
                    try {
                        TheaterDTO theaterDTO = theaterServiceClient.getTheaterById(theaterId);
                        if (theaterDTO != null) {
                            theaterName = Objects.requireNonNullElse(theaterDTO.getName(), "");
                            String location = Objects.requireNonNullElse(theaterDTO.getLocation(), "");
                            String city = Objects.requireNonNullElse(theaterDTO.getCity(), "");
                            theaterLocation = location.isEmpty() ? city
                                    : (city.isEmpty() ? location : location + ", " + city);
                        }
                    } catch (Exception e) {
                        theaterName = "Theater Info Unavailable";
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to empty values if showtime service fails
        }

        return BookingResponse.builder()
                .id(Objects.requireNonNullElse(booking.getId(), ""))
                .userId(Objects.requireNonNullElse(booking.getUserId(), ""))
                .movieId(movieId)
                .movieTitle(movieTitle)
                .moviePosterUrl(moviePosterUrl)
                .theaterId(theaterId)
                .theaterName(theaterName)
                .theaterLocation(theaterLocation)
                .showtimeId(Objects.requireNonNullElse(booking.getShowtimeId(), ""))
                .showtime(showtime)
                .screen(screen)
                .ticketPrice(ticketPrice)
                .seats(booking.getSeats())
                .seatLabels(booking.getSeatLabels())
                .totalAmount(booking.getTotalAmount())
                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus())
                .customerName(Objects.requireNonNullElse(booking.getCustomerName(), ""))
                .customerEmail(Objects.requireNonNullElse(booking.getCustomerEmail(), ""))
                .customerPhone(Objects.requireNonNullElse(booking.getCustomerPhone(), ""))
                .paymentId(Objects.requireNonNullElse(booking.getPaymentId(), ""))
                .qrCode(Objects.requireNonNullElse(booking.getQrCode(), ""))
                .ticketNumber(Objects.requireNonNullElse(booking.getTicketNumber(), ""))
                .refundAmount(booking.getRefundAmount())
                .refundDate(booking.getRefundDate())
                .cancellationReason(Objects.requireNonNullElse(booking.getCancellationReason(), ""))
                .build();
    }

    private void sendBookingConfirmationNotification(Booking booking) {
        Map<String, Object> request = new java.util.HashMap<>();
        request.put("customerEmail", booking.getCustomerEmail());
        request.put("customerName", booking.getCustomerName());
        request.put("ticketNumber", booking.getTicketNumber());
        request.put("bookingId", booking.getId());
        request.put("totalAmount", booking.getTotalAmount());
        // Use seat labels if available, otherwise use seat IDs
        if (booking.getSeatLabels() != null && !booking.getSeatLabels().isEmpty()) {
            request.put("seats", booking.getSeatLabels());
        } else {
            request.put("seats", booking.getSeats());
        }

        // Fetch movie and theater details
        try {
            ShowtimeDTO showtimeDTO = showtimeServiceClient.getShowtimeById(booking.getShowtimeId());
            if (showtimeDTO != null) {
                request.put("showDateTime", showtimeDTO.getShowDateTime());

                // Get movie details
                if (showtimeDTO.getMovieId() != null) {
                    try {
                        MovieDTO movieDTO = movieServiceClient.getMovieById(showtimeDTO.getMovieId());
                        if (movieDTO != null) {
                            request.put("movieTitle", movieDTO.getTitle());
                        }
                    } catch (Exception e) {
                        request.put("movieTitle", "Movie Info Unavailable");
                    }
                }

                // Get theater details
                if (showtimeDTO.getTheaterId() != null) {
                    try {
                        TheaterDTO theaterDTO = theaterServiceClient.getTheaterById(showtimeDTO.getTheaterId());
                        if (theaterDTO != null) {
                            request.put("theaterName", theaterDTO.getName());
                        }
                    } catch (Exception e) {
                        request.put("theaterName", "Theater Info Unavailable");
                    }
                }

                // Get screen details
                if (showtimeDTO.getScreen() != null) {
                    try {
                        Map<String, Object> screenData = theaterServiceClient.getScreenById(showtimeDTO.getScreen());
                        if (screenData != null && screenData.containsKey("name")) {
                            request.put("screenName", screenData.get("name"));
                        } else {
                            request.put("screenName", "Screen " + showtimeDTO.getScreen().substring(0,
                                    Math.min(8, showtimeDTO.getScreen().length())));
                        }
                    } catch (Exception e) {
                        request.put("screenName", "Screen Info Unavailable");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch booking details for notification: " + e.getMessage());
        }

        notificationServiceClient.sendBookingConfirmation(request);
    }

    private void sendCancellationRequestNotification(Booking booking) {
        Map<String, Object> request = new java.util.HashMap<>();
        request.put("bookingId", booking.getId());
        request.put("customerName", booking.getCustomerName());
        request.put("customerEmail", booking.getCustomerEmail());
        request.put("reason", booking.getCancellationReason());
        request.put("ticketNumber", booking.getTicketNumber());
        notificationServiceClient.sendCancellationRequest(request);
    }

    private void sendBookingCancelledNotification(Booking booking) {
        Map<String, Object> request = new java.util.HashMap<>();
        request.put("customerEmail", booking.getCustomerEmail());
        request.put("customerName", booking.getCustomerName());
        request.put("ticketNumber", booking.getTicketNumber());
        request.put("refundAmount", booking.getRefundAmount());
        notificationServiceClient.sendBookingCancelled(request);
    }

    private void sendAdminNewBookingNotification(Booking booking) {
        Map<String, Object> request = new java.util.HashMap<>();
        request.put("bookingId", booking.getId());
        request.put("customerName", booking.getCustomerName());
        request.put("customerEmail", booking.getCustomerEmail());
        request.put("ticketNumber", booking.getTicketNumber());
        request.put("totalAmount", booking.getTotalAmount());
        notificationServiceClient.sendAdminNewBooking(request);
    }
}
