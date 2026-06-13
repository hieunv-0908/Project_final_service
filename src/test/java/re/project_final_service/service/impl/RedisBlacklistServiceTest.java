package re.project_final_service.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho RedisBlacklistService
 *
 * Cách chạy:
 *   ./gradlew test --tests RedisBlacklistServiceTest
 *
 * Coverage: blacklistToken(), isBlacklisted()
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RedisBlacklistService Unit Tests")
class RedisBlacklistServiceTest {

    private RedisBlacklistService redisBlacklistService;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    private String testToken;

    @BeforeEach
    void setUp() {
        redisBlacklistService = new RedisBlacklistService(redisTemplate);
        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.testToken";
    }

    @Test
    @DisplayName("✅ Thành công: Đưa token vào blacklist")
    void testBlacklistTokenSuccess() {
        // Arrange
        long remainingTime = 3600000L; // 1 giờ

        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));

        // Act
        redisBlacklistService.blacklistToken(testToken, remainingTime);

        // Assert
        verify(redisTemplate, times(1)).opsForValue();
    }

    @Test
    @DisplayName("✅ Thành công: Kiểm tra token bị blacklist")
    void testIsBlacklistedTokenFound() {
        // Arrange
        when(redisTemplate.hasKey(testToken)).thenReturn(true);

        // Act
        boolean result = redisBlacklistService.isBlacklisted(testToken);

        // Assert
        assertTrue(result);
        verify(redisTemplate, times(1)).hasKey(testToken);
    }

    @Test
    @DisplayName("✅ Thành công: Kiểm tra token chưa bị blacklist")
    void testIsBlacklistedTokenNotFound() {
        // Arrange
        when(redisTemplate.hasKey(testToken)).thenReturn(false);

        // Act
        boolean result = redisBlacklistService.isBlacklisted(testToken);

        // Assert
        assertFalse(result);
        verify(redisTemplate, times(1)).hasKey(testToken);
    }

    @Test
    @DisplayName("✅ Thành công: Blacklist token với thời gian hết hạn dài")
    void testBlacklistTokenWithLongTTL() {
        // Arrange
        long longRemainingTime = 86400000L; // 24 giờ

        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));

        // Act
        redisBlacklistService.blacklistToken(testToken, longRemainingTime);

        // Assert
        verify(redisTemplate, times(1)).opsForValue();
    }

    @Test
    @DisplayName("✅ Thành công: Blacklist token với thời gian hết hạn ngắn")
    void testBlacklistTokenWithShortTTL() {
        // Arrange
        long shortRemainingTime = 60000L; // 1 phút

        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));

        // Act
        redisBlacklistService.blacklistToken(testToken, shortRemainingTime);

        // Assert
        verify(redisTemplate, times(1)).opsForValue();
    }
}

