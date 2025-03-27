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
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
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
	private Reservation reservation3;

	@Mock
	private List<Reservation> reservations;

	@Mock
	private Schedule firstSchedule;

	@Mock
	private Schedule secondSchedule;

	@Mock
	private Member reservationOwner;

	@Mock
	private Member member1;

	@Mock
	private Member member2;

	@Mock
	private Email member1Email;

	@Mock
	private Email member2Email;

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
		reservation3 = mock(Reservation.class);

		firstSchedule = mock(Schedule.class);
		secondSchedule = mock(Schedule.class);

		reservationOwner = mock(Member.class);
		member1 = mock(Member.class);
		member2 = mock(Member.class);

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
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(),firstSchedule);
		스케줄_설정(firstSchedule, ScheduleSlotStatus.RESERVED, RoomType.GROUP, 13, 0);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("예약이 불가능합니다.");
	}

	@Test
	@DisplayName("입장 시간이 지난 스케줄로 예약 시도 시 예외 발생")
	void 사용_불가능한_스케줄로_예약_시도는_예외_발생2() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		시간_고정_셋업(13, 1);
		스케줄_리스트_설정(request.scheduleId(),firstSchedule);
		스케줄_설정(firstSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.GROUP, 13, 0);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("예약이 불가능합니다.");
	}

	@Test
	@DisplayName("개인전용방 예약 시도는 예외 발생")
	void 개인전용방_예약_시도는_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(),firstSchedule);

		//스케줄을 개인방으로 설정
		스케줄_설정(firstSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.INDIVIDUAL, 13, 0);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("해당 방은 개인예약 전용입니다.");
	}

	@Test
	@DisplayName("패널티를 받은 회원의 예약 요청은 예외 발생")
	void 패널티를_받은_회원의_예약_요청은_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(),firstSchedule);
		스케줄_설정(firstSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.GROUP, 13, 0);
		예약자_존재확인();

		//예약자 패널티 설정
		given(reservationOwner.isPenalty()).willReturn(true);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("예약자가 패널티 상태입니다. 예약이 불가능합니다.");
	}

	@Test
	@DisplayName("예약자의 중복 예약 여부 확인 시 예외 발생")
	void 예약자의_중복_예약_여부_확인_시_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(),firstSchedule);
		스케줄_설정(firstSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.GROUP, 13, 0);
		비패널티_예약자_존재확인();

		given(reservationRepository.findLatestReservationByMemberEmail(Email.of(ownerEmail))).willReturn(Optional.of(reservation));
		given(reservation.getStatus()).willReturn(ReservationStatus.RESERVED);
		given(reservation.getStatus()).willReturn(ReservationStatus.ENTRANCE);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("현재 예약이 진행 중이므로 새로운 예약을 생성할 수 없습니다.");
	}

	@Test
	@DisplayName("참여자 중 존재하지 않는 이메일이 있을 경우 예외 발생")
	void 참여자_목록에_존재하지_않는_이메일이_있을_경우_예외_발생(){
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(),firstSchedule);
		스케줄_설정(firstSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.GROUP, 13, 0);
		비패널티_예약자_존재확인();
		예약자_미중복_예약_확인();

		given(memberRepository.findByEmail(Email.of("member1@hufs.ac.kr"))).willReturn(Optional.of(member1));
		given(memberRepository.findByEmail(Email.of("member2@hufs.ac.kr"))).willReturn(Optional.empty());

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("참여자 이메일이 존재하지 않습니다: member2@hufs.ac.kr");
	}

	@Test
	@DisplayName("참여자 목록에 중복 이메일이 있을 경우 예외 발생")
	void 참여자_목록에_중복_이메일이_있을_경우_예외_발생(){
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{ "member1@hufs.ac.kr", "member1@hufs.ac.kr" } //중복 이메일
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(),firstSchedule);
		스케줄_설정(firstSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.GROUP, 13, 0);
		비패널티_예약자_존재확인();
		예약자_미중복_예약_확인();

		given(memberRepository.findByEmail(Email.of("member1@hufs.ac.kr"))).willReturn(Optional.of(member1));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("중복된 참여자 이메일이 존재합니다: member1@hufs.ac.kr");
	}

	@Test
	@DisplayName("참여자 중 패널티 상태인 경우 예외 발생")
	void 참여자_중_패널티_상태인_경우_예외_발생(){
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(),firstSchedule);
		스케줄_설정(firstSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.GROUP, 13, 0);
		비패널티_예약자_존재확인();
		예약자_미중복_예약_확인();
		예약_인원_명단에_중복이_없음을_확인();

		given(member1.isPenalty()).willReturn(false);
		given(member2.isPenalty()).willReturn(true);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("참여자 중 패널티 상태인 사용자가 있습니다. 예약이 불가능합니다. (이메일: member2@hufs.ac.kr)");
	}

	@Test
	@DisplayName("참여자 중 현재 예약 상태가 RESERVED 또는 ENTRANCE인 경우(현재 특정 예약이 진행 중) 예외 발생")
	void 참여자_중_특정_예약이_진행_중인_경우_예외_발생(){
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(),firstSchedule);
		스케줄_설정(firstSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.GROUP, 13, 0);
		비패널티_예약자_존재확인();
		예약자_미중복_예약_확인();
		예약_인원_명단에_중복이_없음을_확인();
		예약_참여_인원_비패널티_확인();

		given(reservationRepository.findLatestReservationByMemberEmail(Email.of("member1@hufs.ac.kr"))).willReturn(Optional.of(reservation2));
		given(reservationRepository.findLatestReservationByMemberEmail(Email.of("member2@hufs.ac.kr"))).willReturn(Optional.of(reservation3));

		given(reservation2.getStatus()).willReturn(ReservationStatus.COMPLETED);
		given(reservation3.getStatus()).willReturn(ReservationStatus.RESERVED);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("참여자 중 현재 예약이 진행 중인 사용자가 있어 예약이 불가능합니다. (이메일: member2@hufs.ac.kr)");
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

	void 예약자_존재확인(){
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(ownerEmail);
		given(memberRepository.findByEmail(new Email(ownerEmail))).willReturn(Optional.of(reservationOwner));
	}

	void 비패널티_예약자_존재확인(){
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(ownerEmail);
		given(memberRepository.findByEmail(new Email(ownerEmail))).willReturn(Optional.of(reservationOwner));
		given(reservationOwner.isPenalty()).willReturn(false);
	}

	void 예약자_미중복_예약_확인(){
		given(reservationRepository.findLatestReservationByMemberEmail(Email.of(ownerEmail))).willReturn(Optional.of(reservation));
		given(reservation.getStatus()).willReturn(ReservationStatus.COMPLETED);
	}

	void 예약_인원_명단에_중복이_없음을_확인(){
		given(memberRepository.findByEmail(Email.of("member1@hufs.ac.kr"))).willReturn(Optional.of(member1));
		given(memberRepository.findByEmail(Email.of("member2@hufs.ac.kr"))).willReturn(Optional.of(member2));
	}

	void 예약_참여_인원_비패널티_확인(){
		given(member1.isPenalty()).willReturn(false);
		given(member2.isPenalty()).willReturn(false);
	}

	void 예약_참여_인원_특정_예약_없음_확인(){
		given(reservationRepository.findLatestReservationByMemberEmail(Email.of("member1@hufs.ac.kr"))).willReturn(Optional.of(reservation2));
		given(reservationRepository.findLatestReservationByMemberEmail(Email.of("member2@hufs.ac.kr"))).willReturn(Optional.of(reservation3));

		given(reservation2.getStatus()).willReturn(ReservationStatus.COMPLETED);
		given(reservation3.getStatus()).willReturn(ReservationStatus.COMPLETED);
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
