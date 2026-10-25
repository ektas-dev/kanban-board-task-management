package com.boardtask.kanban.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boardtask.kanban.dto.admin.AdminDashboardResponseDto;
import com.boardtask.kanban.dto.task.TaskResponseDto;
import com.boardtask.kanban.dto.user.UserResponseDto;
import com.boardtask.kanban.service.AdminService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Administration", description = "Administrative APIs for managing users and tasks")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final AdminService adminService;

	/**
	 * Retrieve all users with pagination and sorting.
	 * 
	 * @param page
	 * @param size
	 * @param sortBy
	 * @param direction
	 * @return
	 */
	@GetMapping("/users")
	public ResponseEntity<Page<UserResponseDto>> getAllUsers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size, @RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {
		return ResponseEntity.ok(adminService.getAllUsers(page, size, sortBy, direction));
	}

	/**
	 * Retrieves users by id
	 * 
	 * @param userId
	 * @return
	 */
	@GetMapping("/users/{userId}")
	public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
		return ResponseEntity.ok(adminService.getUserById(userId));
	}

	/**
	 * Delete a user by id
	 * 
	 * @param userId
	 * @return
	 */
	@DeleteMapping("/users/{userId}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
		adminService.deleteUser(userId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Retrieve all tasks with pagination and sorting.
	 * 
	 * @param page
	 * @param size
	 * @param direction
	 * @return
	 */
	@GetMapping("/tasks")
	public ResponseEntity<Page<TaskResponseDto>> getAllTasks(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size, @RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {
		return ResponseEntity.ok(adminService.getAllTasks(page, size, sortBy, direction));
	}

	/**
	 * Retrieve a task by id
	 * 
	 * @param taskId
	 * @return
	 */
	@GetMapping("/tasks/{taskId}")
	public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long taskId) {
		return ResponseEntity.ok(adminService.getTaskById(taskId));
	}

	/**
	 * Delete a task by id
	 * 
	 * @param taskId
	 * @return
	 */
	@DeleteMapping("/tasks/{taskId}")
	public ResponseEntity<Void> deleteTaskById(@PathVariable Long taskId) {
		adminService.deleteTask(taskId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Retrieves all the details of users and task like count
	 * 
	 * @return
	 */
	@GetMapping("/dashboard")
	public ResponseEntity<AdminDashboardResponseDto> getDashboard() {
		return ResponseEntity.ok(adminService.getDashboard());
	}
}