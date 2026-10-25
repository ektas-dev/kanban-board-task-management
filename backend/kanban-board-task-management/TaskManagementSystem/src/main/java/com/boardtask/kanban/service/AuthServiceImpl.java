package com.boardtask.kanban.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.boardtask.kanban.dto.auth.AuthResponseDto;
import com.boardtask.kanban.dto.auth.LoginRequestDto;
import com.boardtask.kanban.dto.auth.RegisterRequestDto;
import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.enums.Role;
import com.boardtask.kanban.repository.AppUserRepository;
import com.boardtask.kanban.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final AppUserRepository appUserRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	private final JwtService jwtService;

	@Override
	public AuthResponseDto register(RegisterRequestDto registerRequestDto) {
		// 1. Check email already exists
		if (appUserRepository.existsByEmail(registerRequestDto.getEmail())) {
			throw new RuntimeException("Email already registered");
		}
		// 2. Create new user
		AppUser user = AppUser.builder()
				.name(registerRequestDto.getName())
				.email(registerRequestDto.getEmail())
				.password(passwordEncoder.encode(registerRequestDto.getPassword()))
				.role(Role.USER)
				.build();
		// 3. Save user
		AppUser savedUser = appUserRepository.save(user);
		// 4. Generate JWT
		String token = jwtService.generateToken(new org.springframework.security.core.userdetails.User(
				savedUser.getEmail(),
				savedUser.getPassword(),
				java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
						"ROLE_" + savedUser.getRole().name()))));
		// 5. Return response
		return AuthResponseDto.builder()
				.token(token)
				.tokenType("Bearer")
				.name(savedUser.getName())
				.email(savedUser.getEmail())
				.role(savedUser.getRole())
				.build();

	}

	@Override
	public AuthResponseDto login(LoginRequestDto loginRequestDto) {
		// 1. Authenticate user
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						loginRequestDto.getEmail(),
						loginRequestDto.getPassword()));
		// 2. Generate JWT
		String token = jwtService.generateToken(
				(org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());
		// 3. Fetch user details
		AppUser user = appUserRepository.findByEmail(loginRequestDto.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));
		// 4. Return response
		return AuthResponseDto.builder()
				.token(token)
				.tokenType("Bearer")
				.name(user.getName())
				.email(user.getEmail())
				.role(user.getRole())
				.build();

	}

}
