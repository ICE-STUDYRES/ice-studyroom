package com.ice.studyroom.global.security.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.ice.studyroom.global.exception.jwt.JwtAuthenticationException;
import com.ice.studyroom.global.type.StatusCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {
	private final Key key;

	private static final long ACCESS_TOKEN_EXPIRE_TIME = 2 * 60 * 60 * 1000L;        // 2시간

	public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	// Member 정보를 가지고 AccessToken, RefreshToken을 생성하는 메서드
	public JwtToken generateToken(Authentication authentication) {
		// 권한 가져오기
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
			.collect(Collectors.joining(","));

		long now = (new Date()).getTime();

		// Access Token 생성
		Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
		String accessToken = Jwts.builder()
			.setSubject(authentication.getName())
			.claim("auth", authorities)
			.setExpiration(accessTokenExpiresIn)
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();

		// Refresh Token 생성
		String refreshToken = generateRandomRefreshToken();

		return JwtToken.builder()
			.grantType("Bearer")
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.role(authorities)
			.build();
	}

	// Jwt 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
	public Authentication getAuthentication(String accessToken) {
		// Jwt 토큰 복호화
		Claims claims = parseClaims(accessToken);

		if (claims.get("auth") == null) {
			throw new RuntimeException("권한 정보가 없는 토큰입니다.");
		}

		// 클레임에서 권한 정보 가져오기
		Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
			.map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
			.collect(Collectors.toList());

		// UserDetails 객체를 만들어서 Authentication return
		// UserDetails: interface, User: UserDetails를 구현한 class
		UserDetails principal = new org.springframework.security.core.userdetails.User(
			claims.getSubject(), "", authorities);
		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
	}

	// 토큰 정보를 검증하는 메서드
	public void validateToken(String token, boolean isRefreshTokenRequest) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token);
		} catch (SecurityException | MalformedJwtException | UnsupportedJwtException | SignatureException e) {
			log.info("Invalid JWT Token", e);
			throw new JwtAuthenticationException(StatusCode.UNAUTHORIZED, "Invalid JWT token");
		} catch (ExpiredJwtException e) {
			if (!isRefreshTokenRequest) {
				// 일반 요청에서만 예외 발생시킴
				throw new JwtAuthenticationException(StatusCode.UNAUTHORIZED, "Access token has expired");
			}
		} catch (IllegalArgumentException e) {
			throw new JwtAuthenticationException(StatusCode.UNAUTHORIZED, "JWT claims string is empty");
		}
	}

	private String generateRandomRefreshToken() {
		return UUID.randomUUID().toString();
	}

	public String getUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public String getRoleFromToken(String token) {
		return parseClaims(token).get("auth", String.class);
	}

	// 유효성 검증하지 않을 예정, 여기서는 validateToken에서 검증하기에 예외가 발생하더라도 정상작동
	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(accessToken)
				.getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}
}
