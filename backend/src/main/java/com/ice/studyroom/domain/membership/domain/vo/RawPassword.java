package com.ice.studyroom.domain.membership.domain.vo;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RawPassword {

	@Column(name = "password", nullable = false)
	private String value;

	private RawPassword(String value) {
		validateValue(value);
		this.value = value;
	}

	public static RawPassword of(String value) {
		return new RawPassword(value);
	}

	public String getValue() {
		return value;
	}

	private static void validateValue(String value) {
		if (value == null || value.isBlank()) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "비밀번호는 필수 입력 항목입니다.");
		}

		// 비밀번호 정규식: 소문자 + 숫자 + 특수문자 하나 이상 포함, 8자 이상
		String passwordRegex = "^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[a-z\\d@$!%*?&]{8,}$";

		if (!value.matches(passwordRegex)) {
			throw new BusinessException(StatusCode.BAD_REQUEST,
				"비밀번호는 최소 8자 이상, 하나 이상의 소문자, 숫자 및 특수 문자를 포함해야 합니다.");
		}
	}

	public boolean isSamePassword(String otherPassword) {
		return this.value.equals(otherPassword);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		RawPassword that = (RawPassword) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}
}
