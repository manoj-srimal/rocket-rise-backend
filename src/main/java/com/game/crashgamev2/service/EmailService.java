package com.game.crashgamev2.service;

import com.game.crashgamev2.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(User user, String token) {
        String recipientAddress = user.getEmail();
        String subject = "Crash Game - Account Verification";
        String confirmationUrl = "http://localhost:3000/verify-email?token=" + token;

        // --- HTML Email Content එක ---
        String htmlContent = "<div style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>"
                + "<div style='background-color: #ffffff; max-width: 600px; margin: auto; padding: 20px; border-radius: 8px; text-align: left;'>" // text-align එක left කළා
                + "<h2>Welcome, " + user.getFirstName() + "!</h2>"
                + "<p>Thank you for registering with Crash Game. To complete your registration and activate your account, please click the button below.</p>"

                // --- මෙතන තමයි අලුත් වෙනස ---
                + "<div style='text-align: center; margin: 30px 0;'>" // Button එක මැදට ගන්න අලුත් div එක
                +   "<a href='" + confirmationUrl + "' style='background-color: #0d6efd; color: white; padding: 15px 25px; text-decoration: none; border-radius: 5px; display: inline-block;'>Verify My Account</a>"
                + "</div>"
                // --- වෙනස අවසානයි ---

                + "<p>If the button above doesn't work, please copy and paste the following link into your web browser:</p>"
                + "<p style='word-break: break-all; font-size: 0.9em; color: #555;'>" + confirmationUrl + "</p>"
                + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>"
                + "<p style='font-size: 0.8em; color: #777;'>If you did not create an account, no further action is required.</p>"
                + "</div></div>";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(recipientAddress);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true දැමීමෙන් HTML එකක් ලෙස යවන බව කියනවා
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // In a real app, you'd handle this error more robustly
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendPasswordResetEmail(User user, String token) {
        String recipientAddress = user.getEmail();
        String subject = "Crash Game - Password Reset Request";
        String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2>Password Reset Request</h2>"
                + "<p>Hello " + user.getFirstName() + ",</p>"
                + "<p>We received a request to reset your password. Use the code below to set up a new password.</p>"
                + "<div style='background-color: #eee; padding: 10px 20px; text-align: center; font-size: 24px; letter-spacing: 5px; margin: 20px 0;'>"
                + "<strong>" + token + "</strong>"
                + "</div>"
                + "<p>This code will expire in 15 minutes.</p>"
                + "<p style='font-size: 0.8em; color: #777;'>If you did not request a password reset, please ignore this email.</p>"
                + "</div>";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(recipientAddress);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}