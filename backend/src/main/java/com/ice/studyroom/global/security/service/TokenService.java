package com.ice.studyroom.global.security.service;

import java.time.Duration;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.ice.studyroom.global.security.jwt.JwtToken;
import com.ice.studyroom.global.security.jwt.JwtTokenProvider;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.service.CacheService;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
	private final JwtTokenProvider jwtTokenProvider;
	private final CacheService cacheService;
	private static final String REFRESH_TOKEN_PREFIX = "RT:";
	private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(7);

	public String extractEmailFromAccessToken(String authorizationHeader) {
		String accessToken = authorizationHeader.replace("Bearer ", "");
		return jwtTokenProvider.getUsername(accessToken);
	}

	public void saveRefreshToken(String email, String refreshToken) {
		String key = REFRESH_TOKEN_PREFIX + email;
		cacheService.save(key, refreshToken, REFRESH_TOKEN_VALIDITY);
	}

	/**
	 * 	RefreshToken은 JWT가 아니기에 여기에 두기는 했는데 잘 모르겠네. 애초에 Refresh Token은 JwtProvider에서 만드니 거기에서 하는게 좋을 것 같을지도
	 * 	그러면 JwtProvider에서 REDIS_TEMPLATE를 사용해야한다. 너무 많은 책임이 들어가는 것 같네
	 */
	private boolean validateRefreshToken(String email, String refreshToken) {
		if (email == null || refreshToken == null) {
			return false;
		}

		String key = REFRESH_TOKEN_PREFIX + email;
		String savedRefreshToken = cacheService.get(key);

		return savedRefreshToken.equals(refreshToken);
	}

	public JwtToken rotateToken(String email, String accessToken, String refreshToken) {
		if (!validateRefreshToken(email, refreshToken)) {
			throw new BusinessException(StatusCode.UNAUTHORIZED, "유효하지 않은 Refresh Token 입니다.");
		}

		//1. AccessToken에서 Role 추출
		String role = jwtTokenProvider.getRoleFromToken(accessToken).replace("ROLE_", "");

		// 2. 새로운 토큰 쌍 생성
		// Authentication 객체 생성을 위한 UserDetails 로드
		UserDetails userDetails = User.builder()
			.username(email)
			.password("") // 불필요하지만 필수 필드
			.roles(role)
			.build();

		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, "", userDetails.getAuthorities());

		JwtToken newToken = jwtTokenProvider.generateToken(authentication);

		log.info("Refresh Token 재발급 - 사용자 이메일: {}", email);

		// 3. 새로운 Refresh Token을 Redis에 저장
		saveRefreshToken(email, newToken.getRefreshToken());

		return newToken;
	}

	public void deleteToken(String email, String refreshToken) {
		String key = REFRESH_TOKEN_PREFIX + email;
		cacheService.delete(key);
	}
}
