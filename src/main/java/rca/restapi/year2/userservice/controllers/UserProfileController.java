package rca.restapi.year2.userservice.controllers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.userservice.dtos.UserProfileRequest;
import rca.restapi.year2.userservice.dtos.UserProfileResponse;
import rca.restapi.year2.userservice.mappers.UserProfileMapper;
import rca.restapi.year2.userservice.model.UserProfile;
import rca.restapi.year2.userservice.services.UserProfileService;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {
    private final UserProfileService service;
    @Qualifier("userProfileMapperImpl")
    private final UserProfileMapper mapper;

    public UserProfileController(UserProfileService service,
                                 @Qualifier("userProfileMapperImpl") UserProfileMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public UserProfileResponse getProfile(@RequestHeader("X-User-UUID") String uuidHeader) {
        return service.getUserProfile(uuidHeader);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestHeader("X-User-UUID") UUID uuid,
                                        @RequestBody UserProfileRequest request) {
        UserProfile profile = service.updateUserProfile(uuid, request);
        UserProfileResponse response = mapper.toResponse(profile);
        return ResponseEntity.ok(response);
    }
}
