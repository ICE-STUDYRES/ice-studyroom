package com.ice.studyroom.domain.membership.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EncodedPassword {

	@Column(name = "password", nullable = false)
	private String value;

	private EncodedPassword(String value) {
		this.value = value;
	}

	public static EncodedPassword of(String value) {
		return new EncodedPassword(value);
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		EncodedPassword that = (EncodedPassword) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}
}
