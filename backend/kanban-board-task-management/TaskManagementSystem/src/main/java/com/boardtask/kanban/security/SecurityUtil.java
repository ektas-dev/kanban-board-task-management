package com.boardtask.kanban.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.exception.ResourceNotFoundException;
import com.boardtask.kanban.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

	private final AppUserRepository appUserRepository;

	public static String getCurrentUserEmail() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	public AppUser getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		return appUserRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found."));
	}

}