package rca.restapi.year2.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.restapi.year2.userservice.dto.requests.PasswordResetConfirm;
import rca.restapi.year2.userservice.dto.requests.PasswordResetRequest;
import rca.restapi.year2.userservice.exception.ResourceNotFoundException;
import rca.restapi.year2.userservice.exception.UnauthorizedException;
import rca.restapi.year2.userservice.model.User;
import rca.restapi.year2.userservice.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Request password reset - sends email with reset link
     */
    @Transactional(readOnly = true)
    public void requestPasswordReset(PasswordResetRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername());

        log.info("Password reset email sent to: {}", request.getEmail());
    }

    /**
     * Verify email and show reset form
     */
    public String verifyResetToken(String token) {
        log.info("Verifying password reset token");

        String email = emailService.verifyPasswordResetToken(token);

        if (email == null) {
            throw new UnauthorizedException("Invalid or expired reset token");
        }

        return email;
    }

    /**
     * Reset password with token
     */
    @Transactional
    public void resetPassword(PasswordResetConfirm request) {
        log.info("Resetting password with token");

        // Verify token and get email
        String email = emailService.verifyPasswordResetToken(request.getToken());

        if (email == null) {
            throw new UnauthorizedException("Invalid or expired reset token");
        }

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate token
        emailService.invalidatePasswordResetToken(request.getToken());

        // Send notification email
        emailService.sendPasswordChangeNotification(user.getEmail(), user.getUsername());

        log.info("Password reset successfully for user: {}", email);
    }
}