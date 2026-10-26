package com.boardtask.kanban.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.exception.ResourceNotFoundException;
import com.boardtask.kanban.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class SecurityUtilTest {

	@Mock
	private AppUserRepository appUserRepository;

	private SecurityUtil securityUtil;

	@BeforeEach
	void setUp() {
		securityUtil = new SecurityUtil(appUserRepository);

		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private void authenticateAs(String email) {
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	// ---------------------------------------------------------
	// getCurrentUserEmail()
	// ---------------------------------------------------------

	@Test
	void getCurrentUserEmail_authenticatedUser_returnsEmail() {
		authenticateAs("john@example.com");

		String result = SecurityUtil.getCurrentUserEmail();

		assertEquals("john@example.com", result);
	}

	// ---------------------------------------------------------
	// getCurrentUser()
	// ---------------------------------------------------------

	@Test
	void getCurrentUser_existingUser_returnsUser() {
		String email = "john@example.com";

		authenticateAs(email);

		AppUser expectedUser = new AppUser();

		when(appUserRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));

		AppUser result = securityUtil.getCurrentUser();

		assertSame(expectedUser, result);

		verify(appUserRepository).findByEmail(email);
		verifyNoMoreInteractions(appUserRepository);
	}

	@Test
	void getCurrentUser_missingUser_throwsResourceNotFoundException() {
		String email = "missing@example.com";

		authenticateAs(email);

		when(appUserRepository.findByEmail(email)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> securityUtil.getCurrentUser());

		assertEquals("User not found.", exception.getMessage());

		verify(appUserRepository).findByEmail(email);
		verifyNoMoreInteractions(appUserRepository);
	}
}
