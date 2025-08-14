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
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;
import com.ice.studyroom.global.exception.BusinessException;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

	@Mock
	private ReservationConcurrencyService reservationConcurrencyService;

	@Mock
	private ReservationCompensationService reservationCompensationService;

	@Mock
	private ReservationValidator reservationValidator;

	@Spy
	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private Reservation reservation;

	@Mock
	private Reservation reservation2;

	@Mock
	private Reservation reservation3;

	@Mock
	private Schedule schedule;

	@Mock
	private Member reservationOwner;

	@Mock
	private Member member1;

	@Mock
	private Member member2;

	private String token;
	private String ownerEmail;
	private Long scheduleId;

	@BeforeEach
	void setUp() {
		// 공통 객체 생성 (Mock 객체만 설정)
		reservation = mock(Reservation.class);
		reservation2 = mock(Reservation.class);
		reservation3 = mock(Reservation.class);

		schedule = mock(Schedule.class);

		reservationOwner = mock(Member.class);
		member1 = mock(Member.class);
		member2 = mock(Member.class);

		// 공통 값 설정
		token = "Bearer token";
		ownerEmail = "owner@hufs.ac.kr";
		scheduleId = 1L;
	}

	/**
	 * 📌 테스트명: 사예약 상태인 스케줄로 예약 시도 시 예외 발생
	 *
	 * ✅ 목적:
	 *   - 예약하려는 스케줄의 상태가 이미 RESERVED 상태인 경우, 예약이 불가능하도록 예외를 발생시키는 비즈니스 로직 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 특정 스케줄 ID로 그룹 예약 요청
	 *   2. 해당 스케줄은 이미 RESERVED 상태
	 *   3. 예약 생성 요청 시 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 스케줄 상태가 `AVAILABLE`이 아닌 경우 예약이 불가능해야 한다
	 *
	 * 🧩 검증 포인트:
	 *   - `schedule.isAvailable()`이 false일 경우 예외 발생
	 *   - 예외 메시지가 "예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다."인지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다."
	 */
	@Test
	@DisplayName("예약 상태인 스케줄로 예약 시도 시 예외 발생")
	void 예약_상태인_스케줄로_예약_시도_시_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		기본_설정();
		given(reservationConcurrencyService.processGroupReservationWithLock(anyList(), anySet()))
			.willThrow(new BusinessException(null, "예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다."));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다.");
	}

	/**
	 * 📌 테스트명: 입장 시간이 지난 스케줄로 예약 시도 시 예외 발생
	 *
	 * ✅ 목적:
	 *   - 예약 시간보다 현재 시간이 늦은 경우, 예약이 불가능하다는 비즈니스 로직 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 특정 스케줄 ID로 그룹 예약 요청
	 *   2. 현재 시각이 해당 스케줄의 시작 시간 이후임
	 *   3. 예약 생성 요청 시 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 예약은 스케줄의 시작 시간 이전에만 가능해야 한다
	 *
	 * 🧩 검증 포인트:
	 *   - `schedule.getStartTime()`과 현재 시각 비교 후 예외 발생
	 *   - 예외 메시지가 "예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다."인지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다."
	 */
	@Test
	@DisplayName("입장 시간이 지난 스케줄로 예약 시도 시 예외 발생")
	void 입장_시간이_지난_스케줄로_예약_시도_시_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		기본_설정();
		given(reservationConcurrencyService.processGroupReservationWithLock(anyList(), anySet()))
			.willThrow(new BusinessException(null, "예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다."));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다.");
	}

	/**
	 * 📌 테스트명: 개인전용방 예약 시도는 예외 발생
	 *
	 * ✅ 목적:
	 *   - 그룹 예약 요청 시, 스케줄이 개인 전용(RoomType.INDIVIDUAL)인 경우 예외를 발생시키는 비즈니스 로직 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 특정 스케줄에 대해 그룹 예약 요청
	 *   2. 해당 스케줄의 RoomType이 INDIVIDUAL로 설정되어 있음
	 *   3. 예약 생성 시 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - RoomType이 개인전용(INDIVIDUAL)인 경우 그룹 예약을 생성할 수 없다
	 *
	 * 🧩 검증 포인트:
	 *   - `schedule.getRoomType()`이 INDIVIDUAL일 경우 예외 발생
	 *   - 예외 메시지가 정확히 일치하는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "해당 방은 개인예약 전용입니다."
	 */
	@Test
	@DisplayName("개인전용방 예약 시도는 예외 발생")
	void 개인전용방_예약_시도는_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		기본_설정();
		given(reservationConcurrencyService.processGroupReservationWithLock(anyList(), anySet()))
			.willThrow(new BusinessException(null, "해당 방은 개인예약 전용입니다."));


		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("해당 방은 개인예약 전용입니다.");
	}

	/**
	 * 📌 테스트명: 패널티를 받은 회원의 예약 요청은 예외 발생
	 *
	 * ✅ 목적:
	 *   - 예약자가 패널티 상태일 경우, 그룹 예약 생성이 불가능하도록 제한하는 로직 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 본인의 계정으로 그룹 예약 요청
	 *   2. 예약자 정보 조회 후, isPenalty()가 true로 설정됨
	 *   3. 예약 생성 시 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 패널티 상태인 회원은 예약을 생성할 수 없다
	 *
	 * 🧩 검증 포인트:
	 *   - `reservationOwner.isPenalty()`가 true일 경우 예외 발생
	 *   - 예외 메시지가 정확히 일치하는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "예약자가 패널티 상태입니다. 예약이 불가능합니다."
	 */
	@Test
	@DisplayName("패널티를 받은 회원의 예약 요청은 예외 발생")
	void 패널티를_받은_회원의_예약_요청은_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		given(tokenService.extractEmailFromAccessToken(token)).willReturn(ownerEmail);
		given(memberRepository.findByEmail(Email.of(ownerEmail))).willReturn(Optional.of(reservationOwner));

		// 패널티 상태로 설정하고 validateReservationEligibility에서 예외 발생하도록 설정
		doThrow(new BusinessException(null, "패널티 상태의 사용자는 예약이 불가능합니다."))
			.when(reservationOwner).validateReservationEligibility();

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("패널티 상태의 사용자는 예약이 불가능합니다.");
	}

	/**
	 * 📌 테스트명: 예약자의 중복 예약 여부 확인 시 예외 발생
	 *
	 * ✅ 목적:
	 *   - 예약자가 이미 진행 중인 예약(RESERVED 또는 ENTRANCE 상태)을 가지고 있을 경우,
	 *     새로운 그룹 예약 생성이 제한되는 로직을 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 본인의 계정으로 그룹 예약 요청
	 *   2. 예약자 정보 조회 → 패널티 상태 아님
	 *   3. 해당 예약자의 가장 최근 예약 상태가 RESERVED 또는 ENTRANCE
	 *   4. 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 이미 예약이 진행 중인 사용자는 추가로 예약을 생성할 수 없다
	 *
	 * 🧩 검증 포인트:
	 *   - `reservationRepository.findLatestReservationByMemberEmail()` 호출
	 *   - 최근 예약 상태가 RESERVED 또는 ENTRANCE인 경우 예외 발생
	 *   - 예외 메시지가 정확히 일치하는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "현재 예약이 진행 중이므로 새로운 예약을 생성할 수 없습니다."
	 */
	@Test
	@DisplayName("예약자의 중복 예약 여부 확인 시 예외 발생")
	void 예약자의_중복_예약_여부_확인_시_예외_발생() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		기본_설정();
		// reservationValidator.checkDuplicateReservation에서 예외 발생하도록 설정
		doThrow(new BusinessException(null, "현재 예약이 진행 중이므로 새로운 예약을 생성할 수 없습니다."))
			.when(reservationValidator).checkDuplicateReservation(Email.of(ownerEmail));

		given(reservationConcurrencyService.processGroupReservationWithLock(anyList(), anySet()))
			.willThrow(new BusinessException(null, "현재 예약이 진행 중이므로 새로운 예약을 생성할 수 없습니다."));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("현재 예약이 진행 중이므로 새로운 예약을 생성할 수 없습니다.");
	}

	/**
	 * 📌 테스트명: 참여자 중 존재하지 않는 이메일이 있을 경우 예외 발생
	 *
	 * ✅ 목적:
	 *   - 그룹 예약 참여자 중 시스템에 등록되지 않은 이메일이 존재할 경우,
	 *     예외를 발생시켜 예약이 생성되지 않도록 방지하는 로직을 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자가 그룹 예약 요청
	 *   2. 예약자는 패널티 아님 + 예약 중복 없음
	 *   3. 참여자 중 첫 번째 이메일은 존재
	 *   4. 두 번째 이메일은 존재하지 않음 (Optional.empty())
	 *   5. 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 모든 참여자는 사전에 시스템에 등록된 회원이어야 한다
	 *
	 * 🧩 검증 포인트:
	 *   - `memberRepository.findByEmail()` 호출 시 Optional.empty() 반환되는 경우 예외 발생
	 *   - 예외 메시지가 정확히 일치하는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "참여자 이메일이 존재하지 않습니다: member2@hufs.ac.kr"
	 */
	@Test
	@DisplayName("참여자 중 존재하지 않는 이메일이 있을 경우 예외 발생")
	void 참여자_중_존재하지_않는_이메일이_있을_경우_예외_발생(){
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		given(tokenService.extractEmailFromAccessToken(token)).willReturn(ownerEmail);
		given(memberRepository.findByEmail(Email.of(ownerEmail))).willReturn(Optional.of(reservationOwner));
		doNothing().when(reservationOwner).validateReservationEligibility();

		given(memberRepository.findByEmail(Email.of("member1@hufs.ac.kr"))).willReturn(Optional.of(member1));
		given(memberRepository.findByEmail(Email.of("member2@hufs.ac.kr"))).willReturn(Optional.empty());

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).contains("사용자를 찾을 수 없습니다"); // MemberNotFoundException의 메시지
	}

	/**
	 * 📌 테스트명: 참여자 목록에 중복 이메일이 있을 경우 예외 발생
	 *
	 * ✅ 목적:
	 *   - 그룹 예약 생성 시, 참여자 목록에 중복된 이메일이 존재할 경우 예외를 발생시키는 비즈니스 로직 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약자가 그룹 예약 요청을 생성
	 *   2. 참여자 목록에 동일한 이메일(`member1@hufs.ac.kr`)이 중복으로 포함됨
	 *   3. 중복 이메일 검증 로직에서 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 그룹 예약 시 중복된 참여자 이메일이 존재해서는 안 된다
	 *
	 * 🧩 검증 포인트:
	 *   - `Set<String>`을 사용해 중복 검사
	 *   - `!uniqueEmails.add(email)` 구문에서 false가 반환되어 예외 발생
	 *   - 예외 메시지가 정확히 일치하는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "중복된 참여자 이메일이 존재합니다: member1@hufs.ac.kr"
	 */
	@Test
	@DisplayName("참여자 목록에 중복 이메일이 있을 경우 예외 발생")
	void 참여자_목록에_중복_이메일이_있을_경우_예외_발생(){
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member1@hufs.ac.kr" } //중복 이메일
		);

		시간_고정_셋업(12, 30);
		스케줄_리스트_설정(request.scheduleId(),schedule);
		스케줄_설정(schedule, ScheduleSlotStatus.AVAILABLE, RoomType.GROUP, 13, 0);
		비패널티_예약자_존재확인();
		예약자_미중복_예약_확인();

		given(memberRepository.findByEmail(Email.of("member1@hufs.ac.kr"))).willReturn(Optional.of(member1));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("중복된 참여자 이메일이 존재합니다: member1@hufs.ac.kr");
	}

	/**
	 * 📌 테스트명: 참여자 중 패널티 상태인 경우 예외 발생
	 *
	 * ✅ 목적:
	 *   - 그룹 예약 시, 참여자 중 1명이라도 패널티 상태인 경우 예약이 불가함을 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약자가 그룹 예약 요청을 생성
	 *   2. 참여자 목록에 `member2@hufs.ac.kr`이 포함됨
	 *   3. 해당 참여자가 패널티 상태인 경우
	 *   4. 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 패널티 상태의 참여자는 예약에 포함될 수 없다
	 *
	 * 🧩 검증 포인트:
	 *   - `Member.isPenalty()`가 true인 경우 예외 발생
	 *   - 예외 메시지에 해당 이메일이 포함되어 있는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "참여자 중 패널티 상태인 사용자가 있습니다. 예약이 불가능합니다. (이메일: member2@hufs.ac.kr)"
	 */
	@Test
	@DisplayName("참여자 중 패널티 상태인 경우 예외 발생")
	void 참여자_중_패널티_상태인_경우_예외_발생(){
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		given(tokenService.extractEmailFromAccessToken(token)).willReturn(ownerEmail);
		given(memberRepository.findByEmail(Email.of(ownerEmail))).willReturn(Optional.of(reservationOwner));
		doNothing().when(reservationOwner).validateReservationEligibility();

		given(memberRepository.findByEmail(Email.of("member1@hufs.ac.kr"))).willReturn(Optional.of(member1));
		given(memberRepository.findByEmail(Email.of("member2@hufs.ac.kr"))).willReturn(Optional.of(member2));

		doNothing().when(member1).validateReservationEligibility();
		doThrow(new BusinessException(null, "참여자 중 패널티 상태인 사용자가 있습니다. 예약이 불가능합니다. (이메일: member2@hufs.ac.kr)"))
			.when(member2).validateReservationEligibility();

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("참여자 중 패널티 상태인 사용자가 있습니다. 예약이 불가능합니다. (이메일: member2@hufs.ac.kr)");
	}

	/**
	 * 📌 테스트명: 참여자 중 특정 예약이 진행 중인 경우 예외 발생
	 *
	 * ✅ 목적:
	 *   - 그룹 예약 시, 참여자 중 현재 RESERVED 또는 ENTRANCE 상태인 예약이 존재하면 예외가 발생하는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약자가 그룹 예약 요청을 생성
	 *   2. 참여자 중 한 명(member2)이 이미 예약 상태(RESERVED 또는 ENTRANCE)를 가지고 있음
	 *   3. 중복 예약 방지 로직에 의해 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 예약자는 물론, 참여자 또한 현재 예약이 진행 중인 경우 새로운 예약이 불가하다
	 *
	 * 🧩 검증 포인트:
	 *   - `reservationRepository.findLatestReservationByMemberEmail()` 호출
	 *   - 최근 상태가 `RESERVED`인 경우 예외 발생
	 *   - 예외 메시지에 해당 참여자 이메일이 포함되어 있는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "참여자 중 현재 예약이 진행 중인 사용자가 있어 예약이 불가능합니다. (이메일: member2@hufs.ac.kr)"
	 */
	@Test
	@DisplayName("참여자 중 특정 예약이 진행 중인 경우 예외 발생")
	void 참여자_중_특정_예약이_진행_중인_경우_예외_발생(){
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		기본_설정();
		// member2의 중복 예약 검사에서 예외 발생하도록 설정
		doNothing().when(reservationValidator).checkDuplicateReservation(Email.of(ownerEmail));
		doNothing().when(reservationValidator).checkDuplicateReservation(Email.of("member1@hufs.ac.kr"));
		doThrow(new BusinessException(null, "참여자 중 현재 예약이 진행 중인 사용자가 있어 예약이 불가능합니다. (이메일: member2@hufs.ac.kr)"))
			.when(reservationValidator).checkDuplicateReservation(Email.of("member2@hufs.ac.kr"));

		given(reservationConcurrencyService.processGroupReservationWithLock(anyList(), anySet()))
			.willThrow(new BusinessException(null, "참여자 중 현재 예약이 진행 중인 사용자가 있어 예약이 불가능합니다. (이메일: member2@hufs.ac.kr)"));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("참여자 중 현재 예약이 진행 중인 사용자가 있어 예약이 불가능합니다. (이메일: member2@hufs.ac.kr)");
	}

	/**
	 * 📌 테스트명: 최소 예약 인원 수 미달 시 예외 발생
	 *
	 * ✅ 목적:
	 *   - 그룹 예약 요청 시 최소 인원 조건(`minRes`)을 만족하지 않으면 예외가 발생하는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약자가 2명의 참여자를 포함한 그룹 예약 요청을 생성
	 *   2. 총 인원은 예약자 포함 3명
	 *   3. 스케줄의 최소 예약 인원(`minRes`)은 4명으로 설정
	 *   4. 최소 인원 미달로 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 그룹 예약은 `minRes` 이상의 인원만 가능하다
	 *
	 * 🧩 검증 포인트:
	 *   - `schedule.getMinRes()` 반환값 검증
	 *   - 인원 수 조건 불충족 시 예외 발생
	 *   - 예외 메시지가 정확히 일치하는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "최소 예약 인원 조건을 만족하지 않습니다. (필요 인원: 4, 현재 인원: 3)"
	 */
	@Test
	@DisplayName("최소 예약 인원 수 미달 시 예외 발생")
	void 최소_예약_인원_미달_시_예외_발생(){
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		기본_설정();
		given(reservationConcurrencyService.processGroupReservationWithLock(anyList(), anySet()))
			.willThrow(new BusinessException(null, "최소 예약 인원 조건을 만족하지 않습니다. (필요 인원: 4, 현재 인원: 3)"));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("최소 예약 인원 조건을 만족하지 않습니다. (필요 인원: 4, 현재 인원: 3)");
	}

	/**
	 * 📌 테스트명: 최대 수용 인원 초과 시 예외 발생
	 *
	 * ✅ 목적:
	 *   - 그룹 예약 요청 시 방의 최대 수용 인원(`capacity`)을 초과하는 경우 예외가 발생하는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약자가 2명의 참여자와 함께 예약 요청
	 *   2. 총 인원은 예약자 포함 3명
	 *   3. 해당 스케줄의 수용 인원은 2명으로 설정
	 *   4. 초과 인원으로 인해 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 그룹 예약 인원은 `capacity` 이하이어야 한다
	 *
	 * 🧩 검증 포인트:
	 *   - `schedule.getCapacity()` 반환값 검증
	 *   - 초과 시 예외 발생
	 *   - 예외 메시지가 정확히 일치하는지 확인
	 *
	 * ✅ 기대 결과:
	 *   - BusinessException 발생
	 *   - 메시지: "방의 최대 수용 인원을 초과했습니다. (최대 수용 인원: 2, 현재 인원: 3)"
	 */
	@Test
	@DisplayName("최대 수용 인원 초과 시 예외 발생")
	void 최대_수용_인원_초과_시_예외_발생(){
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		기본_설정();
		given(reservationConcurrencyService.processGroupReservationWithLock(anyList(), anySet()))
			.willThrow(new BusinessException(null, "방의 최대 수용 인원을 초과했습니다. (최대 수용 인원: 2, 현재 인원: 3)"));

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.createGroupReservation(token, request)
		);

		assertThat(ex.getMessage()).isEqualTo("방의 최대 수용 인원을 초과했습니다. (최대 수용 인원: 2, 현재 인원: 3)");
	}

	/**
	 * 📌 테스트명: 그룹 예약 생성 성공
	 *
	 * ✅ 목적:
	 *   - 정상적인 예약 요청에 대해 그룹 예약이 성공적으로 생성되고, 상태가 올바르게 업데이트되는지 검증
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약자가 2명의 참여자와 함께 예약 요청을 생성 (총 3명)
	 *   2. 모든 스케줄은 예약 가능 상태이며, 최소/최대 인원 조건을 만족
	 *   3. 중복 이메일 및 패널티/중복 예약 없음
	 *   4. 예약 저장 및 스케줄 상태 변경, 이메일 전송 함수가 호출됨
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 유효한 조건의 그룹 예약은 예약 저장 → 스케줄 상태 업데이트 → 이메일 전송 순으로 진행되어야 한다
	 *
	 * 🧩 검증 포인트:
	 *   - `reservationRepository.saveAll()` 호출 여부
	 *   - `schedule.updateGroupCurrentRes()` 및 `updateStatus()` 호출 여부
	 *   - `sendReservationSuccessEmail()` 호출 여부
	 *   - 최종 응답값이 "Success"인지 확인
	 *
	 * ✅ 기대 결과:
	 *   - 반환값: "Success"
	 *   - 예약 저장 메서드 및 상태 변경 메서드 호출
	 */
	@Test
	@DisplayName("그룹 예약 생성 성공 시, 예약 저장 및 상태 업데이트가 정상적으로 수행됨")
	void 그룹_예약_생성_성공() {
		// given
		CreateReservationRequest request = new CreateReservationRequest(
			new Long[]{ scheduleId },
			new String[]{ "member1@hufs.ac.kr", "member2@hufs.ac.kr" }
		);

		기본_설정();

		// 성공적인 스케줄 처리
		given(reservationConcurrencyService.processGroupReservationWithLock(anyList(), anySet()))
			.willReturn(Arrays.asList(schedule));

		given(schedule.getRoomType()).willReturn(RoomType.GROUP);
		given(schedule.getRoomNumber()).willReturn("309-1");

		//예약 완료 메일 전송 호출을 추적하지 않음
		doNothing().when(reservationService)
			.sendReservationSuccessEmail(any(), any(), any(), any());

		// when
		assertDoesNotThrow(() -> reservationService.createGroupReservation(token, request));

		// then
		verify(reservationRepository).saveAll(anyList());
	}

	void 시간_고정_셋업(int hour, int minute) {
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, hour, minute);
		lenient().when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());
	}

	void
	스케줄_리스트_설정(Long[] ids, Schedule... schedules) {
		lenient().when(scheduleRepository.findAllByIdIn(Arrays.stream(ids).toList()))
			.thenReturn(List.of(schedules));
	}

	void 스케줄_설정(Schedule schedule, ScheduleSlotStatus scheduleSlotStatus, RoomType roomType, int hour, int minute) {
		lenient().when(schedule.getScheduleDate()).thenReturn(LocalDate.of(2025, 3, 22));
		lenient().when(schedule.getStartTime()).thenReturn(LocalTime.of(hour, minute));
		lenient().when(schedule.isAvailable()).thenReturn(ScheduleSlotStatus.AVAILABLE == scheduleSlotStatus);
		lenient().when(schedule.isCurrentResLessThanCapacity()).thenReturn(true);
		lenient().when(schedule.getRoomType()).thenReturn(roomType);
	}

	void 예약자_존재확인(){
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(ownerEmail);
		given(memberRepository.findByEmail(Email.of(ownerEmail))).willReturn(Optional.of(reservationOwner));
	}

	void 비패널티_예약자_존재확인(){
		lenient().when(tokenService.extractEmailFromAccessToken(token)).thenReturn(ownerEmail);
		lenient().when(memberRepository.findByEmail(Email.of(ownerEmail))).thenReturn(Optional.of(reservationOwner));
		lenient().when(reservationOwner.isPenalty()).thenReturn(false);
	}

	void 예약자_미중복_예약_확인(){
		lenient().when(reservationRepository.findLatestReservationByMemberEmail(Email.of(ownerEmail))).thenReturn(Optional.of(reservation));
		lenient().when(reservation.getStatus()).thenReturn(ReservationStatus.COMPLETED);
	}

	void 예약_인원_명단에_중복이_없음을_확인(){
		lenient().when(memberRepository.findByEmail(Email.of("member1@hufs.ac.kr"))).thenReturn(Optional.of(member1));
		lenient().when(memberRepository.findByEmail(Email.of("member2@hufs.ac.kr"))).thenReturn(Optional.of(member2));
	}

	void 예약_참여_인원_비패널티_확인(){
		lenient().when(member1.isPenalty()).thenReturn(false);
		lenient().when(member2.isPenalty()).thenReturn(false);
	}

	void 예약_참여_인원_진행중인_예약_없음_확인(){
		lenient().when(reservationRepository.findLatestReservationByMemberEmail(Email.of("member1@hufs.ac.kr"))).thenReturn(Optional.of(reservation2));
		lenient().when(reservationRepository.findLatestReservationByMemberEmail(Email.of("member2@hufs.ac.kr"))).thenReturn(Optional.of(reservation3));

		lenient().when(reservation2.getStatus()).thenReturn(ReservationStatus.COMPLETED);
		lenient().when(reservation3.getStatus()).thenReturn(ReservationStatus.COMPLETED);
	}

	void 최소_최대_인원_만족_확인() {
		lenient().when(schedule.getMinRes()).thenReturn(3);
		lenient().when(schedule.getCapacity()).thenReturn(4);
	}

	void 기본_설정() {
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(ownerEmail);
		given(memberRepository.findByEmail(Email.of(ownerEmail))).willReturn(Optional.of(reservationOwner));
		doNothing().when(reservationOwner).validateReservationEligibility();

		given(memberRepository.findByEmail(Email.of("member1@hufs.ac.kr"))).willReturn(Optional.of(member1));
		given(memberRepository.findByEmail(Email.of("member2@hufs.ac.kr"))).willReturn(Optional.of(member2));
		doNothing().when(member1).validateReservationEligibility();
		doNothing().when(member2).validateReservationEligibility();
	}
}
