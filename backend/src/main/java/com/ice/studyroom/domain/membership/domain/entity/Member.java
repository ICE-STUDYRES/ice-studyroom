package com.ice.studyroom.domain.membership.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.ice.studyroom.domain.membership.domain.vo.Email;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private Email email;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "student_num", nullable = false)
	private String studentNum;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
		name = "user_roles",
		joinColumns = @JoinColumn(name = "user_id")
	)
	@Column(name = "role")
	@Builder.Default
	private List<String> roles = new ArrayList<>();

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	public Member(Email email, String password, String name, String studentNum, List<String> roles) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.studentNum = studentNum;
		this.roles = roles != null ? roles : new ArrayList<>();
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public static Member create(Email email, String name, String rawPassword, String studentNum,
		PasswordEncoder passwordEncoder) {
		return Member.builder()
			.email(email)
			.name(name)
			.password(passwordEncoder.encode(rawPassword)) // 비밀번호 해싱
			.studentNum(studentNum)
			.roles(List.of("ROLE_USER"))
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();
	}

	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public boolean isPasswordValid(String rawPassword, PasswordEncoder passwordEncoder) {
		return passwordEncoder.matches(rawPassword, this.password);
	}
}
