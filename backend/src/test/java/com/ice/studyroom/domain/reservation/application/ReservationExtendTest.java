package com.ice.studyroom.domain.reservation.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.domain.identity.domain.service.TokenService;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

@ExtendWith(MockitoExtension.class)
public class ReservationExtendTest {

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private ScheduleRepository scheduleRepository;

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
	private Schedule schedule;

	@Mock
	private Member member1;

	@Mock
	private Member member2;

	private Long reservationId;
	private String token;
	private String ownerEmail;
	private String notOwnerEmail;
	private Long scheduleFirstId;

	@BeforeEach
	void setUp() {
		// 공통 객체 생성 (Mock 객체만 설정)
		reservation = mock(Reservation.class);
		reservation2 = mock(Reservation.class);
		schedule = mock(Schedule.class);
		member1 = mock(Member.class);
		member2 = mock(Member.class);
		reservations = List.of(reservation, reservation2);

		// 공통 값 설정
		reservationId = 1L;
		token = "Bearer token";
		ownerEmail = "owner@hufs.ac.kr";
		notOwnerEmail = "not-owner@hufs.ac.kr";
		scheduleFirstId = 10L;
	}

	@Test
	void 존재하지_않을_예약일_경우_예외() {
		// given
		given(reservationRepository.findById(reservationId))
			.willReturn(Optional.empty());

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals(StatusCode.NOT_FOUND, ex.getStatusCode());
		assertEquals("존재하지 않는 예약입니다.", ex.getMessage());
	}

	@Test
	void 예약_소유자가_아닌_경우_예외() {

		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(notOwnerEmail);
		given(reservation.isOwnedBy(notOwnerEmail)).willReturn(false);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("해당 예약 정보가 존재하지 않습니다.", ex.getMessage());
	}

