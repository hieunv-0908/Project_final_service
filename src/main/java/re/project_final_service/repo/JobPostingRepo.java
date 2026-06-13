package re.project_final_service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import re.project_final_service.model.entity.Application;
import re.project_final_service.model.entity.JobPosting;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.JobStatusEnum;

import java.util.Optional;

public interface JobPostingRepo extends JpaRepository<JobPosting, Long> {
	Page<JobPosting> findByTitleContainingIgnoreCase(String title, org.springframework.data.domain.Pageable pageable);
	Page<JobPosting> findByEmployer(
			User employer,
			Pageable pageable
	);
	Page<JobPosting> findByEmployerAndDeletedFalse(
			User employer,
			Pageable pageable
	);
	Optional<JobPosting> findByIdAndEmployer(
			Long id,
			User employer
	);
	Optional<JobPosting> findByIdAndEmployerAndDeletedFalse(
			Long id,
			User employer
	);

	Page<JobPosting> findByStatusAndDeletedFalse(
			JobStatusEnum status,
			Pageable pageable
	);

	Optional<JobPosting>
	findByIdAndStatusAndDeletedFalse(
			Long id,
			JobStatusEnum status
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
}
