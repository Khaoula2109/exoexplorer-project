package com.example.exoExplorer.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        // Set up dependencies using reflection to match your field injection
        ReflectionTestUtils.setField(mailService, "mailSender", mailSender);
        ReflectionTestUtils.setField(mailService, "templateEngine", templateEngine);
    }

    @Test
    void testSendEmail() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        // When
        mailService.sendEmail(to, subject, content);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals(to, capturedMessage.getTo()[0]);
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(content, capturedMessage.getText());
    }

    @Test
    void testSendOtpEmail_withThymeleaf() throws MessagingException {
        // Given
        String to = "test@example.com";
        String otp = "123456";
        String processedTemplate = "<html><body>OTP: 123456</body></html>";

        // Mock the template engine
        when(templateEngine.process(eq("otp-email"), any(Context.class))).thenReturn(processedTemplate);

        // Mock the MimeMessage
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        mailService.sendOtpEmail(to, otp);

        // Then
        verify(templateEngine).process(eq("otp-email"), any(Context.class));
        verify(mailSender).send(eq(mimeMessage));
    }

    @Test
    void testSendOtpEmail_withThymeleafButExceptionThrown() throws MessagingException {
        // Given
        String to = "test@example.com";
        String otp = "123456";
        String processedTemplate = "<html><body>OTP: 123456</body></html>";

        // Mock the template engine
        when(templateEngine.process(eq("otp-email"), any(Context.class))).thenReturn(processedTemplate);

        // Force an exception when creating the MimeMessage
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Test exception"));

        // When
        mailService.sendOtpEmail(to, otp);

        // Then
        // Should fall back to simple email
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals(to, capturedMessage.getTo()[0]);
        assertEquals("Votre code OTP", capturedMessage.getSubject());
        assertTrue(capturedMessage.getText().contains(otp));
    }

    @Test
    void testSendOtpEmail_withoutThymeleaf() {
        // Given
        String to = "test@example.com";
        String otp = "123456";

        // Remove the Thymeleaf template engine
        ReflectionTestUtils.setField(mailService, "templateEngine", null);

        // When
        mailService.sendOtpEmail(to, otp);

        // Then
        // Should use simple email
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals(to, capturedMessage.getTo()[0]);
        assertEquals("Votre code OTP", capturedMessage.getSubject());
        assertTrue(capturedMessage.getText().contains(otp));
    }

    @Test
    void testSendHtmlEmail() throws MessagingException {
        // Given
        String to = "test@example.com";
        String subject = "HTML Email";
        String htmlContent = "<h1>Test</h1>";

        // Mock MimeMessage
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        mailService.sendHtmlEmail(to, subject, htmlContent);

        // Then
        verify(mailSender).send(mimeMessage);
    }
}