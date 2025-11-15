package rca.restapi.year2.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.userservice.dto.AddressDto;
import rca.restapi.year2.userservice.dto.requests.CreateAddressRequest;
import rca.restapi.year2.userservice.service.AddressService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/me/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressDto>> getUserAddresses(Authentication authentication) {
        String email = authentication.getName();
        log.info("Fetching addresses for user: {}", email);

        List<AddressDto> addresses = addressService.getUserAddresses(email);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDto> getAddressById(
            Authentication authentication,
            @PathVariable Long addressId) {
        String email = authentication.getName();
        log.info("Fetching address {} for user: {}", addressId, email);

        AddressDto address = addressService.getAddressById(email, addressId);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/default")
    public ResponseEntity<AddressDto> getDefaultAddress(Authentication authentication) {
        String email = authentication.getName();
        log.info("Fetching default address for user: {}", email);

        AddressDto address = addressService.getDefaultAddress(email);
        return ResponseEntity.ok(address);
    }

    @PostMapping
    public ResponseEntity<AddressDto> createAddress(
            Authentication authentication,
            @Valid @RequestBody CreateAddressRequest request) {
        String email = authentication.getName();
        log.info("Creating new address for user: {}", email);

        AddressDto address = addressService.createAddress(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDto> updateAddress(
            Authentication authentication,
            @PathVariable Long addressId,
            @Valid @RequestBody CreateAddressRequest request) {
        String email = authentication.getName();
        log.info("Updating address {} for user: {}", addressId, email);

        AddressDto address = addressService.updateAddress(email, addressId, request);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<AddressDto> setDefaultAddress(
            Authentication authentication,
            @PathVariable Long addressId) {
        String email = authentication.getName();
        log.info("Setting default address {} for user: {}", addressId, email);

        AddressDto address = addressService.setDefaultAddress(email, addressId);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Map<String, String>> deleteAddress(
            Authentication authentication,
            @PathVariable Long addressId) {
        String email = authentication.getName();
        log.info("Deleting address {} for user: {}", addressId, email);

        addressService.deleteAddress(email, addressId);
        return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
    }
}