package rca.restapi.year2.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${spring.application.name:user-service}")
    private String appName;

    // In-memory store for verification tokens (use Redis in production)
    private final ConcurrentHashMap<String, TokenData> verificationTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TokenData> passwordResetTokens = new ConcurrentHashMap<>();

    /**
     * Send email verification link
     * In production, integrate with SendGrid, AWS SES, or similar
     */
    public void sendVerificationEmail(String email, String username) {
        String token = generateToken();
        verificationTokens.put(token, new TokenData(email, LocalDateTime.now().plusHours(24)));

        String verificationLink = String.format("http://localhost:8082/api/auth/verify-email?token=%s", token);

        log.info("===========================================");
        log.info("EMAIL VERIFICATION");
        log.info("To: {}", email);
        log.info("Subject: Verify Your Email - {}", appName);
        log.info("-------------------------------------------");
        log.info("Hello {},", username);
        log.info("");
        log.info("Thank you for registering!");
        log.info("Please verify your email by clicking the link below:");
        log.info("");
        log.info("{}", verificationLink);
        log.info("");
        log.info("This link will expire in 24 hours.");
        log.info("===========================================");

        // TODO: Integrate actual email service (SendGrid, AWS SES, etc.)
        // emailClient.send(email, subject, body);
    }

    /**
     * Send password reset link
     */
    public String sendPasswordResetEmail(String email, String username) {
        String token = generateToken();
        passwordResetTokens.put(token, new TokenData(email, LocalDateTime.now().plusHours(1)));

        String resetLink = String.format("http://localhost:8082/api/auth/reset-password?token=%s", token);

        log.info("===========================================");
        log.info("PASSWORD RESET");
        log.info("To: {}", email);
        log.info("Subject: Reset Your Password - {}", appName);
        log.info("-------------------------------------------");
        log.info("Hello {},", username);
        log.info("");
        log.info("We received a request to reset your password.");
        log.info("Click the link below to reset your password:");
        log.info("");
        log.info("{}", resetLink);
        log.info("");
        log.info("This link will expire in 1 hour.");
        log.info("If you didn't request this, please ignore this email.");
        log.info("===========================================");

        return token;
    }

    /**
     * Send welcome email after registration
     */
    public void sendWelcomeEmail(String email, String username) {
        log.info("===========================================");
        log.info("WELCOME EMAIL");
        log.info("To: {}", email);
        log.info("Subject: Welcome to {} - Let's Get Started!", appName);
        log.info("-------------------------------------------");
        log.info("Hello {},", username);
        log.info("");
        log.info("Welcome to {}!", appName);
        log.info("We're excited to have you on board.");
        log.info("");
        log.info("Here are some things you can do:");
        log.info("- Complete your profile");
        log.info("- Add your shipping addresses");
        log.info("- Start shopping!");
        log.info("");
        log.info("If you have any questions, feel free to reach out.");
        log.info("===========================================");
    }

    /**
     * Notify user of password change
     */
    public void sendPasswordChangeNotification(String email, String username) {
        log.info("===========================================");
        log.info("PASSWORD CHANGED NOTIFICATION");
        log.info("To: {}", email);
        log.info("Subject: Your Password Was Changed - {}", appName);
        log.info("-------------------------------------------");
        log.info("Hello {},", username);
        log.info("");
        log.info("Your password was successfully changed.");
        log.info("");
        log.info("If you didn't make this change, please contact support immediately.");
        log.info("Changed at: {}", LocalDateTime.now());
        log.info("===========================================");
    }

    /**
     * Verify email token
     */
    public String verifyEmailToken(String token) {
        TokenData tokenData = verificationTokens.get(token);

        if (tokenData == null) {
            return null;
        }

        if (LocalDateTime.now().isAfter(tokenData.expiresAt)) {
            verificationTokens.remove(token);
            return null;
        }

        verificationTokens.remove(token);
        return tokenData.email;
    }

    /**
     * Verify password reset token
     */
    public String verifyPasswordResetToken(String token) {
        TokenData tokenData = passwordResetTokens.get(token);

        if (tokenData == null) {
            return null;
        }

        if (LocalDateTime.now().isAfter(tokenData.expiresAt)) {
            passwordResetTokens.remove(token);
            return null;
        }

        return tokenData.email;
    }

    /**
     * Invalidate password reset token after use
     */
    public void invalidatePasswordResetToken(String token) {
        passwordResetTokens.remove(token);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Inner class to store token data
     */
    private static class TokenData {
        String email;
        LocalDateTime expiresAt;

        TokenData(String email, LocalDateTime expiresAt) {
            this.email = email;
            this.expiresAt = expiresAt;
        }
    }
}