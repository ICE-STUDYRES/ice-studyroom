package com.ice.studyroom.domain.penalty.domain.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;

@ExtendWith(MockitoExtension.class)
class PenaltyDomainServiceTest {

	@InjectMocks
	private PenaltyDomainService penaltyDomainService;

	@Mock
	private Clock clock;

	@Mock
	private Member member;

	@Mock
	private Reservation reservation;

	/**
	 * 📌 테스트명: LATE_사유_패널티_종료일_확인
	 *
	 * ✅ 목적:
	 *   - LATE 사유로 패널티 부여 시, 영업일 기준으로 3일 후 종료일이 정확히 계산되는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 현재 날짜를 2025년 4월 3일(목)로 고정
	 *   2. LATE 사유로 영업일 기준 3일간의 패널티 부여
	 *   3. 종료일은 주말을 건너뛰고 4월 8일(화)로 계산되어야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - LATE 사유는 영업일 기준 3일간 패널티를 부여
	 *   - 종료 시각은 23:59:59로 고정됨
	 *
	 * 🧩 검증 포인트:
	 *   - `calculatePenaltyEnd()` 호출 결과의 날짜 확인
	 *   - 종료일이 정확히 4월 8일(화) 23:59:59인지 검증
	 *
	 * ✅ 기대 결과:
	 *   - Penalty 종료일: 2025-04-08T23:59:59
	 */
	@Test
	@DisplayName("패널티의 사유가 LATE 일 경우, 영업일 기준 3일 후에 패널티가 종료된다.")
	void LATE_사유_패널티_종료일_확인() {
		현재_날짜_고정(4, 3);

		// when
		Penalty result = penaltyDomainService.createPenalty(member, reservation, PenaltyReasonType.LATE, null);

		// then
		assertThat(result.getPenaltyEnd()).isEqualTo(LocalDateTime.of(2025, 4, 8, 23, 59, 59));
	}

	/**
	 * 📌 테스트명: CANCEL_사유_패널티_종료일_확인
	 *
	 * ✅ 목적:
	 *   - CANCEL 사유로 패널티 부여 시, 영업일 기준 2일 후 종료일이 올바르게 계산되는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 현재 날짜를 2025년 4월 3일(목)로 고정
	 *   2. CANCEL 사유로 영업일 기준 2일 패널티 부여
	 *   3. 종료일은 주말을 제외하고 4월 7일(월)로 계산됨
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - CANCEL 사유는 영업일 기준 2일간의 패널티가 부여됨
	 *   - 종료 시각은 항상 23:59:59로 설정됨
	 *
	 * 🧩 검증 포인트:
	 *   - Penalty 종료일 계산이 영업일 기준으로 되는지 확인
	 *   - 종료일: 2025년 4월 7일(월) 23:59:59
	 *
	 * ✅ 기대 결과:
	 *   - Penalty 종료일: 2025-04-07T23:59:59
	 */
	@Test
	@DisplayName("패널티의 사유가 CANCEL 일 경우, 영업일 기준 2일 후에 패널티가 종료된다.")
	void CANCEL_사유_패널티_종료일_확인() {
		현재_날짜_고정(4, 3);

		// when
		Penalty result = penaltyDomainService.createPenalty(member, reservation, PenaltyReasonType.CANCEL, null);

		// then
		assertThat(result.getPenaltyEnd()).isEqualTo(LocalDateTime.of(2025, 4, 7, 23, 59, 59));
	}

	/**
	 * 📌 테스트명: NO_SHOW_사유_패널티_종료일_확인
	 *
	 * ✅ 목적:
	 *   - NO_SHOW 사유로 패널티 부여 시, 영업일 기준 5일 후 정확한 종료일이 설정되는지 확인
	 *
	 * 🧪 시나리오 설명:
	 *   1. 현재 날짜를 2025년 4월 3일(목)로 고정
	 *   2. NO_SHOW 사유는 평일 기준 5일 패널티 부여
	 *   3. 주말을 건너뛰고 종료일은 4월 10일(목)로 계산됨
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - NO_SHOW 사유는 영업일 기준 5일간 패널티 부여
	 *   - 종료일은 23:59:59로 설정
	 *
	 * 🧩 검증 포인트:
	 *   - `calculatePenaltyEnd()` 로직에서 주말 제외 로직이 정확한지 검증
	 *   - 종료일이 정확히 4월 10일(목)인지 확인
	 *
	 * ✅ 기대 결과:
	 *   - Penalty 종료일: 2025-04-10T23:59:59
	 */
	@Test
	@DisplayName("패널티의 사유가 NO_SHOW 일 경우, 영업일 기준 5일 후에 패널티가 종료된다.")
	void NO_SHOW_사유_패널티_종료일_확인() {
		현재_날짜_고정(4, 3);

		// when
		Penalty result = penaltyDomainService.createPenalty(member, reservation, PenaltyReasonType.NO_SHOW, null);

		// then
		assertThat(result.getPenaltyEnd()).isEqualTo(LocalDateTime.of(2025, 4, 10, 23, 59, 59));
	}

	/**
	 * 📌 테스트명: ADMIN_사유_패널티_종료일_확인
	 *
	 * ✅ 목적:
	 *   - ADMIN 사유로 수동 지정된 종료일이 정상적으로 반영되는지 확인
	 *
	 * 🧪 시나리오 설명:
	 *   1. ADMIN 권한으로 패널티 종료일을 수동 지정
	 *   2. Penalty 엔티티 생성 시 입력된 종료일이 그대로 반영되는지 검증
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - ADMIN 사유일 경우 penaltyEndAt을 직접 지정 가능
	 *   - 계산 로직을 거치지 않고 바로 저장됨
	 *
	 * 🧩 검증 포인트:
	 *   - 종료일 필드가 입력값과 동일하게 설정되는지 검증
	 *   - `updatePenalty(true)`가 호출되는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - Penalty 종료일: 2025-04-30T23:59:59
	 */
	@Test
	@DisplayName("패널티의 사유가 ADMIN 일 경우, ADMIN이 지정한 날짜에 패널티가 종료된다.")
	void ADMIN_사유_패널티_종료일_확인() {
		//given
		LocalDateTime penaltyEnd = LocalDateTime.of(2025, 4, 30, 23, 59, 59);

		// when
		Penalty result = penaltyDomainService.createPenalty(member, reservation, PenaltyReasonType.ADMIN, penaltyEnd);

		// then
		assertThat(result.getPenaltyEnd()).isEqualTo(penaltyEnd);
	}

	void 현재_날짜_고정(int month, int dayOfMonth) {
		LocalDateTime fixedNow = LocalDateTime.of(2025, month, dayOfMonth, 13, 1);
		given(clock.instant()).willReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		given(clock.getZone()).willReturn(ZoneId.systemDefault());
	}
}
