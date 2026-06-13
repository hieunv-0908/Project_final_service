package re.project_final_service.service;

import org.springframework.stereotype.Service;
import re.project_final_service.model.dto.request.Job.JobNewPostingDto;
import re.project_final_service.model.entity.JobPosting;

public interface JobPostingService {
    public JobPosting postNewJobPosting(JobNewPostingDto jobNewPostingDto);
    public JobPosting updateJobPosting(JobPosting jobPosting);
    public JobPosting deleteJobPosting(JobPosting jobPosting);
}
