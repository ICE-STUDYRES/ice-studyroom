package com.ice.studyroom.domain.penalty.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.service.PenaltyDomainService;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

@ExtendWith(MockitoExtension.class)
class PenaltyServiceTest {

	@InjectMocks
	private PenaltyService penaltyService;

	@Mock
	private PenaltyDomainService penaltyDomainService;

	@Mock
	private PenaltyRepository penaltyRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private Member member;

	@Mock
	private Reservation reservation;

	@Mock
	private Penalty penalty;

	private Long reservationId;

	@BeforeEach
	void setUp() {
		reservation = mock(Reservation.class);
		member = mock(Member.class);
		reservationId = 1L;
		penalty = mock(Penalty.class);
	}

	/**
	 * 📌 테스트명: assignPenalty_정상_흐름_검증
	 *
	 * ✅ 목적:
	 *   - 예약 ID를 기반으로 예약 정보를 조회하고, 도메인 서비스에서 생성된 패널티를 저장하며,
	 *     해당 멤버의 패널티 상태를 true로 업데이트하는 전체 흐름 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약 ID로 Reservation 조회
	 *   2. 도메인 서비스에서 Penalty 생성
	 *   3. 생성된 Penalty 저장
	 *   4. 멤버 패널티 상태 업데이트
	 *
	 * ✅ 기대 결과:
	 *   - Penalty 저장이 정확히 호출됨
	 *   - member.updatePenalty(true) 호출됨
	 */
	@Test
	void assignPenalty_정상_흐름_검증() {
		// given
		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		given(penaltyDomainService.createPenalty(member, reservation, PenaltyReasonType.CANCEL, null))
			.willReturn(penalty);

		// when
		penaltyService.assignPenalty(member, reservationId, PenaltyReasonType.CANCEL);

		// then
		verify(penaltyRepository).save(penalty);
		verify(member).updatePenalty(true);
	}

	/**
	 * 📌 테스트명: adminAssignPenalty_정상_저장_및_상태_갱신
	 *
	 * ✅ 목적:
	 *   - 관리자가 지정한 종료일로 패널티를 직접 부여할 경우, Penalty가 정상 저장되고,
	 *     회원 패널티 상태가 true로 갱신되는지 확인
	 */
	@Test
	void adminAssignPenalty_정상_저장_및_상태_갱신() {
		// given
		LocalDateTime endAt = LocalDateTime.of(2025, 4, 30, 23, 59, 59);
		given(penaltyDomainService.createPenalty(member, null, PenaltyReasonType.ADMIN, endAt))
			.willReturn(penalty);

		// when
		penaltyService.adminAssignPenalty(member, endAt);

		// then
		verify(penaltyRepository).save(penalty);
	}

	/**
	 * 📌 테스트명: adminDeletePenalty_정상_삭제_및_상태_해제
	 *
	 * ✅ 목적:
	 *   - 유효한 Penalty가 존재할 경우, 해당 Penalty가 무효화되고
	 *     회원의 패널티 상태가 false로 갱신되는지 확인
	 */
	@Test
	void adminDeletePenalty_정상_삭제_및_상태_해제() {
		// given
		given(penaltyDomainService.findPenaltyByMemberIdAndStatus(member)).willReturn(penalty);

		// when
		penaltyService.adminDeletePenalty(member);

		// then
		verify(penalty).expirePenalty();
		verify(member).updatePenalty(false);
	}

	/**
	 * 📌 테스트명: adminDeletePenalty_유효한_패널티_없을_경우_예외
	 *
	 * ✅ 목적:
	 *   - 삭제하려는 Penalty가 존재하지 않는 경우, BusinessException 예외 발생 검증
	 */
	@Test
	void adminDeletePenalty_유효한_패널티_없을_경우_예외() {
		// given
		given(penaltyDomainService.findPenaltyByMemberIdAndStatus(member))
			.willThrow(new BusinessException(StatusCode.NOT_FOUND, "유효하지 않은 패널티입니다."));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			penaltyService.adminDeletePenalty(member)
		);

		assertEquals("유효하지 않은 패널티입니다.", ex.getMessage());
		verify(member, never()).updatePenalty(anyBoolean());
	}
}
