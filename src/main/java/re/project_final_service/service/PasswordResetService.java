package re.project_final_service.service;

import re.project_final_service.model.entity.PasswordResetToken;
import re.project_final_service.model.entity.User;

import java.util.Optional;

public interface PasswordResetService {
    PasswordResetToken createTokenForUser(User user);
    Optional<PasswordResetToken> findByToken(String token);
    void revokeAllForUser(User user);
}

