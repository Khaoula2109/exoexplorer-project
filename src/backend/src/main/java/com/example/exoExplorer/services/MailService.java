package com.example.exoExplorer.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for sending emails.
 */
@Service
public class MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    /**
     * Sends a simple text email.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param content Email content
     */
    public void sendEmail(String to, String subject, String content) {
        logger.debug("Sending email to: {}", to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    /**
     * Sends an OTP email for two-factor authentication.
     *
     * @param to Recipient email address
     * @param otp One-time password
     */
    public void sendOtpEmail(String to, String otp) {
        String subject = "Votre code OTP";
        String content = "Bonjour,\n\nVotre code OTP est : " + otp + "\n\nIl est valable 5 minutes.";

        if (templateEngine != null) {
            try {
                sendHtmlEmail(to, subject, createOtpEmailContent(otp));
            } catch (Exception e) {
                // Changed from MessagingException to catch all exceptions including RuntimeException
                logger.error("Error sending HTML OTP email to: {}", to, e);
                sendEmail(to, subject, content);
            }
        } else {
            sendEmail(to, subject, content);
        }
    }

    /**
     * Sends an HTML email.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML content
     * @throws MessagingException if there's an error creating or sending the message
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        logger.debug("Sending HTML email to: {}", to);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Creates HTML content for an OTP email using Thymeleaf template.
     *
     * @param otp One-time password
     * @return HTML content
     */
    private String createOtpEmailContent(String otp) {
        if (templateEngine == null) {
            return "<p>Votre code OTP est : <strong>" + otp + "</strong></p><p>Il est valable 5 minutes.</p>";
        }

        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("expiration", "5 minutes");

        return templateEngine.process("otp-email", context);
    }
}