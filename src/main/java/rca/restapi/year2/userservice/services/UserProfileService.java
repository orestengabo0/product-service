package rca.restapi.year2.userservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rca.restapi.year2.userservice.client.AuthClient;
import rca.restapi.year2.userservice.dtos.UserCreatedEvent;
import rca.restapi.year2.userservice.dtos.UserProfileRequest;
import rca.restapi.year2.userservice.dtos.UserProfileResponse;
import rca.restapi.year2.userservice.model.UserProfile;
import rca.restapi.year2.userservice.repository.UserProfileRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final AuthClient authClient;

    public UserProfileResponse getUserProfile(String uuidHeader){
        UUID uuid = UUID.fromString(uuidHeader);
        if(!authClient.userExists(uuid)){
            throw new RuntimeException("User does not exist");
        }
        UserProfile profile = userProfileRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        return UserProfileResponse.builder()
                .username(profile.getUsername())
                .email(profile.getEmail())
                .build();
    }

    public UserProfile updateUserProfile(UUID uuid, UserProfileRequest request){
        if(!authClient.userExists(uuid)){
            throw new RuntimeException("User does not exist");
        }
        UserProfile profile = userProfileRepository.findByUuid(uuid)
                .orElse(UserProfile.builder().uuid(uuid).build());
        profile.setUsername(request.getUsername());
        profile.setEmail(request.getEmail());

        userProfileRepository.save(profile);
        return profile;
    }

    public void createUserProfile(UserCreatedEvent event){
        UserProfile profile = new UserProfile();
        profile.setUuid(UUID.fromString(event.getUuid()));
        profile.setUsername(event.getUsername());
        profile.setEmail(event.getEmail());

        userProfileRepository.save(profile);
    }
}
