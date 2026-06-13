package re.project_final_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import re.project_final_service.model.dto.request.auth.UserDto;
import re.project_final_service.model.dto.request.auth.UserDtoLogin;
import re.project_final_service.model.entity.RefreshToken;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.Role;
import re.project_final_service.repo.UserRepo;
import re.project_final_service.security.JwtTokenProvider;
import re.project_final_service.service.PasswordResetService;
import re.project_final_service.service.impl.RedisBlacklistService;
import re.project_final_service.service.impl.RefreshTokenService;
import re.project_final_service.service.impl.UserServiceImpl;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Test cho AuthController
 *
 * Cách chạy:
 *   ./gradlew test --tests AuthControllerTest
 *
 * Coverage: register(), login(), logout()
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserServiceImpl userService;

    @MockitoBean
    private UserRepo userRepo;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private RedisBlacklistService redisBlacklistService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDto validUserDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        validUserDto = new UserDto(
                "testuser@example.com",
                "SecurePassword123",
                Role.CANDIDATE
        );

        testUser = User.builder()
                .id(1L)
                .email("testuser@example.com")
                .username("testuser")
                .passwordHash(passwordEncoder.encode("SecurePassword123"))
                .role(Role.CANDIDATE)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("✅ Thành công: Đăng ký tài khoản CANDIDATE")
    void testRegisterSuccess() throws Exception {
        // Arrange
        when(userService.register(any(UserDto.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng ký thành công"))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(userService, times(1)).register(any(UserDto.class));
    }

    @Test
    @DisplayName("❌ Lỗi: Email đã tồn tại")
    void testRegisterWithDuplicateEmail() throws Exception {
        // Arrange
        when(userService.register(any(UserDto.class)))
                .thenThrow(new RuntimeException("Email đã được đăng ký"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().is4xxClientError());

        verify(userService, times(1)).register(any(UserDto.class));
    }

    @Test
    @DisplayName("✅ Thành công: Đăng nhập tài khoản")
    void testLoginSuccess() throws Exception {
        // Arrange
        UserDtoLogin loginDto = new UserDtoLogin(
                "testuser@example.com",
                "SecurePassword123"
        );

        when(userRepo.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("valid-access-token");

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("valid-refresh-token")
                .user(testUser)
                .build();

        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"));

        verify(userRepo, times(1)).findByEmail(anyString());
        verify(jwtTokenProvider, times(1)).generateAccessToken(any());
        verify(refreshTokenService, times(1)).createRefreshToken(any(User.class));
    }

    @Test
    @DisplayName("✅ Thành công: Đăng ký với role EMPLOYER")
    void testRegisterWithEmployerRole() throws Exception {
        // Arrange
        UserDto employerDto = new UserDto(
                "employer@company.com",
                "EmployerPassword123",
                Role.EMPLOYER
        );

        User employerUser = User.builder()
                .id(2L)
                .email("employer@company.com")
                .username("employer")
                .role(Role.EMPLOYER)
                .isActive(true)
                .build();

        when(userService.register(any(UserDto.class))).thenReturn(employerUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("EMPLOYER"));

        verify(userService, times(1)).register(any(UserDto.class));
    }

    @Test
    @DisplayName("✅ Thành công: Đăng xuất người dùng")
    void testLogoutSuccess() throws Exception {
        // Arrange
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.validToken";

        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getExpirationDate(validToken))
                .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(jwtTokenProvider.getUsernameFromJwt(validToken))
                .thenReturn("testuser@example.com");
        when(userRepo.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(redisBlacklistService).blacklistToken(anyString(), anyLong());
        doNothing().when(refreshTokenService).deleteByUser(any(User.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công"));

        verify(redisBlacklistService, times(1)).blacklistToken(anyString(), anyLong());
        verify(refreshTokenService, times(1)).deleteByUser(any(User.class));
    }
}

