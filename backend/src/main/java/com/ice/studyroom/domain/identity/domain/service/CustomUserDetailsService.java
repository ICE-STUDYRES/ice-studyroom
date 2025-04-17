package com.ice.studyroom.domain.identity.domain.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.identity.domain.SecurityUser;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Member user = memberRepository.findByEmail(Email.of(username))
			.orElseThrow(() -> {
				log.warn("사용자를 찾을 수 없습니다. - email: {}", username);
				return new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
			});
		return new SecurityUser(user);
	}
}
