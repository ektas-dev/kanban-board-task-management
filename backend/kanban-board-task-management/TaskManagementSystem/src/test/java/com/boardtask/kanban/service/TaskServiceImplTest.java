package com.boardtask.kanban.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.boardtask.kanban.dto.task.TaskRequestDto;
import com.boardtask.kanban.dto.task.TaskResponseDto;
import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.entity.Task;
import com.boardtask.kanban.enums.TaskStatus;
import com.boardtask.kanban.exception.ResourceNotFoundException;
import com.boardtask.kanban.repository.TaskRepository;
import com.boardtask.kanban.security.SecurityUtil;
import com.boardtask.kanban.service.TaskServiceImpl;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private TaskServiceImpl taskService;

    private AppUser currentUser;
    private TaskRequestDto taskRequestDto;

    @BeforeEach
    void setUp() {
        currentUser = org.mockito.Mockito.mock(AppUser.class);

        taskRequestDto = new TaskRequestDto();
        taskRequestDto.setTitle("Complete testing");
        taskRequestDto.setDescription("Write unit tests for TaskServiceImpl");

        when(securityUtil.getCurrentUser()).thenReturn(currentUser);
    }

    // --------------------------------------------------
    // CREATE TASK
    // --------------------------------------------------

    @Test
    void createTask_validRequest_savesTaskWithCurrentUserAndTodoStatus() {
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto response = taskService.createTask(taskRequestDto);

        ArgumentCaptor<Task> taskCaptor =
                ArgumentCaptor.forClass(Task.class);

        verify(taskRepository, times(1)).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();

        assertNotNull(response);
        assertEquals("Complete testing", savedTask.getTitle());
        assertEquals(
                "Write unit tests for TaskServiceImpl",
                savedTask.getDescription()
        );
        assertEquals(currentUser, savedTask.getAppUser());
        assertEquals(TaskStatus.TODO, savedTask.getStatus());

        verify(securityUtil, times(1)).getCurrentUser();
    }

    @Test
    void createTask_repositoryThrowsException_propagatesException() {
        when(taskRepository.save(any(Task.class)))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.createTask(taskRequestDto)
        );

        assertEquals("Database error", exception.getMessage());

        verify(securityUtil, times(1)).getCurrentUser();
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // --------------------------------------------------
    // GET MY TASKS: FILTERING, PAGINATION AND SORTING
    // --------------------------------------------------

    @Test
    void getMyTasks_noFilters_returnsCurrentUsersTasks() {
        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("createdAt").descending()
        );

        Page<Task> taskPage =
                new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(taskRepository.findByAppUser(currentUser, pageable))
                .thenReturn(taskPage);

        Page<TaskResponseDto> response = taskService.getMyTasks(
                0, 10, "createdAt", "desc", null, null
        );

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(taskRepository).findByAppUser(currentUser, pageable);
        verify(taskRepository, never()).findByAppUserAndStatus(
                any(AppUser.class), any(TaskStatus.class), any(Pageable.class)
        );
        verify(taskRepository, never())
                .findByAppUserAndTitleContainingIgnoreCase(
                        any(AppUser.class), anyString(), any(Pageable.class)
                );
        verify(taskRepository, never())
                .findByAppUserAndStatusAndTitleContainingIgnoreCase(
                        any(AppUser.class),
                        any(TaskStatus.class),
                        anyString(),
                        any(Pageable.class)
                );
    }

    @Test
    void getMyTasks_statusFilter_returnsTasksMatchingStatus() {
        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("createdAt").descending()
        );

        Page<Task> taskPage =
                new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(taskRepository.findByAppUserAndStatus(
                currentUser, TaskStatus.TODO, pageable
        )).thenReturn(taskPage);

        Page<TaskResponseDto> response = taskService.getMyTasks(
                0, 10, "createdAt", "desc", TaskStatus.TODO, null
        );

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(taskRepository).findByAppUserAndStatus(
                currentUser, TaskStatus.TODO, pageable
        );
        verify(taskRepository, never()).findByAppUser(
                any(AppUser.class), any(Pageable.class)
        );
    }

    @Test
    void getMyTasks_keywordFilter_returnsMatchingTasks() {
        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("title").ascending()
        );

        Page<Task> taskPage =
                new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(taskRepository.findByAppUserAndTitleContainingIgnoreCase(
                currentUser, "testing", pageable
        )).thenReturn(taskPage);

        Page<TaskResponseDto> response = taskService.getMyTasks(
                0, 10, "title", "ASC", null, "testing"
        );

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(taskRepository).findByAppUserAndTitleContainingIgnoreCase(
                currentUser, "testing", pageable
        );
    }

    @Test
    void getMyTasks_statusAndKeywordFilters_usesCombinedQuery() {
        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("title").descending()
        );

        Page<Task> taskPage =
                new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(taskRepository.findByAppUserAndStatusAndTitleContainingIgnoreCase(
                currentUser, TaskStatus.IN_PROGRESS, "testing", pageable
        )).thenReturn(taskPage);

        Page<TaskResponseDto> response = taskService.getMyTasks(
                0, 10, "title", "desc",
                TaskStatus.IN_PROGRESS, "testing"
        );

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(taskRepository)
                .findByAppUserAndStatusAndTitleContainingIgnoreCase(
                        currentUser,
                        TaskStatus.IN_PROGRESS,
                        "testing",
                        pageable
                );
    }

    @Test
    void getMyTasks_blankKeywordWithStatus_usesStatusOnlyQuery() {
        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("createdAt").descending()
        );

        Page<Task> taskPage =
                new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(taskRepository.findByAppUserAndStatus(
                currentUser, TaskStatus.DONE, pageable
        )).thenReturn(taskPage);

        Page<TaskResponseDto> response = taskService.getMyTasks(
                0, 10, "createdAt", "desc", TaskStatus.DONE, "   "
        );

        assertNotNull(response);

        verify(taskRepository).findByAppUserAndStatus(
                currentUser, TaskStatus.DONE, pageable
        );
        verify(taskRepository, never())
                .findByAppUserAndStatusAndTitleContainingIgnoreCase(
                        any(AppUser.class),
                        any(TaskStatus.class),
                        anyString(),
                        any(Pageable.class)
                );
    }

    @Test
    void getMyTasks_blankKeywordWithoutStatus_usesUnfilteredQuery() {
        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("createdAt").descending()
        );

        Page<Task> taskPage =
                new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(taskRepository.findByAppUser(currentUser, pageable))
                .thenReturn(taskPage);

        Page<TaskResponseDto> response = taskService.getMyTasks(
                0, 10, "createdAt", "desc", null, " "
        );

        assertNotNull(response);

        verify(taskRepository).findByAppUser(currentUser, pageable);
    }

    @Test
    void getMyTasks_uppercaseAscDirection_usesAscendingSort() {
        Pageable expectedPageable = PageRequest.of(
                1, 5, Sort.by("title").ascending()
        );

        when(taskRepository.findByAppUser(currentUser, expectedPageable))
                .thenReturn(new PageImpl<>(
                        Collections.emptyList(), expectedPageable, 0
                ));

        taskService.getMyTasks(
                1, 5, "title", "ASC", null, null
        );

        verify(taskRepository).findByAppUser(
                currentUser, expectedPageable
        );
    }

    @Test
    void getMyTasks_nonAscDirection_usesDescendingSort() {
        Pageable expectedPageable = PageRequest.of(
                0, 5, Sort.by("title").descending()
        );

        when(taskRepository.findByAppUser(currentUser, expectedPageable))
                .thenReturn(new PageImpl<>(
                        Collections.emptyList(), expectedPageable, 0
                ));

        taskService.getMyTasks(
                0, 5, "title", "invalid-direction", null, null
        );

        verify(taskRepository).findByAppUser(
                currentUser, expectedPageable
        );
    }

    @Test
    void getMyTasks_negativePage_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getMyTasks(
                        -1, 10, "createdAt", "desc", null, null
                )
        );

        verify(taskRepository, never()).findByAppUser(
                any(AppUser.class), any(Pageable.class)
        );
    }

    @Test
    void getMyTasks_zeroPageSize_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getMyTasks(
                        0, 0, "createdAt", "desc", null, null
                )
        );

        verify(taskRepository, never()).findByAppUser(
                any(AppUser.class), any(Pageable.class)
        );
    }

    // --------------------------------------------------
    // GET TASK BY ID
    // --------------------------------------------------

    @Test
    void getTaskById_existingOwnedTask_returnsTask() {
        Task task = new Task();

        when(taskRepository.findByIdAndAppUser(1L, currentUser))
                .thenReturn(Optional.of(task));

        TaskResponseDto response = taskService.getTaskById(1L);

        assertNotNull(response);

        verify(securityUtil, times(1)).getCurrentUser();
        verify(taskRepository, times(1))
                .findByIdAndAppUser(1L, currentUser);
    }

    @Test
    void getTaskById_nonexistentOrUnownedTask_throwsResourceNotFoundException() {
        when(taskRepository.findByIdAndAppUser(99L, currentUser))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(99L)
        );

        assertEquals("Task not found.", exception.getMessage());

        verify(taskRepository).findByIdAndAppUser(99L, currentUser);
    }

    // --------------------------------------------------
    // UPDATE TASK
    // --------------------------------------------------

    @Test
    void updateTask_existingOwnedTask_updatesAndSavesTask() {
        Task existingTask = new Task();

        when(taskRepository.findByIdAndAppUser(1L, currentUser))
                .thenReturn(Optional.of(existingTask));

        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        TaskResponseDto response = taskService.updateTask(
                1L, taskRequestDto
        );

        assertNotNull(response);

        verify(taskRepository).findByIdAndAppUser(1L, currentUser);
        verify(taskRepository).save(existingTask);

        assertEquals("Complete testing", existingTask.getTitle());
        assertEquals(
                "Write unit tests for TaskServiceImpl",
                existingTask.getDescription()
        );
    }

    @Test
    void updateTask_nonexistentOrUnownedTask_throwsExceptionWithoutSaving() {
        when(taskRepository.findByIdAndAppUser(99L, currentUser))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.updateTask(99L, taskRequestDto)
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_repositorySaveFails_propagatesException() {
        Task existingTask = new Task();

        when(taskRepository.findByIdAndAppUser(1L, currentUser))
                .thenReturn(Optional.of(existingTask));

        when(taskRepository.save(existingTask))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.updateTask(1L, taskRequestDto)
        );

        assertEquals("Database error", exception.getMessage());
    }

    // --------------------------------------------------
    // UPDATE TASK STATUS
    // --------------------------------------------------

    @Test
    void updateTaskStatus_existingOwnedTask_updatesStatus() {
        Task existingTask = new Task();

        when(taskRepository.findByIdAndAppUser(1L, currentUser))
                .thenReturn(Optional.of(existingTask));

        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        TaskResponseDto response = taskService.updateTaskStatus(
                1L, TaskStatus.DONE
        );

        assertNotNull(response);
        assertEquals(TaskStatus.DONE, existingTask.getStatus());

        verify(taskRepository).save(existingTask);
    }

    @Test
    void updateTaskStatus_nonexistentOrUnownedTask_throwsExceptionWithoutSaving() {
        when(taskRepository.findByIdAndAppUser(99L, currentUser))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.updateTaskStatus(99L, TaskStatus.DONE)
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    // --------------------------------------------------
    // DELETE TASK
    // --------------------------------------------------

    @Test
    void deleteTask_existingOwnedTask_deletesTask() {
        Task existingTask = new Task();

        when(taskRepository.findByIdAndAppUser(1L, currentUser))
                .thenReturn(Optional.of(existingTask));

        taskService.deleteTask(1L);

        verify(taskRepository).findByIdAndAppUser(1L, currentUser);
        verify(taskRepository, times(1)).delete(existingTask);
    }

    @Test
    void deleteTask_nonexistentOrUnownedTask_throwsExceptionWithoutDeleting() {
        when(taskRepository.findByIdAndAppUser(99L, currentUser))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.deleteTask(99L)
        );

        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void deleteTask_repositoryThrowsException_propagatesException() {
        Task existingTask = new Task();

        when(taskRepository.findByIdAndAppUser(1L, currentUser))
                .thenReturn(Optional.of(existingTask));

        doThrow(new RuntimeException("Delete failed"))
                .when(taskRepository).delete(existingTask);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.deleteTask(1L)
        );

        assertEquals("Delete failed", exception.getMessage());
        
    }
}
