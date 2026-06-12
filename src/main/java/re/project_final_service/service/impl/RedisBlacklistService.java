package re.project_final_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public void blacklistToken(
            String token,
            long remainingMillis
    ) {

        redisTemplate.opsForValue().set(
                token,
                "BLACKLISTED",
                remainingMillis,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isBlacklisted(String token) {

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(token)
        );
    }
}