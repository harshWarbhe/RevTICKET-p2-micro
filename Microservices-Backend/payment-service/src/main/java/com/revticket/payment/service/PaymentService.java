package com.revticket.payment.service;

import com.revticket.payment.client.BookingConfirmationClient;
import com.revticket.payment.dto.PaymentRequest;
import com.revticket.payment.dto.PaymentStatsResponse;
import com.revticket.payment.entity.Payment;
import com.revticket.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingConfirmationClient bookingConfirmationClient;

    @Transactional
    public Payment processPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod()));
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());

        Payment savedPayment = paymentRepository.save(payment);

        // Update booking with payment information
        try {
            updateBookingPaymentStatus(request.getBookingId(), savedPayment.getTransactionId());
        } catch (Exception e) {
            // Log error but don't fail payment
            System.err.println("Failed to update booking payment status: " + e.getMessage());
        }

        return savedPayment;
    }

    private void updateBookingPaymentStatus(String bookingId, String transactionId) {
        try {
            bookingConfirmationClient.confirmPayment(bookingId, transactionId);
            System.out.println("Successfully updated booking payment status for: " + bookingId);
        } catch (Exception e) {
            System.err.println("Failed to update booking payment status: " + e.getMessage());
            throw e;
        }
    }

    public Optional<Payment> getPaymentStatus(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    public PaymentStatsResponse getPaymentStats() {
        Double totalRevenue = paymentRepository.sumSuccessfulPayments();
        Long totalSuccessfulPayments = paymentRepository.countByStatus(Payment.PaymentStatus.SUCCESS);
        Long failedPayments = paymentRepository.countByStatus(Payment.PaymentStatus.FAILED);
        LocalDateTime now = LocalDateTime.now();
        Double revenueLast7Days = paymentRepository.sumSuccessfulPaymentsAfter(now.minusDays(7));
        Double revenueLast30Days = paymentRepository.sumSuccessfulPaymentsAfter(now.minusDays(30));
        return new PaymentStatsResponse(totalRevenue, totalSuccessfulPayments, failedPayments, revenueLast7Days,
                revenueLast30Days);
    }
}
