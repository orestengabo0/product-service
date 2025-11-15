package rca.restapi.year2.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.restapi.year2.userservice.exception.ResourceNotFoundException;
import rca.restapi.year2.userservice.exception.UnauthorizedException;
import rca.restapi.year2.userservice.model.User;
import rca.restapi.year2.userservice.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Send verification email to user
     */
    public void sendVerificationEmail(String email) {
        log.info("Sending verification email to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getEmailVerified()) {
            log.info("Email already verified for user: {}", email);
            return;
        }

        emailService.sendVerificationEmail(user.getEmail(), user.getUsername());
        log.info("Verification email sent successfully to: {}", email);
    }

    /**
     * Verify email with token
     */
    @Transactional
    public void verifyEmail(String token) {
        log.info("Verifying email with token");

        String email = emailService.verifyEmailToken(token);

        if (email == null) {
            throw new UnauthorizedException("Invalid or expired verification token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getEmailVerified()) {
            log.info("Email already verified for user: {}", email);
            return;
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", email);
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail(String email) {
        log.info("Resending verification email to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getEmailVerified()) {
            throw new IllegalStateException("Email already verified");
        }

        emailService.sendVerificationEmail(user.getEmail(), user.getUsername());
        log.info("Verification email resent successfully to: {}", email);
    }
}