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

import com.ice.studyroom.domain.reservation.domain.exception.reservation.ReservationAccessDeniedException;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationAccessDeniedReason;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.global.security.service.TokenService;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;
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
	private Schedule nextSchedule;

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
		nextSchedule = mock(Schedule.class);
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

	/**
	 * 📌 테스트명: 존재하지_않을_예약일_경우_예외
	 *
	 * ✅ 목적:
	 *   - 사용자가 연장을 요청한 예약 ID가 실제로 존재하지 않는 경우, 시스템이 적절한 예외를 던지는지를 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 존재하지 않는 예약 ID를 전달한다.
	 *   2. reservationRepository.findById()가 Optional.empty()를 반환한다.
	 *   3. 예약을 찾지 못한 경우 BusinessException이 발생해야 한다.
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 존재하지 않는 예약에 대해 연장을 요청할 경우, 연장은 불가능하며 NOT_FOUND 예외를 발생시켜야 한다.
	 *
	 * 🧩 검증 포인트:
	 *   - reservationRepository.findById() 호출 여부
	 *   - BusinessException이 발생하는가?
	 *   - 예외의 상태 코드와 메시지가 예상과 일치하는가?
	 *
	 * ✅ 기대 결과:
	 *   - StatusCode.NOT_FOUND 예외 발생
	 *   - 예외 메시지: "존재하지 않는 예약입니다."
	 *   - 테스트는 해당 예외가 발생함을 검증하며 통과
	 */

	@Test
	@DisplayName("존재하지 않을 예약일 경우 예외 발생")
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

	/**
	 * 📌 테스트명: 예약_소유자가_아닌_경우_예외
	 *
	 * ✅ 목적:
	 *   - 토큰에서 추출한 이메일이 예약의 소유자와 다를 경우, 시스템이 예외를 발생시키는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약 ID에 해당하는 예약은 존재함
	 *   2. 토큰에서 추출한 이메일은 예약자의 이메일과 다름
	 *   3. 예약 소유자 검증 로직에서 false 반환
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 예약 연장은 해당 예약의 소유자만 가능하다
	 *
	 * 🧩 검증 포인트:
	 *   - 예약의 소유자가 아닌 경우 `BusinessException`이 발생하는가?
	 *   - 예외 메시지가 정확한가?
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "해당 예약 정보가 존재하지 않습니다."
	 */
	@Test
	@DisplayName("예약 소유자가 아닌 경우 예외 발생")
	void 예약_소유자가_아닌_경우_예외() {

		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(notOwnerEmail);
		willThrow(new ReservationAccessDeniedException(ReservationAccessDeniedReason.NOT_OWNER, reservationId, notOwnerEmail, ReservationActionType.EXTEND_RESERVATION))
			.given(reservation).validateOwnership(notOwnerEmail, ReservationActionType.EXTEND_RESERVATION);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("해당 예약 정보가 존재하지 않습니다.", ex.getMessage());
	}

	/**
	 * 📌 테스트명: 연장_요청이_이른_경우_예외
	 *
	 * ✅ 목적:
	 *   - 퇴실 10분 전 이전에 연장을 요청하는 경우 예외가 발생하는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 현재 시각은 13:49
	 *   2. 예약의 종료 시각은 14:00
	 *   3. 연장 조건인 "10분 전부터 가능"을 충족하지 않음
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 퇴실 10분 전부터 연장 요청이 가능하다
	 *
	 * 🧩 검증 포인트:
	 *   - 현재 시각과 예약 종료 시각 간의 차이를 기반으로 조건 검증
	 *   - 예외 메시지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "연장은 퇴실 시간 10분 전부터 가능합니다."
	 */
	@Test
	@DisplayName("연장 요청이 이른 경우 예외 발생")
	void 연장_요청이_이른_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);

		// 현재 시각: 13:49
		현재_시간_고정(13, 49);

		// 예약 종료 시간: 14:00
		예약_종료_시간_고정(14, 0);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("연장은 퇴실 시간 10분 전부터 가능합니다.", ex.getMessage());
	}

	/**
	 * 📌 테스트명: 연장_요청이_늦은_경우_예외
	 *
	 * ✅ 목적:
	 *   - 퇴실 시간이 지난 후 연장 요청 시 예외가 발생하는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약자는 본인 확인을 통과함
	 *   2. 현재 시각은 14:01로, 예약 종료 시간(14:00)을 이미 초과함
	 *   3. 연장 유효 시간이 지났기 때문에 예외가 발생해야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 연장은 퇴실 시간 이후에는 불가능하다
	 *
	 * 🧩 검증 포인트:
	 *   - 현재 시간이 종료 시간을 초과할 경우 예외 발생 여부
	 *   - 예외 메시지가 정확히 "연장 가능한 시간이 지났습니다."인지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "연장 가능한 시간이 지났습니다."
	 */
	@Test
	@DisplayName("연장 요청이 늦은 경우 예외 발생")
	void 연장_요청이_늦은_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);

		// 현재 시각: 14:01
		현재_시간_고정(14, 1);

		// 예약 종료 시간: 14:00
		예약_종료_시간_고정(14, 0);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("연장 가능한 시간이 지났습니다.", ex.getMessage());
	}

	/**
	 * 📌 테스트명: 스케줄이_존재하지_않을_경우_예외
	 *
	 * ✅ 목적:
	 *   - 연장 대상인 다음 스케줄이 존재하지 않을 경우 예외가 발생하는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약자는 본인 확인과 연장 가능한 시간 조건을 통과함
	 *   2. 예약의 마지막 스케줄 ID를 기반으로 다음 스케줄을 조회
	 *   3. 해당 스케줄이 존재하지 않으므로 예외가 발생해야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 연장 시, 다음 시간대의 스케줄이 존재하지 않으면 연장할 수 없다
	 *
	 * 🧩 검증 포인트:
	 *   - 다음 스케줄이 DB에 없을 경우 예외 발생 여부
	 *   - 예외 메시지가 정확히 "스터디룸 이용 가능 시간을 확인해주세요."인지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "스터디룸 이용 가능 시간을 확인해주세요."
	 */
	@Test
	@DisplayName("스케줄이 존재하지 않을 경우 예외 발생")
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

	/**
	 * 📌 테스트명: 다음_스케줄의_방번호가_다를_경우_예외
	 *
	 * ✅ 목적:
	 *   - 연장 대상인 다음 스케줄이 현재 예약한 방과 다를 경우 예외가 발생하는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자는 본인의 예약 정보를 통해 연장 요청을 시도
	 *   2. 현재 시각은 퇴실 10분 전으로 연장 가능 시간에 해당
	 *   3. 다음 스케줄이 존재하지만 다른 방 번호를 갖고 있음
	 *   4. 방이 다르므로 예외가 발생해야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 연장은 동일한 스터디룸에 대해 연속된 시간대만 가능
	 *
	 * 🧩 검증 포인트:
	 *   - 다음 스케줄의 roomNumber와 현재 예약의 roomNumber 비교
	 *   - 일치하지 않을 경우 BusinessException 발생 여부
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "스터디룸 이용 가능 시간을 확인해주세요."
	 */

	@Test
	@DisplayName("다음 스케줄의 방 번호가 다를 경우 예외 발생")
	void 다음_스케줄의_방번호가_다를_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();

		// 임의의 스케줄 ID 설정
		given(reservation.getFirstScheduleId()).willReturn(scheduleFirstId);
		given(reservation.getSecondScheduleId()).willReturn(null);

		// 다음 스케줄이 존재하지만, 다른 방의 스케줄일 경우
		given(scheduleRepository.findById(scheduleFirstId + 1)).willReturn(Optional.of(nextSchedule));
		given(nextSchedule.getRoomNumber()).willReturn("409-1");
		given(reservation.getRoomNumber()).willReturn("409-2");

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("스터디룸 이용 가능 시간을 확인해주세요.", ex.getMessage());
	}

	/**
	 * 📌 테스트명: 다음_스케줄_예약_불가_예외1
	 *
	 * ✅ 목적:
	 *   - 다음 스케줄이 예약 불가 상태(UNAVAILABLE or 이미 예약됨)일 경우 예외가 발생하는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자는 본인의 예약 정보를 통해 연장을 요청
	 *   2. 현재 시각은 연장 가능한 시간에 해당
	 *   3. 다음 스케줄은 현재 방과 동일한 방 번호를 가지고 있음
	 *   4. 다음 스케줄의 수용 인원에는 여유가 있으나, 이용 불가 상태임
	 *   5. 연장 불가 예외가 발생해야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 다음 스케줄은 반드시 `isAvailable == true` && `currentRes < maxCapacity`일 때만 연장 가능
	 *
	 * 🧩 검증 포인트:
	 *   - `nextSchedule.isAvailable()`이 false인 경우 예외 발생
	 *   - 예외 메시지가 정확히 일치하는지 검증
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "다음 시간대가 이미 예약이 완료되었거나, 이용이 불가능한 상태입니다."
	 */
	@Test
	@DisplayName("다음 스케줄이 예약 불가 상태(UNAVAILABLE or 이미 예약됨)일 경우 예외 발생")
	void 다음_스케줄_예약_불가_예외1(){

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);

		// 다음 스케줄이 예약 상태이거나 이용 불가 상태일 경우
		given(nextSchedule.isCurrentResLessThanCapacity()).willReturn(true);
		given(nextSchedule.isAvailable()).willReturn(false);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("다음 시간대가 이미 예약이 완료되었거나, 이용이 불가능한 상태입니다.", ex.getMessage());
	}

	/**
	 * 📌 테스트명: 다음_스케줄_예약_불가_예외2
	 *
	 * ✅ 목적:
	 *   - 다음 스케줄의 정원이 이미 가득 찬 경우, 연장 요청이 거절되는지를 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 본인의 예약에 대해 연장을 요청
	 *   2. 현재 시각은 연장 가능한 시간에 해당
	 *   3. 다음 스케줄은 현재 방과 동일한 방 번호를 가지고 있음
	 *   4. 다음 스케줄의 정원이 가득 차 이용이 불가능한 상태임
	 *   5. 연장 불가 예외가 발생해야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 다음 스케줄은 반드시 `isAvailable == true` && `currentRes < maxCapacity`일 때만 연장 가능
	 *
	 * 🧩 검증 포인트:
	 *   - `nextSchedule.isCurrentResLessThanCapacity()`가 false인 경우 예외 발생
	 *   - 적절한 예외 메시지 반환 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "다음 시간대가 이미 예약이 완료되었거나, 이용이 불가능한 상태입니다."
	 */
	@Test
	@DisplayName("다음 스케줄의 정원이 이미 가득 찬 경우 예외 발생")
	void 다음_스케줄_예약_불가_예외2(){

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);

		// 다음 스케줄의 예약이 이미 가득 찬 경우
		given(nextSchedule.isCurrentResLessThanCapacity()).willReturn(false);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("다음 시간대가 이미 예약이 완료되었거나, 이용이 불가능한 상태입니다.", ex.getMessage());
	}

	/**
	 * 📌 테스트명: 그룹_예약일_때_패널티_멤버가_있을_경우_예외
	 *
	 * ✅ 목적:
	 *   - 그룹 예약의 참여자 중 패널티 상태인 멤버가 1명이라도 존재할 경우, 예약 연장이 제한되는 비즈니스 규칙 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 본인의 예약에 대해 연장을 요청
	 *   2. 현재 시각은 연장 가능한 시간에 해당
	 *   3. 다음 스케줄은 현재 방과 동일한 방 번호를 가지고 있음
	 *   4. 다음 스케줄의 상태가 예약 가능 상태
	 *   5. 그룹 예약이며, 참여자 목록을 조회
	 *   6. 참여자 중 한 명이라도 패널티 상태일 경우 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 그룹 예약의 경우 모든 참여자가 패널티 상태가 아니어야 연장이 가능하다
	 *
	 * 🧩 검증 포인트:
	 *   - `reservationRepository.findByFirstScheduleId()`로 그룹 참여자 조회
	 *   - 참여자 중 `isPenalty()`가 true인 멤버가 있으면 예외 발생
	 *   - 예외 메시지가 정확히 매칭되는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "패널티가 있는 멤버로 인해 연장이 불가능합니다."
	 */
	@Test
	@DisplayName("그룹 예약의 참여자 중 패널티 상태인 멤버가 1명이라도 존재할 경우 예외 발생")
	void 그룹_예약일_때_패널티_멤버가_있을_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);
		통과된_다음_스케줄_이용_가능_여부_셋업();

		// 그룹 예약일 경우
		given(nextSchedule.getRoomType()).willReturn(RoomType.GROUP);
		given(reservationRepository.findByFirstScheduleId(scheduleFirstId)).willReturn(reservations);
		given(reservation.getMember()).willReturn(member1);
		given(reservation2.getMember()).willReturn(member2);

		// 첫 번째 멤버는 패널티가 없고, 두 번째 멤버는 패널티가 있는 경우
		given(reservations.get(0).getMember().isPenalty()).willReturn(false);
		given(reservations.get(0).isEntered()).willReturn(true);
		given(reservations.get(1).getMember().isPenalty()).willReturn(true);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("패널티가 있는 멤버로 인해 연장이 불가능합니다.", ex.getMessage());
	}

	/**
	 * 📌 테스트명: 그룹_예약일_때_입실하지_않은_멤버가_있을_경우_예외
	 *
	 * ✅ 목적:
	 *   - 그룹 예약에서 모든 참여자가 입실 처리되어 있지 않으면, 연장이 불가능하다는 비즈니스 규칙을 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 본인의 예약에 대해 연장을 요청
	 *   2. 현재 시각은 연장 가능한 시간에 해당
	 *   3. 다음 스케줄은 현재 방과 동일한 방 번호를 가지고 있음
	 *   4. 다음 스케줄이 예약 가능한 상태
	 *   5. 그룹 예약이며 참여자 전체 조회
	 *   6. 패널티는 없지만, 입실하지 않은 멤버가 존재
	 *   7. 연장 요청 시 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 그룹 예약은 모든 참여자가 입실한 상태여야 연장 가능
	 *   - 입실하지 않은 멤버가 한 명이라도 존재하면 연장 불가
	 *
	 * 🧩 검증 포인트:
	 *   - `reservationRepository.findByFirstScheduleId()`로 그룹 멤버 전체 조회
	 *   - `isEntered()` 값이 false인 예약자가 포함되어 있는 경우 예외 발생
	 *   - 예외 메시지 정확히 일치하는지 검증
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "입실 처리 되어있지 않은 유저가 있어 연장이 불가능합니다."
	 */
	@Test
	@DisplayName("그룹 예약에서 모든 참여자가 입실 처리되어 있지 않으면 예외 발생")
	void 그룹_예약일_때_입실하지_않은_멤버가_있을_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);
		통과된_다음_스케줄_이용_가능_여부_셋업();

		// 그룹 예약 설정
		given(nextSchedule.getRoomType()).willReturn(RoomType.GROUP);
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

	/**
	 * 📌 테스트명: 개인_예약일_때_패널티_사용자_예외
	 *
	 * ✅ 목적:
	 *   - 개인 예약 사용자가 패널티 상태일 경우, 연장이 불가능하다는 도메인 규칙을 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 본인의 예약에 대해 연장을 요청
	 *   2. 현재 시각은 연장 가능한 시간에 해당
	 *   3. 다음 스케줄은 현재 방과 동일한 방 번호를 가지고 있음
	 *   4. 다음 스케줄이 예약 가능한 상태
	 *   5. 예약은 개인 예약이며, 사용자 상태는 패널티가 부여된 상태
	 *   6. 연장 요청 시 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 패널티 상태인 개인 예약자가 연창을 요청할 경우 연장 불가
	 *
	 * 🧩 검증 포인트:
	 *   - `RoomType.INDIVIDUAL` 조건 분기 진입 여부
	 *   - `member.isPenalty()`가 true일 때 `BusinessException` 발생하는지
	 *   - 예외 메시지가 정확히 일치하는지
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "패넡티 상태이므로, 연장이 불가능합니다."
	 */
	@Test
	@DisplayName("개인 예약 사용자가 패널티 상태일 경우 예외 발생")
	void 개인_예약일_때_패널티_사용자_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);
		통과된_다음_스케줄_이용_가능_여부_셋업();

		given(nextSchedule.getRoomType()).willReturn(RoomType.INDIVIDUAL);
		given(reservation.getMember()).willReturn(member1);
		given(reservation.getMember().isPenalty()).willReturn(true);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("패넡티 상태이므로, 연장이 불가능합니다.", ex.getMessage());
	}

	/**
	 * 📌 테스트명: 개인_예약일_때_입실하지_않은_경우_예외
	 *
	 * ✅ 목적:
	 *   - 개인 예약자가 입실하지 않은 상태에서는 예약 연장이 불가능하다는 비즈니스 규칙을 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 본인의 예약에 대해 연장을 요청
	 *   2. 현재 시각은 연장 가능한 시간에 해당
	 *   3. 다음 스케줄은 현재 방과 동일한 방 번호를 가지고 있음
	 *   4. 다음 스케줄이 예약 가능한 상태
	 *   5. 개인 예약이며, 패널티는 없음
	 *   6. 하지만 사용자가 아직 입실하지 않은 상태
	 *   7. 연장 요청 시 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 예약자는 입실 후에만 연장 가능
	 *
	 * 🧩 검증 포인트:
	 *   - `RoomType.INDIVIDUAL` 조건 진입 여부
	 *   - `isEntered()` 값이 false일 때 `BusinessException` 발생하는지 확인
	 *   - 예외 메시지가 정확한지
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "예약 연장은 입실 후 가능합니다."
	 */
	@Test
	@DisplayName("개인 예약 사용자가 입실을 하지 않을 예외 발생")
	void 개인_예약일_때_입실하지_않은_경우_예외() {

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);
		통과된_다음_스케줄_이용_가능_여부_셋업();

		given(nextSchedule.getRoomType()).willReturn(RoomType.INDIVIDUAL);
		given(reservation.getMember()).willReturn(member1);
		given(reservation.getMember().isPenalty()).willReturn(false);
		given(reservation.isEntered()).willReturn(false);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("예약 연장은 입실 후 가능합니다.", ex.getMessage());
	}

	/**
	 * 📌 테스트명: 그룹_예약_연장_성공
	 *
	 * ✅ 목적:
	 *   - 그룹 예약의 연장이 성공적으로 처리되는 시나리오를 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 본인의 예약에 대해 연장을 요청
	 *   2. 현재 시각은 연장 가능한 시간에 해당
	 *   3. 다음 스케줄은 현재 방과 동일한 방 번호를 가지고 있음
	 *   4. 다음 스케줄이 예약 가능한 상태
	 *   5. 그룹 예약이며 모든 참여자가 입실했고 패널티 없음
	 *   6. 연장 처리 후 nextSchedule 상태 업데이트 및 인원 수 반영
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 그룹 예약자는 모든 참여자가 입실했으며, 패널티가 없어야 연장 가능
	 *   - 연장 시 다음 스케줄의 상태를 RESERVED로 변경
	 *
	 * 🧩 검증 포인트:
	 *   - 예약 연장 후 반환값이 "Success"인지
	 *   - 각 참여자에 대해 `extendReservation()` 호출되었는지
	 *   - `nextSchedule.updateStatus()`가 호출되었는지
	 *
	 * ✅ 기대 결과:
	 *   - "Success" 반환
	 *   - 참여자 수 만큼 extendReservation 호출
	 *   - nextSchedule 상태가 RESERVED로 설정됨
	 */
	@Test
	@DisplayName("그룹 예약 연장 성공")
	void 그룹_예약_연장_성공(){

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);
		통과된_다음_스케줄_이용_가능_여부_셋업();
		통과된_그룹_예약_입실_및_패널티_검증_셋업();

		// when
		String result = reservationService.extendReservation(reservationId, token);

		// then
		assertEquals("Success", result);
		verify(nextSchedule).updateStatus(ScheduleSlotStatus.RESERVED);

		for (Reservation res : reservations) {
			verify(res).extendReservation(nextSchedule.getId(), nextSchedule.getEndTime());
		}
	}

	/**
	 * 📌 테스트명: 그룹_예약_연장_성공_취소된_예약자_제외
	 *
	 * ✅ 목적:
	 *   - 그룹 예약 연장 시, 취소된 예약자는 연장 로직에서 제외되어야 함을 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약자가 그룹 예약의 연장을 요청함
	 *   2. 다음 스케줄은 현재 방과 동일하고 예약 가능 상태임
	 *   3. 그룹에 속한 예약자 중 일부는 `CANCELLED` 상태
	 *   4. 해당 예약자는 연장 처리 로직에서 제외되어야 함
	 *   5. `ENTRANCE` 상태의 예약자만 연장 처리
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 연장 시 예약 상태가 `CANCELLED`인 경우 연장 처리 대상에서 제외해야 함
	 *
	 * 🧩 검증 포인트:
	 *   - `ReservationStatus.CANCELLED`인 예약자는 `extendReservation()` 호출되지 않아야 함
	 *   - 나머지 정상 예약자는 `extendReservation()` 호출 확인
	 *   - 전체 결과는 "Success" 반환
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException이 발생하지 않고 정상 처리됨
	 *   - 연장 대상에서 `CANCELLED` 예약 제외됨
	 *   - 연장 대상자에게만 `extendReservation()` 호출됨
	 */
	@Test
	@DisplayName("그룹 예약 연장 시 취소된 예약자는 연장 처리 대상에서 제외됨")
	void 그룹_예약_연장_성공_취소된_예약자_제외(){
		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);
		통과된_다음_스케줄_이용_가능_여부_셋업();
		통과된_그룹_예약_입실_및_패널티_검증_셋업_취소된_예약자_포함();

		// when
		String result = reservationService.extendReservation(reservationId, token);

		// then
		assertEquals("Success", result);
		verify(nextSchedule).updateStatus(ScheduleSlotStatus.RESERVED);

		for (Reservation res : reservations) {
			if(res.getStatus() == ReservationStatus.CANCELLED) continue;
			verify(res).extendReservation(nextSchedule.getId(), nextSchedule.getEndTime());
		}
	}

	/**
	 * 📌 테스트명: 개인_예약_연장_성공
	 *
	 * ✅ 목적:
	 *   - 개인 예약의 연장이 성공적으로 처리되는 시나리오를 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 본인의 예약에 대해 연장을 요청
	 *   2. 현재 시각은 연장 가능한 시간에 해당
	 *   3. 다음 스케줄은 현재 방과 동일한 방 번호를 가지고 있음
	 *   4. 다음 스케줄이 예약 가능한 상태
	 *   5. 예약자는 입실했으며 패널티 없음
	 *   6. 연장 처리 후 nextSchedule 예약 인원 증가 및 상태 변경 (필요 시)
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 개인 예약자는 입실 후에만 연장 가능하며, 패널티 상태에서는 불가능
	 *   - 연장 시 예약 인원 증가 및 상태 RESERVED로 전환 조건 검토
	 *
	 * 🧩 검증 포인트:
	 *   - 반환값이 "Success"인지
	 *   - `reservation.extendReservation()`이 호출되는지
	 *   - `nextSchedule.reserve()`가 호출되는지
	 *   - 인원이 남은 경우 `updateStatus()`는 호출되지 않아야 함
	 *
	 * ✅ 기대 결과:
	 *   - "Success" 반환
	 *   - 예약 정보 연장 처리 완료
	 *   - nextSchedule.reserve() 호출
	 *   - 예약 인원이 꽉 차지 않았으면 updateStatus 호출되지 않음
	 */
	@Test
	@DisplayName("개인 예약 연장 성공")
	void 개인_예약_연장_성공(){

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		통과된_스케줄_연장_시간_검증_셋업();
		통과된_다음_스케줄_존재_여부_셋업(scheduleFirstId);
		통과된_다음_스케줄_이용_가능_여부_셋업();
		통과된_개인_예약_입실_및_패널티_검증_셋업();

		// 용량 초과 X (updateStatus 호출 안 됨)
		given(nextSchedule.isCurrentResLessThanCapacity()).willReturn(true);

		// when
		String result = reservationService.extendReservation(reservationId, token);

		// then
		assertEquals("Success", result);
		verify(reservation).extendReservation(nextSchedule.getId(), nextSchedule.getEndTime());
		verify(nextSchedule).reserve();
		verify(nextSchedule, never()).updateStatus(ScheduleSlotStatus.RESERVED);
	}

	private void 통과된_기본_예약_검증_셋업(Long reservationId, String token, String email) {
		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(email);
		willDoNothing().given(reservation).validateOwnership(email, ReservationActionType.EXTEND_RESERVATION);
	}

	private void 통과된_스케줄_연장_시간_검증_셋업() {
		// 현재 시각: 13:59
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 13, 59);
		given(clock.instant()).willReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
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
		given(scheduleRepository.findById(scheduleId + 1)).willReturn(Optional.of(nextSchedule));
		given(nextSchedule.getRoomNumber()).willReturn("409-1");
	}

	private void 통과된_다음_스케줄_이용_가능_여부_셋업(){
		given(nextSchedule.isCurrentResLessThanCapacity()).willReturn(true);
		given(nextSchedule.isAvailable()).willReturn(true);
	}

	private void 통과된_개인_예약_입실_및_패널티_검증_셋업(){
		given(nextSchedule.getRoomType()).willReturn(RoomType.INDIVIDUAL);
		given(reservation.getMember()).willReturn(member1);
		given(reservation.getMember().isPenalty()).willReturn(false);
		given(reservation.isEntered()).willReturn(true);
	}

	private void 통과된_그룹_예약_입실_및_패널티_검증_셋업(){
		given(nextSchedule.getRoomType()).willReturn(RoomType.GROUP);
		given(reservationRepository.findByFirstScheduleId(scheduleFirstId)).willReturn(reservations);
		given(reservation.getMember()).willReturn(member1);
		given(reservation2.getMember()).willReturn(member2);

		given(reservations.get(0).getMember().isPenalty()).willReturn(false);
		given(reservations.get(1).getMember().isPenalty()).willReturn(false);

		given(reservations.get(0).isEntered()).willReturn(true);
		given(reservations.get(1).isEntered()).willReturn(true);
	}

	//취소하여 패널티 제재를 받은 member0 때문에 연장이 불가능해서는 안된다.
	// 로직 상 주석처리된 메서드는 호출되지 않지만, 이해를 위해 남김
	private void 통과된_그룹_예약_입실_및_패널티_검증_셋업_취소된_예약자_포함(){
		given(nextSchedule.getRoomType()).willReturn(RoomType.GROUP);
		given(reservationRepository.findByFirstScheduleId(scheduleFirstId)).willReturn(reservations);
		//given(reservation.getMember()).willReturn(member1);
		given(reservation2.getMember()).willReturn(member2);

		given(reservations.get(0).getStatus()).willReturn(ReservationStatus.CANCELLED);
		//given(reservations.get(0).getMember().isPenalty()).willReturn(true);
		given(reservations.get(1).getStatus()).willReturn(ReservationStatus.ENTRANCE);
		given(reservations.get(1).getMember().isPenalty()).willReturn(false);

		//given(reservations.get(0).isEntered()).willReturn(false);
		given(reservations.get(1).isEntered()).willReturn(true);
	}

	private void 현재_시간_고정(int hour, int minute) {
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, hour, minute);
		given(clock.instant()).willReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());
	}

	private void 예약_종료_시간_고정(int hour, int minute) {
		given(reservation.getScheduleDate()).willReturn(LocalDate.of(2025, 3, 22));
		given(reservation.getEndTime()).willReturn(LocalTime.of(hour, minute));
	}
}
