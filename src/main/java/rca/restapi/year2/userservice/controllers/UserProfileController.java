package rca.restapi.year2.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.userservice.dtos.UserProfileRequest;
import rca.restapi.year2.userservice.dtos.UserProfileResponse;
import rca.restapi.year2.userservice.services.UserProfileService;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService service;
    @GetMapping
    public UserProfileResponse getProfile(@RequestHeader("X-User-UUID") String uuidHeader) {
        return service.getUserProfile(uuidHeader);
    }

    @PutMapping
    public void updateProfile(@RequestHeader("X-User-UUID") UUID uuid,
                              @RequestBody UserProfileRequest request) {
        service.updateUserProfile(uuid, request);
    }
}
