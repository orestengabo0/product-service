package rca.restapi.year2.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.userservice.dto.UserDto;
import rca.restapi.year2.userservice.dto.requests.ChangePasswordRequest;
import rca.restapi.year2.userservice.dto.requests.UpdateProfileRequest;
import rca.restapi.year2.userservice.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        log.info("Fetching current user profile: {}", email);

        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        String email = authentication.getName();
        log.info("Updating profile for user: {}", email);

        UserDto updatedUser = userService.updateProfile(email, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        String email = authentication.getName();
        log.info("Password change request from user: {}", email);

        userService.changePassword(email, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteAccount(Authentication authentication) {
        String email = authentication.getName();
        log.info("Account deletion request from user: {}", email);

        userService.deleteAccount(email);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        log.info("Fetching user by ID: {}", userId);

        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}