package com.boardtask.kanban.service;

import com.boardtask.kanban.dto.user.ChangePasswordRequestDto;
import com.boardtask.kanban.dto.user.UpdateProfileRequestDto;
import com.boardtask.kanban.dto.user.UserResponseDto;

public interface UserService {

	UserResponseDto getProfile();

	UserResponseDto updateProfile(UpdateProfileRequestDto updateProfileRequestDto);

	void changePassword(ChangePasswordRequestDto changePasswordRequestDto);
}
