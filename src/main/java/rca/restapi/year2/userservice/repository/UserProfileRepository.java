package rca.restapi.year2.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rca.restapi.year2.userservice.model.UserProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUuid(UUID uuid);
}
