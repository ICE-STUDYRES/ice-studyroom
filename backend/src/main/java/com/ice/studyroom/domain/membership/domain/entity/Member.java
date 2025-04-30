package com.ice.studyroom.domain.membership.domain.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ice.studyroom.domain.membership.domain.vo.EncodedPassword;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.global.entity.BaseTimeEntity;

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
public class Member extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private Email email;

	@Embedded
	private EncodedPassword password;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "student_num", nullable = false)
	private String studentNum;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(
		name = "user_roles",
		joinColumns = @JoinColumn(name = "user_id")
	)
	@Column(name = "role", nullable = false)
	@JsonIgnore
	private List<String> roles = new ArrayList<>();

	@Column(name = "is_penalty", nullable = false)
	@Builder.Default
	private boolean isPenalty = false;

	public void updatePenalty(boolean isPenalty) {
		this.isPenalty = isPenalty;
	}

	public static Member create(Email email, EncodedPassword encodedPassword, String name, String studentNum) {
		return Member.builder()
			.email(email)
			.password(encodedPassword)
			.name(name)
			.studentNum(studentNum)
			.roles(List.of("ROLE_USER"))
			.build();
	}

	public void changePassword(EncodedPassword newPassword) {
		this.password = newPassword;
	}
}
