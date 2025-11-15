package rca.restapi.year2.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rca.restapi.year2.userservice.model.Address;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}
