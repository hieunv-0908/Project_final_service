package re.project_final_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import re.project_final_service.model.entity.PasswordResetToken;
import re.project_final_service.model.entity.User;
import re.project_final_service.repo.PasswordResetTokenRepo;
import re.project_final_service.service.PasswordResetService;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepo tokenRepo;

    @Value("${app.password-reset.token-expiration-minutes:60}")
    private long tokenExpirationMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public PasswordResetToken createTokenForUser(User user) {
        // Revoke / delete any existing tokens for user
        tokenRepo.deleteByUser(user);

        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        PasswordResetToken prt = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plus(tokenExpirationMinutes, ChronoUnit.MINUTES))
                .revoked(false)
                .build();

        return tokenRepo.save(prt);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return tokenRepo.findByToken(token);
    }

    @Override
    public void revokeAllForUser(User user) {
        tokenRepo.deleteByUser(user);
    }
}

