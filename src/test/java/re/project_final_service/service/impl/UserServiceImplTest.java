package re.project_final_service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import re.project_final_service.model.dto.request.auth.UserDto;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.Role;
import re.project_final_service.repo.UserRepo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho UserServiceImpl
 *
 * Cách chạy:
 *   ./gradlew test --tests UserServiceImplTest
 *
 * Coverage: register(), changePassword()
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService;

    @MockitoBean
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDto validUserDto;

    @BeforeEach
    void setUp() {
        validUserDto = new UserDto(
                "testuser@example.com",
                "SecurePassword123",
                Role.CANDIDATE
        );
    }

    // ============ TEST REGISTER ============

    @Test
    @DisplayName("✅ Thành công: Đăng ký người dùng mới với role CANDIDATE")
    void testRegisterSuccess() {
        // Arrange
        when(userRepo.existsByEmail(validUserDto.getEmail())).thenReturn(false);
        User savedUser = User.builder()
                .id(1L)
                .email(validUserDto.getEmail())
                .username(validUserDto.getEmail())
                .role(validUserDto.getRole())
                .isActive(true)
                .passwordHash(passwordEncoder.encode(validUserDto.getPasswordHash()))
                .build();
        when(userRepo.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.register(validUserDto);

        // Assert
        assertNotNull(result);
        assertEquals("testuser@example.com", result.getEmail());
        assertEquals(Role.CANDIDATE, result.getRole());
        assertTrue(result.getIsActive());
        verify(userRepo, times(1)).existsByEmail(validUserDto.getEmail());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("❌ Lỗi: Email đã tồn tại trong hệ thống")
    void testRegisterWithDuplicateEmail() {
        // Arrange
        when(userRepo.existsByEmail(validUserDto.getEmail())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.register(validUserDto)
        );
        assertEquals("Email đã được đăng ký", exception.getMessage());
        verify(userRepo, times(1)).existsByEmail(validUserDto.getEmail());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    @DisplayName("❌ Lỗi: Email rỗng")
    void testRegisterWithEmptyEmail() {

        UserDto dto = new UserDto(
                "",
                "Password123",
                Role.CANDIDATE
        );

        assertThrows(Exception.class,
                () -> userService.register(dto));
    }

    @Test
    @DisplayName("❌ Lỗi: Không được đăng ký với role ADMIN")
    void testRegisterWithAdminRole() {
        // Arrange
        UserDto adminDto = new UserDto(
                "admin@example.com",
                "AdminPassword123",
                Role.ADMIN
        );
        when(userRepo.existsByEmail(adminDto.getEmail())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.register(adminDto)
        );
        assertEquals("Không được đăng ký với role ADMIN", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    @DisplayName("✅ Thành công: Đổi mật khẩu người dùng")
    void testChangePasswordSuccess() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .passwordHash(passwordEncoder.encode("OldPassword123"))
                .role(Role.CANDIDATE)
                .build();

        String newPassword = "NewPassword123";
        User updatedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .passwordHash(passwordEncoder.encode(newPassword))
                .role(Role.CANDIDATE)
                .build();

        when(userRepo.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.changePassword(user, newPassword);

        // Assert
        assertNotNull(result);
        assertNotEquals(passwordEncoder.encode("OldPassword123"), result.getPasswordHash());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("✅ Thành công: Đăng ký với role EMPLOYER")
    void testRegisterWithEmployerRole() {
        // Arrange
        UserDto employerDto = new UserDto(
                "employer@company.com",
                "EmployerPassword123",
                Role.EMPLOYER
        );
        when(userRepo.existsByEmail(employerDto.getEmail())).thenReturn(false);
        User savedUser = User.builder()
                .id(2L)
                .email(employerDto.getEmail())
                .username(employerDto.getEmail())
                .role(employerDto.getRole())
                .isActive(true)
                .build();
        when(userRepo.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.register(employerDto);

        // Assert
        assertNotNull(result);
        assertEquals(Role.EMPLOYER, result.getRole());
        verify(userRepo, times(1)).save(any(User.class));
    }
}

