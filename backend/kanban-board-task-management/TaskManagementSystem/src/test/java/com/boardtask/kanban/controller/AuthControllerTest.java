package com.boardtask.kanban.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.boardtask.kanban.dto.auth.AuthResponseDto;
import com.boardtask.kanban.dto.auth.LoginRequestDto;
import com.boardtask.kanban.dto.auth.RegisterRequestDto;
import com.boardtask.kanban.enums.Role;
import com.boardtask.kanban.security.JwtService;
import com.boardtask.kanban.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private JwtService jwtService;

	@Test
	void register_validRequest_returnsCreated() throws Exception {
		RegisterRequestDto request = new RegisterRequestDto();
		request.setName("John Doe");
		request.setEmail("john@example.com");
		request.setPassword("StrongPassword123!");

		AuthResponseDto response = AuthResponseDto.builder().token("test-jwt-token").tokenType("Bearer")
				.name("John Doe").email("john@example.com").role(Role.USER).build();

		when(authService.register(any(RegisterRequestDto.class))).thenReturn(response);

		mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").value("test-jwt-token"))
				.andExpect(jsonPath("$.tokenType").value("Bearer")).andExpect(jsonPath("$.name").value("John Doe"))
				.andExpect(jsonPath("$.email").value("john@example.com")).andExpect(jsonPath("$.role").value("USER"));

		verify(authService).register(any(RegisterRequestDto.class));
	}

	@Test
	void login_validRequest_returnsOk() throws Exception {
		LoginRequestDto request = new LoginRequestDto();
		request.setEmail("john@example.com");
		request.setPassword("StrongPassword123!");

		AuthResponseDto response = AuthResponseDto.builder().token("test-jwt-token").tokenType("Bearer")
				.name("John Doe").email("john@example.com").role(Role.USER).build();

		when(authService.login(any(LoginRequestDto.class))).thenReturn(response);

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("test-jwt-token"))
				.andExpect(jsonPath("$.tokenType").value("Bearer")).andExpect(jsonPath("$.name").value("John Doe"))
				.andExpect(jsonPath("$.email").value("john@example.com")).andExpect(jsonPath("$.role").value("USER"));

		verify(authService).login(any(LoginRequestDto.class));
	}

	@Test
	void register_invalidRequest_returnsBadRequest() throws Exception {
		RegisterRequestDto request = new RegisterRequestDto();
		request.setName("");
		request.setEmail("invalid-email");
		request.setPassword("");

		mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

		verifyNoInteractions(authService);
	}

	@Test
	void login_invalidRequest_returnsBadRequest() throws Exception {
		LoginRequestDto request = new LoginRequestDto();
		request.setEmail("invalid-email");
		request.setPassword("");

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

		verifyNoInteractions(authService);
	}

	@Test
	void register_malformedJson_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{invalid-json"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(authService);
	}

	@Test
	void login_malformedJson_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{invalid-json"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(authService);
	}
}