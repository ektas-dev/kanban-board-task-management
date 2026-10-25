package com.boardtask.kanban.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boardtask.kanban.dto.task.TaskRequestDto;
import com.boardtask.kanban.dto.task.TaskResponseDto;
import com.boardtask.kanban.dto.task.TaskStatusUpdateDto;
import com.boardtask.kanban.enums.TaskStatus;
import com.boardtask.kanban.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Task Management", description = "APIs for managing personal tasks")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

	private final TaskService taskService;

	/**
	 * Create a new task for the authenticated user.
	 * 
	 * @param taskRequestDto
	 * @return
	 */
	@PostMapping
	public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto taskRequestDto) {
		TaskResponseDto taskResponseDto = taskService.createTask(taskRequestDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(taskResponseDto);
	}

	/**
	 * Retrieve all tasks of the authenticated user with pagination, sorting,
	 * filtering and search support.
	 * 
	 * @param page
	 * @param size
	 * @param sortBy
	 * @param direction
	 * @param status
	 * @param keyword
	 * @return
	 */
	@Operation(summary = "Retrieve all tasks", description = "Returns paginated, sorted, filtered, and searchable tasks belonging to the authenticated user.")
	@GetMapping
	public ResponseEntity<Page<TaskResponseDto>> getMyTasks(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size, @RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction, @RequestParam(required = false) TaskStatus status,
			@RequestParam(required = false) String keyword) {
		return ResponseEntity.ok(taskService.getMyTasks(page, size, sortBy, direction, status, keyword));
	}

	/**
	 * Retrieve task by id
	 * 
	 * @param taskId
	 * @return
	 */
	@GetMapping("/{taskId}")
	public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long taskId) {
		TaskResponseDto taskResponseDto = taskService.getTaskById(taskId);
		return ResponseEntity.ok(taskResponseDto);
	}

	/**
	 * Update task details
	 * 
	 * @param taskId
	 * @param taskRequestDto
	 * @return
	 */
	@PutMapping("/{taskId}")
	public ResponseEntity<TaskResponseDto> updateTask(@PathVariable Long taskId,
			@Valid @RequestBody TaskRequestDto taskRequestDto) {
		TaskResponseDto taskResponseDto = taskService.updateTask(taskId, taskRequestDto);
		return ResponseEntity.ok(taskResponseDto);
	}

	/**
	 * Delete a task by id
	 * 
	 * @param taskId
	 * @return
	 */
	@DeleteMapping("/{taskId}")
	public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
		taskService.deleteTask(taskId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Update the task status
	 * 
	 * @param taskId
	 * @param requestDto
	 * @return
	 */
	@PatchMapping("/{taskId}/status")
	public ResponseEntity<TaskResponseDto> updateTaskStatus(@PathVariable Long taskId,
			@Valid @RequestBody TaskStatusUpdateDto requestDto) {
		return ResponseEntity.ok(taskService.updateTaskStatus(taskId, requestDto.getStatus()));
	}

}
