package rca.restapi.year2.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rca.restapi.year2.userservice.dto.AddressDto;
import rca.restapi.year2.userservice.dto.requests.CreateAddressRequest;
import rca.restapi.year2.userservice.exception.ResourceNotFoundException;
import rca.restapi.year2.userservice.model.Address;
import rca.restapi.year2.userservice.model.User;
import rca.restapi.year2.userservice.repository.AddressRepository;
import rca.restapi.year2.userservice.repository.UserRepository;
import rca.restapi.year2.userservice.util.TestDataBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService Unit Tests")
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User testUser;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildUser();
        testAddress = TestDataBuilder.buildAddress();
        testAddress.setUser(testUser);
    }

    @Test
    @DisplayName("Should get user addresses successfully")
    void testGetUserAddresses_Success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(addressRepository.findByUserId(testUser.getId()))
                .thenReturn(Arrays.asList(testAddress));

        // When
        List<AddressDto> result = addressService.getUserAddresses(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(userRepository).findByEmail(email);
        verify(addressRepository).findByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testGetUserAddresses_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> addressService.getUserAddresses(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("Should get address by ID successfully")
    void testGetAddressById_Success() {
        // Given
        String email = "test@example.com";
        Long addressId = 1L;
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(testAddress));

        // When
        AddressDto result = addressService.getAddressById(email, addressId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(addressId);
        verify(userRepository).findByEmail(email);
        verify(addressRepository).findById(addressId);
    }

    @Test
    @DisplayName("Should throw exception when address belongs to different user")
    void testGetAddressById_DifferentUser() {
        // Given
        String email = "test@example.com";
        Long addressId = 1L;
        User differentUser = TestDataBuilder.buildAdminUser();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(testAddress));
        testAddress.setUser(differentUser);

        // When/Then
        assertThatThrownBy(() -> addressService.getAddressById(email, addressId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Address not found");
    }

    @Test
    @DisplayName("Should create address successfully")
    void testCreateAddress_Success() {
        // Given
        String email = "test@example.com";
        CreateAddressRequest request = CreateAddressRequest.builder()
                .label("Home")
                .streetAddress("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .isDefault(true)
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // When
        AddressDto result = addressService.createAddress(email, request);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findByEmail(email);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("Should delete address successfully")
    void testDeleteAddress_Success() {
        // Given
        String email = "test@example.com";
        Long addressId = 1L;
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(testAddress));
        doNothing().when(addressRepository).delete(testAddress);

        // When
        addressService.deleteAddress(email, addressId);

        // Then
        verify(userRepository).findByEmail(email);
        verify(addressRepository).findById(addressId);
        verify(addressRepository).delete(testAddress);
    }
}

