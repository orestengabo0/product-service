package rca.restapi.year2.userservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.userservice.dto.UserDto;
import rca.restapi.year2.userservice.types.Role;
import rca.restapi.year2.userservice.types.UserStatus;
import rca.restapi.year2.userservice.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        log.info("Admin: Fetching all users with pagination");
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        log.info("Admin: Fetching user by ID: {}", userId);
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserDto>> getUsersByStatus(@PathVariable UserStatus status) {
        log.info("Admin: Fetching users by status: {}", status);
        List<UserDto> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        UserStatus status = UserStatus.valueOf(request.get("status"));
        log.info("Admin: Updating user {} status to: {}", userId, status);

        UserDto user = userService.updateUserStatus(userId, status);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        Role role = Role.valueOf(request.get("role"));
        log.info("Admin: Updating user {} role to: {}", userId, role);

        UserDto user = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        log.info("Admin: Deleting user: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}