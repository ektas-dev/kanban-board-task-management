package com.boardtask.kanban.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardtask.kanban.dto.task.TaskRequestDto;
import com.boardtask.kanban.dto.task.TaskResponseDto;
import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.entity.Task;
import com.boardtask.kanban.enums.TaskStatus;
import com.boardtask.kanban.exception.ResourceNotFoundException;
import com.boardtask.kanban.mapper.TaskMapper;
import com.boardtask.kanban.repository.TaskRepository;
import com.boardtask.kanban.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

	private final TaskRepository taskRepository;
	private final SecurityUtil securityUtil;

	@Override
	@Transactional
	public TaskResponseDto createTask(TaskRequestDto taskRequestDto) {
		AppUser currentUser = securityUtil.getCurrentUser();
		Task task = TaskMapper.toEntity(taskRequestDto);
		task.setAppUser(currentUser);
		task.setStatus(TaskStatus.TODO);
		Task savedTask = taskRepository.save(task);
		return TaskMapper.toResponseDto(savedTask);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TaskResponseDto> getMyTasks(int page, int size, String sortBy, String direction, TaskStatus status,
			String keyword) {
		AppUser currentUser = securityUtil.getCurrentUser();
		Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<Task> taskPage;
		if (status != null && keyword != null && !keyword.isBlank()) {
			taskPage = taskRepository.findByAppUserAndStatusAndTitleContainingIgnoreCase(currentUser, status, keyword,
					pageable);
		} else if (status != null) {
			taskPage = taskRepository.findByAppUserAndStatus(currentUser, status, pageable);
		} else if (keyword != null && !keyword.isBlank()) {
			taskPage = taskRepository.findByAppUserAndTitleContainingIgnoreCase(currentUser, keyword, pageable);
		} else {
			taskPage = taskRepository.findByAppUser(currentUser, pageable);
		}
		return taskPage.map(TaskMapper::toResponseDto);
	}

	@Override
	@Transactional(readOnly = true)
	public TaskResponseDto getTaskById(Long id) {
		Task task = getTaskForCurrentUser(id);
		return TaskMapper.toResponseDto(task);
	}

	@Override
	@Transactional
	public TaskResponseDto updateTask(Long id, TaskRequestDto taskRequestDto) {
		Task task = getTaskForCurrentUser(id);
		TaskMapper.updateEntity(task, taskRequestDto);
		Task updatedTask = taskRepository.save(task);
		return TaskMapper.toResponseDto(updatedTask);
	}

	@Override
	@Transactional
	public TaskResponseDto updateTaskStatus(Long id, TaskStatus status) {
		Task task = getTaskForCurrentUser(id);
		task.setStatus(status);
		Task updatedTask = taskRepository.save(task);
		return TaskMapper.toResponseDto(updatedTask);
	}

	@Override
	@Transactional
	public void deleteTask(Long id) {
		Task task = getTaskForCurrentUser(id);
		taskRepository.delete(task);
	}

	private Task getTaskForCurrentUser(Long id) {
		AppUser currentUser = securityUtil.getCurrentUser();
		return taskRepository.findByIdAndAppUser(id, currentUser)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found."));
	}
}
