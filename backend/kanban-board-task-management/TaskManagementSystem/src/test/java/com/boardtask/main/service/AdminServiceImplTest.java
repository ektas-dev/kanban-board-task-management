package com.boardtask.main.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.kanban.dto.admin.AdminDashboardResponseDto;
import com.example.kanban.dto.task.TaskResponseDto;
import com.example.kanban.dto.user.UserResponseDto;
import com.example.kanban.entity.AppUser;
import com.example.kanban.entity.Task;
import com.example.kanban.enums.TaskStatus;
import com.example.kanban.exception.ResourceNotFoundException;
import com.example.kanban.mapper.TaskMapper;
import com.example.kanban.mapper.UserMapper;
import com.example.kanban.repository.AppUserRepository;
import com.example.kanban.repository.TaskRepository;
import com.example.kanban.service.AdminServiceImpl;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AppUserRepository appUserRepository;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(
                taskRepository, appUserRepository);
    }

    // ---------------------------------------------------------
    // getAllUsers
    // ---------------------------------------------------------

    @ParameterizedTest
    @MethodSource("sortDirections")
    void getAllUsers_validPagination_returnsMappedPage(
            String direction, Sort.Direction expectedDirection) {

        int pageNumber = 1;
        int pageSize = 5;
        String sortBy = "id";

        AppUser user = mock(AppUser.class);
        UserResponseDto userDto = mock(UserResponseDto.class);

        Page<AppUser> userPage = new PageImpl<>(
                List.of(user),
                PageRequest.of(pageNumber, pageSize,
                        Sort.by(expectedDirection, sortBy)),
                1);

        when(appUserRepository.findAll(
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(userPage);

        try (MockedStatic<UserMapper> mapper =
                     mockStatic(UserMapper.class)) {

            mapper.when(() -> UserMapper.toResponseDto(user))
                    .thenReturn(userDto);

            Page<UserResponseDto> result = adminService.getAllUsers(
                    pageNumber, pageSize, sortBy, direction);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(pageNumber, result.getNumber());
            assertEquals(pageSize, result.getSize());
            assertEquals(userDto, result.getContent().get(0));

            ArgumentCaptor<Pageable> captor =
                    ArgumentCaptor.forClass(Pageable.class);

            verify(appUserRepository).findAll(captor.capture());

            Pageable actualPageable = captor.getValue();
            assertEquals(pageNumber, actualPageable.getPageNumber());
            assertEquals(pageSize, actualPageable.getPageSize());
            assertEquals(
                    expectedDirection,
                    actualPageable.getSort().getOrderFor(sortBy)
                            .getDirection());

            mapper.verify(() -> UserMapper.toResponseDto(user), times(1));
        }
    }

    static Stream<org.junit.jupiter.params.provider.Arguments>
            sortDirections() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        "asc", Sort.Direction.ASC),
                org.junit.jupiter.params.provider.Arguments.of(
                        "desc", Sort.Direction.DESC),
                org.junit.jupiter.params.provider.Arguments.of(
                        "ASC", Sort.Direction.ASC),
                org.junit.jupiter.params.provider.Arguments.of(
                        "DESC", Sort.Direction.DESC));
    }

    @Test
    void getAllUsers_emptyRepositoryPage_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        when(appUserRepository.findAll(
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        Page<UserResponseDto> result =
                adminService.getAllUsers(0, 10, "id", "asc");

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getNumberOfElements());
        verify(appUserRepository).findAll(pageable);
    }

    // ---------------------------------------------------------
    // getUserById
    // ---------------------------------------------------------

    @Test
    void getUserById_existingUser_returnsMappedDto() {
        Long userId = 10L;
        AppUser user = mock(AppUser.class);
        UserResponseDto expectedDto = mock(UserResponseDto.class);

        when(appUserRepository.findById(userId))
                .thenReturn(Optional.of(user));

        try (MockedStatic<UserMapper> mapper =
                     mockStatic(UserMapper.class)) {

            mapper.when(() -> UserMapper.toResponseDto(user))
                    .thenReturn(expectedDto);

            UserResponseDto result = adminService.getUserById(userId);

            assertEquals(expectedDto, result);
            verify(appUserRepository).findById(userId);
            mapper.verify(() -> UserMapper.toResponseDto(user), times(1));
        }
    }

    @Test
    void getUserById_missingUser_throwsResourceNotFoundException() {
        Long userId = 999L;

        when(appUserRepository.findById(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminService.getUserById(userId));

        assertEquals(
                "User not found with the id " + userId,
                exception.getMessage());

        verify(appUserRepository).findById(userId);
        verifyNoMoreInteractions(appUserRepository);
    }

    // ---------------------------------------------------------
    // deleteUser
    // ---------------------------------------------------------

    @Test
    void deleteUser_existingUser_deletesUser() {
        Long userId = 10L;
        AppUser user = mock(AppUser.class);

        when(appUserRepository.findById(userId))
                .thenReturn(Optional.of(user));

        adminService.deleteUser(userId);

        verify(appUserRepository).findById(userId);
        verify(appUserRepository).delete(user);
    }

    @Test
    void deleteUser_missingUser_throwsExceptionWithoutDeleting() {
        Long userId = 999L;

        when(appUserRepository.findById(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminService.deleteUser(userId));

        assertEquals(
                "User not found with the id " + userId,
                exception.getMessage());

        verify(appUserRepository).findById(userId);
        verify(appUserRepository, never())
                .delete(org.mockito.ArgumentMatchers.any(AppUser.class));
    }

    // ---------------------------------------------------------
    // getAllTasks
    // ---------------------------------------------------------

    @ParameterizedTest
    @MethodSource("sortDirections")
    void getAllTasks_validPagination_returnsMappedPage(
            String direction, Sort.Direction expectedDirection) {

        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "id";

        Task task = mock(Task.class);
        TaskResponseDto taskDto = mock(TaskResponseDto.class);

        Page<Task> taskPage = new PageImpl<>(
                List.of(task),
                PageRequest.of(pageNumber, pageSize,
                        Sort.by(expectedDirection, sortBy)),
                1);

        when(taskRepository.findAll(
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(taskPage);

        try (MockedStatic<TaskMapper> mapper =
                     mockStatic(TaskMapper.class)) {

            mapper.when(() -> TaskMapper.toResponseDto(task))
                    .thenReturn(taskDto);

            Page<TaskResponseDto> result = adminService.getAllTasks(
                    pageNumber, pageSize, sortBy, direction);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(taskDto, result.getContent().get(0));

            ArgumentCaptor<Pageable> captor =
                    ArgumentCaptor.forClass(Pageable.class);

            verify(taskRepository).findAll(captor.capture());

            Pageable actualPageable = captor.getValue();
            assertEquals(pageNumber, actualPageable.getPageNumber());
            assertEquals(pageSize, actualPageable.getPageSize());
            assertEquals(
                    expectedDirection,
                    actualPageable.getSort().getOrderFor(sortBy)
                            .getDirection());

            mapper.verify(() -> TaskMapper.toResponseDto(task), times(1));
        }
    }

    @Test
    void getAllTasks_emptyRepositoryPage_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        when(taskRepository.findAll(
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        Page<TaskResponseDto> result =
                adminService.getAllTasks(0, 10, "id", "asc");

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getNumberOfElements());
        verify(taskRepository).findAll(pageable);
    }

    // ---------------------------------------------------------
    // getTaskById
    // ---------------------------------------------------------

    @Test
    void getTaskById_existingTask_returnsMappedDto() {
        Long taskId = 20L;
        Task task = mock(Task.class);
        TaskResponseDto expectedDto = mock(TaskResponseDto.class);

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        try (MockedStatic<TaskMapper> mapper =
                     mockStatic(TaskMapper.class)) {

            mapper.when(() -> TaskMapper.toResponseDto(task))
                    .thenReturn(expectedDto);

            TaskResponseDto result = adminService.getTaskById(taskId);

            assertEquals(expectedDto, result);
            verify(taskRepository).findById(taskId);
            mapper.verify(() -> TaskMapper.toResponseDto(task), times(1));
        }
    }

    @Test
    void getTaskById_missingTask_throwsResourceNotFoundException() {
        Long taskId = 999L;

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminService.getTaskById(taskId));

        assertEquals(
                "Task not found with the id " + taskId,
                exception.getMessage());

        verify(taskRepository).findById(taskId);
        verifyNoMoreInteractions(taskRepository);
    }

    // ---------------------------------------------------------
    // deleteTask
    // ---------------------------------------------------------

    @Test
    void deleteTask_existingTask_deletesTask() {
        Long taskId = 20L;
        Task task = mock(Task.class);

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        adminService.deleteTask(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_missingTask_throwsExceptionWithoutDeleting() {
        Long taskId = 999L;

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminService.deleteTask(taskId));

        assertEquals(
                "Task not found with the id " + taskId,
                exception.getMessage());

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never())
                .delete(org.mockito.ArgumentMatchers.any(Task.class));
    }

    // ---------------------------------------------------------
    // getDashboard
    // ---------------------------------------------------------

    @Test
    void getDashboard_repositoryReturnsCounts_returnsCorrectDashboard() {
        when(appUserRepository.count()).thenReturn(12L);
        when(taskRepository.count()).thenReturn(30L);
        when(taskRepository.countByStatus(TaskStatus.DONE))
                .thenReturn(10L);
        when(taskRepository.countByStatus(TaskStatus.TODO))
                .thenReturn(8L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS))
                .thenReturn(12L);

        AdminDashboardResponseDto result = adminService.getDashboard();

        assertNotNull(result);
        assertEquals(12L, result.getTotalUsers());
        assertEquals(30L, result.getTotalTasks());
        assertEquals(10L, result.getCompletedTasks());
        assertEquals(8L, result.getToDoTasks());
        assertEquals(12L, result.getInProgressTasks());

        verify(appUserRepository, times(1)).count();
        verify(taskRepository, times(1)).count();
        verify(taskRepository, times(1))
                .countByStatus(TaskStatus.DONE);
        verify(taskRepository, times(1))
                .countByStatus(TaskStatus.TODO);
        verify(taskRepository, times(1))
                .countByStatus(TaskStatus.IN_PROGRESS);
        verifyNoMoreInteractions(appUserRepository, taskRepository);
    }

    @Test
    void getDashboard_noUsersOrTasks_returnsZeroCounts() {
        when(appUserRepository.count()).thenReturn(0L);
        when(taskRepository.count()).thenReturn(0L);
        when(taskRepository.countByStatus(TaskStatus.DONE))
                .thenReturn(0L);
        when(taskRepository.countByStatus(TaskStatus.TODO))
                .thenReturn(0L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS))
                .thenReturn(0L);

        AdminDashboardResponseDto result = adminService.getDashboard();

        assertNotNull(result);
        assertEquals(0L, result.getTotalUsers());
        assertEquals(0L, result.getTotalTasks());
        assertEquals(0L, result.getCompletedTasks());
        assertEquals(0L, result.getToDoTasks());
        assertEquals(0L, result.getInProgressTasks());
    }
}
