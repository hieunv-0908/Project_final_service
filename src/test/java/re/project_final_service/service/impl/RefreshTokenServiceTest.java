package re.project_final_service.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import re.project_final_service.model.entity.RefreshToken;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.Role;
import re.project_final_service.repo.RefreshTokenRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho RefreshTokenService
 *
 * Cách chạy:
 *   ./gradlew test --tests RefreshTokenServiceTest
 *
 * Coverage: createRefreshToken(), findByToken(), delete()
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RefreshTokenService Unit Tests")
class RefreshTokenServiceTest {

    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;
    private RefreshToken testToken;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);

        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .username("testuser")
                .role(Role.CANDIDATE)
                .isActive(true)
                .build();

        testToken = RefreshToken.builder()
                .id(1L)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(86400 * 7))
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("✅ Thành công: Tạo RefreshToken mới")
    void testCreateRefreshTokenSuccess() {
        // Arrange
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testToken);

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(testUser);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(testUser.getId(), result.getUser().getId());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("✅ Thành công: Tìm RefreshToken bằng token string")
    void testFindByTokenSuccess() {
        // Arrange
        when(refreshTokenRepository.findByToken(testToken.getToken()))
                .thenReturn(Optional.of(testToken));

        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken(testToken.getToken());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testToken.getToken(), result.get().getToken());
        assertEquals(testUser.getId(), result.get().getUser().getId());
        verify(refreshTokenRepository, times(1)).findByToken(testToken.getToken());
    }

    @Test
    @DisplayName("❌ Lỗi: Token không tồn tại trong hệ thống")
    void testFindByTokenNotFound() {
        // Arrange
        when(refreshTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken("invalid-token");

        // Assert
        assertFalse(result.isPresent());
        verify(refreshTokenRepository, times(1)).findByToken("invalid-token");
    }

    @Test
    @DisplayName("✅ Thành công: Xóa RefreshToken")
    void testDeleteTokenSuccess() {
        // Arrange
        doNothing().when(refreshTokenRepository).delete(testToken);

        // Act
        refreshTokenService.delete(testToken);

        // Assert
        verify(refreshTokenRepository, times(1)).delete(testToken);
    }

    @Test
    @DisplayName("✅ Thành công: Xóa tất cả token của người dùng")
    void testDeleteByUserSuccess() {
        // Arrange
        doNothing().when(refreshTokenRepository).deleteByUser(testUser);

        // Act
        refreshTokenService.deleteByUser(testUser);

        // Assert
        verify(refreshTokenRepository, times(1)).deleteByUser(testUser);
    }
}

