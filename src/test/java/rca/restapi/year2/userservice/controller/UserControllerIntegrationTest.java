package rca.restapi.year2.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import rca.restapi.year2.userservice.dto.UserDto;
import rca.restapi.year2.userservice.dto.requests.ChangePasswordRequest;
import rca.restapi.year2.userservice.dto.requests.UpdateProfileRequest;
import rca.restapi.year2.userservice.service.UserService;
import rca.restapi.year2.userservice.util.TestDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private UserService userService;

        private UserDto userDto;
        private UpdateProfileRequest updateProfileRequest;
        private ChangePasswordRequest changePasswordRequest;

        @BeforeEach
        void setUp() {
                userDto = UserDto.builder()
                                .id(1L)
                                .username("testuser")
                                .email("test@example.com")
                                .firstName("Test")
                                .lastName("User")
                                .build();

                updateProfileRequest = TestDataBuilder.buildUpdateProfileRequest();
                changePasswordRequest = TestDataBuilder.buildChangePasswordRequest();
        }

        @Test
        @DisplayName("Should get current user successfully")
        @WithMockUser(username = "test@example.com")
        void testGetCurrentUser_Success() throws Exception {
                // Given
                when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);

                // When/Then
                mockMvc.perform(get("/users/me"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.username").value("testuser"));
        }

        @Test
        @DisplayName("Should return 403 for unauthenticated request")
        void testGetCurrentUser_Unauthenticated() throws Exception {
                // When/Then - Spring Security returns 403 Forbidden for authenticated endpoints
                mockMvc.perform(get("/users/me"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should update profile successfully")
        @WithMockUser(username = "test@example.com")
        void testUpdateProfile_Success() throws Exception {
                // Given
                UserDto updatedUser = UserDto.builder()
                                .id(1L)
                                .email("test@example.com")
                                .firstName("Updated")
                                .lastName("Name")
                                .build();
                when(userService.updateProfile(eq("test@example.com"), any(UpdateProfileRequest.class)))
                                .thenReturn(updatedUser);

                // When/Then
                mockMvc.perform(put("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateProfileRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.firstName").value("Updated"))
                                .andExpect(jsonPath("$.lastName").value("Name"));
        }

        @Test
        @DisplayName("Should return 400 for invalid update profile request")
        @WithMockUser(username = "test@example.com")
        void testUpdateProfile_InvalidRequest() throws Exception {
                // Given
                UpdateProfileRequest invalidRequest = UpdateProfileRequest.builder()
                                .phone("123") // Too short
                                .build();

                // When/Then
                mockMvc.perform(put("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should change password successfully")
        @WithMockUser(username = "test@example.com")
        void testChangePassword_Success() throws Exception {
                // When/Then
                mockMvc.perform(put("/users/me/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Password changed successfully"));
        }

        @Test
        @DisplayName("Should return 400 for invalid change password request")
        @WithMockUser(username = "test@example.com")
        void testChangePassword_InvalidRequest() throws Exception {
                // Given
                ChangePasswordRequest invalidRequest = ChangePasswordRequest.builder()
                                .currentPassword("")
                                .newPassword("short") // Too short
                                .build();

                // When/Then
                mockMvc.perform(put("/users/me/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should delete account successfully")
        @WithMockUser(username = "test@example.com")
        void testDeleteAccount_Success() throws Exception {
                // When/Then
                mockMvc.perform(delete("/users/me"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Account deleted successfully"));
        }

        @Test
        @DisplayName("Should get user by ID successfully")
        @WithMockUser(username = "test@example.com")
        void testGetUserById_Success() throws Exception {
                // Given
                when(userService.getUserById(1L)).thenReturn(userDto);

                // When/Then
                mockMvc.perform(get("/users/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.email").value("test@example.com"));
        }
}
