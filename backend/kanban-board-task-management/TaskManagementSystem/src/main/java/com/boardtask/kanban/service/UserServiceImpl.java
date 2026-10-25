package com.boardtask.kanban.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardtask.kanban.dto.user.ChangePasswordRequestDto;
import com.boardtask.kanban.dto.user.UpdateProfileRequestDto;
import com.boardtask.kanban.dto.user.UserResponseDto;
import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.exception.BadRequestException;
import com.boardtask.kanban.mapper.UserMapper;
import com.boardtask.kanban.repository.AppUserRepository;
import com.boardtask.kanban.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

	private final SecurityUtil securityUtil;
	private final PasswordEncoder passwordEncoder;
	private final AppUserRepository appUserRepository;

	@Override
	@Transactional
	public UserResponseDto getProfile() {
		AppUser currentUser = securityUtil.getCurrentUser();
		return UserMapper.toResponseDto(currentUser);
	}

	@Override
	@Transactional
	public UserResponseDto updateProfile(UpdateProfileRequestDto updateProfileRequestDto) {
		AppUser currentUser = securityUtil.getCurrentUser();
		currentUser.setName(updateProfileRequestDto.getName());
		appUserRepository.save(currentUser);
		return UserMapper.toResponseDto(currentUser);
	}

	@Override
	@Transactional
	public void changePassword(ChangePasswordRequestDto changePasswordRequestDto) {
		AppUser currentUser = securityUtil.getCurrentUser();
		if (!passwordEncoder.matches(changePasswordRequestDto.getCurrentPassword(), currentUser.getPassword())) {
			throw new BadRequestException("Current password is incorrect");
		}
		if (passwordEncoder.matches(changePasswordRequestDto.getNewPassword(), currentUser.getPassword())) {
			throw new BadRequestException("New password must be different from the current password.");
		}
		currentUser.setPassword(passwordEncoder.encode(changePasswordRequestDto.getNewPassword()));
		appUserRepository.save(currentUser);

	}

}
