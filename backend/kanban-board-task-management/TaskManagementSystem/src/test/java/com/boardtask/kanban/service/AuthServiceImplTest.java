package com.boardtask.kanban.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.boardtask.kanban.dto.auth.AuthResponseDto;
import com.boardtask.kanban.dto.auth.LoginRequestDto;
import com.boardtask.kanban.dto.auth.RegisterRequestDto;
import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.enums.Role;
import com.boardtask.kanban.repository.AppUserRepository;
import com.boardtask.kanban.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@Mock
	private AppUserRepository appUserRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtService jwtService;

	@InjectMocks
	private AuthServiceImpl authService;

	private RegisterRequestDto registerRequest;
	private LoginRequestDto loginRequest;

	@BeforeEach
	void setUp() {
		registerRequest = mock(RegisterRequestDto.class);
		loginRequest = mock(LoginRequestDto.class);
	}

	// ---------------------------------------------------------
	// register()
	// ---------------------------------------------------------

	@Test
	void register_newUser_returnsAuthResponse() {
		when(registerRequest.getName()).thenReturn("John Doe");
		when(registerRequest.getEmail()).thenReturn("john@example.com");
		when(registerRequest.getPassword()).thenReturn("rawPassword");

		when(appUserRepository.existsByEmail("john@example.com")).thenReturn(false);
		when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

		AppUser savedUser = mock(AppUser.class);

		when(savedUser.getName()).thenReturn("John Doe");
		when(savedUser.getEmail()).thenReturn("john@example.com");
		when(savedUser.getPassword()).thenReturn("encodedPassword");
		when(savedUser.getRole()).thenReturn(Role.USER);

		when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);
		when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

		AuthResponseDto response = authService.register(registerRequest);

		assertNotNull(response);
		assertEquals("jwt-token", response.getToken());
		assertEquals("Bearer", response.getTokenType());
		assertEquals("John Doe", response.getName());
		assertEquals("john@example.com", response.getEmail());
		assertEquals(Role.USER, response.getRole());

		verify(appUserRepository).existsByEmail("john@example.com");
		verify(passwordEncoder).encode("rawPassword");

		ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);

		verify(appUserRepository).save(userCaptor.capture());

		AppUser userToSave = userCaptor.getValue();

		assertEquals("John Doe", userToSave.getName());
		assertEquals("john@example.com", userToSave.getEmail());
		assertEquals("encodedPassword", userToSave.getPassword());
		assertEquals(Role.USER, userToSave.getRole());

		ArgumentCaptor<UserDetails> userDetailsCaptor = ArgumentCaptor.forClass(UserDetails.class);

		verify(jwtService).generateToken(userDetailsCaptor.capture());

		UserDetails tokenUser = userDetailsCaptor.getValue();

		assertEquals("john@example.com", tokenUser.getUsername());
		assertEquals("encodedPassword", tokenUser.getPassword());

		assertEquals(java.util.Set.of(new SimpleGrantedAuthority("ROLE_USER")), tokenUser.getAuthorities());

		verify(authenticationManager, never()).authenticate(any());
	}

	@Test
	void register_emailAlreadyExists_throwsRuntimeException() {
		when(registerRequest.getEmail()).thenReturn("john@example.com");

		when(appUserRepository.existsByEmail("john@example.com")).thenReturn(true);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(registerRequest));

		assertEquals("Email already registered", exception.getMessage());

		verify(appUserRepository).existsByEmail("john@example.com");
		verify(appUserRepository, never()).save(any(AppUser.class));

		verifyNoInteractions(passwordEncoder, jwtService, authenticationManager);
	}

	@Test
	void register_passwordEncoderFails_propagatesException() {
		when(registerRequest.getName()).thenReturn("John Doe");
		when(registerRequest.getEmail()).thenReturn("john@example.com");
		when(registerRequest.getPassword()).thenReturn("rawPassword");

		when(appUserRepository.existsByEmail("john@example.com")).thenReturn(false);

		when(passwordEncoder.encode("rawPassword")).thenThrow(new IllegalStateException("Encoding failed"));

		IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> authService.register(registerRequest));

		assertEquals("Encoding failed", exception.getMessage());

		verify(appUserRepository, never()).save(any(AppUser.class));
		verifyNoInteractions(jwtService, authenticationManager);
	}

	// ---------------------------------------------------------
	// login()
	// ---------------------------------------------------------

	@Test
	void login_validCredentials_returnsAuthResponse() {
		when(loginRequest.getEmail()).thenReturn("john@example.com");
		when(loginRequest.getPassword()).thenReturn("rawPassword");

		UserDetails principal = User.withUsername("john@example.com").password("encodedPassword")
				.authorities("ROLE_USER").build();

		Authentication authentication = mock(Authentication.class);

		when(authentication.getPrincipal()).thenReturn(principal);

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);

		when(jwtService.generateToken(principal)).thenReturn("jwt-token");

		AppUser user = mock(AppUser.class);

		when(user.getName()).thenReturn("John Doe");
		when(user.getEmail()).thenReturn("john@example.com");
		when(user.getRole()).thenReturn(Role.USER);

		when(appUserRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

		AuthResponseDto response = authService.login(loginRequest);

		assertNotNull(response);
		assertEquals("jwt-token", response.getToken());
		assertEquals("Bearer", response.getTokenType());
		assertEquals("John Doe", response.getName());
		assertEquals("john@example.com", response.getEmail());
		assertEquals(Role.USER, response.getRole());

		ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = ArgumentCaptor
				.forClass(UsernamePasswordAuthenticationToken.class);

		verify(authenticationManager).authenticate(authCaptor.capture());

		UsernamePasswordAuthenticationToken credentials = authCaptor.getValue();

		assertEquals("john@example.com", credentials.getPrincipal());
		assertEquals("rawPassword", credentials.getCredentials());

		verify(jwtService).generateToken(principal);
		verify(appUserRepository).findByEmail("john@example.com");
	}

	@Test
	void login_invalidCredentials_propagatesAuthenticationException() {
		when(loginRequest.getEmail()).thenReturn("john@example.com");
		when(loginRequest.getPassword()).thenReturn("wrongPassword");

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

		verifyNoInteractions(jwtService);
		verify(appUserRepository, never()).findByEmail(any());
	}

	@Test
	void login_authenticatedUserNotFound_throwsRuntimeException() {
		when(loginRequest.getEmail()).thenReturn("john@example.com");
		when(loginRequest.getPassword()).thenReturn("rawPassword");

		UserDetails principal = User.withUsername("john@example.com").password("encodedPassword")
				.authorities("ROLE_USER").build();

		Authentication authentication = mock(Authentication.class);

		when(authentication.getPrincipal()).thenReturn(principal);

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);

		when(jwtService.generateToken(principal)).thenReturn("jwt-token");

		when(appUserRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

		RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));

		assertEquals("User not found", exception.getMessage());

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

		verify(jwtService).generateToken(principal);
		verify(appUserRepository).findByEmail("john@example.com");
	}

	@Test
	void login_jwtGenerationFails_propagatesException() {
		when(loginRequest.getEmail()).thenReturn("john@example.com");
		when(loginRequest.getPassword()).thenReturn("rawPassword");

		UserDetails principal = User.withUsername("john@example.com").password("encodedPassword")
				.authorities("ROLE_USER").build();

		Authentication authentication = mock(Authentication.class);

		when(authentication.getPrincipal()).thenReturn(principal);

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);

		when(jwtService.generateToken(principal)).thenThrow(new IllegalStateException("JWT generation failed"));

		IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> authService.login(loginRequest));

		assertEquals("JWT generation failed", exception.getMessage());

		verify(appUserRepository, never()).findByEmail(any());
	}
}