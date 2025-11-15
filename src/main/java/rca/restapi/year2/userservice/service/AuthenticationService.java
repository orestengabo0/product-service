package rca.restapi.year2.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.restapi.year2.userservice.dto.*;
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
import rca.restapi.year2.userservice.types.Role;
import rca.restapi.year2.userservice.types.UserStatus;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Value("${security.rate-limit.login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${security.rate-limit.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already taken");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Save refresh token
        saveRefreshToken(user, refreshToken);

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            throw new UnauthorizedException("Account is locked. Try again later.");
        }

        try {
            // Authenticate
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Reset failed attempts on successful login
            user.resetFailedLoginAttempts();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Revoke old refresh tokens and save new one
            refreshTokenRepository.deleteAllByUserId(user.getId());
            saveRefreshToken(user, refreshToken);

            log.info("User logged in successfully: {}", user.getEmail());
            return buildAuthResponse(accessToken, refreshToken, user);

        } catch (Exception e) {
            // Increment failed attempts
            user.incrementFailedLoginAttempts();

            if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
                user.lockAccount(lockoutDurationMinutes);
                log.warn("Account locked due to failed login attempts: {}", user.getEmail());
            }

            userRepository.save(user);
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isExpired() || refreshToken.getRevoked()) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        log.info("Token refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(900000L) // 15 minutes
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenRepository.deleteAllByUserId(user.getId());
        log.info("User logged out: {}", email);
    }

    private void saveRefreshToken(User user, String token) {
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtService.getRefreshTokenExpiration() / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900000L) // 15 minutes
                .user(mapToUserDto(user))
                .build();
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}