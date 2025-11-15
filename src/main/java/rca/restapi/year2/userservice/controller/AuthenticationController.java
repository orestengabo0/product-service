package rca.restapi.year2.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.userservice.dto.requests.*;
import rca.restapi.year2.userservice.dto.responses.AuthResponse;
import rca.restapi.year2.userservice.service.AuthenticationService;
import rca.restapi.year2.userservice.service.EmailVerificationService;
import rca.restapi.year2.userservice.service.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for: {}", request.getEmail());
        AuthResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request received for: {}", request.getEmail());
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        AuthResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        String email = authentication.getName();
        log.info("Logout request received for: {}", email);
        authenticationService.logout(email);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(Authentication authentication) {
        return ResponseEntity.ok(Map.of("valid", authentication != null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        log.info("Password reset request for: {}", request.getEmail());
        passwordResetService.requestPasswordReset(request);
        return ResponseEntity.ok(Map.of("message", "Password reset link sent to your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetConfirm request) {
        log.info("Password reset confirmation");
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        log.info("Email verification request");
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(
            @RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("Resend verification email for: {}", email);
        emailVerificationService.resendVerificationEmail(email);
        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }
}