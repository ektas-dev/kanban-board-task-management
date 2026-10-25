package com.boardtask.kanban.service;

import com.boardtask.kanban.dto.auth.AuthResponseDto;
import com.boardtask.kanban.dto.auth.LoginRequestDto;
import com.boardtask.kanban.dto.auth.RegisterRequestDto;

public interface AuthService {
	AuthResponseDto register(RegisterRequestDto registerRequestDto);

	AuthResponseDto login(LoginRequestDto loginRequestDto);

}
