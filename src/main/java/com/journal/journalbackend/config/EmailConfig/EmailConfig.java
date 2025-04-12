package com.journal.journalbackend.config.EmailConfig;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

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
}