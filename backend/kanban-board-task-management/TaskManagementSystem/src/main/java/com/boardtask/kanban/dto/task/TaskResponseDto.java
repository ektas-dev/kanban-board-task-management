package com.boardtask.kanban.dto.task;

import com.boardtask.kanban.enums.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto {

	private Long id;
	private String title;
	private String description;
	private TaskStatus status;

}
