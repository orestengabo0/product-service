package rca.restapi.year2.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.restapi.year2.userservice.dto.AddressDto;
import rca.restapi.year2.userservice.dto.requests.CreateAddressRequest;
import rca.restapi.year2.userservice.exception.ResourceNotFoundException;
import rca.restapi.year2.userservice.model.Address;
import rca.restapi.year2.userservice.model.User;
import rca.restapi.year2.userservice.repository.AddressRepository;
import rca.restapi.year2.userservice.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<AddressDto> getUserAddresses(String email) {
        log.info("Fetching addresses for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToAddressDto)
                .collect(Collectors.toList());
    }

    public AddressDto getAddressById(String email, Long addressId) {
        log.info("Fetching address {} for user: {}", addressId, email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Verify address belongs to user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Address not found");
        }

        return mapToAddressDto(address);
    }

    @Transactional
    public AddressDto createAddress(String email, CreateAddressRequest request) {
        log.info("Creating new address for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If this is set as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            List<Address> existingAddresses = addressRepository.findByUserId(user.getId());
            existingAddresses.forEach(addr -> addr.setIsDefault(false));
            addressRepository.saveAll(existingAddresses);
        }

        Address address = Address.builder()
                .user(user)
                .label(request.getLabel())
                .streetAddress(request.getStreetAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        address = addressRepository.save(address);
        log.info("Address created successfully for user: {}", email);

        return mapToAddressDto(address);
    }

    @Transactional
    public AddressDto updateAddress(String email, Long addressId, CreateAddressRequest request) {
        log.info("Updating address {} for user: {}", addressId, email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Verify address belongs to user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Address not found");
        }

        // If setting as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            List<Address> existingAddresses = addressRepository.findByUserId(user.getId());
            existingAddresses.forEach(addr -> {
                if (!addr.getId().equals(addressId)) {
                    addr.setIsDefault(false);
                }
            });
            addressRepository.saveAll(existingAddresses);
        }

        // Update address fields
        address.setLabel(request.getLabel());
        address.setStreetAddress(request.getStreetAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);

        address = addressRepository.save(address);
        log.info("Address updated successfully for user: {}", email);

        return mapToAddressDto(address);
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        log.info("Deleting address {} for user: {}", addressId, email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Verify address belongs to user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Address not found");
        }

        addressRepository.delete(address);
        log.info("Address deleted successfully for user: {}", email);
    }

    @Transactional
    public AddressDto setDefaultAddress(String email, Long addressId) {
        log.info("Setting default address {} for user: {}", addressId, email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Verify address belongs to user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Address not found");
        }

        // Unset all other defaults
        List<Address> existingAddresses = addressRepository.findByUserId(user.getId());
        existingAddresses.forEach(addr -> addr.setIsDefault(false));
        addressRepository.saveAll(existingAddresses);

        // Set this as default
        address.setIsDefault(true);
        address = addressRepository.save(address);

        log.info("Default address set successfully for user: {}", email);
        return mapToAddressDto(address);
    }

    public AddressDto getDefaultAddress(String email) {
        log.info("Fetching default address for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No default address found"));

        return mapToAddressDto(address);
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