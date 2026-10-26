package com.boardtask.kanban.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.enums.Role;
import com.boardtask.kanban.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailServiceTest {

	@Mock
	private AppUserRepository appUserRepository;

	@InjectMocks
	private CustomUserDetailsService customUserDetailsService;

	private AppUser user;

	@BeforeEach
	void setUp() {
		user = org.mockito.Mockito.mock(AppUser.class);
	}

	// ---------------------------------------------------------
	// loadUserByUsername()
	// ---------------------------------------------------------

	@Test
	void loadUserByUsername_existingUser_returnsUserDetails() {
		String email = "john@example.com";
		String encodedPassword = "encodedPassword";

		when(user.getEmail()).thenReturn(email);
		when(user.getPassword()).thenReturn(encodedPassword);
		when(user.getRole()).thenReturn(Role.USER);

		when(appUserRepository.findByEmail(email)).thenReturn(Optional.of(user));

		UserDetails result = customUserDetailsService.loadUserByUsername(email);

		assertNotNull(result);
		assertEquals(email, result.getUsername());
		assertEquals(encodedPassword, result.getPassword());
		assertEquals(List.of(new SimpleGrantedAuthority("ROLE_USER")), List.copyOf(result.getAuthorities()));

		verify(appUserRepository).findByEmail(email);
		verifyNoMoreInteractions(appUserRepository);
	}

	@Test
	void loadUserByUsername_adminUser_returnsAdminAuthority() {
		String email = "admin@example.com";

		when(user.getEmail()).thenReturn(email);
		when(user.getPassword()).thenReturn("encodedPassword");
		when(user.getRole()).thenReturn(Role.ADMIN);

		when(appUserRepository.findByEmail(email)).thenReturn(Optional.of(user));

		UserDetails result = customUserDetailsService.loadUserByUsername(email);

		assertNotNull(result);
		assertEquals(email, result.getUsername());
		assertEquals(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), List.copyOf(result.getAuthorities()));

		verify(appUserRepository).findByEmail(email);
		verifyNoMoreInteractions(appUserRepository);
	}

	@Test
	void loadUserByUsername_missingUser_throwsUsernameNotFoundException() {
		String email = "missing@example.com";

		when(appUserRepository.findByEmail(email)).thenReturn(Optional.empty());

		UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
				() -> customUserDetailsService.loadUserByUsername(email));

		assertEquals("User not found with email: " + email, exception.getMessage());

		verify(appUserRepository).findByEmail(email);
		verifyNoMoreInteractions(appUserRepository);

		verify(user, never()).getEmail();
		verify(user, never()).getPassword();
		verify(user, never()).getRole();
	}
}
