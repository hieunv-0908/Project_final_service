package re.project_final_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import re.project_final_service.model.entity.TokenBlacklist;

@Repository
public interface TokenBlackListRepo extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByTokenString(String token);
}
