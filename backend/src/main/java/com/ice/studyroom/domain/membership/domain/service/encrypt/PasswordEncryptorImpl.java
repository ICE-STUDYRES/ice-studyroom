package com.ice.studyroom.domain.membership.domain.service.encrypt;

import com.ice.studyroom.domain.membership.domain.vo.EncodedPassword;
import com.ice.studyroom.domain.membership.domain.vo.RawPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordEncryptorImpl implements PasswordEncryptor {

	private final PasswordEncoder passwordEncoder;

	@Override
	public EncodedPassword encrypt(RawPassword rawPassword) {
		return EncodedPassword.of(passwordEncoder.encode(rawPassword.getValue()));
	}

	@Override
	public boolean matches(RawPassword rawPassword, EncodedPassword encodedPassword) {
		return passwordEncoder.matches(rawPassword.getValue(), encodedPassword.getValue());
	}
}
