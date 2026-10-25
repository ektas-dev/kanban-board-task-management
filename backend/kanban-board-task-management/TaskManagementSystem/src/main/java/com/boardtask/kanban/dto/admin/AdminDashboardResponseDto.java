package com.boardtask.kanban.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponseDto {

	private long totalUsers;
	private long totalTasks;
	private long toDoTasks;
	private long inProgressTasks;
	private long completedTasks;

}
