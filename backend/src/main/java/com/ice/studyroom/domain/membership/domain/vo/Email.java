package com.ice.studyroom.domain.membership.domain.vo;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email implements Serializable {
	// private final String value;
	@Column(name = "email")
	private String value;

	private Email(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Email of(String value) {
		validateEmailFormat(value);
		return new Email(value);
	}

	private static void validateEmailFormat(String value) {
		if (!value.endsWith("@hufs.ac.kr")) {
			throw new IllegalArgumentException("이메일은 @hufs.ac.kr로 끝나야 합니다.");
		}
	}
}
