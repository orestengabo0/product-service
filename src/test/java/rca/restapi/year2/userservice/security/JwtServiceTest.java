package rca.restapi.year2.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;
    private String testSecret = "test-secret-key-for-jwt-token-generation-in-test-environment-minimum-256-bits-required";
    private Long accessTokenExpiration = 900000L; // 15 minutes
    private Long refreshTokenExpiration = 604800000L; // 7 days

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", accessTokenExpiration);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", refreshTokenExpiration);

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    @DisplayName("Should generate access token successfully")
    void testGenerateAccessToken_Success() {
        // When
        String token = jwtService.generateAccessToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should generate refresh token successfully")
    void testGenerateRefreshToken_Success() {
        // When
        String token = jwtService.generateRefreshToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsername_Success() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo(userDetails.getUsername());
    }

    @Test
    @DisplayName("Should extract expiration from token")
    void testExtractExpiration_Success() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);

        // When
        Date expiration = jwtService.extractExpiration(token);

        // Then
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Should extract roles claim from token")
    void testExtractRolesClaim_Success() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);

        // When
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) jwtService.extractClaim(token, claims -> claims.get("roles"));

        // Then
        assertThat(roles).isNotNull();
        assertThat(roles).contains("USER");
    }

    @Test
    @DisplayName("Should validate token successfully")
    void testValidateToken_Success() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);

        // When
        Boolean isValid = jwtService.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false for invalid token")
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Boolean isValid = jwtService.validateToken(invalidToken, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for token with wrong username")
    void testValidateToken_WrongUsername() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);
        UserDetails differentUser = org.springframework.security.core.userdetails.User.builder()
                .username("different@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // When
        Boolean isValid = jwtService.validateToken(token, differentUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for token signed with different secret")
    void testValidateToken_DifferentSecret() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);
        
        // Change secret
        ReflectionTestUtils.setField(jwtService, "secret", "different-secret-key-for-jwt-token-generation-in-test-environment-minimum-256-bits");

        // When
        Boolean isValid = jwtService.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should extract custom claim from token")
    void testExtractClaim_Success() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);

        // When
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Then
        assertThat(subject).isEqualTo(userDetails.getUsername());
    }

    @Test
    @DisplayName("Should handle token with multiple roles")
    void testGenerateToken_MultipleRoles() {
        // Given
        UserDetails adminUser = org.springframework.security.core.userdetails.User.builder()
                .username("admin@example.com")
                .password("password")
                .authorities(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER")
                )
                .build();

        // When
        String token = jwtService.generateAccessToken(adminUser);
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) jwtService.extractClaim(token, claims -> claims.get("roles"));

        // Then
        assertThat(roles).contains("ADMIN", "USER");
    }

    @Test
    @DisplayName("Should extract custom claim from token")
    void testExtractCustomClaim() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);
        
        // When
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) jwtService.extractClaim(token, claims -> claims.get("roles"));

        // Then
        assertThat(roles).isNotNull();
        assertThat(roles).contains("USER");
    }
}

