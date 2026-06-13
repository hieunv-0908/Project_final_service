package re.project_final_service.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import re.project_final_service.model.entity.PasswordResetToken;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.Role;
import re.project_final_service.repo.PasswordResetTokenRepo;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho PasswordResetServiceImpl
 *
 * Cách chạy:
 *   ./gradlew test --tests PasswordResetServiceImplTest
 *
 * Coverage: createTokenForUser(), findByToken(), revokeAllForUser()
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PasswordResetServiceImpl Unit Tests")
class PasswordResetServiceImplTest {

    private PasswordResetServiceImpl passwordResetService;

    @MockitoBean
    private PasswordResetTokenRepo passwordResetTokenRepo;

    private User testUser;
    private PasswordResetToken testResetToken;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetServiceImpl(passwordResetTokenRepo);

        testUser = User.builder()
                .id(1L)
                .email("resettest@example.com")
                .username("resetuser")
                .role(Role.CANDIDATE)
                .isActive(true)
                .build();

        testResetToken = PasswordResetToken.builder()
                .id(1L)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(testUser)
                .revoked(false)
                .build();
    }

    @Test
    @DisplayName("✅ Thành công: Tạo token reset mật khẩu")
    void testCreateTokenForUserSuccess() {
        // Arrange
        when(passwordResetTokenRepo.save(any(PasswordResetToken.class))).thenReturn(testResetToken);

        // Act
        PasswordResetToken result = passwordResetService.createTokenForUser(testUser);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(testUser.getId(), result.getUser().getId());
        assertFalse(result.isRevoked());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
        verify(passwordResetTokenRepo, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("✅ Thành công: Tìm token reset bằng token string")
    void testFindByTokenSuccess() {
        // Arrange
        when(passwordResetTokenRepo.findByToken(testResetToken.getToken()))
                .thenReturn(Optional.of(testResetToken));

        // Act
        Optional<PasswordResetToken> result = passwordResetService.findByToken(testResetToken.getToken());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testResetToken.getToken(), result.get().getToken());
        verify(passwordResetTokenRepo, times(1)).findByToken(testResetToken.getToken());
    }

    @Test
    @DisplayName("❌ Lỗi: Token reset không tồn tại")
    void testFindByTokenNotFound() {
        // Arrange
        when(passwordResetTokenRepo.findByToken("nonexistent-token"))
                .thenReturn(Optional.empty());

        // Act
        Optional<PasswordResetToken> result = passwordResetService.findByToken("nonexistent-token");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("✅ Thành công: Thu hồi tất cả token của người dùng")
    void testRevokeAllForUserSuccess() {
        // Arrange
        doNothing().when(passwordResetTokenRepo)
                .deleteByUser(testUser);

        passwordResetService.revokeAllForUser(testUser);

        // Act
        passwordResetService.revokeAllForUser(testUser);

        // Assert
        verify(passwordResetTokenRepo, times(1))
                .deleteByUser(testUser);
    }

    @Test
    @DisplayName("✅ Thành công: Kiểm tra token chưa hết hạn")
    void testTokenNotExpired() {
        // Arrange
        PasswordResetToken validToken = PasswordResetToken.builder()
                .id(2L)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(testUser)
                .revoked(false)
                .build();

        // Act & Assert
        assertTrue(validToken.getExpiryDate().isAfter(Instant.now()));
        assertFalse(validToken.isRevoked());
    }
}

