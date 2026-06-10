package re.project_final_service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.Role;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
	java.util.Optional<User> findByEmail(String email);

	Page<User> findByEmailContainingIgnoreCase(String email, org.springframework.data.domain.Pageable pageable);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByRole(Role role);
}
