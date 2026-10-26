package com.boardtask.kanban.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.boardtask.kanban.config.SecurityConfig;
import com.boardtask.kanban.dto.task.TaskRequestDto;
import com.boardtask.kanban.dto.task.TaskResponseDto;
import com.boardtask.kanban.dto.task.TaskStatusUpdateDto;
import com.boardtask.kanban.enums.TaskStatus;
import com.boardtask.kanban.security.JwtAuthenticationFilter;
import com.boardtask.kanban.security.JwtService;
import com.boardtask.kanban.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TaskService taskService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@MockitoBean
	private UserDetailsService userDetailsService;

	private org.springframework.test.web.servlet.request.RequestPostProcessor authenticatedUser() {
		return user("testuser").authorities(new SimpleGrantedAuthority("ROLE_USER"));
	}

	private TaskResponseDto createTaskResponse() {
		return TaskResponseDto.builder().id(1L).title("Test task").build();
	}

	// ---------------------------------------------------------
	// POST /api/tasks
	// ---------------------------------------------------------

	@Test
	void createTask_validRequest_returnsCreated() throws Exception {
		TaskRequestDto request = new TaskRequestDto();
		request.setTitle("Test task");
		request.setDescription("Test description");

		TaskResponseDto response = createTaskResponse();

		when(taskService.createTask(any(TaskRequestDto.class))).thenReturn(response);

		mockMvc.perform(post("/api/tasks").with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.title").value("Test task"));

		verify(taskService).createTask(any(TaskRequestDto.class));
	}

	// ---------------------------------------------------------
	// GET /api/tasks
	// ---------------------------------------------------------

	@Test
	void getMyTasks_defaultParameters_returnsOk() throws Exception {
		TaskResponseDto response = createTaskResponse();

		Page<TaskResponseDto> page = new PageImpl<>(List.of(response),
				PageRequest.of(0, 5, Sort.by("createdAt").descending()), 1);

		when(taskService.getMyTasks(0, 5, "createdAt", "desc", null, null)).thenReturn(page);

		mockMvc.perform(get("/api/tasks").with(authenticatedUser())).andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].title").value("Test task"))
				.andExpect(jsonPath("$.totalElements").value(1));

		verify(taskService).getMyTasks(0, 5, "createdAt", "desc", null, null);
	}

	@Test
	void getMyTasks_customParameters_passesFiltersToService() throws Exception {

		TaskResponseDto response = createTaskResponse();

		Page<TaskResponseDto> page = new PageImpl<>(List.of(response),
				PageRequest.of(1, 10, Sort.by("title").ascending()), 1);

		when(taskService.getMyTasks(1, 10, "title", "asc", TaskStatus.IN_PROGRESS, "urgent")).thenReturn(page);

		mockMvc.perform(get("/api/tasks").with(authenticatedUser()).param("page", "1").param("size", "10")
				.param("sortBy", "title").param("direction", "asc").param("status", "IN_PROGRESS")
				.param("keyword", "urgent")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1));

		verify(taskService).getMyTasks(1, 10, "title", "asc", TaskStatus.IN_PROGRESS, "urgent");
	}

	@Test
	void getMyTasks_invalidStatus_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/tasks").with(authenticatedUser()).param("status", "INVALID_STATUS"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(taskService);
	}

	// ---------------------------------------------------------
	// GET /api/tasks/{taskId}
	// ---------------------------------------------------------

	@Test
	void getTaskById_taskExists_returnsOk() throws Exception {
		TaskResponseDto response = createTaskResponse();

		when(taskService.getTaskById(1L)).thenReturn(response);

		mockMvc.perform(get("/api/tasks/{taskId}", 1L).with(authenticatedUser())).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.title").value("Test task"));

		verify(taskService).getTaskById(1L);
	}

	// ---------------------------------------------------------
	// PUT /api/tasks/{taskId}
	// ---------------------------------------------------------

	@Test
	void updateTask_validRequest_returnsOk() throws Exception {
		TaskRequestDto request = new TaskRequestDto();
		request.setTitle("Updated task");
		request.setDescription("Updated description");

		// Create a real DTO with the expected values
		TaskResponseDto response = TaskResponseDto.builder().id(1L).title("Updated task").build();

		// Mock the service response
		when(taskService.updateTask(eq(1L), any(TaskRequestDto.class))).thenReturn(response);

		mockMvc.perform(put("/api/tasks/{taskId}", 1L).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.title").value("Updated task"));

		verify(taskService).updateTask(eq(1L), any(TaskRequestDto.class));
	}

	// ---------------------------------------------------------
	// DELETE /api/tasks/{taskId}
	// ---------------------------------------------------------

	@Test
	void deleteTask_taskExists_returnsNoContent() throws Exception {
		mockMvc.perform(delete("/api/tasks/{taskId}", 1L).with(authenticatedUser())).andExpect(status().isNoContent());

		verify(taskService).deleteTask(1L);
	}

	// ---------------------------------------------------------
	// PATCH /api/tasks/{taskId}/status
	// ---------------------------------------------------------

	@Test
	void updateTaskStatus_validRequest_returnsOk() throws Exception {
		TaskStatusUpdateDto request = new TaskStatusUpdateDto();
		request.setStatus(TaskStatus.DONE);

		TaskResponseDto response = TaskResponseDto.builder().id(1L).title("Test task").build();

		when(taskService.updateTaskStatus(1L, TaskStatus.DONE)).thenReturn(response);

		mockMvc.perform(patch("/api/tasks/{taskId}/status", 1L).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.title").value("Test task"));

		verify(taskService).updateTaskStatus(1L, TaskStatus.DONE);
	}

	@Test
	void updateTaskStatus_invalidStatus_returnsBadRequest() throws Exception {

		mockMvc.perform(patch("/api/tasks/{taskId}/status", 1L).with(authenticatedUser())
				.contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INVALID_STATUS\"}"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(taskService);
	}

	@Test
	void createTask_malformedJson_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/tasks").with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON)
				.content("{invalid-json")).andExpect(status().isBadRequest());

		verifyNoInteractions(taskService);
	}
}
