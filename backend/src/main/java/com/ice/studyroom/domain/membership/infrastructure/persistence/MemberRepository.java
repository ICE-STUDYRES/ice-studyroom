package com.ice.studyroom.domain.membership.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;

public interface MemberRepository extends JpaRepository<Member, Long> {
	boolean existsByEmail(Email email);

	Optional<Member> findByEmail(Email email);

	Member getMemberByEmail(Email email);
}
