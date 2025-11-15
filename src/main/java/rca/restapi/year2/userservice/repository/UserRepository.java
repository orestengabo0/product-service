package rca.restapi.year2.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rca.restapi.year2.userservice.model.User;
import rca.restapi.year2.userservice.types.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByStatus(UserStatus status);

    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffDate")
    List<User> findUnverifiedUsersBefore(java.time.LocalDateTime cutoffDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(UserStatus status);
}
