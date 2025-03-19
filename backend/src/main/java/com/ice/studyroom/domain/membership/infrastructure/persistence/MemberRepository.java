package com.ice.studyroom.domain.membership.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Member getMemberByEmail(Email email);

	Optional<Member> findByEmail(Email email);

	List<Member> findByEmailIn(Collection<Email> email);

	boolean existsByEmail(Email email);

	boolean existsByStudentNum(String studentNum);
}
