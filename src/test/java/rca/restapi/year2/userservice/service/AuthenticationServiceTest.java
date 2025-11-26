package rca.restapi.year2.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import rca.restapi.year2.userservice.dto.requests.LoginRequest;
import rca.restapi.year2.userservice.dto.requests.RefreshTokenRequest;
import rca.restapi.year2.userservice.dto.requests.RegisterRequest;
import rca.restapi.year2.userservice.dto.responses.AuthResponse;
import rca.restapi.year2.userservice.exception.ResourceAlreadyExistsException;
import rca.restapi.year2.userservice.exception.ResourceNotFoundException;
import rca.restapi.year2.userservice.exception.UnauthorizedException;
import rca.restapi.year2.userservice.model.RefreshToken;
import rca.restapi.year2.userservice.model.User;
import rca.restapi.year2.userservice.repository.RefreshTokenRepository;
import rca.restapi.year2.userservice.repository.UserRepository;
import rca.restapi.year2.userservice.security.CustomUserDetailsService;
import rca.restapi.year2.userservice.security.JwtService;
import rca.restapi.year2.userservice.util.TestDataBuilder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildUser();
        registerRequest = TestDataBuilder.buildRegisterRequest();
        loginRequest = TestDataBuilder.buildLoginRequest();

        // Set maxLoginAttempts for tests
        ReflectionTestUtils.setField(authenticationService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(authenticationService, "lockoutDurationMinutes", 15);
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegister_Success() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refreshToken");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mock(RefreshToken.class));

        // When
        AuthResponse response = authenticationService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(900000L);

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).existsByUsername(registerRequest.getUsername());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateAccessToken(userDetails);
        verify(jwtService).generateRefreshToken(userDetails);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegister_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Email already registered");

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegister_UsernameAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Username already taken");

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).existsByUsername(registerRequest.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refreshToken");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mock(RefreshToken.class));

        // When
        AuthResponse response = authenticationService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).deleteAllByUserId(testUser.getId());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testLogin_UserNotFound() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Should throw exception when account is locked")
    void testLogin_AccountLocked() {
        // Given
        testUser.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Account is locked. Try again later.");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Should increment failed login attempts on invalid credentials")
    void testLogin_InvalidCredentials_IncrementAttempts() {
        // Given
        User user = spy(testUser);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When/Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(user).incrementFailedLoginAttempts();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should lock account after max failed login attempts")
    void testLogin_MaxFailedAttempts_LockAccount() {
        // Given
        User user = spy(testUser);
        user.setFailedLoginAttempts(4);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When/Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Account locked due to 5 failed login attempts");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(user).incrementFailedLoginAttempts();
        verify(user).lockAccount(anyInt());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should reset failed attempts on successful login")
    void testLogin_Success_ResetFailedAttempts() {
        // Given
        User user = spy(testUser);
        user.setFailedLoginAttempts(3);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refreshToken");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mock(RefreshToken.class));

        // When
        authenticationService.login(loginRequest);

        // Then
        verify(user).resetFailedLoginAttempts();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshToken_Success() {
        // Given
        RefreshToken refreshToken = TestDataBuilder.buildRefreshToken(testUser);
        RefreshTokenRequest request = TestDataBuilder.buildRefreshTokenRequest();
        refreshToken.setToken(request.getRefreshToken());

        when(refreshTokenRepository.findByToken(request.getRefreshToken()))
                .thenReturn(Optional.of(refreshToken));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("newAccessToken");

        // When
        AuthResponse response = authenticationService.refreshToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo(request.getRefreshToken());
        verify(refreshTokenRepository).findByToken(request.getRefreshToken());
        verify(jwtService).generateAccessToken(userDetails);
    }

    @Test
    @DisplayName("Should throw exception when refresh token not found")
    void testRefreshToken_TokenNotFound() {
        // Given
        RefreshTokenRequest request = TestDataBuilder.buildRefreshTokenRequest();
        when(refreshTokenRepository.findByToken(request.getRefreshToken()))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authenticationService.refreshToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    @DisplayName("Should throw exception when refresh token is expired")
    void testRefreshToken_TokenExpired() {
        // Given
        RefreshToken refreshToken = TestDataBuilder.buildRefreshToken(testUser);
        refreshToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        RefreshTokenRequest request = TestDataBuilder.buildRefreshTokenRequest();
        refreshToken.setToken(request.getRefreshToken());

        when(refreshTokenRepository.findByToken(request.getRefreshToken()))
                .thenReturn(Optional.of(refreshToken));

        // When/Then
        assertThatThrownBy(() -> authenticationService.refreshToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token expired or revoked");
    }

    @Test
    @DisplayName("Should throw exception when refresh token is revoked")
    void testRefreshToken_TokenRevoked() {
        // Given
        RefreshToken refreshToken = TestDataBuilder.buildRefreshToken(testUser);
        refreshToken.setRevoked(true);
        RefreshTokenRequest request = TestDataBuilder.buildRefreshTokenRequest();
        refreshToken.setToken(request.getRefreshToken());

        when(refreshTokenRepository.findByToken(request.getRefreshToken()))
                .thenReturn(Optional.of(refreshToken));

        // When/Then
        assertThatThrownBy(() -> authenticationService.refreshToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token expired or revoked");
    }

    @Test
    @DisplayName("Should logout successfully")
    void testLogout_Success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        doNothing().when(refreshTokenRepository).deleteAllByUserId(testUser.getId());

        // When
        authenticationService.logout(email);

        // Then
        verify(userRepository).findByEmail(email);
        verify(refreshTokenRepository).deleteAllByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when user not found during logout")
    void testLogout_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authenticationService.logout(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail(email);
        verify(refreshTokenRepository, never()).deleteAllByUserId(any());
    }
}
