package re.project_final_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import re.project_final_service.model.dto.request.Job.JobNewPostingDto;
import re.project_final_service.model.dto.response.APIDataResponse;
import re.project_final_service.model.entity.Application;
import re.project_final_service.model.entity.JobPosting;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.ApplicationStatusEnum;
import re.project_final_service.repo.ApplicationRepo;
import re.project_final_service.repo.JobPostingRepo;
import re.project_final_service.repo.UserRepo;
import re.project_final_service.service.JobPostingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/employee", "/api/v1/employer"})
public class EmployerController {
    private final JobPostingService jobPostingService;
    private final JobPostingRepo jobPostingRepo;
    private final ApplicationRepo applicationRepo;
    private final UserRepo userRepo;

    @PostMapping("/jobs")
    public ResponseEntity<APIDataResponse<JobPosting>> postNewJobPosting(
            @Valid @RequestBody JobNewPostingDto jobNewPostingDto
            ) {

        return ResponseEntity.status(201).body(
                new APIDataResponse<JobPosting>(
                        jobPostingService.postNewJobPosting(jobNewPostingDto),
                        "Tạo tin thành công, đang chờ kiểm duyệt",
                        true,
                        HttpStatusCode.valueOf(201)
                )
        );
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobPosting>> listMyJobs() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User employer = userRepo.findByEmail(username).orElse(null);
        if (employer == null) {
            return ResponseEntity.notFound().build();
        }
        List<JobPosting> jobs = jobPostingRepo.findAll().stream()
                .filter(j -> j.getEmployer() != null && j.getEmployer().getId().equals(employer.getId()))
                .toList();
        return ResponseEntity.ok(jobs);
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<JobPosting> updateJob(@PathVariable Long id, @RequestBody JobPosting payload) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User employer = userRepo.findByEmail(username).orElse(null);

        var jobOpt = jobPostingRepo.findById(id);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var job = jobOpt.get();
            if (job.getEmployer() == null || !job.getEmployer().getId().equals(employer.getId())) {
                return ResponseEntity.<JobPosting>status(403).build();
            }
            job.setTitle(payload.getTitle() != null ? payload.getTitle() : job.getTitle());
            job.setDescription(payload.getDescription() != null ? payload.getDescription() : job.getDescription());
            job.setSalaryRange(payload.getSalaryRange() != null ? payload.getSalaryRange() : job.getSalaryRange());
            job.setStatus(payload.getStatus() != null ? payload.getStatus() : job.getStatus());
            jobPostingRepo.save(job);
            return ResponseEntity.ok(job);
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User employer = userRepo.findByEmail(username).orElse(null);

        var jobOpt = jobPostingRepo.findById(id);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var job = jobOpt.get();
            if (job.getEmployer() == null || !job.getEmployer().getId().equals(employer.getId())) {
                return ResponseEntity.<Void>status(403).build();
            }
            jobPostingRepo.deleteById(id);
            return ResponseEntity.noContent().<Void>build();
    }

    @GetMapping("/applications")
    public ResponseEntity<List<Application>> listApplications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User employer = userRepo.findByEmail(username).orElse(null);
        if (employer == null) {
            return ResponseEntity.notFound().build();
        }
        List<JobPosting> myJobs = jobPostingRepo.findAll().stream()
                .filter(j -> j.getEmployer() != null && j.getEmployer().getId().equals(employer.getId()))
                .toList();
        List<Application> apps = myJobs.stream()
                .flatMap(job -> applicationRepo.findByJobPostingId(job.getId()).stream())
                .toList();
        return ResponseEntity.ok(apps);
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<Application> updateApplicationStatus(@PathVariable Long id, @RequestParam ApplicationStatusEnum status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User employer = userRepo.findByEmail(username).orElse(null);

        var appOpt = applicationRepo.findById(id);
        if (appOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var app = appOpt.get();
            if (app.getJobPosting() == null || app.getJobPosting().getEmployer() == null ||
                    !app.getJobPosting().getEmployer().getId().equals(employer.getId())) {
                return ResponseEntity.<Application>status(403).build();
            }
            app.setStatus(status);
            applicationRepo.save(app);
            return ResponseEntity.ok(app);
    }
}
