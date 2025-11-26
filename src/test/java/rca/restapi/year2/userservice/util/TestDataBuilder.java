package rca.restapi.year2.userservice.util;

import rca.restapi.year2.userservice.dto.requests.*;
import rca.restapi.year2.userservice.model.Address;
import rca.restapi.year2.userservice.model.RefreshToken;
import rca.restapi.year2.userservice.model.User;
import rca.restapi.year2.userservice.types.Role;
import rca.restapi.year2.userservice.types.UserStatus;

import java.time.LocalDateTime;
import java.util.HashSet;

public class TestDataBuilder {

    public static User buildUser() {
        return User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("$2a$12$encodedPasswordHash")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .failedLoginAttempts(0)
                .accountLockedUntil(null)
                .createdAt(LocalDateTime.now())
                .addresses(new HashSet<>())
                .build();
    }

    public static User buildAdminUser() {
        return User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .password("$2a$12$encodedPasswordHash")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .addresses(new HashSet<>())
                .build();
    }

    public static RegisterRequest buildRegisterRequest() {
        return RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .phone("1234567890")
                .build();
    }

    public static LoginRequest buildLoginRequest() {
        return LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    public static UpdateProfileRequest buildUpdateProfileRequest() {
        return UpdateProfileRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .phone("9876543210")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();
    }

    public static ChangePasswordRequest buildChangePasswordRequest() {
        return ChangePasswordRequest.builder()
                .currentPassword("oldPassword")
                .newPassword("newPassword123")
                .build();
    }

    public static Address buildAddress() {
        Address address = Address.builder()
                .id(1L)
                .label("Home")
                .streetAddress("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .isDefault(true)
                .build();
        address.setUser(buildUser());
        return address;
    }

    public static RefreshToken buildRefreshToken(User user) {
        return RefreshToken.builder()
                .id(1L)
                .user(user)
                .token("refresh-token-123")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
    }

    public static RefreshTokenRequest buildRefreshTokenRequest() {
        return RefreshTokenRequest.builder()
                .refreshToken("refresh-token-123")
                .build();
    }

    public static PasswordResetRequest buildPasswordResetRequest() {
        return PasswordResetRequest.builder()
                .email("test@example.com")
                .build();
    }

    public static PasswordResetConfirm buildPasswordResetConfirm() {
        return PasswordResetConfirm.builder()
                .token("reset-token-123")
                .newPassword("newPassword123")
                .build();
    }
}

