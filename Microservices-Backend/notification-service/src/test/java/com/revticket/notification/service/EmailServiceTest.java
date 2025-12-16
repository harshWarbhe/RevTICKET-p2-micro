package com.revticket.notification.service;

import com.revticket.notification.dto.EmailRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@revticket.com");
        ReflectionTestUtils.setField(emailService, "adminEmail", "admin@revticket.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void testSendEmail_Success() {
        EmailRequest request = new EmailRequest();
        request.setTo("user@example.com");
        request.setSubject("Test Subject");
        request.setBody("Test Body");

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail(request);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmail_FailureWithRetry() {
        EmailRequest request = new EmailRequest();
        request.setTo("user@example.com");
        request.setSubject("Test Subject");
        request.setBody("Test Body");

        doThrow(new RuntimeException("Mail server down")).when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail(request);

        // Max retries is 1 in the code, so it should be called 1 time (or more if retry
        // logic changes)
        // Actually the code loops: for (int attempt = 1; attempt <= maxRetries;
        // attempt++)
        // maxRetries = 1. So it runs once.
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
