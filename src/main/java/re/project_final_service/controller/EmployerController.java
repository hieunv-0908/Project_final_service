package re.project_final_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import re.project_final_service.model.dto.request.Job.JobNewPostingDto;
import re.project_final_service.model.dto.request.Job.UpdateJobDto;
import re.project_final_service.model.dto.request.application.EmployerApplicationDto;
import re.project_final_service.model.dto.request.application.UpdateApplicationResultDto;
import re.project_final_service.model.dto.response.APIDataResponse;
import re.project_final_service.model.entity.Application;
import re.project_final_service.model.entity.JobPosting;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.ApplicationStatusEnum;
import re.project_final_service.model.entity.myEnum.JobStatusEnum;
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
    public ResponseEntity<Page<JobPosting>> listMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        String email =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();

        User employer =
                userRepo.findByEmail(email)
                        .orElseThrow(
                                () -> new RuntimeException("Employer không tồn tại")
                        );

        return ResponseEntity.ok(
                jobPostingRepo.findByEmployerAndDeletedFalse(
                        employer,
                        PageRequest.of(page, size)
                )
        );
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<JobPosting> updateJob(
            @PathVariable Long id,
            @RequestBody UpdateJobDto payload) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User employer = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Employer không tồn tại"));

        JobPosting job = jobPostingRepo
                .findByIdAndEmployerAndDeletedFalse(
                        id,
                        employer
                )
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy bài đăng"));

        if (job.getStatus() == JobStatusEnum.APPROVED
                || job.getStatus() == JobStatusEnum.PENDING_APPROVAL) {

            throw new RuntimeException(
                    "Không thể sửa bài đã gửi duyệt hoặc đã được duyệt"
            );
        }

        if (payload.getTitle() != null) {
            job.setTitle(payload.getTitle());
        }

        if (payload.getDescription() != null) {
            job.setDescription(payload.getDescription());
        }

        if (payload.getSalaryRange() != null) {
            job.setSalaryRange(payload.getSalaryRange());
        }

        jobPostingRepo.save(job);

        return ResponseEntity.ok(job);
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<String> deleteJob(
            @PathVariable Long id) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User employer = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Employer không tồn tại"));

        JobPosting job = jobPostingRepo
                .findByIdAndEmployer(id, employer)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy bài đăng"));

        if (job.getStatus() == JobStatusEnum.APPROVED
                || job.getStatus() == JobStatusEnum.CLOSED) {

            throw new RuntimeException(
                    "Không thể xóa bài đã được duyệt hoặc đã đóng"
            );
        }

        job.setDeleted(true);

        jobPostingRepo.save(job);

        return ResponseEntity.ok().body("Xoá thành công");
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

    @PutMapping("/applications/{id}/result")
    public ResponseEntity<Application> updateApplicationResult(
            @PathVariable Long id,
            @RequestBody UpdateApplicationResultDto dto) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User employer = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Employer không tồn tại"));

        Application app = applicationRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy hồ sơ"));

        if (!app.getJobPosting()
                .getEmployer()
                .getId()
                .equals(employer.getId())) {

            return ResponseEntity.status(403).build();
        }

        if (app.getStatus() != ApplicationStatusEnum.PENDING) {
            throw new RuntimeException("Hồ sơ đã được xử lý");
        }

        app.setStatus(dto.getStatus());
        app.setEmployerFeedback(dto.getFeedback());

        applicationRepo.save(app);

        return ResponseEntity.ok(app);
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobPosting> getMyJob(
            @PathVariable Long id) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User employer = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Employer không tồn tại"));

        JobPosting job = jobPostingRepo
                .findByIdAndEmployerAndDeletedFalse(
                        id,
                        employer
                )
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy bài đăng"));

        return ResponseEntity.ok(job);
    }

    @PutMapping("/jobs/{id}/submit")
    public ResponseEntity<JobPosting> submitJob(
            @PathVariable Long id) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User employer = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Employer không tồn tại"));

        JobPosting job = jobPostingRepo
                .findByIdAndEmployerAndDeletedFalse(
                        id,
                        employer
                )
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy bài đăng"));

        if (job.getStatus() != JobStatusEnum.DRAFT
                && job.getStatus() != JobStatusEnum.REJECTED) {

            throw new RuntimeException(
                    "Chỉ bài nháp hoặc bài bị từ chối mới được gửi duyệt"
            );
        }

        job.setStatus(JobStatusEnum.PENDING_APPROVAL);

        jobPostingRepo.save(job);

        return ResponseEntity.ok(job);
    }

    @PutMapping("/jobs/{id}/close")
    public ResponseEntity<JobPosting> closeJob(
            @PathVariable Long id) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User employer = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Employer không tồn tại"));

        JobPosting job = jobPostingRepo
                .findByIdAndEmployerAndDeletedFalse(
                        id,
                        employer
                )
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy bài đăng"));

        if (job.getStatus() != JobStatusEnum.APPROVED) {

            throw new RuntimeException(
                    "Chỉ bài đã được duyệt mới được đóng"
            );
        }

        job.setStatus(JobStatusEnum.CLOSED);

        jobPostingRepo.save(job);

        return ResponseEntity.ok(job);
    }

    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<Page<EmployerApplicationDto>> getApplicationsByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User employer = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Employer không tồn tại"));

        JobPosting job = jobPostingRepo
                .findByIdAndEmployer(jobId, employer)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy bài đăng"));

        Page<Application> applications =
                applicationRepo.findByJobPosting(
                        job,
                        PageRequest.of(page, size)
                );

        Page<EmployerApplicationDto> result =
                applications.map(app ->
                        EmployerApplicationDto.builder()
                                .applicationId(app.getId())
                                .candidateName(
                                        app.getCandidate().getUsername())
                                .candidateEmail(
                                        app.getCandidate().getEmail())
                                .cvUrl(app.getCvUrl())
                                .coverLetter(app.getCoverLetter())
                                .status(app.getStatus())
                                .build());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<Application> getApplicationDetail(
            @PathVariable Long id) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User employer = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Employer không tồn tại"));

        Application application =
                applicationRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy hồ sơ"));

        if (!application.getJobPosting()
                .getEmployer()
                .getId()
                .equals(employer.getId())) {

            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(application);
    }
}
