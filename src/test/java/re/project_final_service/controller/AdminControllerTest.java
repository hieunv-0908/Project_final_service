package re.project_final_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import re.project_final_service.model.dto.request.user.AdminUpdateUserDto;
import re.project_final_service.model.dto.request.user.ListUserDto;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.Role;
import re.project_final_service.repo.JobPostingRepo;
import re.project_final_service.repo.UserRepo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Test cho AdminController
 *
 * Cách chạy:
 *   ./gradlew test --tests AdminControllerTest
 *
 * Coverage: listUsers(), getUser(), updateUser()
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AdminController Unit Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepo userRepo;

    @MockitoBean
    private JobPostingRepo jobPostingRepo;

    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = User.builder()
                .id(0L)
                .email("admin@system.com")
                .username("admin")
                .role(Role.ADMIN)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .username("testuser")
                .role(Role.CANDIDATE)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("✅ Thành công: Lấy danh sách người dùng (phân trang)")
    void testListUsersSuccess() throws Exception {
        // Arrange
        Page<User> userPage = new PageImpl<>(
                Arrays.asList(testUser),
                PageRequest.of(0, 10),
                1
        );
        when(userRepo.findAll(any(PageRequest.class))).thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/users?page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("user@example.com"))
                .andExpect(jsonPath("$.content[0].role").value("CANDIDATE"));

        verify(userRepo, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("✅ Thành công: Tìm kiếm người dùng theo email")
    void testListUsersWithSearch() throws Exception {
        // Arrange
        Page<User> searchResult = new PageImpl<>(
                Arrays.asList(testUser),
                PageRequest.of(0, 10),
                1
        );
        when(userRepo.findByEmailContainingIgnoreCase(
                "user@example.com",
                PageRequest.of(0, 10)
        )).thenReturn(searchResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/users?page=0&size=10&q=user@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userRepo, times(1)).findByEmailContainingIgnoreCase(
                anyString(),
                any(PageRequest.class)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("✅ Thành công: Lấy thông tin chi tiết người dùng")
    void testGetUserSuccess() throws Exception {
        // Arrange
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"));

        verify(userRepo, times(1)).findById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("❌ Lỗi: Người dùng không tồn tại")
    void testGetUserNotFound() throws Exception {
        // Arrange
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/users/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userRepo, times(1)).findById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("✅ Thành công: Cập nhật thông tin người dùng")
    void testUpdateUserSuccess() throws Exception {
        // Arrange
        AdminUpdateUserDto updateDto = new AdminUpdateUserDto(
                "newemail@example.com",
                Role.EMPLOYER,
                false
        );

        User updatedUser = User.builder()
                .id(1L)
                .email("newemail@example.com")
                .username("testuser")
                .role(Role.EMPLOYER)
                .isActive(false)
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(User.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/v1/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newemail@example.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYER"));

        verify(userRepo, times(1)).findById(1L);
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("✅ Thành công: Toggle trạng thái người dùng")
    void testToggleUserStatusSuccess() throws Exception {
        // Arrange
        User activeUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .username("testuser")
                .isActive(true)
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(activeUser));

        User deactivatedUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .username("testuser")
                .isActive(false)
                .build();

        when(userRepo.save(any(User.class))).thenReturn(deactivatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/v1/admin/users/1/toggle-status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(userRepo, times(1)).findById(1L);
        verify(userRepo, times(1)).save(any(User.class));
    }
}

