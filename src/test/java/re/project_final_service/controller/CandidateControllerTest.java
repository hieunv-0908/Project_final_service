package re.project_final_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import re.project_final_service.model.entity.Application;
import re.project_final_service.model.entity.JobPosting;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.ApplicationStatusEnum;
import re.project_final_service.model.entity.myEnum.JobStatusEnum;
import re.project_final_service.model.entity.myEnum.Role;
import re.project_final_service.repo.ApplicationRepo;
import re.project_final_service.repo.JobPostingRepo;
import re.project_final_service.repo.UserRepo;
import re.project_final_service.service.CloudinaryService;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Test cho CandidateController
 *
 * Cách chạy:
 *   ./gradlew test --tests CandidateControllerTest
 *
 * Coverage: uploadCv(), getJobs(), applyJob()
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CandidateController Unit Tests")
class CandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepo userRepo;

    @MockitoBean
    private JobPostingRepo jobPostingRepo;

    @MockitoBean
    private ApplicationRepo applicationRepo;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    private User testCandidate;
    private User testEmployer;
    private JobPosting testJobPosting;

    @BeforeEach
    void setUp() {
        testCandidate = User.builder()
                .id(1L)
                .email("candidate@example.com")
                .username("candidate")
                .role(Role.CANDIDATE)
                .isActive(true)
                .build();

        testEmployer = User.builder()
                .id(2L)
                .email("employer@company.com")
                .username("employer")
                .role(Role.EMPLOYER)
                .isActive(true)
                .build();

        testJobPosting = JobPosting.builder()
                .id(1L)
                .title("Java Developer")
                .description("Open position for Java developer")
                .salaryRange("15000 - 25000 USD")
                .employer(testEmployer)
                .status(JobStatusEnum.APPROVED)
                .deleted(false)
                .build();
    }

    @Test
    @WithMockUser(username = "candidate@example.com", roles = "CANDIDATE")
    @DisplayName("✅ Thành công: Tải lên CV (PDF)")
    void testUploadCvSuccess() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        User candidateWithCv = User.builder()
                .id(1L)
                .email("candidate@example.com")
                .username("candidate")
                .role(Role.CANDIDATE)
                .isActive(true)
                .cvUrl("https://cloudinary.com/cv/resume.pdf")
                .build();

        when(userRepo.findByEmail("candidate@example.com")).thenReturn(Optional.of(testCandidate));
        when(cloudinaryService.uploadPdf(file)).thenReturn("https://cloudinary.com/cv/resume.pdf");
        when(userRepo.save(any(User.class))).thenReturn(candidateWithCv);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/candidate/cv/upload")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cvUrl").exists());

        verify(cloudinaryService, times(1)).uploadPdf(any());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "candidate@example.com", roles = "CANDIDATE")
    @DisplayName("❌ Lỗi: File không phải PDF")
    void testUploadCvWithInvalidFileType() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.doc",
                "application/msword",
                "DOC content".getBytes()
        );

        when(userRepo.findByEmail("candidate@example.com")).thenReturn(Optional.of(testCandidate));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/candidate/cv/upload")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is4xxClientError());

        verify(cloudinaryService, never()).uploadPdf(any());
    }

    @Test
    @WithMockUser(username = "candidate@example.com", roles = "CANDIDATE")
    @DisplayName("✅ Thành công: Lấy danh sách việc làm (phân trang)")
    void testGetJobsSuccess() throws Exception {
        // Arrange
        Page<JobPosting> jobPage = new PageImpl<>(
                Arrays.asList(testJobPosting),
                PageRequest.of(0, 10),
                1
        );

        when(jobPostingRepo.findByStatusAndDeletedFalse(
                JobStatusEnum.APPROVED,
                PageRequest.of(0, 10)
        )).thenReturn(jobPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/candidate/jobs?page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Java Developer"));

        verify(jobPostingRepo, times(1)).findByStatusAndDeletedFalse(
                JobStatusEnum.APPROVED,
                PageRequest.of(0, 10)
        );
    }

    @Test
    @WithMockUser(username = "candidate@example.com", roles = "CANDIDATE")
    @DisplayName("✅ Thành công: Xem chi tiết công việc")
    void testGetJobDetailSuccess() throws Exception {
        // Arrange
        when(jobPostingRepo.findByIdAndStatusAndDeletedFalse(
                1L,
                JobStatusEnum.APPROVED
        )).thenReturn(Optional.of(testJobPosting));

        // Act & Assert
        mockMvc.perform(get("/api/v1/candidate/jobs/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Developer"));

        verify(jobPostingRepo, times(1)).findByIdAndStatusAndDeletedFalse(1L, JobStatusEnum.APPROVED);
    }

    @Test
    @WithMockUser(username = "candidate@example.com", roles = "CANDIDATE")
    @DisplayName("✅ Thành công: Nộp hồ sơ ứng tuyển")
    void testApplyJobSuccess() throws Exception {
        // Arrange
        testCandidate.setCvUrl("https://cloudinary.com/cv/resume.pdf");

        Application application = Application.builder()
                .id(1L)
                .candidate(testCandidate)
                .jobPosting(testJobPosting)
                .cvUrl("https://cloudinary.com/cv/resume.pdf")
                .coverLetter("I am interested in this position")
                .status(ApplicationStatusEnum.PENDING)
                .build();

        String applyJobJson = "{\"jobId\":1,\"coverLetter\":\"I am interested in this position\"}";

        when(userRepo.findByEmail("candidate@example.com")).thenReturn(Optional.of(testCandidate));
        when(jobPostingRepo.findByIdAndStatusAndDeletedFalse(1L, JobStatusEnum.APPROVED))
                .thenReturn(Optional.of(testJobPosting));
        when(applicationRepo.findByCandidateAndJobPosting(testCandidate, testJobPosting))
                .thenReturn(Optional.empty());
        when(applicationRepo.save(any(Application.class))).thenReturn(application);

        // Act & Assert
        mockMvc.perform(post("/api/v1/candidate/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(applyJobJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(applicationRepo, times(1)).save(any(Application.class));
    }

    @Test
    @WithMockUser(username = "candidate@example.com", roles = "CANDIDATE")
    @DisplayName("❌ Lỗi: Ứng viên chưa tải CV")
    void testApplyJobWithoutCV() throws Exception {
        // Arrange
        User candidateNoCV = User.builder()
                .id(1L)
                .email("candidate@example.com")
                .username("candidate")
                .role(Role.CANDIDATE)
                .isActive(true)
                .cvUrl(null)
                .build();

        String applyJobJson = "{\"jobId\":1,\"coverLetter\":\"I am interested in this position\"}";

        when(userRepo.findByEmail("candidate@example.com")).thenReturn(Optional.of(candidateNoCV));
        when(jobPostingRepo.findByIdAndStatusAndDeletedFalse(1L, JobStatusEnum.APPROVED))
                .thenReturn(Optional.of(testJobPosting));

        // Act & Assert
        mockMvc.perform(post("/api/v1/candidate/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(applyJobJson))
                .andExpect(status().is4xxClientError());

        verify(applicationRepo, never()).save(any(Application.class));
    }
}

