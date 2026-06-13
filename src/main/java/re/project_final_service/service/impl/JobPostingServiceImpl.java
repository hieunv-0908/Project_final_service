package re.project_final_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import re.project_final_service.model.dto.request.Job.JobNewPostingDto;
import re.project_final_service.model.entity.JobPosting;
import re.project_final_service.model.entity.User;
import re.project_final_service.repo.JobPostingRepo;
import re.project_final_service.repo.UserRepo;
import re.project_final_service.service.JobPostingService;

@Service
@RequiredArgsConstructor
public class JobPostingServiceImpl implements JobPostingService {
    public final JobPostingRepo jobPostingRepo;
    private final UserRepo userRepo;

    @Override
    public JobPosting postNewJobPosting(JobNewPostingDto jobNewPostingDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User employer = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Employer không tồn tại"));
        JobPosting jobPosting = JobPosting.builder()
                .title(jobNewPostingDto.getTitle())
                .description(jobNewPostingDto.getDescription())
                .salaryRange(jobNewPostingDto.getSalaryRange())
                .employer(employer)
                .build();
        jobPostingRepo.save(jobPosting);
        return jobPosting;
    }

    @Override
    public JobPosting updateJobPosting(JobPosting jobPosting) {
        return null;
    }

    @Override
    public JobPosting deleteJobPosting(JobPosting jobPosting) {
        return null;
    }
}
