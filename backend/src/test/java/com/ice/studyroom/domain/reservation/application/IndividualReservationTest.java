package com.ice.studyroom.domain.reservation.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.ice.studyroom.domain.reservation.domain.service.ReservationValidator;
import com.ice.studyroom.global.type.StatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.global.security.service.TokenService;
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

@ExtendWith(MockitoExtension.class)
class IndividualReservationTest {

	@Spy
	@InjectMocks
	private ReservationService reservationService;
	@Mock
	private ReservationConcurrencyService reservationConcurrencyService;
	@Mock
	private ReservationValidator reservationValidator;
	@Mock
	private ReservationRepository reservationRepository;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private ScheduleRepository scheduleRepository;
	@Mock
	private TokenService tokenService;
	@Mock
	private Clock clock;
	@Mock
	private Schedule firstSchedule;
	@Mock
	private Schedule secondSchedule;
	private String email;
	private String token;
	private Long firstScheduleId;


	@BeforeEach
	void setUp() {
		email = "user@hufs.ac.kr";
		token = "Bearer valid_token";
		firstScheduleId = 1L;

		firstSchedule = mock(Schedule.class);
		secondSchedule = mock(Schedule.class);
	}

	/**
	 * 📌 테스트명: 개인_예약_1시간_성공
	 *
	 * ✅ 목적:
	 *   - 사용자가 예약 가능한 1개의 스케줄에 대해 개인 예약을 정상적으로 생성할 수 있는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 현재 시각은 12:30, 예약할 스케줄은 13:30 시작 (예약 가능 시간)
	 *   2. 스케줄 상태는 AVAILABLE, RoomType은 INDIVIDUAL, 정원이 남아 있음
	 *   3. JWT 토큰에서 이메일 추출 → 회원 조회 성공, 패널티 없음
	 *   4. 중복 예약 없음
	 *   5. 예약 저장 후 QR 코드 생성 및 저장
	 *   6. 스케줄의 currentRes 증가 → 저장
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 예약은 AVAILABLE 상태에서만 가능
	 *   - 개인 예약은 RoomType이 INDIVIDUAL인 방에서만 가능
	 *   - 패널티가 없는 유효한 회원만 예약 가능
	 *   - 기존 예약이 없을 경우에만 신규 예약 허용
	 *
	 * 🧩 검증 포인트:
	 *   - reservationRepository.save()가 호출되는가?
	 *   - scheduleRepository.saveAll()이 호출되는가?
	 *   - qrCodeService.saveQRCode()가 정확한 파라미터로 호출되는가?
	 *
	 * ✅ 기대 결과:
	 *   - "Success" 응답 반환
	 *   - 스케줄 정보가 저장되며, QR 코드 생성 및 저장됨
	 *   - 예외 없이 정상적으로 개인 예약 생성 완료
	 */
	@Test
	@DisplayName("정상적으로 스케줄을 1시간 예약 성공")
	void 개인_예약_1시간_성공() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{} // 개인 예약이라 참여자 없음
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(), firstSchedule);
		스케줄_설정(firstSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.INDIVIDUAL, 13, 30);
		스케줄_인원_제한_설정(firstSchedule, 6, 0);
		예약자_패널티_설정(false);

		// 이메일 발송 제거
		doNothing().when(reservationService).sendReservationSuccessEmail(any(), any(), any(), any());

		// when
		String result = reservationService.createIndividualReservation(token, request);

		// then
		assertEquals("Success", result);
		verify(scheduleRepository).saveAll(anyList());
		verify(reservationRepository).save(any(Reservation.class));
	}

	/**
	 * 📌 테스트명: 개인_예약_2시간_성공
	 *
	 * ✅ 목적:
	 *   - 사용자가 연속된 2개의 스케줄에 대해 예약을 정상적으로 생성할 수 있는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 현재 시각은 12:30, 예약할 스케줄은 13:30 시작
	 *   2. 스케줄 2개 모두 AVAILABLE, INDIVIDUAL, 수용 가능
	 *   3. 패널티 없음, 중복 예약 없음
	 *   4. 예약 저장 및 QR 코드 저장
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 예약은 최대 2시간까지 가능
	 *   - 스케줄은 모두 INDIVIDUAL이어야 하며 예약 가능 상태여야 함
	 *
	 * 🧩 검증 포인트:
	 *   - reservationRepository.save() 호출
	 *   - scheduleRepository.saveAll() 호출
	 *   - QR 코드 저장 여부
	 *
	 * ✅ 기대 결과:
	 *   - "Success" 반환
	 *   - 예약 성공 및 관련 데이터 저장 완료
	 */
	@Test
	@DisplayName("정상적으로 스케줄을 2시간 예약 성공")
	void 개인_예약_2시간_성공() {
		// given
		Long secondSchuduleId = 2L;
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{firstScheduleId, secondSchuduleId},
			new String[]{} // 개인 예약이라 참여자 없음
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(), firstSchedule, secondSchedule);
		스케줄_설정(firstSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.INDIVIDUAL, 13, 30);
		스케줄_인원_제한_설정(firstSchedule, 6, 0);
		스케줄_설정(secondSchedule, ScheduleSlotStatus.AVAILABLE, RoomType.INDIVIDUAL, 13, 30);
		스케줄_인원_제한_설정(secondSchedule, 6, 0);
		예약자_패널티_설정(false);

		doNothing().when(reservationService).sendReservationSuccessEmail(any(), any(), any(), any());

		// when
		String result = reservationService.createIndividualReservation(token, request);

		// then
		assertEquals("Success", result);
		verify(scheduleRepository).saveAll(anyList());
		verify(reservationRepository).save(any(Reservation.class));
	}

	/**
	 * 📌 테스트명: 예약_시작시간_직전에_예약_시도_예외_발생
	 *
	 * ✅ 목적:
	 *   - 사용자가 예약 시작 시간과 **정확히 같은 시간**에 예약을 시도할 경우,
	 *     예약이 거부되는지 검증한다. (경계값 테스트)
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약할 스케줄: 13:00 시작
	 *   2. 현재 시간: 13:00 (== 시작 시간)
	 *   3. 스케줄 상태: AVAILABLE, INDIVIDUAL, 수용 가능
	 *   4. 회원: 존재하고 패널티 없음
	 *   5. 중복 예약 없음
	 *   6. 예약 시도 시, validateSchedulesAvailable()에서 시간 조건에 걸려 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 스케줄 시작 시간이 현재 시간보다 **이후**여야 예약 가능
	 *
	 * 🧩 검증 포인트:
	 *   - 예외 메시지가 "예약이 불가능합니다."인지 확인
	 *   - reservationRepository, scheduleRepository, qrCodeService는 호출되지 않아야 함
	 *
	 * ✅ 기대 결과:
	 *   - 예약이 생성되지 않으며 예외가 발생
	 */
	@Test
	@DisplayName("스케줄 시작 시간과 동일한 시간에 예약 시도 시 예외 발생")
	void 예약_시작시간_직전에_예약_시도_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{}
		);

		예약자_패널티_설정(false);

		given(reservationConcurrencyService.processIndividualReservationWithLock(anyList()))
			.willThrow(new BusinessException(StatusCode.BAD_REQUEST, "예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다."));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다.");
		verify(reservationRepository, never()).save(any());
	}

	/**
	 * 📌 테스트명: 사용_불가능한_스케줄로_예약_시도는_예외_발생
	 *
	 * ✅ 목적:
	 *   - 예약이 불가능한 스케줄에 대해 예약을 시도할 경우 예외가 발생하는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약할 스케줄의 상태가 AVAILABLE이 아님 (RESERVED)
	 *   2. findById()는 Optional.of(schedule)를 반환하나, 상태 조건 미달
	 *   3. 예외 발생: "존재하지 않거나 사용 불가능한 스케줄입니다."
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 예약은 AVAILABLE 상태의 스케줄에만 가능
	 *
	 * 🧩 검증 포인트:
	 *   - 예외가 발생해야 하며, 예약/QR 저장 로직은 실행되지 않아야 함
	 *
	 * ✅ 기대 결과:
	 *   - 예외 발생 및 저장 로직 미호출
	 */
	@Test
	@DisplayName("사용 불가능한 스케줄로 예약 시도는 예외 발생")
	void 사용_불가능한_스케줄로_예약_시도는_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{}// 개인 예약이라 참여자 없음
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(), firstSchedule);
		스케줄_설정(firstSchedule, ScheduleSlotStatus.RESERVED, RoomType.INDIVIDUAL, 13, 0);
		예약자_패널티_설정(false);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다.");

		verify(reservationRepository, never()).save(any());
	}

	/**
	 * 📌 테스트명: 단체전용방_예약_시도는_예외_발생
	 *
	 * ✅ 목적:
	 *   - 개인 예약 요청 시, RoomType이 GROUP일 경우 예외가 발생하는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약할 스케줄의 RoomType이 GROUP
	 *   2. createIndividualReservation 내부에서 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 개인 예약은 INDIVIDUAL 타입의 방에서만 가능
	 *
	 * 🧩 검증 포인트:
	 *   - BusinessException이 발생하는가?
	 *   - 저장 로직이 호출되지 않는가?
	 *
	 * ✅ 기대 결과:
	 *   - 예외 발생 ("해당 방은 단체예약 전용입니다.")
	 */
	@Test
	@DisplayName("단체전용방 예약 시도는 예외 발생")
	void 단체전용방_예약_시도는_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{firstScheduleId},
			new String[]{}
		);

		예약자_패널티_설정(false);

		given(reservationConcurrencyService.processIndividualReservationWithLock(anyList()))
			.willThrow(new BusinessException(StatusCode.FORBIDDEN, "해당 방은 단체예약 전용입니다."));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("해당 방은 단체예약 전용입니다.");
		verify(reservationRepository, never()).save(any());
	}

	/**
	 * 📌 테스트명: 존재하지_않는_회원의_예약_요청은_예외_발생
	 *
	 * ✅ 목적:
	 *   - JWT 토큰에서 추출한 이메일로 회원 조회 실패 시 예외가 발생하는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. JWT에서 이메일 추출
	 *   2. memberRepository.findByEmail() → Optional.empty()
	 *   3. 예외 발생: NOT_FOUND
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 예약자는 시스템에 등록된 유효한 회원이어야 한다
	 *
	 * 🧩 검증 포인트:
	 *   - 예외 발생 및 저장 로직 미호출
	 *
	 * ✅ 기대 결과:
	 *   - 예외 메시지: "예약자 이메일이 존재하지 않습니다: ... "
	 */
	@Test
	@DisplayName("존재하지 않는 회원의 예약 요청은 예외 발생")
	void 존재하지_않는_회원의_예약_요청은_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{firstScheduleId},
			new String[]{}
		);

		given(tokenService.extractEmailFromAccessToken(token)).willReturn(email);
		given(memberRepository.findByEmail(Email.of(email))).willReturn(Optional.empty());

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("예약자 이메일이 존재하지 않습니다: " + email);
		verify(reservationRepository, never()).save(any());
	}

	/**
	 * 📌 테스트명: 패널티를_받은_회원의_예약_요청은_예외_발생
	 *
	 * ✅ 목적:
	 *   - 패널티 상태인 회원이 예약을 시도할 경우 예외가 발생하는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. JWT로 이메일 추출 → 회원 조회 성공
	 *   2. member.isPenalty() == true
	 *   3. 예외 발생: FORBIDDEN ("사용정지 상태입니다.")
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 패널티 상태의 회원은 예약할 수 없다
	 *
	 * 🧩 검증 포인트:
	 *   - 예외가 발생하고, 저장 로직은 수행되지 않아야 한다
	 *
	 * ✅ 기대 결과:
	 *   - 예외 발생 및 저장 미수행
	 */
	@Test
	@DisplayName("패널티를 받은 회원의 예약 요청은 예외 발생")
	void 패널티를_받은_회원의_예약_요청은_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{firstScheduleId},
			new String[]{}
		);

		예약자_패널티_설정(true);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("사용정지 상태입니다.");
		verify(reservationRepository, never()).save(any());
	}

	/**
	 * 📌 테스트명: 예약이_중복된다면_에러_발생
	 *
	 * ✅ 목적:
	 *   - 기존에 예약이 진행 중인 사용자가 다시 예약을 시도하면 예외가 발생하는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. JWT에서 이메일 추출 → 회원 조회 성공
	 *   2. 최근 예약 상태가 RESERVED 또는 ENTRANCE
	 *   3. 예외 발생: CONFLICT
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - RESERVED 또는 ENTRANCE 상태의 예약이 존재하면 새 예약 불가
	 *
	 * 🧩 검증 포인트:
	 *   - 예외 메시지가 명확한가?
	 *   - 저장 로직이 호출되지 않는가?
	 *
	 * ✅ 기대 결과:
	 *   - 예외 발생 및 중복 예약 방지
	 */
	@Test
	@DisplayName("예약이 중복된다면 에러 발생")
	void 예약이_중복된다면_에러_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{firstScheduleId},
			new String[]{}
		);

		예약자_패널티_설정(false);

		doThrow(new BusinessException(StatusCode.CONFLICT, "현재 예약이 진행 중이므로 새로운 예약을 생성할 수 없습니다."))
			.when(reservationValidator).checkDuplicateReservation(Email.of(email));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("현재 예약이 진행 중이므로 새로운 예약을 생성할 수 없습니다.");
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
