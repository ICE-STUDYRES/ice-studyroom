package com.ice.studyroom.domain.membership.domain.service.manage;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.service.encrypt.PasswordEncryptor;
import com.ice.studyroom.domain.membership.domain.vo.EncodedPassword;
import com.ice.studyroom.domain.membership.domain.vo.RawPassword;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import com.ice.studyroom.domain.membership.domain.util.MembershipLogUtil;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPasswordService {

	private final PasswordEncryptor passwordEncryptor;
	private final MemberRepository memberRepository;

	/**
	 * 회원의 비밀번호를 변경합니다.
	 * 기존 비밀번호 확인, 새로운 비밀번호의 유효성 검증, 암호화 후 저장까지 수행합니다.
	 *
	 * @param member            비밀번호를 변경하려는 회원 엔티티
	 * @param currentPassword   현재 비밀번호 (입력값)
	 * @param newPassword       새로 설정할 비밀번호
	 * @param confirmPassword   새 비밀번호 확인 입력값
	 * @throws BusinessException  비밀번호 불일치, 동일 비밀번호 재사용, 새 비밀번호 불일치 시 예외를 발생시킵니다.
	 */
	public void updateMemberPassword(Member member, RawPassword currentPassword, RawPassword newPassword, RawPassword confirmPassword) {
		validateCurrentPassword(member, currentPassword);
		validateNewPasswordDifference(currentPassword, newPassword);
		validateConfirmationMatch(newPassword, confirmPassword);

		EncodedPassword encoded = passwordEncryptor.encrypt(newPassword);
		member.changePassword(encoded);
		memberRepository.save(member);
	}

	/**
	 * 로그인 시 입력한 비밀번호가 저장된 비밀번호와 일치하는지 검증합니다.
	 * 인증 실패 시 예외를 발생시킵니다.
	 *
	 * @param member        로그인 시도 중인 회원 엔티티
	 * @param rawPassword   사용자가 입력한 비밀번호 문자열
	 * @throws BusinessException  비밀번호가 일치하지 않을 경우 예외를 발생시킵니다.
	 */
	public void assertPasswordMatches(Member member, String rawPassword) {
		RawPassword input = RawPassword.of(rawPassword);
		if (!passwordEncryptor.matches(input, member.getPassword())) {
			MembershipLogUtil.logWarn("로그인 실패 - 비밀번호 불일치");
			throw new BusinessException(StatusCode.BAD_REQUEST, "아이디 혹은 비밀번호가 일치하지 않습니다.");
		}
	}

	/**
	 * 사용자의 현재 비밀번호가 저장된 비밀번호와 일치하는지 검증합니다.
	 *
	 * @param member    비밀번호를 변경하려는 회원 엔티티
	 * @param current   사용자가 입력한 현재 비밀번호
	 * @throws BusinessException  현재 비밀번호가 일치하지 않을 경우 예외를 발생시킵니다.
	 */
	private void validateCurrentPassword(Member member, RawPassword current) {
		if (!passwordEncryptor.matches(current, member.getPassword())) {
			MembershipLogUtil.logWarn("비밀번호 변경 실패 - 기존 비밀번호 불일치");
			throw new BusinessException(StatusCode.UNAUTHORIZED, "기존 비밀번호가 일치하지 않습니다.");
		}
	}

	/**
	 * 현재 비밀번호와 새 비밀번호가 서로 다른지 검증합니다.
	 * 새 비밀번호가 기존 비밀번호와 같으면 변경을 허용하지 않습니다.
	 *
	 * @param current   현재 비밀번호
	 * @param newPass   새로 설정할 비밀번호
	 * @throws BusinessException  기존 비밀번호와 새 비밀번호가 동일한 경우 예외를 발생시킵니다.
	 */
	private void validateNewPasswordDifference(RawPassword current, RawPassword newPass) {
		if (current.equals(newPass)) {
			MembershipLogUtil.logWarn("비밀번호 변경 실패 - 기존의 비밀번호와 새로운 비밀번호 일치");
			throw new BusinessException(StatusCode.BAD_REQUEST, "기존 비밀번호와 새로운 비밀번호가 동일합니다.");
		}
	}

	/**
	 * 새 비밀번호와 비밀번호 확인 입력값이 일치하는지 검증합니다.
	 *
	 * @param newPass   새로 설정할 비밀번호
	 * @param confirm   새 비밀번호 확인 입력값
	 * @throws BusinessException  새 비밀번호와 확인 입력값이 일치하지 않을 경우 예외를 발생시킵니다.
	 */
	private void validateConfirmationMatch(RawPassword newPass, RawPassword confirm) {
		if (!newPass.equals(confirm)) {
			MembershipLogUtil.logWarn("비밀번호 변경 실패 - 새로운 비밀번호 간의 불일치 발생");
			throw new BusinessException(StatusCode.BAD_REQUEST, "새로운 비밀번호가 서로 일치하지 않습니다.");
		}
	}
}
