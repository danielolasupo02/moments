package com.journal.journalbackend.config;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.cglib.core.Local;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailConfig {
    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    // Constructor for dependency injection
    public EmailConfig(
            JavaMailSender emailSender,
            SpringTemplateEngine templateEngine
    ) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    public void sendVerificationEmail(String to, String token, String firstName) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            // Prepare Thymeleaf context
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("token", token);
            context.setVariable("verificationUrl", "http://localhost:8080/verify?token=" + token);

            // Process the email template
            String htmlBody = templateEngine.process("verification-email", context);

            helper.setTo(to);
            helper.setFrom("daniel.olasupo@stu.cu.edu.ng");
            helper.setSubject("Verify Your JournalApp Account");
            helper.setText(htmlBody, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendPasswordResetEmail(String to, String token, String firstName) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            // Prepare Thymeleaf context
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("token", token);
            context.setVariable("resetUrl", "http://localhost:8081/reset-password?token=" + token);

            // Process the email template
            String htmlBody = templateEngine.process("reset-password-email", context);

            helper.setTo(to);
            helper.setFrom("elvisikenna07@gmail.com");
            helper.setSubject("Reset Your JournalApp Password");
            helper.setText(htmlBody, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    // EmailService.java
    public void sendMonthlySummary(String toEmail, long entryCount, LocalDate monthYear) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("Recipient email address is missing.");
        }

        Context context = new Context();
        context.setVariable("entryCount", entryCount);
        context.setVariable("monthYear", monthYear.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        context.setVariable("link", generateViewLink(monthYear));

        String content = templateEngine.process("email/monthly-reflection", context);

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail); // <-- This was missing
            helper.setSubject("Your Monthly Journal Reflection");
            helper.setText(content, true); // true = HTML

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendMemoryLaneEmail(String toEmail, LocalDate year, long entryCount) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("Recipient email address is missing.");
        }

        Context context = new Context();
        context.setVariable("year", year);
        context.setVariable("entryCount", entryCount);
        context.setVariable("link", generateMemoryLaneLink(year));

        String content = templateEngine.process("email/memory-lane", context);

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject("Take a Stroll Down Memory Lane – Your " + year + " Journal Recap");
            helper.setText(content, true); // HTML content

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send Memory Lane email", e);
        }
    }



    private String generateViewLink(LocalDate monthYear) {
        return "http://localhost:8081/view-summary?month=" + monthYear.toString();
    }

    private String generateMemoryLaneLink(LocalDate year) {
        return "http://localhost:8081/memory-lane?year=" + year;
    }


}