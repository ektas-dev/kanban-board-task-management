package com.boardtask.kanban.mapper;

import com.boardtask.kanban.dto.auth.AuthResponseDto;
import com.boardtask.kanban.dto.auth.RegisterRequestDto;
import com.boardtask.kanban.entity.AppUser;

public class AuthMapper {

	// DTO -> Entity
	public static AppUser toEntity(RegisterRequestDto dto) {
		AppUser user = new AppUser();
		user.setName(dto.getName());
		user.setEmail(dto.getEmail());
		user.setPassword(dto.getPassword());
		return user;
	}

	// Entity -> DTO
	public static AuthResponseDto toAuthResponseDto(AppUser appUser, String token) {
		return AuthResponseDto.builder()
				.token(token)
				.tokenType("Bearer")
				.name(appUser.getName())
				.email(appUser.getEmail())
				.role(appUser.getRole())
				.build();
	}
}
