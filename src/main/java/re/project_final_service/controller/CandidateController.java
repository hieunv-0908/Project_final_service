package re.project_final_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import re.project_final_service.model.dto.request.Job.ApplyJobDto;
import re.project_final_service.model.dto.request.Job.CandidateJobDto;
import re.project_final_service.model.dto.request.application.CandidateApplicationDto;
import re.project_final_service.model.entity.Application;
import re.project_final_service.model.entity.JobPosting;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.ApplicationStatusEnum;
import re.project_final_service.model.entity.myEnum.JobStatusEnum;
import re.project_final_service.repo.ApplicationRepo;
import re.project_final_service.repo.JobPostingRepo;
import re.project_final_service.repo.UserRepo;
import re.project_final_service.service.CloudinaryService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/candidate")
public class CandidateController {

    private final UserRepo userRepo;
    private final JobPostingRepo jobPostingRepo;
    private final ApplicationRepo applicationRepo;
    private final CloudinaryService cloudinaryService;

    // =============================
    // Upload CV
    // =============================

    @PostMapping("/cv/upload")
    public ResponseEntity<User> uploadCv(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User candidate = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Candidate không tồn tại"));

        if (!"application/pdf"
                .equals(file.getContentType())) {

            throw new RuntimeException(
                    "Chỉ chấp nhận file PDF");
        }

        if (file.getSize() > 15 * 1024 * 1024) {

            throw new RuntimeException(
                    "Dung lượng tối đa là 15MB");
        }

        String cvUrl =
                cloudinaryService.uploadPdf(file);

        candidate.setCvUrl(cvUrl);

        userRepo.save(candidate);

        return ResponseEntity.ok(candidate);
    }

    // =============================
    // Danh sách việc làm
    // =============================

    @GetMapping("/jobs")
    public ResponseEntity<Page<CandidateJobDto>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<JobPosting> jobs =
                jobPostingRepo.findByStatusAndDeletedFalse(
                        JobStatusEnum.APPROVED,
                        PageRequest.of(page, size)
                );

        Page<CandidateJobDto> result =
                jobs.map(job ->
                        CandidateJobDto.builder()
                                .id(job.getId())
                                .title(job.getTitle())
                                .description(job.getDescription())
                                .salaryRange(job.getSalaryRange())
                                .companyName(
                                        job.getEmployer().getUsername()
                                )
                                .build());

        return ResponseEntity.ok(result);
    }

    // =============================
    // Chi tiết việc làm
    // =============================

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobPosting> getJobDetail(
            @PathVariable Long id) {

        JobPosting job =
                jobPostingRepo
                        .findByIdAndStatusAndDeletedFalse(
                                id,
                                JobStatusEnum.APPROVED
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy công việc"));

        return ResponseEntity.ok(job);
    }

    // =============================
    // Apply Job
    // =============================

    @PostMapping("/applications")
    public ResponseEntity<Application> applyJob(
            @RequestBody ApplyJobDto dto) {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        User candidate =
                userRepo.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Candidate không tồn tại"));

        JobPosting job =
                jobPostingRepo
                        .findByIdAndStatusAndDeletedFalse(
                                dto.getJobId(),
                                JobStatusEnum.APPROVED
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy công việc"));

        if (candidate.getCvUrl() == null
                || candidate.getCvUrl().isBlank()) {

            throw new RuntimeException(
                    "Vui lòng tải CV trước khi ứng tuyển");
        }

        boolean existed =
                applicationRepo
                        .findByCandidateAndJobPosting(
                                candidate,
                                job
                        )
                        .isPresent();

        if (existed) {

            throw new RuntimeException(
                    "Bạn đã ứng tuyển công việc này");
        }

        Application application =
                Application.builder()
                        .candidate(candidate)
                        .jobPosting(job)
                        .cvUrl(candidate.getCvUrl())
                        .coverLetter(dto.getCoverLetter())
                        .status(ApplicationStatusEnum.PENDING)
                        .build();

        applicationRepo.save(application);

        return ResponseEntity.status(201)
                .body(application);
    }

    // =============================
    // Danh sách hồ sơ đã nộp
    // =============================

    @GetMapping("/applications")
    public ResponseEntity<Page<CandidateApplicationDto>>
    myApplications(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        User candidate =
                userRepo.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Candidate không tồn tại"));

        Page<Application> applications =
                applicationRepo.findByCandidate(
                        candidate,
                        PageRequest.of(page, size)
                );

        Page<CandidateApplicationDto> result =
                applications.map(app ->
                        CandidateApplicationDto.builder()
                                .applicationId(app.getId())
                                .jobTitle(
                                        app.getJobPosting().getTitle()
                                )
                                .status(app.getStatus())
                                .employerFeedback(
                                        app.getEmployerFeedback()
                                )
                                .build());

        return ResponseEntity.ok(result);
    }

    // =============================
    // Chi tiết hồ sơ
    // =============================

    @GetMapping("/applications/{id}")
    public ResponseEntity<Application>
    getMyApplicationDetail(
            @PathVariable Long id) {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        User candidate =
                userRepo.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Candidate không tồn tại"));

        Application application =
                applicationRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy hồ sơ"));

        if (!application.getCandidate()
                .getId()
                .equals(candidate.getId())) {

            return ResponseEntity.status(403)
                    .build();
        }

        return ResponseEntity.ok(application);
    }
}