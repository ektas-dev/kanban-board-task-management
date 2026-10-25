package com.boardtask.kanban.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.enums.Role;
import com.boardtask.kanban.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;

	@Bean
	CommandLineRunner createAdmin() {
		return args -> {
			if (!appUserRepository.existsByEmail("admin@example.com")) {
				AppUser admin = AppUser.builder().name("Admin").email("admin@example.com")
						.password(passwordEncoder.encode("Admin@123")).role(Role.ADMIN).build();
				appUserRepository.save(admin);
				System.out.println("Admin account created successfully.");
			}
		};
	}
}
