package re.project_final_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import re.project_final_service.model.dto.request.Job.JobNewPostingDto;
import re.project_final_service.model.entity.JobPosting;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.JobStatusEnum;
import re.project_final_service.model.entity.myEnum.Role;
import re.project_final_service.repo.ApplicationRepo;
import re.project_final_service.repo.JobPostingRepo;
import re.project_final_service.repo.UserRepo;
import re.project_final_service.service.JobPostingService;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("EmployerController Unit Tests")
class EmployerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobPostingService jobPostingService;

    @MockitoBean
    private JobPostingRepo jobPostingRepo;

    @MockitoBean
    private ApplicationRepo applicationRepo;

    @MockitoBean
    private UserRepo userRepo;

    private User testEmployer;
    private JobPosting testJobPosting;

    @BeforeEach
    void setUp() {
        testEmployer = User.builder()
                .id(1L)
                .email("employer@company.com")
                .username("employer")
                .role(Role.EMPLOYER)
                .isActive(true)
                .build();

        testJobPosting = JobPosting.builder()
                .id(1L)
                .title("Senior Java Developer")
                .description("Looking for experienced Java developer")
                .salaryRange("20000 - 30000 USD")
                .employer(testEmployer)
                .status(JobStatusEnum.DRAFT)
                .deleted(false)
                .build();
    }
}