package rca.restapi.year2.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.restapi.year2.userservice.dto.*;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = "users", key = "#email")
    public UserDto getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToUserDto(user);
    }

    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToUserDto(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#email")
    public UserDto updateProfile(String email, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        user = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", email);

        return mapToUserDto(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#email")
    public void changePassword(String email, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", email);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#email")
    public void deleteAccount(String email) {
        log.info("Deleting account for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.delete(user);
        log.info("Account deleted successfully for user: {}", email);
    }

    // Admin methods
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination");
        return userRepository.findAll(pageable)
                .map(this::mapToUserDto);
    }

    public List<UserDto> getUsersByStatus(UserStatus status) {
        log.info("Fetching users by status: {}", status);
        return userRepository.findByStatus(status)
                .stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserDto updateUserStatus(Long userId, UserStatus status) {
        log.info("Updating status for user ID {}: {}", userId, status);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setStatus(status);
        user = userRepository.save(user);

        log.info("Status updated successfully for user ID: {}", userId);
        return mapToUserDto(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserDto updateUserRole(Long userId, Role role) {
        log.info("Updating role for user ID {}: {}", userId, role);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setRole(role);
        user = userRepository.save(user);

        log.info("Role updated successfully for user ID: {}", userId);
        return mapToUserDto(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Admin deleting user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.delete(user);
        log.info("User deleted successfully by admin: {}", userId);
    }

    // Helper method
    private UserDto mapToUserDto(User user) {
        List<AddressDto> addressDtos = user.getAddresses().stream()
                .map(this::mapToAddressDto)
                .collect(Collectors.toList());

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
                .addresses(addressDtos)
                .build();
    }

    private AddressDto mapToAddressDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .label(address.getLabel())
                .streetAddress(address.getStreetAddress())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .build();
    }
}