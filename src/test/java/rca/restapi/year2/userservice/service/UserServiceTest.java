package rca.restapi.year2.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import rca.restapi.year2.userservice.dto.UserDto;
import rca.restapi.year2.userservice.dto.requests.ChangePasswordRequest;
import rca.restapi.year2.userservice.dto.requests.UpdateProfileRequest;
import rca.restapi.year2.userservice.exception.ResourceNotFoundException;
import rca.restapi.year2.userservice.exception.UnauthorizedException;
import rca.restapi.year2.userservice.model.Address;
import rca.restapi.year2.userservice.model.User;
import rca.restapi.year2.userservice.repository.AddressRepository;
import rca.restapi.year2.userservice.repository.UserRepository;
import rca.restapi.year2.userservice.types.Role;
import rca.restapi.year2.userservice.types.UserStatus;
import rca.restapi.year2.userservice.util.TestDataBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildUser();
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void testGetUserByEmail_Success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserByEmail(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw exception when user not found by email")
    void testGetUserByEmail_NotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void testGetUserById_NotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should update profile successfully")
    void testUpdateProfile_Success() {
        // Given
        String email = "test@example.com";
        UpdateProfileRequest request = TestDataBuilder.buildUpdateProfileRequest();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.updateProfile(email, request);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update profile with partial fields")
    void testUpdateProfile_PartialUpdate() {
        // Given
        String email = "test@example.com";
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Updated")
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateProfile(email, request);

        // Then
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when updating profile for non-existent user")
    void testUpdateProfile_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        UpdateProfileRequest request = TestDataBuilder.buildUpdateProfileRequest();
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.updateProfile(email, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should change password successfully")
    void testChangePassword_Success() {
        // Given
        String email = "test@example.com";
        ChangePasswordRequest request = TestDataBuilder.buildChangePasswordRequest();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getCurrentPassword(), testUser.getPassword()))
                .thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.changePassword(email, request);

        // Then
        verify(userRepository).findByEmail(email);
        // testUser.getPassword() returns "$2a$12$encodedPasswordHash" from
        // TestDataBuilder
        verify(passwordEncoder).matches("oldPassword", "$2a$12$encodedPasswordHash");
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when current password is incorrect")
    void testChangePassword_IncorrectCurrentPassword() {
        // Given
        String email = "test@example.com";
        ChangePasswordRequest request = TestDataBuilder.buildChangePasswordRequest();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getCurrentPassword(), testUser.getPassword()))
                .thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.changePassword(email, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Current password is incorrect");

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(request.getCurrentPassword(), testUser.getPassword());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete account successfully")
    void testDeleteAccount_Success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // When
        userService.deleteAccount(email);

        // Then
        verify(userRepository).findByEmail(email);
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void testGetAllUsers_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = Arrays.asList(testUser, TestDataBuilder.buildAdminUser());
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserDto> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get users by status")
    void testGetUsersByStatus_Success() {
        // Given
        UserStatus status = UserStatus.ACTIVE;
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByStatus(status)).thenReturn(users);

        // When
        List<UserDto> result = userService.getUsersByStatus(status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(userRepository).findByStatus(status);
    }

    @Test
    @DisplayName("Should update user status successfully")
    void testUpdateUserStatus_Success() {
        // Given
        Long userId = 1L;
        UserStatus newStatus = UserStatus.SUSPENDED;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.updateUserStatus(userId, newStatus);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update user role successfully")
    void testUpdateUserRole_Success() {
        // Given
        Long userId = 1L;
        Role newRole = Role.ADMIN;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.updateUserRole(userId, newRole);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("Should map user with addresses correctly")
    void testMapToUserDto_WithAddresses() {
        // Given
        Address address = TestDataBuilder.buildAddress();
        testUser.getAddresses().add(address);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserByEmail(testUser.getEmail());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAddresses()).isNotEmpty();
        assertThat(result.getAddresses().get(0).getStreetAddress()).isEqualTo(address.getStreetAddress());
    }
}
