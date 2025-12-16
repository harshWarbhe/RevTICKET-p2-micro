package com.revticket.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.revticket.payment.client.BookingServiceClient;
import com.revticket.payment.client.ShowtimeServiceClient;
import com.revticket.payment.dto.RazorpayOrderRequest;
import com.revticket.payment.dto.RazorpayOrderResponse;
import com.revticket.payment.dto.RazorpayVerificationRequest;
import com.revticket.payment.entity.Payment;
import com.revticket.payment.repository.PaymentRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class RazorpayService {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayService.class);

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingServiceClient bookingServiceClient;

    @Autowired
    private ShowtimeServiceClient showtimeServiceClient;

    public RazorpayOrderResponse createOrder(RazorpayOrderRequest request) throws RazorpayException {
        logger.info("Creating Razorpay order for amount: {} {}", request.getAmount(), request.getCurrency());

        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        int amountInPaise = (int) (request.getAmount() * 100);
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", request.getCurrency());
        String receipt = "order_" + System.currentTimeMillis();
        orderRequest.put("receipt", receipt);

        logger.debug("Razorpay order request: amount={}, currency={}, receipt={}", amountInPaise, request.getCurrency(),
                receipt);
        Order order = razorpayClient.orders.create(orderRequest);
        String orderId = (String) order.get("id");
        logger.info("Razorpay order created successfully: {}", orderId);

        return new RazorpayOrderResponse(
                orderId,
                (String) order.get("currency"),
                order.get("amount"),
                razorpayKeyId);
    }

    /**
     * Verify payment and create booking - migrated from monolithic version
     * Exactly matches monolithic implementation but uses Feign clients for
     * inter-service calls
     */
    @Transactional
    public Map<String, Object> verifyPaymentAndCreateBooking(String userId,
            RazorpayVerificationRequest request) throws Exception {
        logger.info("Verifying payment signature for order: {}", request.getRazorpayOrderId());

        // Step 1: Verify Razorpay signature
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", request.getRazorpayOrderId());
        options.put("razorpay_payment_id", request.getRazorpayPaymentId());
        options.put("razorpay_signature", request.getRazorpaySignature());

        boolean isValidSignature = Utils.verifyPaymentSignature(options, razorpayKeySecret);

        if (!isValidSignature) {
            logger.error("Invalid payment signature for order: {}", request.getRazorpayOrderId());
            throw new RuntimeException("Invalid payment signature");
        }
        logger.info("Payment signature verified successfully");

        // Step 2: Get showtime details
        logger.info("Fetching showtime details for: {}", request.getShowtimeId());
        Map<String, Object> showtime = showtimeServiceClient.getShowtimeById(request.getShowtimeId());
        Map<String, Object> movie = (Map<String, Object>) showtime.get("movie");
        Map<String, Object> theater = (Map<String, Object>) showtime.get("theater");
        String showtimeDateTime = (String) showtime.get("showDateTime");
        logger.debug("Showtime details fetched: movie={}, theater={}", movie.get("id"), theater.get("id"));

        // Step 3: Create booking via booking service
        com.revticket.payment.dto.BookingRequest bookingRequest = new com.revticket.payment.dto.BookingRequest();
        bookingRequest.setUserId(userId);
        bookingRequest.setMovieId((String) movie.get("id"));
        bookingRequest.setTheaterId((String) theater.get("id"));
        bookingRequest.setShowtimeId(request.getShowtimeId());
        bookingRequest.setSeats(request.getSeats());
        bookingRequest.setSeatLabels(request.getSeatLabels());
        bookingRequest.setTotalAmount(request.getTotalAmount());

        // Parse showtime string to LocalDateTime
        LocalDateTime showtimeLocalDateTime = parseShowtimeString(showtimeDateTime);
        bookingRequest.setShowtime(showtimeLocalDateTime);

        bookingRequest.setCustomerName(request.getCustomerName());
        bookingRequest.setCustomerEmail(request.getCustomerEmail());
        bookingRequest.setCustomerPhone(request.getCustomerPhone());

        int seatCount = request.getSeats() != null ? request.getSeats().size() : 0;
        logger.debug(
                "Booking request details: userId={}, movieId={}, theaterId={}, showtimeId={}, showtime={}, seats={}",
                userId, bookingRequest.getMovieId(), bookingRequest.getTheaterId(),
                request.getShowtimeId(), showtimeLocalDateTime, seatCount);

        logger.info("Creating booking for user: {} with {} seats", userId, seatCount);

        logger.info("Creating booking for user: {} with {} seats", userId, seatCount);
        logger.info("Booking request: {}", bookingRequest);
        
        Map<String, Object> bookingResponse;
        try {
            bookingResponse = bookingServiceClient.createBooking(bookingRequest);
        } catch (Exception e) {
            logger.error("Booking creation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Booking creation failed: " + e.getMessage());
        }
        
        String bookingId = (String) bookingResponse.get("id");
        String ticketNumber = (String) bookingResponse.get("ticketNumber");
        logger.info("Booking created successfully: {} with ticket: {}", bookingId, ticketNumber);

        // Step 4: Save payment record
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setAmount(request.getTotalAmount());
        payment.setPaymentMethod(Payment.PaymentMethod.UPI);
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setRazorpayOrderId(request.getRazorpayOrderId());
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setTransactionId(request.getRazorpayPaymentId());

        paymentRepository.save(payment);
        logger.info("Payment record saved with ID: {}", payment.getId());

        // Step 5: Return result
        Map<String, Object> result = new HashMap<>();
        result.put("bookingId", bookingId);
        result.put("ticketNumber", ticketNumber);

        return result;
    }

    @Transactional
    public void handlePaymentFailure(String userId, RazorpayVerificationRequest request) {
        logger.info("Handling payment failure for user: {} and order: {}", userId, request.getRazorpayOrderId());

        String bookingId = "BKG_FAILED_" + System.currentTimeMillis();

        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setAmount(request.getTotalAmount());
        payment.setPaymentMethod(Payment.PaymentMethod.UPI);
        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setRazorpayOrderId(request.getRazorpayOrderId());

        paymentRepository.save(payment);
        logger.info("Payment failure recorded with ID: {}", payment.getId());
    }

    /**
     * Parse showtime string to LocalDateTime
     * Handles multiple date format possibilities
     */
    private LocalDateTime parseShowtimeString(String showtimeDateTime) {
        if (showtimeDateTime == null || showtimeDateTime.isEmpty()) {
            logger.warn("Showtime datetime is null or empty, using current time");
            return LocalDateTime.now();
        }

        try {
            // Try ISO format first (most common)
            if (showtimeDateTime.contains("T")) {
                return LocalDateTime.parse(showtimeDateTime);
            }

            // Try other common formats
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(showtimeDateTime, formatter);
        } catch (Exception e) {
            logger.error("Failed to parse showtime: {}, using current time. Error: {}", showtimeDateTime,
                    e.getMessage());
            return LocalDateTime.now();
        }
    }
}