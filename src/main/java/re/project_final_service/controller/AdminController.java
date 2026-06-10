package re.project_final_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import re.project_final_service.model.entity.JobPosting;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.JobStatusEnum;
import re.project_final_service.repo.JobPostingRepo;
import re.project_final_service.repo.UserRepo;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
	private final UserRepo userRepo;
	private final JobPostingRepo jobPostingRepo;

	@GetMapping("/users")
	public ResponseEntity<Page<User>> listUsers(@RequestParam(defaultValue = "0") int page,
												@RequestParam(defaultValue = "10") int size,
												@RequestParam(required = false) String q) {
		PageRequest pr = PageRequest.of(page, size);
		Page<User> result = (q == null || q.isBlank()) ? userRepo.findAll(pr) : userRepo.findByEmailContainingIgnoreCase(q, pr);
		return ResponseEntity.ok(result);
	}

	@PutMapping("/users/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User payload) {
		return userRepo.findById(id).map(u -> {
			u.setEmail(payload.getEmail() != null ? payload.getEmail() : u.getEmail());
			u.setRole(payload.getRole() != null ? payload.getRole() : u.getRole());
			u.setActive(payload.isActive());
			userRepo.save(u);
			return ResponseEntity.ok(u);
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/jobs")
	public ResponseEntity<Page<JobPosting>> listJobs(@RequestParam(defaultValue = "0") int page,
													 @RequestParam(defaultValue = "10") int size,
													 @RequestParam(required = false) String q) {
		PageRequest pr = PageRequest.of(page, size);
		Page<JobPosting> result = (q == null || q.isBlank()) ? jobPostingRepo.findAll(pr) : jobPostingRepo.findByTitleContainingIgnoreCase(q, pr);
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
}

