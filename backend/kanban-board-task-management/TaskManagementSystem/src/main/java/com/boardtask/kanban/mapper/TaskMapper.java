package com.boardtask.kanban.mapper;

import com.boardtask.kanban.dto.task.TaskRequestDto;
import com.boardtask.kanban.dto.task.TaskResponseDto;
import com.boardtask.kanban.entity.Task;

public class TaskMapper {

	// DTO → Entity
	public static Task toEntity(TaskRequestDto taskRequestDto) {
		Task task = new Task();
		task.setTitle(taskRequestDto.getTitle());
		task.setDescription(taskRequestDto.getDescription());
		return task;
	}

	// Entity → Response DTO
	public static TaskResponseDto toResponseDto(Task task) {
		return new TaskResponseDto(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(),
				task.getCreatedAt(), task.getUpdatedAt());
	}

	// Update existing entity from DTO
	public static void updateEntity(Task task, TaskRequestDto taskRequestDto) {
		task.setTitle(taskRequestDto.getTitle());
		task.setDescription(taskRequestDto.getDescription());
	}
}
