package com.ice.studyroom.domain.penalty.application;

import static org.assertj.core.api.AssertionsForClassTypes.*;
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
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;

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

	private Long reservationId;

	@BeforeEach
	void setUp() {
		reservation = mock(Reservation.class);
		member = mock(Member.class);
		reservationId = 1L;
	}

	@Test
	@DisplayName("[주말 미포함] CANCEL 사유로 패널티가 부여할 경우, 평일 기준 2일 후에 종료된다.")
	void CANCEL_사유로_패널티_부여시_종료일_확인_주말_미포함(){
		Penalty savedPenalty = 이유별_예약_부여_셋업(4, 1, PenaltyReasonType.CANCEL);

		// expected: 4/1(화) 기준, 영업일 기준 2일 후 => 4/3(목)
		LocalDateTime expectedEnd = LocalDateTime.of(2025, 4, 3, 23, 59, 59);
		assertThat(savedPenalty.getPenaltyEnd()).isEqualTo(expectedEnd);
	}

	@Test
	@DisplayName("[주말 포함] CANCEL 사유로 패널티가 부여할 경우, 평일 기준 2일 후에 종료된다.")
	void CANCEL_사유로_패널티_부여시_종료일_확인_주말_포함(){
		Penalty savedPenalty = 이유별_예약_부여_셋업(4, 3, PenaltyReasonType.CANCEL);

		// expected: 4/3(목) 기준, 영업일 기준 2일 후 => 4/7(월)
		LocalDateTime expectedEnd = LocalDateTime.of(2025, 4, 7, 23, 59, 59);
		assertThat(savedPenalty.getPenaltyEnd()).isEqualTo(expectedEnd);
	}

	@Test
	@DisplayName("[주말 미포함] LATE 사유로 패널티가 부여할 경우, 평일 기준 3일 후에 종료된다.")
	void LATE_사유로_패널티_부여시_종료일_확인_주말_미포함(){
		Penalty savedPenalty = 이유별_예약_부여_셋업(4, 1, PenaltyReasonType.LATE);

		// expected: 4/1(화) 기준, 3일 후 평일 => 4/4(금)
		LocalDateTime expectedEnd = LocalDateTime.of(2025, 4, 4, 23, 59, 59);
		assertThat(savedPenalty.getPenaltyEnd()).isEqualTo(expectedEnd);
	}

	@Test
	@DisplayName("[주말 포함] LATE 사유로 패널티가 부여할 경우, 평일 기준 3일 후에 종료된다.")
	void LATE_사유로_패널티_부여시_종료일_확인_주말_포함(){
		Penalty savedPenalty = 이유별_예약_부여_셋업(4, 3, PenaltyReasonType.LATE);

		// expected: 4/3(목) 기준, 3일 후 평일 => 4/8(화)
		LocalDateTime expectedEnd = LocalDateTime.of(2025, 4, 8, 23, 59, 59);
		assertThat(savedPenalty.getPenaltyEnd()).isEqualTo(expectedEnd);
	}

	@Test
	@DisplayName("NO_SHOW 사유로 패널티를 부여할 경우, 평일 기준 5일 후에 종료된다.")
	void NO_SHOW_사유로_패널티_부여시_종료일_확인(){
		Penalty savedPenalty = 이유별_예약_부여_셋업(4, 3, PenaltyReasonType.NO_SHOW);

		// expected: 4/3(목) 기준, 5일 후 평일 => 4/13(목), NO_SHOW 일 경우 무조건 주말이 포함된다.
		LocalDateTime expectedEnd = LocalDateTime.of(2025, 4, 10, 23, 59, 59);
		assertThat(savedPenalty.getPenaltyEnd()).isEqualTo(expectedEnd);
	}

	private Penalty 이유별_예약_부여_셋업(int month, int dayOfMonth, PenaltyReasonType reason) {
		현재_날짜_고정(month, dayOfMonth);

		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		ArgumentCaptor<Penalty> captor = ArgumentCaptor.forClass(Penalty.class);

		// when
		penaltyService.assignPenalty(member, reservationId, reason);

		// then
		verify(penaltyRepository).save(captor.capture());
		return captor.getValue();
	}

	private void 현재_날짜_고정(int month, int dayOfMonth) {
		LocalDateTime fixedNow = LocalDateTime.of(2025, month, dayOfMonth, 13, 1);
		given(clock.instant()).willReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());
	}
}
