package com.ice.studyroom.domain.penalty.application;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyStatus;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.global.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class PenaltyServiceTest {

	@InjectMocks
	private PenaltyService penaltyService;

	@Mock
	private PenaltyRepository penaltyRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private Clock clock;

	@Mock
	private Member member;

	@Mock
	private Reservation reservation;

	@Mock
	private Penalty penalty;

	private Long reservationId;
	private Long memberId;

	@BeforeEach
	void setUp() {
		reservation = mock(Reservation.class);
		member = mock(Member.class);
		reservationId = 1L;
		memberId = 1L;
		penalty = mock(Penalty.class);
	}

	/**
	 * 📌 테스트명: CANCEL_사유로_패널티_부여시_종료일_확인_주말_미포함
	 *
	 * ✅ 목적:
	 *   - CANCEL 사유로 패널티 부여 시, 주말을 제외한 평일 기준으로 패널티 종료일이 정확히 계산되는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 현재 날짜를 2025년 4월 1일(화)로 고정
	 *   2. CANCEL 사유로 2일 간의 영업일 패널티 부여
	 *   3. 패널티 종료일은 주말을 포함하지 않기 때문에 4월 3일(목)로 계산되어야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - CANCEL 사유는 평일 기준 2일간의 패널티가 부여됨
	 *   - 패널티 종료일은 23:59:59로 고정됨
	 *
	 * 🧩 검증 포인트:
	 *   - `calculatePenaltyEnd()`의 결과가 영업일 기준 2일 후인지 확인
	 *   - 종료일이 정확히 4월 3일(목) 23:59:59인지 검증
	 *
	 * ✅ 기대 결과:
	 *   - 패널티 종료일: 2025-04-03T23:59:59
	 */
	@Test
	@DisplayName("[주말 미포함] CANCEL 사유로 패널티가 부여할 경우, 영업일 기준 2일 후에 종료된다.")
	void CANCEL_사유로_패널티_부여시_종료일_확인_주말_미포함(){
		Penalty savedPenalty = 이유별_예약_부여_셋업(4, 1, PenaltyReasonType.CANCEL);

		// expected: 4/1(화) 기준, 영업일 기준 2일 후 => 4/3(목)
		LocalDateTime expectedEnd = LocalDateTime.of(2025, 4, 3, 23, 59, 59);
		assertThat(savedPenalty.getPenaltyEnd()).isEqualTo(expectedEnd);
	}

	/**
	 * 📌 테스트명: CANCEL_사유로_패널티_부여시_종료일_확인_주말_포함
	 *
	 * ✅ 목적:
	 *   - CANCEL 사유로 패널티 부여 시, 영업일 기준(주말 제외)으로 종료일이 정확히 산출되는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 현재 날짜를 2025년 4월 3일(목)로 고정
	 *   2. CANCEL 사유로 2일간의 영업일 패널티 부여
	 *   3. 4/5(토), 4/6(일)은 주말이므로 포함되지 않고, 종료일은 4월 7일(월)로 계산되어야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - CANCEL 사유는 평일 기준 2일 간의 패널티가 부여됨
	 *   - 주말은 패널티 계산에서 제외됨
	 *
	 * 🧩 검증 포인트:
	 *   - `calculatePenaltyEnd()`가 주말을 건너뛰고 계산되는지 확인
	 *   - 종료일이 정확히 4월 7일(월) 23:59:59인지 검증
	 *
	 * ✅ 기대 결과:
	 *   - 패널티 종료일: 2025-04-07T23:59:59
	 */
	@Test
	@DisplayName("[주말 포함] CANCEL 사유로 패널티가 부여할 경우, 영업일 기준 2일 후에 종료된다.")
	void CANCEL_사유로_패널티_부여시_종료일_확인_주말_포함(){
		Penalty savedPenalty = 이유별_예약_부여_셋업(4, 3, PenaltyReasonType.CANCEL);

		// expected: 4/3(목) 기준, 영업일 기준 2일 후 => 4/7(월)
		LocalDateTime expectedEnd = LocalDateTime.of(2025, 4, 7, 23, 59, 59);
		assertThat(savedPenalty.getPenaltyEnd()).isEqualTo(expectedEnd);
	}

	/**
	 * 📌 테스트명: LATE_사유로_패널티_부여시_종료일_확인_주말_미포함
	 *
	 * ✅ 목적:
	 *   - LATE 사유로 패널티 부여 시, 주말이 포함되지 않은 상황에서 종료일이 영업일 기준으로 3일이 정확히 계산되는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 현재 날짜를 2025년 4월 1일(화)로 고정
	 *   2. LATE 사유로 평일 기준 3일 패널티 부여
	 *   3. 주말이 포함되지 않으므로 종료일은 4월 4일(금)로 계산
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - LATE 사유는 영업일 기준 3일의 패널티 부여
	 *
	 * 🧩 검증 포인트:
	 *   - `calculatePenaltyEnd()` 호출 결과가 정확히 3일 후인지 검증
	 *
	 * ✅ 기대 결과:
	 *   - Penalty 종료일: 2025-04-04T23:59:59
	 */
	@Test
	@DisplayName("[주말 미포함] LATE 사유로 패널티가 부여할 경우, 영업일 기준 3일 후에 종료된다.")
	void LATE_사유로_패널티_부여시_종료일_확인_주말_미포함(){
		Penalty savedPenalty = 이유별_예약_부여_셋업(4, 1, PenaltyReasonType.LATE);

		// expected: 4/1(화) 기준, 영업일 기준 3일 후 => 4/4(금)
		LocalDateTime expectedEnd = LocalDateTime.of(2025, 4, 4, 23, 59, 59);
		assertThat(savedPenalty.getPenaltyEnd()).isEqualTo(expectedEnd);
	}


	/**
	 * 📌 테스트명: LATE_사유로_패널티_부여시_종료일_확인_주말_포함
	 *
	 * ✅ 목적:
	 *   - LATE 사유로 패널티 부여 시, 주말을 포함하더라도 영업일 기준으로 종료일이 정확히 계산되는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 현재 날짜를 2025년 4월 3일(목)로 고정
	 *   2. 평일 기준 3일의 패널티가 부여되며 4/5~6은 주말
	 *   3. 종료일은 4월 8일(화)로 계산되어야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 주말을 제외한 평일 기준으로 LATE 패널티 3일 부여
	 *
	 * 🧩 검증 포인트:
	 *   - `calculatePenaltyEnd()` 로직이 주말을 건너뛰고 3일 후를 계산하는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - Penalty 종료일: 2025-04-08T23:59:59
	 */
	@Test
	@DisplayName("[주말 포함] LATE 사유로 패널티가 부여할 경우, 영업일 기준 3일 후에 종료된다.")
	void LATE_사유로_패널티_부여시_종료일_확인_주말_포함(){
		Penalty savedPenalty = 이유별_예약_부여_셋업(4, 3, PenaltyReasonType.LATE);

		// expected: 4/3(목) 기준, 영업일 기준 3일 후 => 4/8(화)
		LocalDateTime expectedEnd = LocalDateTime.of(2025, 4, 8, 23, 59, 59);
		assertThat(savedPenalty.getPenaltyEnd()).isEqualTo(expectedEnd);
	}


	/**
	 * 📌 테스트명: NO_SHOW_사유로_패널티_부여시_종료일_확인
	 *
	 * ✅ 목적:
	 *   - NO_SHOW 사유로 부여된 패널티가 영업일 기준 5일 후에 종료되는지 검증 (주말 포함 상황)
	 *
	 * 🧪 시나리오 설명:
	 *   1. 현재 날짜를 2025년 4월 3일(목)로 고정
	 *   2. NO_SHOW 사유로 영업일 기준 5일간 패널티 부여
	 *   3. 주말(4/5~6)을 건너뛰어 종료일은 4월 10일(목)로 계산됨
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - NO_SHOW 사유는 영업일 기준 5일의 패널티가 부여됨
	 *   - 종료일은 항상 23:59:59로 고정됨
	 *
	 * 🧩 검증 포인트:
	 *   - 영업일 계산에서 주말을 건너뛰고 정확히 5일 후인지 확인
	 *
	 * ✅ 기대 결과:
	 *   - Penalty 종료일: 2025-04-10T23:59:59
	 */
	@Test
	@DisplayName("NO_SHOW 사유로 패널티를 부여할 경우, 평일 기준 5일 후에 종료된다.")
	void NO_SHOW_사유로_패널티_부여시_종료일_확인(){
		Penalty savedPenalty = 이유별_예약_부여_셋업(4, 3, PenaltyReasonType.NO_SHOW);

		// expected: 4/3(목) 기준, 영업일 기준 5일 후 => 4/13(목), NO_SHOW 일 경우 무조건 주말이 포함된다.
		LocalDateTime expectedEnd = LocalDateTime.of(2025, 4, 10, 23, 59, 59);
		assertThat(savedPenalty.getPenaltyEnd()).isEqualTo(expectedEnd);
	}

	/**
	 * 📌 테스트명: ADMIN_권한으로_패널티_부여시_종료일_확인
	 *
	 * ✅ 목적:
	 *   - 관리자가 직접 지정한 날짜까지 패널티가 정확히 적용되는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 관리자가 특정 종료일(LocalDateTime)을 지정하여 패널티 부여 요청
	 *   2. 패널티 저장 시 `penaltyEnd` 필드가 해당 날짜로 설정되어야 함
	 *   3. 회원의 패널티 상태도 `true`로 업데이트되어야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - ADMIN은 시스템에서 직접 패널티 종료일을 지정할 수 있다
	 *   - 지정한 날짜로 Penalty가 생성되고 저장되어야 한다
	 *
	 * 🧩 검증 포인트:
	 *   - `PenaltyRepository.save()`로 저장된 Penalty 객체의 종료일이 입력값과 일치하는지 검증
	 *   - `member.updatePenalty(true)` 호출 여부 검증
	 *
	 * ✅ 기대 결과:
	 *   - 저장된 Penalty 객체의 종료일이 2025-04-03T14:30:00임
	 *   - 회원의 패널티 상태가 true로 갱신됨
	 */
	@Test
	@DisplayName("ADMIN 권한으로 패널티가 부여할 경우, ADMIN 이 입력한 날짜까지 패널티가 부여된다.")
	void ADMIN_권한으로_패널티_부여시_종료일_확인(){
		ArgumentCaptor<Penalty> captor = ArgumentCaptor.forClass(Penalty.class);

		// when
		LocalDateTime expectedEnd = LocalDateTime.of(2025, 4, 3, 14, 30);
		penaltyService.adminAssignPenalty(member, expectedEnd);

		//then
		verify(penaltyRepository).save(captor.capture());
		verify(member).updatePenalty(true);
		Penalty savedPenalty = captor.getValue();
		assertThat(savedPenalty.getPenaltyEnd()).isEqualTo(expectedEnd);
	}

	/**
	 * 📌 테스트명: ADMIN_권한으로_패널티_삭제시_패널티_무효_확인
	 *
	 * ✅ 목적:
	 *   - 관리자 권한으로 유효한 패널티를 삭제할 경우, 해당 패널티가 무효화되고 회원의 패널티 상태가 갱신되는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 회원의 ID로 유효한 패널티(PenaltyStatus.VALID)를 조회
	 *   2. 해당 패널티에 대해 `expirePenalty()`를 호출해 무효화
	 *   3. 동시에 `member.updatePenalty(false)`가 호출되어야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - ADMIN은 현재 유효한 패널티에 대해서만 삭제 처리할 수 있다
	 *   - 삭제 시 Penalty 상태는 INVALID로 변경되며, 멤버의 패널티 상태도 해제된다
	 *
	 * 🧩 검증 포인트:
	 *   - `penalty.expirePenalty()`가 호출되는지 확인
	 *   - `member.updatePenalty(false)`가 정확히 호출되는지 검증
	 *
	 * ✅ 기대 결과:
	 *   - 해당 Penalty가 무효화됨
	 *   - 멤버의 패널티 상태가 false로 전환됨
	 */
	@Test
	@DisplayName("ADMIN 권한으로 패널티를 삭제할 경우, 해당 패널티는 무효화된다.")
	void ADMIN_권한으로_패널티_삭제시_패널티_무효_확인(){
		given(member.getId()).willReturn(memberId);
		given(penaltyRepository.findByMemberIdAndStatus(member.getId(), PenaltyStatus.VALID)).willReturn(
			Optional.of(penalty));

		// when
		penaltyService.adminDeletePenalty(member);

		// then
		verify(penalty).expirePenalty();
		verify(member).updatePenalty(false);
	}

	/**
	 * 📌 테스트명: ADMIN_권한으로_삭제할_패널티가_유효한_패널티가_아닐_경우
	 *
	 * ✅ 목적:
	 *   - 관리자가 삭제 요청한 패널티가 유효하지 않은 경우 예외가 발생하는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. `findByMemberIdAndStatus()` 호출 시 유효한 패널티가 조회되지 않음 (Optional.empty)
	 *   2. 예외 `BusinessException`이 발생해야 함
	 *   3. 이후에 `member.updatePenalty()` 같은 부가 작업이 수행되어서는 안 됨
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 유효한 상태의 패널티가 존재하지 않으면 삭제 요청은 실패해야 한다
	 *
	 * 🧩 검증 포인트:
	 *   - BusinessException 발생 여부 확인
	 *   - 메시지: "유효하지 않은 패널티입니다."가 정확히 매칭되는지 확인
	 *   - `member.updatePenalty()`가 호출되지 않아야 함
	 *
	 * ✅ 기대 결과:
	 *   - 예외 발생 (BusinessException)
	 *   - 회원의 상태 변경 로직이 실행되지 않음
	 */
	@Test
	@DisplayName("ADMIN 권한으로 삭제하려는 패널티가 유효한 패널티가 아닐 경우 예외 발생")
	void ADMIN_권한으로_삭제할_패널티가_유효한_패널티가_아닐_경우(){
		given(member.getId()).willReturn(memberId);
		given(penaltyRepository.findByMemberIdAndStatus(memberId, PenaltyStatus.VALID))
			.willReturn(Optional.empty());

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			penaltyService.adminDeletePenalty(member)
		);

		assertEquals("유효하지 않은 패널티입니다.", ex.getMessage());

		// then (호출되지 않아야 함)
		verify(member, never()).updatePenalty(anyBoolean());
	}

	private Penalty 이유별_예약_부여_셋업(int month, int dayOfMonth, PenaltyReasonType reason) {
		현재_날짜_고정(month, dayOfMonth);

		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		ArgumentCaptor<Penalty> captor = ArgumentCaptor.forClass(Penalty.class);

		// when
		penaltyService.assignPenalty(member, reservationId, reason);

		// then
		verify(penaltyRepository).save(captor.capture());
		verify(member).updatePenalty(true);
		return captor.getValue();
	}

	private void 현재_날짜_고정(int month, int dayOfMonth) {
		LocalDateTime fixedNow = LocalDateTime.of(2025, month, dayOfMonth, 13, 1);
		given(clock.instant()).willReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());
	}
}