	@Test
	void 연장_요청이_이른_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);

		// 현재 시각: 13:49
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 13, 49);
		when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		// 예약 종료 시간: 14:00
		LocalDate reservationDate = LocalDate.of(2025, 3, 22);
		LocalTime reservationEndTime = LocalTime.of(14, 0);
		given(reservation.getScheduleDate()).willReturn(reservationDate);
		given(reservation.getEndTime()).willReturn(reservationEndTime);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("연장은 퇴실 시간 10분 전부터 가능합니다.", ex.getMessage());
	}

	@Test
	void 연장_요청이_늦은_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);

		// 현재 시각: 14:01
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 14, 1);
		when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		// 예약 종료 시간: 14:00
		LocalDate reservationDate = LocalDate.of(2025, 3, 22);
		LocalTime reservationEndTime = LocalTime.of(14, 0);
		given(reservation.getScheduleDate()).willReturn(reservationDate);
		given(reservation.getEndTime()).willReturn(reservationEndTime);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("연장 가능한 시간이 지났습니다.", ex.getMessage());
	}

	@Test
	void 스케줄이_존재하지_않을_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();

		// 임의의 스케줄 ID 설정
		given(reservation.getFirstScheduleId()).willReturn(scheduleFirstId);
		given(reservation.getSecondScheduleId()).willReturn(null);
		// 다음 스케줄이 존재하지 않을 경우
		given(scheduleRepository.findById(scheduleFirstId + 1)).willReturn(Optional.empty());

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("스터디룸 이용 가능 시간을 확인해주세요.", ex.getMessage());
	}

	@Test
	void 다음_스케줄의_방번호가_다를_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();

		// 임의의 스케줄 ID 설정
		given(reservation.getFirstScheduleId()).willReturn(scheduleFirstId);
		given(reservation.getSecondScheduleId()).willReturn(null);

		// 다음 스케줄이 존재하지만, 다른 방의 스케줄일 경우
		given(scheduleRepository.findById(scheduleFirstId + 1)).willReturn(Optional.of(schedule));
		given(schedule.getRoomNumber()).willReturn("409-1");
		given(reservation.getRoomNumber()).willReturn("409-2");

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("스터디룸 이용 가능 시간을 확인해주세요.", ex.getMessage());
	}

	/**
	 * 다음 스케줄의 상태가 UNAVAILABLE 이거나 RESERVED 인 경우 예외
	 */
	@Test
	void 다음_스케줄_예약_불가_예외1(){

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);

		// 다음 스케줄이 예약 상태이거나 이용 불가 상태일 경우
		given(schedule.isCurrentResLessThanCapacity()).willReturn(true);
		given(schedule.isAvailable()).willReturn(false);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("다음 시간대가 이미 예약이 완료되었거나, 이용이 불가능한 상태입니다.", ex.getMessage());

	}

	/**
	 *  다음 스케줄의 예약의 인원수가 이미 가득 찬 경우 예외
	 */
	@Test
	void 다음_스케줄_예약_불가_예외2(){

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);

		// 다음 스케줄의 예약이 이미 가득 찬 경우
		given(schedule.isCurrentResLessThanCapacity()).willReturn(false);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("다음 시간대가 이미 예약이 완료되었거나, 이용이 불가능한 상태입니다.", ex.getMessage());
	}

	@Test
	void 그룹_예약일_때_패널티_멤버가_있을_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);
		통과된_다음_스케줄_이용_가능_여부_셋업();

		// 그룹 예약일 경우
		given(schedule.getRoomType()).willReturn(RoomType.GROUP);
		given(reservationRepository.findByFirstScheduleId(scheduleFirstId)).willReturn(reservations);
		given(reservation.getMember()).willReturn(member1);
		given(reservation2.getMember()).willReturn(member2);

		// 첫 번째 멤버는 패널티가 없고, 두 번째 멤버는 패널티가 있는 경우
		given(reservations.get(0).getMember().isPenalty()).willReturn(false);
		given(reservations.get(1).getMember().isPenalty()).willReturn(true);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("패널티가 있는 멤버로 인해 연장이 불가능합니다.", ex.getMessage());
	}

	@Test
	void 그룹_예약일_때_입실하지_않은_멤버가_있을_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);
		통과된_다음_스케줄_이용_가능_여부_셋업();

		// 그룹 예약 설정
		given(schedule.getRoomType()).willReturn(RoomType.GROUP);
		given(reservationRepository.findByFirstScheduleId(scheduleFirstId)).willReturn(reservations);
		given(reservation.getMember()).willReturn(member1);
		// reservation2 는 reservation과 같은 예약이라, reservations 에 속해있음
		given(reservation2.getMember()).willReturn(member2);

		given(reservations.get(0).getMember().isPenalty()).willReturn(false);
		given(reservations.get(1).getMember().isPenalty()).willReturn(false);

		// 첫 번째 멤버는 입실 처리가 되어있고, 두 번째 멤버는 입실 처리가 되어있지 않은 경우
		given(reservations.get(0).isEntered()).willReturn(true);
		given(reservations.get(1).isEntered()).willReturn(false);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("입실 처리 되어있지 않은 유저가 있어 연장이 불가능합니다.", ex.getMessage());
	}

	@Test
	void 개인_예약일_때_패널티_사용자_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);
		통과된_다음_스케줄_이용_가능_여부_셋업();

		given(schedule.getRoomType()).willReturn(RoomType.INDIVIDUAL);
		given(reservation.getMember()).willReturn(member1);
		given(reservation.getMember().isPenalty()).willReturn(true);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("패넡티 상태이므로, 연장이 불가능합니다.", ex.getMessage());
	}

	@Test
	void 개인_예약일_때_입실하지_않은_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);
		통과된_다음_스케줄_이용_가능_여부_셋업();

		given(schedule.getRoomType()).willReturn(RoomType.INDIVIDUAL);
		given(reservation.getMember()).willReturn(member1);
		given(reservation.getMember().isPenalty()).willReturn(false);
		given(reservation.isEntered()).willReturn(false);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("예약 연장은 입실 후 가능합니다.", ex.getMessage());
	}

	private void 통과된_기본_예약_검증_셋업(Long reservationId, String token, String email) {
		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(email);
		given(reservation.isOwnedBy(email)).willReturn(true);
	}

	private void 통과된_스케줄_연장_시간_검증_셋업() {
		// 현재 시각: 13:59
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 13, 59);
		when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		// 예약 종료 시간: 14:00
		LocalDate reservationDate = LocalDate.of(2025, 3, 22);
		LocalTime reservationEndTime = LocalTime.of(14, 0);
		given(reservation.getScheduleDate()).willReturn(reservationDate);
		given(reservation.getEndTime()).willReturn(reservationEndTime);
	}

	private void 통과된_다음_스케줄_존재_여부_셋업(Long scheduleId){
		// 임의의 스케줄 ID 설정
		given(reservation.getFirstScheduleId()).willReturn(scheduleId);
		given(reservation.getSecondScheduleId()).willReturn(null);
		given(reservation.getRoomNumber()).willReturn("409-1");

		// 다음 스케줄이 같은 방인 경우에는 예외 발생하지 않음
		given(scheduleRepository.findById(scheduleId + 1)).willReturn(Optional.of(schedule));
		given(schedule.getRoomNumber()).willReturn("409-1");
	}

	private void 통과된_다음_스케줄_이용_가능_여부_셋업(){
		given(schedule.isCurrentResLessThanCapacity()).willReturn(true);
		given(schedule.isAvailable()).willReturn(true);
	}

}
