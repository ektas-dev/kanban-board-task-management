package com.boardtask.kanban.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
	@Mock
	private JwtService jwtService;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private FilterChain filterChain;

	@InjectMocks
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();

		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();

		request.setServletPath("/tasks");
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	// ---------------------------------------------------------
	// Auth endpoints
	// ---------------------------------------------------------

	@Test
	void doFilterInternal_authEndpoint_skipsJwtProcessing() throws ServletException, IOException {

		request.setServletPath("/auth/login");

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		verifyNoInteractions(jwtService, userDetailsService);
	}

	// ---------------------------------------------------------
	// Missing or invalid Authorization header
	// ---------------------------------------------------------

	@Test
	void doFilterInternal_missingAuthorizationHeader_continuesChain() throws ServletException, IOException {

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		verifyNoInteractions(jwtService, userDetailsService);
	}

	@Test
	void doFilterInternal_nonBearerAuthorizationHeader_continuesChain() throws ServletException, IOException {

		request.addHeader("Authorization", "Basic abc123");

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		verifyNoInteractions(jwtService, userDetailsService);
	}

	// ---------------------------------------------------------
	// Invalid JWT
	// ---------------------------------------------------------

	@Test
	void doFilterInternal_extractUsernameThrowsJwtException_continuesChain() throws ServletException, IOException {

		request.addHeader("Authorization", "Bearer invalid-token");

		when(jwtService.extractUsername("invalid-token")).thenThrow(new JwtException("Invalid JWT"));

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());

		verify(jwtService).extractUsername("invalid-token");
		verify(filterChain).doFilter(request, response);
		verifyNoInteractions(userDetailsService);
		verifyNoMoreInteractions(jwtService);
	}

	// ---------------------------------------------------------
	// Username extraction
	// ---------------------------------------------------------

	@Test
	void doFilterInternal_nullUsername_doesNotAuthenticate() throws ServletException, IOException {

		request.addHeader("Authorization", "Bearer token");

		when(jwtService.extractUsername("token")).thenReturn(null);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());

		verify(jwtService).extractUsername("token");
		verifyNoInteractions(userDetailsService);
		verifyNoMoreInteractions(jwtService);
		verify(filterChain).doFilter(request, response);
	}

	// ---------------------------------------------------------
	// Existing authentication
	// ---------------------------------------------------------

	@Test
	void doFilterInternal_existingAuthentication_skipsAuthenticationCreation() throws ServletException, IOException {

		request.addHeader("Authorization", "Bearer token");

		Authentication existingAuthentication = mock(Authentication.class);

		SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

		when(jwtService.extractUsername("token")).thenReturn("john@example.com");

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertSame(existingAuthentication, SecurityContextHolder.getContext().getAuthentication());

		verify(jwtService).extractUsername("token");
		verifyNoInteractions(userDetailsService);
		verifyNoMoreInteractions(jwtService);
		verify(filterChain).doFilter(request, response);
	}

	// ---------------------------------------------------------
	// Valid JWT
	// ---------------------------------------------------------

	@Test
	void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {

		String email = "john@example.com";
		String token = "valid-token";

		request.addHeader("Authorization", "Bearer " + token);

		UserDetails userDetails = User.withUsername(email).password("encoded-password").authorities("ROLE_USER")
				.build();

		when(jwtService.extractUsername(token)).thenReturn(email);

		when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

		when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		assertNotNull(authentication);
		assertEquals(userDetails, authentication.getPrincipal());
		assertNull(authentication.getCredentials());
		assertEquals(java.util.Set.copyOf(userDetails.getAuthorities()),
				java.util.Set.copyOf(authentication.getAuthorities()));
		assertNotNull(authentication.getDetails());

		verify(jwtService).extractUsername(token);
		verify(userDetailsService).loadUserByUsername(email);
		verify(jwtService).isTokenValid(token, userDetails);
		verify(filterChain).doFilter(request, response);
		verifyNoMoreInteractions(jwtService, userDetailsService);
	}

	// ---------------------------------------------------------
	// Token validation fails
	// ---------------------------------------------------------

	@Test
	void doFilterInternal_invalidToken_doesNotSetAuthentication() throws ServletException, IOException {

		String email = "john@example.com";
		String token = "invalid-token";

		request.addHeader("Authorization", "Bearer " + token);

		UserDetails userDetails = User.withUsername(email).password("encoded-password").authorities("ROLE_USER")
				.build();

		when(jwtService.extractUsername(token)).thenReturn(email);

		when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

		when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());

		verify(jwtService).extractUsername(token);
		verify(userDetailsService).loadUserByUsername(email);
		verify(jwtService).isTokenValid(token, userDetails);
		verify(filterChain).doFilter(request, response);
		verifyNoMoreInteractions(jwtService, userDetailsService);
	}
}
