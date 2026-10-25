package com.boardtask.kanban.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.boardtask.kanban.dto.admin.AdminDashboardResponseDto;
import com.boardtask.kanban.dto.task.TaskResponseDto;
import com.boardtask.kanban.dto.user.UserResponseDto;
import com.boardtask.kanban.security.JwtService;
import com.boardtask.kanban.service.AdminService;

import org.springframework.context.annotation.Import;
import com.boardtask.kanban.config.SecurityConfig;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdminService adminService;

	@MockitoBean
	private JwtService jwtService;
	
	@MockitoBean
	private UserDetailsService userDetailsService;

	private UserResponseDto userDto;
	private TaskResponseDto taskDto;
	private AdminDashboardResponseDto dashboardDto;

	@BeforeEach
	void setUp() {
		userDto = UserResponseDto.builder().id(1L).name("John Doe").email("john@example.com").build();

		taskDto = TaskResponseDto.builder().id(10L).title("Test task").build();

		dashboardDto = AdminDashboardResponseDto.builder().totalUsers(10L).totalTasks(20L).completedTasks(5L)
				.toDoTasks(8L).inProgressTasks(7L).build();
	}

	// ---------------------------------------------------------
	// GET /api/admin/users
	// ---------------------------------------------------------

	@Test
	void getAllUsers_defaultParameters_returnsOk() throws Exception {
		Page<UserResponseDto> page = new PageImpl<>(List.of(userDto),
				PageRequest.of(0, 5, Sort.by("createdAt").descending()), 1);

		when(adminService.getAllUsers(0, 5, "createdAt", "desc")).thenReturn(page);

		mockMvc.perform(
				get("/api/admin/users").with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].name").value("John Doe"))
				.andExpect(jsonPath("$.content[0].email").value("john@example.com"))
				.andExpect(jsonPath("$.totalElements").value(1));

		verify(adminService).getAllUsers(0, 5, "createdAt", "desc");
	}

	@Test
	void getAllUsers_customParameters_passesParametersToService() throws Exception {

		Page<UserResponseDto> page = new PageImpl<>(List.of(userDto),
				PageRequest.of(2, 10, Sort.by("name").ascending()), 21);

		when(adminService.getAllUsers(2, 10, "name", "asc")).thenReturn(page);

		mockMvc.perform(get("/api/admin/users").param("page", "2").param("size", "10").param("sortBy", "name")
				.param("direction", "asc").with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(21));

		verify(adminService).getAllUsers(2, 10, "name", "asc");
	}

	// ---------------------------------------------------------
	// GET /api/admin/users/{userId}
	// ---------------------------------------------------------

	@Test
	void getUserById_userExists_returnsOk() throws Exception {
		when(adminService.getUserById(1L)).thenReturn(userDto);

		mockMvc.perform(get("/api/admin/users/{userId}", 1L)
				.with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.name").value("John Doe"))
				.andExpect(jsonPath("$.email").value("john@example.com"));

		verify(adminService).getUserById(1L);
	}

	// ---------------------------------------------------------
	// DELETE /api/admin/users/{userId}
	// ---------------------------------------------------------

	@Test
	void deleteUser_userExists_returnsNoContent() throws Exception {
		doNothing().when(adminService).deleteUser(1L);

		mockMvc.perform(delete("/api/admin/users/{userId}", 1L)
				.with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
				.andExpect(status().isNoContent());

		verify(adminService).deleteUser(1L);
	}

	// ---------------------------------------------------------
	// GET /api/admin/tasks
	// ---------------------------------------------------------

	@Test
	void getAllTasks_defaultParameters_returnsOk() throws Exception {
		Page<TaskResponseDto> page = new PageImpl<>(List.of(taskDto),
				PageRequest.of(0, 5, Sort.by("createdAt").descending()), 1);

		when(adminService.getAllTasks(0, 5, "createdAt", "desc")).thenReturn(page);

		mockMvc.perform(
				get("/api/admin/tasks").with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(10))
				.andExpect(jsonPath("$.content[0].title").value("Test task"))
				.andExpect(jsonPath("$.totalElements").value(1));

		verify(adminService).getAllTasks(0, 5, "createdAt", "desc");
	}

	@Test
	void getAllTasks_customParameters_passesParametersToService() throws Exception {

		Page<TaskResponseDto> page = new PageImpl<>(List.of(taskDto),
				PageRequest.of(1, 20, Sort.by("title").ascending()), 25);

		when(adminService.getAllTasks(1, 20, "title", "asc")).thenReturn(page);

		mockMvc.perform(get("/api/admin/tasks").param("page", "1").param("size", "20").param("sortBy", "title")
				.param("direction", "asc").with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(20));

		verify(adminService).getAllTasks(1, 20, "title", "asc");
	}

	// ---------------------------------------------------------
	// GET /api/admin/tasks/{taskId}
	// ---------------------------------------------------------

	@Test
	void getTaskById_taskExists_returnsOk() throws Exception {
		when(adminService.getTaskById(10L)).thenReturn(taskDto);

		mockMvc.perform(get("/api/admin/tasks/{taskId}", 10L)
				.with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10)).andExpect(jsonPath("$.title").value("Test task"));

		verify(adminService).getTaskById(10L);
	}

	// ---------------------------------------------------------
	// DELETE /api/admin/tasks/{taskId}
	// ---------------------------------------------------------

	@Test
	void deleteTaskById_taskExists_returnsNoContent() throws Exception {
		doNothing().when(adminService).deleteTask(10L);

		mockMvc.perform(delete("/api/admin/tasks/{taskId}", 10L)
				.with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
				.andExpect(status().isNoContent());

		verify(adminService).deleteTask(10L);
	}

	// ---------------------------------------------------------
	// GET /api/admin/dashboard
	// ---------------------------------------------------------

	@Test
	void getDashboard_returnsDashboardStatistics() throws Exception {
		when(adminService.getDashboard()).thenReturn(dashboardDto);

		mockMvc.perform(
				get("/api/admin/dashboard").with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalUsers").value(10))
				.andExpect(jsonPath("$.totalTasks").value(20)).andExpect(jsonPath("$.completedTasks").value(5))
				.andExpect(jsonPath("$.toDoTasks").value(8)).andExpect(jsonPath("$.inProgressTasks").value(7));

		verify(adminService).getDashboard();
	}

	// ---------------------------------------------------------
	// Authorization
	// ---------------------------------------------------------

	@Test
	void getDashboard_unauthenticatedUser_returnsUnauthorized() throws Exception {

		mockMvc.perform(get("/api/admin/dashboard")).andExpect(status().isUnauthorized());
	}

	@Test
	void getDashboard_authenticatedNonAdmin_returnsForbidden() throws Exception {

		mockMvc.perform(get("/api/admin/dashboard")
				.with(user("regular-user").authorities(new SimpleGrantedAuthority("ROLE_USER"))))
				.andExpect(status().isForbidden());

		verifyNoMoreInteractions(adminService);
	}
}
