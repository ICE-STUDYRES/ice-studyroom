package com.ice.studyroom.domain.membership.domain.service.encrypt;

import com.ice.studyroom.domain.membership.domain.vo.EncodedPassword;
import com.ice.studyroom.domain.membership.domain.vo.RawPassword;

public interface PasswordEncryptor {
	EncodedPassword encrypt(RawPassword rawPassword);
	boolean matches(RawPassword rawPassword, EncodedPassword encodedPassword);
}
