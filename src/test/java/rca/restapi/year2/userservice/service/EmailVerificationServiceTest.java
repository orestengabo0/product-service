package rca.restapi.year2.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rca.restapi.year2.userservice.exception.ResourceNotFoundException;
import rca.restapi.year2.userservice.exception.UnauthorizedException;
import rca.restapi.year2.userservice.model.User;
import rca.restapi.year2.userservice.repository.UserRepository;
import rca.restapi.year2.userservice.util.TestDataBuilder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerificationService Unit Tests")
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildUser();
    }

    @Test
    @DisplayName("Should send verification email successfully")
    void testSendVerificationEmail_Success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // When
        emailVerificationService.sendVerificationEmail(email);

        // Then
        verify(userRepository).findByEmail(email);
        verify(emailService).sendVerificationEmail(testUser.getEmail(), testUser.getUsername());
    }

    @Test
    @DisplayName("Should not send verification email if already verified")
    void testSendVerificationEmail_AlreadyVerified() {
        // Given
        String email = "test@example.com";
        testUser.setEmailVerified(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        emailVerificationService.sendVerificationEmail(email);

        // Then
        verify(userRepository).findByEmail(email);
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testSendVerificationEmail_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> emailVerificationService.sendVerificationEmail(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("Should verify email successfully")
    void testVerifyEmail_Success() {
        // Given
        String token = "valid-token";
        when(emailService.verifyEmailToken(token)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        emailVerificationService.verifyEmail(token);

        // Then
        verify(emailService).verifyEmailToken(token);
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception for invalid token")
    void testVerifyEmail_InvalidToken() {
        // Given
        String token = "invalid-token";
        when(emailService.verifyEmailToken(token)).thenReturn(null);

        // When/Then
        assertThatThrownBy(() -> emailVerificationService.verifyEmail(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid or expired verification token");
    }
}

