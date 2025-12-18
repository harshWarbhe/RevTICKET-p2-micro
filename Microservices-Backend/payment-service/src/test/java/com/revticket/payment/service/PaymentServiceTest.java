package com.revticket.payment.service;

import com.revticket.payment.client.BookingConfirmationClient;
import com.revticket.payment.dto.PaymentRequest;
import com.revticket.payment.entity.Payment;
import com.revticket.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingConfirmationClient bookingConfirmationClient;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void testProcessPayment_Success() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId("booking1");
        request.setAmount(100.0);
        request.setPaymentMethod("CARD");

        Payment payment = new Payment();
        payment.setId("pay1");
        payment.setBookingId("booking1");
        payment.setAmount(100.0);
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN12345");

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(bookingConfirmationClient.confirmPayment(anyString(), anyString())).thenReturn(null);

        Payment result = paymentService.processPayment(request);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus().name());
        assertEquals("TXN12345", result.getTransactionId());
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingConfirmationClient).confirmPayment("booking1", "TXN12345");
    }

    @Test
    void testGetPaymentStatus_Success() {
        Payment payment = new Payment();
        payment.setTransactionId("TXN12345");

        when(paymentRepository.findByTransactionId("TXN12345")).thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentService.getPaymentStatus("TXN12345");

        assertTrue(result.isPresent());
        assertEquals("TXN12345", result.get().getTransactionId());
    }

}
