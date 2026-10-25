package com.boardtask.kanban.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardtask.kanban.dto.admin.AdminDashboardResponseDto;
import com.boardtask.kanban.dto.task.TaskResponseDto;
import com.boardtask.kanban.dto.user.UserResponseDto;
import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.entity.Task;
import com.boardtask.kanban.enums.TaskStatus;
import com.boardtask.kanban.exception.ResourceNotFoundException;
import com.boardtask.kanban.mapper.TaskMapper;
import com.boardtask.kanban.mapper.UserMapper;
import com.boardtask.kanban.repository.AppUserRepository;
import com.boardtask.kanban.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

	private final TaskRepository taskRepository;
	private final AppUserRepository appUserRepository;

	@Override
	@Transactional(readOnly = true)
	public Page<UserResponseDto> getAllUsers(int page, int size, String sortBy, String direction) {
		Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<AppUser> userPage = appUserRepository.findAll(pageable);
		return userPage.map(UserMapper::toResponseDto);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponseDto getUserById(Long userId) {
		return appUserRepository.findById(userId).map(UserMapper::toResponseDto)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with the id " + userId));
	}

	@Override
	public void deleteUser(Long userId) {
		AppUser user = appUserRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with the id " + userId));
		appUserRepository.delete(user);
	}

	@Override
	public Page<TaskResponseDto> getAllTasks(int page, int size, String sortBy, String direction) {
		Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<Task> taskPage = taskRepository.findAll(pageable);
		return taskPage.map(TaskMapper::toResponseDto);
	}

	@Override
	@Transactional(readOnly = true)
	public TaskResponseDto getTaskById(Long taskId) {
		return taskRepository.findById(taskId).map(TaskMapper::toResponseDto)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with the id " + taskId));
	}

	@Override
	public void deleteTask(Long taskId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with the id " + taskId));
		taskRepository.delete(task);
	}

	@Override
	@Transactional(readOnly = true)
	public AdminDashboardResponseDto getDashboard() {
		long totalUsers = appUserRepository.count();
		long totalTasks = taskRepository.count();
		long completedTasks = taskRepository.countByStatus(TaskStatus.DONE);
		long toDoTasks = taskRepository.countByStatus(TaskStatus.TODO);
		long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);

		return AdminDashboardResponseDto.builder().totalUsers(totalUsers).totalTasks(totalTasks)
				.completedTasks(completedTasks).toDoTasks(toDoTasks).inProgressTasks(inProgressTasks).build();
	}

}
