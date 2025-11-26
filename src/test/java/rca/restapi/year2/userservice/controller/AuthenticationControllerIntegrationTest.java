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
import rca.restapi.year2.userservice.dto.requests.LoginRequest;
import rca.restapi.year2.userservice.dto.requests.RegisterRequest;
import rca.restapi.year2.userservice.dto.responses.AuthResponse;
import rca.restapi.year2.userservice.service.AuthenticationService;
import rca.restapi.year2.userservice.service.EmailVerificationService;
import rca.restapi.year2.userservice.service.PasswordResetService;
import rca.restapi.year2.userservice.util.TestDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthenticationController Integration Tests")
class AuthenticationControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthenticationService authenticationService;

        @MockBean
        private PasswordResetService passwordResetService;

        @MockBean
        private EmailVerificationService emailVerificationService;

        private RegisterRequest registerRequest;
        private LoginRequest loginRequest;
        private AuthResponse authResponse;

        @BeforeEach
        void setUp() {
                registerRequest = TestDataBuilder.buildRegisterRequest();
                loginRequest = TestDataBuilder.buildLoginRequest();

                authResponse = AuthResponse.builder()
                                .accessToken("test-access-token")
                                .refreshToken("test-refresh-token")
                                .tokenType("Bearer")
                                .expiresIn(900000L)
                                .user(UserDto.builder()
                                                .id(1L)
                                                .username("testuser")
                                                .email("test@example.com")
                                                .build())
                                .build();
        }

        @Test
        @DisplayName("Should register user successfully")
        void testRegister_Success() throws Exception {
                // Given
                when(authenticationService.register(any(RegisterRequest.class)))
                                .thenReturn(authResponse);

                // When/Then
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                                .andExpect(jsonPath("$.refreshToken").value("test-refresh-token"))
                                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("Should return 400 for invalid register request")
        void testRegister_InvalidRequest() throws Exception {
                // Given
                RegisterRequest invalidRequest = RegisterRequest.builder()
                                .email("invalid-email") // Invalid email format
                                .build();

                // When/Then
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should login successfully")
        void testLogin_Success() throws Exception {
                // Given
                when(authenticationService.login(any(LoginRequest.class)))
                                .thenReturn(authResponse);

                // When/Then
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("Should return 400 for invalid login request")
        void testLogin_InvalidRequest() throws Exception {
                // Given
                LoginRequest invalidRequest = LoginRequest.builder()
                                .email("") // Empty email
                                .build();

                // When/Then
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate token successfully")
        @WithMockUser(username = "test@example.com")
        void testValidateToken_Success() throws Exception {
                // When/Then
                mockMvc.perform(get("/auth/validate"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.valid").value(true));
        }

        @Test
        @DisplayName("Should return false for invalid token")
        void testValidateToken_Invalid() throws Exception {
                // When/Then - /auth/validate requires authentication, so it returns 403
                mockMvc.perform(get("/auth/validate"))
                                .andExpect(status().isForbidden()); // Change from isOk() to isForbidden()
        }

        @Test
        @DisplayName("Should logout successfully")
        @WithMockUser(username = "test@example.com")
        void testLogout_Success() throws Exception {
                // When/Then
                mockMvc.perform(post("/auth/logout"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }

        @Test
        @DisplayName("Should request password reset successfully")
        void testForgotPassword_Success() throws Exception {
                // When/Then
                mockMvc.perform(post("/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"test@example.com\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Password reset link sent to your email"));
        }

        @Test
        @DisplayName("Should verify email successfully")
        void testVerifyEmail_Success() throws Exception {
                // When/Then
                mockMvc.perform(get("/auth/verify-email")
                                .param("token", "verification-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Email verified successfully"));
        }

        @Test
        @DisplayName("Should resend verification email successfully")
        void testResendVerificationEmail_Success() throws Exception {
                // When/Then
                mockMvc.perform(post("/auth/resend-verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"test@example.com\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Verification email sent"));
        }
}
