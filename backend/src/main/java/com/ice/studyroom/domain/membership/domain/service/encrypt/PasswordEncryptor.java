package com.ice.studyroom.domain.membership.domain.service.encrypt;

import com.ice.studyroom.domain.membership.domain.vo.EncodedPassword;
import com.ice.studyroom.domain.membership.domain.vo.RawPassword;

public interface PasswordEncryptor {
	/**
	 * 원시 비밀번호(RawPassword)를 암호화(해싱)합니다.
	 *
	 * @param rawPassword 암호화할 원시 비밀번호
	 * @return 암호화된 비밀번호 객체
	 */
	EncodedPassword encrypt(RawPassword rawPassword);
	
	/**
	 * 입력된 원시 비밀번호와 저장된 암호화 비밀번호가 일치하는지 확인합니다.
	 *
	 * @param rawPassword 사용자가 입력한 원시 비밀번호
	 * @param encodedPassword 저장된 암호화된 비밀번호
	 * @return 비밀번호가 일치하면 true, 일치하지 않으면 false
	 */
	boolean matches(RawPassword rawPassword, EncodedPassword encodedPassword);
}
