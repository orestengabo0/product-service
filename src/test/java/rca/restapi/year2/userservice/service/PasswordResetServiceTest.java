package rca.restapi.year2.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import rca.restapi.year2.userservice.dto.requests.PasswordResetConfirm;
import rca.restapi.year2.userservice.dto.requests.PasswordResetRequest;
import rca.restapi.year2.userservice.exception.ResourceNotFoundException;
import rca.restapi.year2.userservice.exception.UnauthorizedException;
import rca.restapi.year2.userservice.model.User;
import rca.restapi.year2.userservice.repository.UserRepository;
import rca.restapi.year2.userservice.util.TestDataBuilder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService Unit Tests")
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildUser();
    }

    @Test
    @DisplayName("Should request password reset successfully")
    void testRequestPasswordReset_Success() {
        // Given
        PasswordResetRequest request = TestDataBuilder.buildPasswordResetRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(emailService.sendPasswordResetEmail(testUser.getEmail(), testUser.getUsername()))
                .thenReturn("reset-token");

        // When
        passwordResetService.requestPasswordReset(request);

        // Then
        verify(userRepository).findByEmail(request.getEmail());
        verify(emailService).sendPasswordResetEmail(testUser.getEmail(), testUser.getUsername());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testRequestPasswordReset_UserNotFound() {
        // Given
        PasswordResetRequest request = TestDataBuilder.buildPasswordResetRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> passwordResetService.requestPasswordReset(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
        
        verify(userRepository).findByEmail(request.getEmail());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should verify reset token successfully")
    void testVerifyResetToken_Success() {
        // Given
        String token = "valid-token";
        when(emailService.verifyPasswordResetToken(token)).thenReturn(testUser.getEmail());

        // When
        String email = passwordResetService.verifyResetToken(token);

        // Then
        assertThat(email).isEqualTo(testUser.getEmail());
        verify(emailService).verifyPasswordResetToken(token);
    }

    @Test
    @DisplayName("Should throw exception for invalid reset token")
    void testVerifyResetToken_InvalidToken() {
        // Given
        String token = "invalid-token";
        when(emailService.verifyPasswordResetToken(token)).thenReturn(null);

        // When/Then
        assertThatThrownBy(() -> passwordResetService.verifyResetToken(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid or expired reset token");
        
        verify(emailService).verifyPasswordResetToken(token);
    }

    @Test
    @DisplayName("Should reset password successfully")
    void testResetPassword_Success() {
        // Given
        PasswordResetConfirm request = TestDataBuilder.buildPasswordResetConfirm();
        when(emailService.verifyPasswordResetToken(request.getToken())).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        passwordResetService.resetPassword(request);

        // Then
        verify(emailService).verifyPasswordResetToken(request.getToken());
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(passwordEncoder).encode(request.getNewPassword());
        verify(userRepository).save(testUser);
        verify(emailService).invalidatePasswordResetToken(request.getToken());
    }

    @Test
    @DisplayName("Should throw exception when user not found during password reset")
    void testResetPassword_UserNotFound() {
        // Given
        PasswordResetConfirm request = TestDataBuilder.buildPasswordResetConfirm();
        when(emailService.verifyPasswordResetToken(request.getToken())).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
        
        verify(emailService).verifyPasswordResetToken(request.getToken());
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
    }
}

