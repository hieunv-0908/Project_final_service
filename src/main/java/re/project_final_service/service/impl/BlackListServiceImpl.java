package re.project_final_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import re.project_final_service.repo.TokenBlackListRepo;
import re.project_final_service.service.BlackListService;

@Service
@RequiredArgsConstructor
public class BlackListServiceImpl implements BlackListService {
    private final TokenBlackListRepo tokenBlackListRepo;

    @Override
    public boolean existsByTokenString(String token) {
        return tokenBlackListRepo.existsByTokenString(token);
    }
}
