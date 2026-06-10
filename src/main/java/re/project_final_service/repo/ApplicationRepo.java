package re.project_final_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import re.project_final_service.model.entity.Application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ApplicationRepo extends JpaRepository<Application, Long> {
	List<Application> findByJobPostingId(Long jobPostingId);
	Page<Application> findByJobPostingId(Long jobPostingId, Pageable pageable);
}
