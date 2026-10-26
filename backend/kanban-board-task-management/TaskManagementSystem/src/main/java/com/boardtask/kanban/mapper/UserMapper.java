package com.boardtask.kanban.mapper;

import com.boardtask.kanban.dto.user.UserResponseDto;
import com.boardtask.kanban.entity.AppUser;

public class UserMapper {
	// Entity -> Response DTO
	public static UserResponseDto toResponseDto(AppUser user) {
		return UserResponseDto.builder().id(user.getId()).name(user.getName()).email(user.getEmail())
				.role(user.getRole()).build();
	}
}
