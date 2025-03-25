package com.ice.studyroom.domain.membership.domain.vo;

import java.io.Serializable;
import java.util.Objects;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

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

	public Email(String value) {
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
			throw new BusinessException(StatusCode.BAD_REQUEST, "이메일은 @hufs.ac.kr로 끝나야 합니다.");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Email)) return false;
		Email email = (Email) o;
		return Objects.equals(value, email.value);
	}
}
