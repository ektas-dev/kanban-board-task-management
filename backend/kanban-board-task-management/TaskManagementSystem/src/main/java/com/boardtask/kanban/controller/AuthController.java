package com.boardtask.kanban.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardtask.kanban.dto.auth.AuthResponseDto;
import com.boardtask.kanban.dto.auth.LoginRequestDto;
import com.boardtask.kanban.dto.auth.RegisterRequestDto;
import com.boardtask.kanban.service.AuthService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Authentication", description = "User registration and login APIs")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * Registration of a user
	 * 
	 * @param registerRequestDto
	 * @return
	 */
	@PostMapping("/register")
	public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
		AuthResponseDto authResponseDto = authService.register(registerRequestDto);
		return new ResponseEntity<>(authResponseDto, HttpStatus.CREATED);
	}

	/**
	 * Login of user
	 * 
	 * @param loginRequestDto
	 * @return
	 */
	@PostMapping("/login")
	public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
		AuthResponseDto authResponseDto = authService.login(loginRequestDto);
		return ResponseEntity.ok(authResponseDto);
	}
}
