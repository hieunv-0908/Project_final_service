package re.project_final_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import re.project_final_service.model.entity.JobPosting;

public interface JobPostingRepo extends JpaRepository<JobPosting, Long> {
	org.springframework.data.domain.Page<JobPosting> findByTitleContainingIgnoreCase(String title, org.springframework.data.domain.Pageable pageable);
}
