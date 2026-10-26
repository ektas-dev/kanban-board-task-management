package com.boardtask.kanban.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

	private static final String SECRET = "01234567890123456789012345678901";

	private static final long EXPIRATION = 3_600_000L;

	private JwtService jwtService;
	private UserDetails userDetails;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService();

		ReflectionTestUtils.setField(jwtService, "secret", SECRET);
		ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);

		userDetails = User.withUsername("john@example.com").password("encodedPassword").authorities("ROLE_USER")
				.build();
	}

	@Test
	void generateToken_returnsNonEmptyToken() {
		String token = jwtService.generateToken(userDetails);

		assertNotNull(token);
		assertFalse(token.isBlank());
	}

	@Test
	void generateToken_tokenContainsIssuedAtAndExpiration() {
		String token = jwtService.generateToken(userDetails);

		Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
		Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

		assertNotNull(issuedAt);
		assertNotNull(expiration);

		// JWT timestamps may have second-level precision.
		assertTrue(issuedAt.getTime() <= System.currentTimeMillis());

		assertEquals(EXPIRATION, expiration.getTime() - issuedAt.getTime());
	}

	@Test
	void extractUsername_validToken_returnsUsername() {
		String token = jwtService.generateToken(userDetails);

		assertEquals("john@example.com", jwtService.extractUsername(token));
	}

	@Test
	void extractUsername_invalidToken_throwsJwtException() {
		assertThrows(JwtException.class, () -> jwtService.extractUsername("invalid.token.value"));
	}

	@Test
	void extractClaim_validToken_returnsSubject() {
		String token = jwtService.generateToken(userDetails);

		String subject = jwtService.extractClaim(token, Claims::getSubject);

		assertEquals("john@example.com", subject);
	}

	@Test
	void extractClaim_validToken_returnsExpiration() {
		String token = jwtService.generateToken(userDetails);

		Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

		assertNotNull(expiration);
		assertTrue(expiration.after(new Date()));
	}

	@Test
	void isTokenValid_matchingUsername_returnsTrue() {
		String token = jwtService.generateToken(userDetails);

		assertTrue(jwtService.isTokenValid(token, userDetails));
	}

	@Test
	void isTokenValid_differentUsername_returnsFalse() {
		String token = jwtService.generateToken(userDetails);

		UserDetails anotherUser = User.withUsername("other@example.com").password("encodedPassword")
				.authorities("ROLE_USER").build();

		assertFalse(jwtService.isTokenValid(token, anotherUser));
	}

	@Test
	void isTokenValid_expiredToken_throwsJwtException() {
		String token = createToken("john@example.com", System.currentTimeMillis() - 10_000L);

		assertThrows(JwtException.class, () -> jwtService.isTokenValid(token, userDetails));
	}

	@Test
	void isTokenValid_wrongSigningKey_throwsJwtException() {
		SecretKey anotherKey = Keys.hmacShaKeyFor("abcdefghijklmnopqrstuvwxyz123456".getBytes());

		String token = Jwts.builder().setSubject("john@example.com").setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
				.signWith(anotherKey, SignatureAlgorithm.HS256).compact();

		assertThrows(JwtException.class, () -> jwtService.isTokenValid(token, userDetails));
	}

	private String createToken(String username, long expirationTime) {
		return Jwts.builder().setSubject(username).setIssuedAt(new Date(System.currentTimeMillis() - 20_000L))
				.setExpiration(new Date(expirationTime))
				.signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256).compact();
	}
}
