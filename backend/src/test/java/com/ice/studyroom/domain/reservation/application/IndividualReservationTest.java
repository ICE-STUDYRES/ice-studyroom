package com.ice.studyroom.domain.reservation.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.domain.identity.domain.service.QRCodeService;
import com.ice.studyroom.domain.identity.domain.service.TokenService;
import com.ice.studyroom.domain.identity.infrastructure.security.QRCodeUtil;
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

@ExtendWith(MockitoExtension.class)
class IndividualReservationTest {

	@Spy
	@InjectMocks
	private ReservationService reservationService;
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
	@Mock
	private QRCodeUtil qrCodeUtil;
	@Mock
	private QRCodeService qrCodeService;

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

	@DisplayName("정상적으로 스케줄을 1시간 예약 성공")
	@Test
	void 개인_예약_1시간_성공() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{firstScheduleId},
			new String[]{}, // 개인 예약이라 참여자 없음
			"305-1",        // 예시 방 번호
			LocalTime.of(12, 0),
			LocalTime.of(13, 0)
		);

		시간_고정_셋업(12, 30);
		스케줄_설정(firstSchedule, firstScheduleId, ScheduleSlotStatus.AVAILABLE, RoomType.INDIVIDUAL, 13, 30);
		스케줄_인원_제한_설정(firstSchedule, 6, 0);
		예약자_패널티_설정(false);
		QrCode_생성();

		// 이메일 발송 제거
		doNothing().when(reservationService).sendReservationSuccessEmail(any(), any(), any(), any());

		// when
		String result = reservationService.createIndividualReservation(token, request);

		// then
		assertEquals("Success", result);
		verify(scheduleRepository).saveAll(anyList());
		verify(reservationRepository).save(any(Reservation.class));
		verify(qrCodeService).saveQRCode(eq(email), eq(123L), eq(request.scheduleId().toString()), eq("fake-qrcode"));
	}

	@DisplayName("정상적으로 스케줄을 2시간 예약 성공")
	@Test
	void 개인_예약_2시간_성공() {
		// given
		String name = "홍길동";
		Long secondSchuduleId = 2L;
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{firstScheduleId, secondSchuduleId},
			new String[]{}, // 개인 예약이라 참여자 없음
			"305-1",        // 예시 방 번호
			LocalTime.of(12, 0),
			LocalTime.of(13, 0)
		);

		시간_고정_셋업(12, 30);
		스케줄_설정(firstSchedule, firstScheduleId, ScheduleSlotStatus.AVAILABLE, RoomType.INDIVIDUAL, 13, 30);
		스케줄_인원_제한_설정(firstSchedule, 6, 0);
		스케줄_설정(secondSchedule, secondSchuduleId, ScheduleSlotStatus.AVAILABLE, RoomType.INDIVIDUAL, 13, 30);
		스케줄_인원_제한_설정(secondSchedule, 6, 0);
		예약자_패널티_설정(false);
		QrCode_생성();

		doNothing().when(reservationService).sendReservationSuccessEmail(any(), any(), any(), any());

		// when
		String result = reservationService.createIndividualReservation(token, request);

		// then
		assertEquals("Success", result);
		verify(scheduleRepository).saveAll(anyList());
		verify(reservationRepository).save(any(Reservation.class));
		verify(qrCodeService).saveQRCode(eq(email), eq(123L), eq(request.scheduleId().toString()), eq("fake-qrcode"));
	}

	@DisplayName("사용 불가능한 스케줄로 예약 시도는 예외 발생")
	@Test
	void 사용_불가능한_스케줄로_예약_시도는_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ firstScheduleId },
			new String[]{}, // 개인 예약이라 참여자 없음
			"305-1",        // 예시 방 번호
			LocalTime.of(12, 0),
			LocalTime.of(13, 0)
		);

		given(scheduleRepository.findById(firstScheduleId)).willReturn(Optional.of(firstSchedule));
		given(firstSchedule.getStatus()).willReturn(ScheduleSlotStatus.RESERVED); // not AVAILABLE

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("존재하지 않거나 사용 불가능한 스케줄입니다.");

		verify(reservationRepository, never()).save(any());
		verify(qrCodeService, never()).saveQRCode(any(), any(), any(), any());
	}

	@DisplayName("단체전용방 예약 시도는 예외 발생")
	@Test
	void 단체전용방_예약_시도는_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{firstScheduleId},
			new String[]{},
			"305-1",
			LocalTime.of(12, 0),
			LocalTime.of(13, 0)
		);

		시간_고정_셋업(12, 30);
		스케줄_설정(firstSchedule, firstScheduleId, ScheduleSlotStatus.AVAILABLE, RoomType.GROUP, 13, 30);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("해당 방은 단체예약 전용입니다.");
		verify(reservationRepository, never()).save(any());
	}

	@DisplayName("존재하지 않는 회원의 예약 요청은 예외 발생")
	@Test
	void 존재하지_않는_회원의_예약_요청은_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{firstScheduleId},
			new String[]{},
			"305-1",
			LocalTime.of(12, 0),
			LocalTime.of(13, 0)
		);

		시간_고정_셋업(12, 30);
		스케줄_설정(firstSchedule, firstScheduleId, ScheduleSlotStatus.AVAILABLE, RoomType.INDIVIDUAL, 13, 30);

		given(tokenService.extractEmailFromAccessToken(token)).willReturn(email);
		given(memberRepository.findByEmail(Email.of(email))).willReturn(Optional.empty());

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("예약자 이메일이 존재하지 않습니다: " + email);
		verify(reservationRepository, never()).save(any());
	}

	@DisplayName("패널티를 받은 회원의 예약 요청은 예외 발생")
	@Test
	void 패널티를_받은_회원의_예약_요청은_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{firstScheduleId},
			new String[]{}, // 개인 예약이라 참여자 없음
			"305-1",        // 예시 방 번호
			LocalTime.of(12, 0),
			LocalTime.of(13, 0)
		);

		String name = "도성현";

		시간_고정_셋업(12, 30);
		스케줄_설정(firstSchedule, firstScheduleId, ScheduleSlotStatus.AVAILABLE, RoomType.INDIVIDUAL, 13, 30);
		예약자_패널티_설정(true);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createIndividualReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("사용정지 상태입니다.");
		verify(reservationRepository, never()).save(any());
	}

	@DisplayName("예약이 중복된다면 에러 발생")
	@Test
	void 예약이_중복된다면_에러_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{firstScheduleId},
			new String[]{}, // 개인 예약이라 참여자 없음
			"305-1",        // 예시 방 번호
			LocalTime.of(12, 0),
			LocalTime.of(13, 0)
		);

		String name = "도성현";

		시간_고정_셋업(12, 30);
		스케줄_설정(firstSchedule, firstScheduleId, ScheduleSlotStatus.AVAILABLE, RoomType.INDIVIDUAL, 13, 30);
		// 회원 정보 찾기
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(email);
		given(memberRepository.findByEmail(Email.of(email))).willReturn(Optional.empty());

		// 패널티 여부 확인
		Member member = Member.builder()
			.email(Email.of(email))
			.name(name)
			.isPenalty(false)
			.build();

		given(memberRepository.findByEmail(Email.of(email))).willReturn(Optional.of(member));

		Reservation duplicatedReservation = mock(Reservation.class);
		given(duplicatedReservation.getStatus()).willReturn(ReservationStatus.RESERVED); // 진행 중인 예약
		given(reservationRepository.findLatestReservationByUserEmail(email))
			.willReturn(Optional.of(duplicatedReservation));


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

	void 스케줄_설정(Schedule schedule, Long scheduleId, ScheduleSlotStatus scheduleSlotStatus, RoomType roomType, int hour, int minute) {
		given(schedule.getScheduleDate()).willReturn(LocalDate.of(2025, 3, 22));
		given(schedule.getStartTime()).willReturn(LocalTime.of(hour, minute));
		given(schedule.isAvailable()).willReturn(true);
		given(schedule.isCurrentResLessThanCapacity()).willReturn(true);
		given(schedule.getStatus()).willReturn(scheduleSlotStatus);
		lenient().when(schedule.getRoomType()).thenReturn(roomType);
		given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
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

	void QrCode_생성() {
		given(reservationRepository.save(any())).willAnswer(invocation -> {
			Reservation reservation = invocation.getArgument(0);
			reservation.setId(123L);
			return reservation;
		});

		given(qrCodeUtil.generateQRCode(eq(email), anyString())).willReturn("fake-qrcode");
	}
}

