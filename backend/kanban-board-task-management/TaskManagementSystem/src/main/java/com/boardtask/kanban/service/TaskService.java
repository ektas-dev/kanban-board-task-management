package com.boardtask.kanban.service;

import org.springframework.data.domain.Page;

import com.boardtask.kanban.dto.task.TaskRequestDto;
import com.boardtask.kanban.dto.task.TaskResponseDto;
import com.boardtask.kanban.enums.TaskStatus;

public interface TaskService {
	TaskResponseDto createTask(TaskRequestDto taskRequestDto);

	Page<TaskResponseDto> getMyTasks(int page, int size, String sortBy,String direction,TaskStatus status,String keyword);

	TaskResponseDto getTaskById(Long id);

	TaskResponseDto updateTask(Long id, TaskRequestDto taskRequestDto);

	TaskResponseDto updateTaskStatus(Long id, TaskStatus status);
	
	void deleteTask(Long id);

}
