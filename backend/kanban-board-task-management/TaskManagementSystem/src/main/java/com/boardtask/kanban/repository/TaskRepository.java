package com.boardtask.kanban.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.entity.Task;
import com.boardtask.kanban.enums.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {

	Page<Task> findByAppUser(AppUser appUser, Pageable pageable);

	Optional<Task> findByIdAndAppUser(Long id, AppUser appUser);

	Page<Task> findByAppUserAndStatus(AppUser appUser, TaskStatus status, Pageable pageable);

	Page<Task> findByAppUserAndTitleContainingIgnoreCase(AppUser appUser, String keyword, Pageable pageable);

	Page<Task> findByAppUserAndStatusAndTitleContainingIgnoreCase(AppUser appUser, TaskStatus status, String keyword,
			Pageable pageable);

	long countByStatus(TaskStatus status);
}