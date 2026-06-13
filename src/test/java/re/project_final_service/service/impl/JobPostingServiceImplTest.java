package re.project_final_service.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import re.project_final_service.model.dto.request.Job.JobNewPostingDto;
import re.project_final_service.model.entity.JobPosting;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.JobStatusEnum;
import re.project_final_service.model.entity.myEnum.Role;
import re.project_final_service.repo.JobPostingRepo;
import re.project_final_service.repo.UserRepo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho JobPostingServiceImpl
 *
 * Cách chạy:
 *   ./gradlew test --tests JobPostingServiceImplTest
 *
 * Coverage: postNewJobPosting()
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JobPostingServiceImpl Unit Tests")
class JobPostingServiceImplTest {

    @MockitoBean
    private JobPostingServiceImpl jobPostingService;

    @MockitoBean
    private JobPostingRepo jobPostingRepo;

    @MockitoBean
    private UserRepo userRepo;

    private JobNewPostingDto validJobDto;
    private User mockEmployer;

    @BeforeEach
    void setUp() {
        validJobDto = new JobNewPostingDto(
                "Senior Java Developer",
                "Looking for experienced Java developer with Spring Boot",
                "15000 - 25000 USD"
        );

        mockEmployer = User.builder()
                .id(1L)
                .email("employer@company.com")
                .username("employer")
                .role(Role.EMPLOYER)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("✅ Thành công: Tạo tin tuyển dụng mới")
    void testPostNewJobPostingSuccess() {
        // Arrange
        when(userRepo.findByEmail("employer@company.com")).thenReturn(Optional.of(mockEmployer));

        JobPosting expectedPosting = JobPosting.builder()
                .id(1L)
                .title(validJobDto.getTitle())
                .description(validJobDto.getDescription())
                .salaryRange(validJobDto.getSalaryRange())
                .employer(mockEmployer)
                .status(JobStatusEnum.DRAFT)
                .deleted(false)
                .build();

        when(jobPostingRepo.save(any(JobPosting.class))).thenReturn(expectedPosting);

        // Mock SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("employer@company.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        jobPostingService = new JobPostingServiceImpl(jobPostingRepo, userRepo);
        JobPosting result = jobPostingService.postNewJobPosting(validJobDto);

        // Assert
        assertNotNull(result);
        assertEquals("Senior Java Developer", result.getTitle());
        assertEquals(JobStatusEnum.DRAFT, result.getStatus());
        assertFalse(result.getDeleted());
        verify(jobPostingRepo, times(1)).save(any(JobPosting.class));
    }

    @Test
    @DisplayName("✅ Thành công: Tin được tạo với đầy đủ thông tin")
    void testPostJobWithCompleteInfo() {
        // Arrange
        JobNewPostingDto complexJob = new JobNewPostingDto(
                "Product Manager",
                "Detailed description with requirements and benefits",
                "20000 - 30000 USD"
        );

        when(userRepo.findByEmail("employer@company.com")).thenReturn(Optional.of(mockEmployer));

        JobPosting expectedPosting = JobPosting.builder()
                .id(2L)
                .title(complexJob.getTitle())
                .description(complexJob.getDescription())
                .salaryRange(complexJob.getSalaryRange())
                .employer(mockEmployer)
                .status(JobStatusEnum.DRAFT)
                .build();

        when(jobPostingRepo.save(any(JobPosting.class))).thenReturn(expectedPosting);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("employer@company.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        jobPostingService = new JobPostingServiceImpl(jobPostingRepo, userRepo);
        JobPosting result = jobPostingService.postNewJobPosting(complexJob);

        // Assert
        assertEquals("Product Manager", result.getTitle());
        assertEquals("Detailed description with requirements and benefits", result.getDescription());
        assertEquals("20000 - 30000 USD", result.getSalaryRange());
    }
}

