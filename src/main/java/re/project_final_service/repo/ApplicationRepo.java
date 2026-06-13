package re.project_final_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import re.project_final_service.model.entity.Application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import re.project_final_service.model.entity.JobPosting;
import re.project_final_service.model.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepo extends JpaRepository<Application, Long> {
	List<Application> findByJobPostingId(Long jobPostingId);
	Page<Application> findByJobPosting(
			JobPosting jobPosting,
			Pageable pageable
	);
	Optional<Application>
	findByCandidateAndJobPosting(
			User candidate,
			JobPosting job
	);
	Page<Application>
	findByCandidate(
			User candidate,
			Pageable pageable
	);
	Page<Application> findByJobPostingId(Long jobPostingId, Pageable pageable);

}
