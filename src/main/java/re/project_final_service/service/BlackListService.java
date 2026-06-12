package re.project_final_service.service;

import org.springframework.stereotype.Service;

@Service
public interface BlackListService {
    public boolean existsByTokenString(String token);
}
