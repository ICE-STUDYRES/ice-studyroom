package com.ice.studyroom.domain.reservation.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.domain.identity.domain.service.TokenService;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

@ExtendWith(MockitoExtension.class)
public class GroupReservationTest {

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private ScheduleRepository scheduleRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private TokenService tokenService;

	@Mock
	private Clock clock;

	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private Reservation reservation;

	@Mock
	private Reservation reservation2;

	@Mock
	private List<Reservation> reservations;

	@Mock
	private Schedule firstSchedule;

	@Mock
	private Schedule secondSchedule;

	@Mock
	private Member member1;

	@Mock
	private Member member2;

	private Long reservationId;
	private String token;
	private String ownerEmail;
	private String notOwnerEmail;
	private String email;
	private Long firstScheduleId;

	@BeforeEach
	void setUp() {
		// 공통 객체 생성 (Mock 객체만 설정)
		reservation = mock(Reservation.class);
		reservation2 = mock(Reservation.class);

		firstSchedule = mock(Schedule.class);
		secondSchedule = mock(Schedule.class);

		member1 = mock(Member.class);
		given(member1.getEmail()).willReturn(Email.of("member1@hufs.ac.kr"));

		member2 = mock(Member.class);
		given(member2.getEmail()).willReturn(Email.of("member2@hufs.ac.kr"));

		reservations = List.of(reservation, reservation2);

		// 공통 값 설정
		reservationId = 1L;
		token = "Bearer token";
		ownerEmail = "owner@hufs.ac.kr";
		notOwnerEmail = "not-owner@hufs.ac.kr";
		firstScheduleId = 1L;
	}


	@Test
	@DisplayName("예약 상태인 스케줄로 예약 시도 시 예외 발생")
	void 사용_불가능한_스케줄로_예약_시도는_예외_발생1() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{member1.getEmail().getValue(), member2.getEmail().getValue()} //그룹 예약 참여자 이메일
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(),firstSchedule);

		//스케줄을 예약 상태로 설정
		스케줄_설정(firstSchedule, ScheduleSlotStatus.RESERVED, RoomType.GROUP, 13, 0);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("예약이 불가능합니다.");

		verify(reservationRepository, never()).save(any());
	}

	@Test
	@DisplayName("입장 시간이 지난 스케줄로 예약 시도 시 예외 발생")
	void 사용_불가능한_스케줄로_예약_시도는_예외_발생2() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{member1.getEmail().getValue(), member2.getEmail().getValue()} //그룹 예약 참여자 이메일
		);

		시간_고정_셋업(13, 1);
		스케줄_리스트_설정(request.scheduleId(),firstSchedule);

		//스케줄을 예약 상태로 설정
		스케줄_설정(firstSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.GROUP, 13, 0);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("예약이 불가능합니다.");

		verify(reservationRepository, never()).save(any());
	}

	void 시간_고정_셋업(int hour, int minute) {
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, hour, minute);
		given(clock.instant()).willReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		given(clock.getZone()).willReturn(ZoneId.systemDefault());
	}

	void 스케줄_리스트_설정(Long[] ids, Schedule... schedules) {
		given(scheduleRepository.findAllByIdIn(Arrays.stream(ids).toList()))
			.willReturn(List.of(schedules));
	}

	void 스케줄_설정(Schedule schedule, ScheduleSlotStatus scheduleSlotStatus, RoomType roomType, int hour, int minute) {
		given(schedule.getScheduleDate()).willReturn(LocalDate.of(2025, 3, 22));
		given(schedule.getStartTime()).willReturn(LocalTime.of(hour, minute));
		given(schedule.isAvailable()).willReturn(ScheduleSlotStatus.AVAILABLE == scheduleSlotStatus);
		lenient().when(schedule.isCurrentResLessThanCapacity()).thenReturn(true);
		lenient().when(schedule.getRoomType()).thenReturn(roomType);
	}

	void 스케줄_인원_제한_설정(Schedule schedule, int capacity, int curResCnt) {
		lenient().when(schedule.getRoomNumber()).thenReturn("305-1");
		given(schedule.getCapacity()).willReturn(capacity);
		given(schedule.getCurrentRes()).willReturn(curResCnt);
	}

	void 예약자_패널티_설정(boolean isPenalty) {
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(email);

		Member member = Member.builder()
			.email(Email.of(email))
			.name("홍길동")
			.isPenalty(isPenalty)
			.build();

		given(memberRepository.findByEmail(Email.of(email))).willReturn(Optional.of(member));
	}
}
