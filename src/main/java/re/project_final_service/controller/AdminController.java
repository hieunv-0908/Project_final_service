package re.project_final_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import re.project_final_service.model.dto.request.user.AdminUpdateUserDto;
import re.project_final_service.model.dto.request.user.ListJobDto;
import re.project_final_service.model.dto.request.user.ListUserDto;
import re.project_final_service.model.entity.JobPosting;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.JobStatusEnum;
import re.project_final_service.repo.JobPostingRepo;
import re.project_final_service.repo.UserRepo;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
	private final UserRepo userRepo;
	private final JobPostingRepo jobPostingRepo;

	@GetMapping("/users")
	public ResponseEntity<Page<ListUserDto>> listUsers(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String q) {

		PageRequest pr = PageRequest.of(page, size);

		Page<User> users =
				(q == null || q.isBlank())
						? userRepo.findAll(pr)
						: userRepo.findByEmailContainingIgnoreCase(q, pr);

		Page<ListUserDto> result = users.map(user ->
				ListUserDto.builder()
						.id(user.getId())
						.username(user.getUsername())
						.email(user.getEmail())
						.role(user.getRole())
						.isActive(user.getIsActive())
						.createdAt(user.getCreatedAt())
						.updatedAt(user.getUpdatedAt())
						.build()
		);

		return ResponseEntity.ok(result);
	}

	@PutMapping("/users/{id}")
	public ResponseEntity<ListUserDto> updateUser(
			@PathVariable Long id,
			@RequestBody AdminUpdateUserDto payload) {

		return userRepo.findById(id)
				.map(user -> {

					if (payload.getEmail() != null) {
						user.setEmail(payload.getEmail());
					}

					if (payload.getRole() != null) {
						user.setRole(payload.getRole());
					}

					if (payload.getIsActive() != null) {
						user.setIsActive(payload.getIsActive());
					}

					userRepo.save(user);

					return ResponseEntity.ok(
							ListUserDto.builder()
									.id(user.getId())
									.username(user.getUsername())
									.email(user.getEmail())
									.role(user.getRole())
									.isActive(user.getIsActive())
									.createdAt(user.getCreatedAt())
									.updatedAt(user.getUpdatedAt())
									.build()
					);
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/users/{id}")
	public ResponseEntity<ListUserDto> getUser(
			@PathVariable Long id) {

		return userRepo.findById(id)
				.map(user -> ResponseEntity.ok(
						ListUserDto.builder()
								.id(user.getId())
								.username(user.getUsername())
								.email(user.getEmail())
								.role(user.getRole())
								.isActive(user.getIsActive())
								.createdAt(user.getCreatedAt())
								.updatedAt(user.getUpdatedAt())
								.build()
				))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PutMapping("/users/{id}/toggle-status")
	public ResponseEntity<?> toggleUserStatus(
			@PathVariable Long id) {

		return userRepo.findById(id)
				.map(user -> {

					user.setIsActive(!user.getIsActive());

					userRepo.save(user);

					return ResponseEntity.ok(
							Map.of(
									"userId", user.getId(),
									"isActive", user.getIsActive()
							)
					);
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/jobs")
	public ResponseEntity<Page<ListJobDto>> listJobs(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String q) {

		PageRequest pr = PageRequest.of(page, size);

		Page<JobPosting> jobs =
				(q == null || q.isBlank())
						? jobPostingRepo.findAll(pr)
						: jobPostingRepo.findByTitleContainingIgnoreCase(q, pr);

		Page<ListJobDto> result = jobs.map(this::toDto);

		return ResponseEntity.ok(result);
	}

	@PutMapping("/jobs/{id}/approve")
	public ResponseEntity<JobPosting> approveJob(@PathVariable Long id) {
		return jobPostingRepo.findById(id).map(j -> {
			j.setStatus(JobStatusEnum.APPROVED);
			jobPostingRepo.save(j);
			return ResponseEntity.ok(j);
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/jobs/{id}")
	public ResponseEntity deleteJob(@PathVariable Long id) {

		if (!jobPostingRepo.existsById(id)) {
			return ResponseEntity.notFound().build();
		}

		jobPostingRepo.deleteById(id);

		return ResponseEntity.ok().body("Xoá thành công");
	}

	private ListJobDto toDto(JobPosting job) {
		return ListJobDto.builder()
				.id(job.getId())
				.title(job.getTitle())
				.salaryRange(job.getSalaryRange())
				.status(job.getStatus())
				.employerId(job.getEmployer().getId())
				.employerName(job.getEmployer().getUsername())
				.employerEmail(job.getEmployer().getEmail())
				.build();
	}
}

