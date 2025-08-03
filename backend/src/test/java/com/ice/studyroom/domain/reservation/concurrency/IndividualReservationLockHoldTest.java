package com.ice.studyroom.domain.reservation.concurrency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import static org.mockito.Mockito.when;

import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.domain.vo.EncodedPassword;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.reservation.application.ReservationService;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.security.jwt.JwtTokenProvider;
import com.ice.studyroom.global.security.service.TokenService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.ice.studyroom.global.type.StatusCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IndividualReservationLockHoldTest { // 테스트 클래스명은 원래대로 유지

	@MockitoBean
	private TokenService tokenService;

	@MockitoBean // 오류를 해결하기 위한 핵심 코드
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private ReservationService reservationService;

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	@Test
	@DisplayName("10명이 동시에 용량 1인 스케줄 예약 시, 1명만 성공하고 락 점유 시간 단축 효과를 검증한다")
	void 개인_예약_오버부킹_시나리오() throws InterruptedException {
		// Given: 테스트 데이터 준비
		Schedule schedule = createTestSchedule();
		Schedule savedSchedule = scheduleRepository.save(schedule);
		scheduleRepository.flush();

		List<Member> testMembers = createTestMembers();
		memberRepository.saveAll(testMembers);
		memberRepository.flush();
		setupTokenServiceMocking(testMembers);

		// 동시성 테스트 설정
		int threadCount = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(threadCount);

		// 결과 집계를 위한 원자적 카운터
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger conflictCount = new AtomicInteger(0);
		AtomicInteger capacityExceededCount = new AtomicInteger(0);
		AtomicInteger otherExceptionCount = new AtomicInteger(0);
		List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

		// When: 10명이 동시에 용량 1인 스케줄 예약 시도
		for (int i = 0; i < threadCount; i++) {
			final int userIndex = i;
			executorService.submit(() -> {
				try {
					startLatch.await(); // 모든 스레드가 동시에 시작

					long requestStart = System.nanoTime();

					CreateReservationRequest request = createTestRequest(savedSchedule.getId());
					String authHeader = "Bearer test-token-" + userIndex;

					reservationService.createIndividualReservation(authHeader, request);
					successCount.incrementAndGet(); // 성공 카운트 증가

					long requestEnd = System.nanoTime();
					responseTimes.add((requestEnd - requestStart) / 1_000_000);

				} catch (BusinessException e) {
					// 실패 원인 정밀 분석
					if (e.getStatusCode() == StatusCode.CONFLICT) {
						conflictCount.incrementAndGet();
					} else if (e.getStatusCode() == StatusCode.BAD_REQUEST && e.getMessage().contains("자리가 없습니다")) {
						capacityExceededCount.incrementAndGet();
					} else {
						otherExceptionCount.incrementAndGet();
						System.out.println("[기타 BusinessException 발생]: " + e.getMessage());
					}
				} catch (Exception e) {
					otherExceptionCount.incrementAndGet();
					System.out.println("[알 수 없는 예외 발생]: " + e.getClass().getSimpleName() + " - " + e.getMessage());
				} finally {
					endLatch.countDown();
				}
			});
		}

		startLatch.countDown(); // 모든 스레드 시작 신호
		boolean finished = endLatch.await(15, TimeUnit.SECONDS); // 완료 대기
		executorService.shutdown();

		// Then: 결과 검증
		assertThat(finished).isTrue(); // 테스트가 시간 내에 종료되었는지 확인

		// 최종 데이터 상태 조회
		Schedule finalSchedule = scheduleRepository.findById(savedSchedule.getId()).get();
		List<Reservation> finalReservations = reservationRepository.findByFirstScheduleId(savedSchedule.getId());

		// 프로그래밍적 검증 (Assertion)
		assertAll("동시성 예약 결과 검증",
			() -> assertThat(successCount.get()).as("성공한 예약 수").isEqualTo(1),
			() -> assertThat(finalReservations.size()).as("DB에 저장된 예약 레코드 수").isEqualTo(1),
			() -> assertThat(finalSchedule.getCurrentRes()).as("스케줄의 최종 예약 인원").isEqualTo(1),
			() -> assertThat(finalSchedule.getStatus()).as("스케줄의 최종 상태").isEqualTo(ScheduleSlotStatus.RESERVED),
			() -> assertThat(conflictCount.get() + capacityExceededCount.get() + otherExceptionCount.get())
				.as("실패한 총 요청 수").isEqualTo(threadCount - 1)
		);

		// 상세 결과 출력을 통한 분석 (기존 로직 유지)
		printDetailedTestResults(successCount, conflictCount, capacityExceededCount, otherExceptionCount,
			schedule, finalReservations, responseTimes);
	}

	// 나머지 private 헬퍼 메서드들은 변경 없이 그대로 유지됩니다.
	private Schedule createTestSchedule() {
		return Schedule.builder()
			.roomType(RoomType.INDIVIDUAL)
			.scheduleDate(LocalDate.now().plusDays(1))
			.roomNumber("101")
			.roomTimeSlotId(1L)
			.startTime(LocalTime.of(9, 0))
			.endTime(LocalTime.of(10, 0))
			.currentRes(0)
			.capacity(1) // 용량 1로 설정하여 경쟁 극대화
			.minRes(1)
			.status(ScheduleSlotStatus.AVAILABLE)
			.dayOfWeek(DayOfWeekStatus.MONDAY)
			.build();
	}

	private List<Member> createTestMembers() {
		List<Member> members = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Member member = Member.create(Email.of("test" + i + "@hufs.ac.kr"), EncodedPassword.of("encoded-test-password"),
				"테스트사용자" + i, "2024000" + (i + 1));
			members.add(member);
		}
		return members;
	}

	private void setupTokenServiceMocking(List<Member> testMembers) {
		for (int i = 0; i < testMembers.size(); i++) {
			String authHeader = "Bearer test-token-" + i;
			String expectedEmail = testMembers.get(i).getEmail().getValue();
			when(tokenService.extractEmailFromAccessToken(authHeader)).thenReturn(expectedEmail);
		}
	}

	private CreateReservationRequest createTestRequest(Long scheduleId) {
		return new CreateReservationRequest(new Long[]{scheduleId}, new String[]{});
	}

	private void printDetailedTestResults(AtomicInteger successCount, AtomicInteger conflictCount, AtomicInteger capacityExceededCount, AtomicInteger otherExceptionCount,
										  Schedule originalSchedule, List<Reservation> reservationList, List<Long> responseTimes) {

		Schedule updatedSchedule = scheduleRepository.findById(originalSchedule.getId()).get();

		System.out.println("\n=== 상세 오버부킹 테스트 결과 ===");
		System.out.println("📊 실패 원인 분석 (락 점유 시간 단축 효과 검증):");
		System.out.println("  - ✅ 성공: " + successCount.get());
		System.out.println("  - 💥 락 충돌 실패 (CONFLICT): " + conflictCount.get() + " <--- 이 수치가 낮을수록 락 점유 시간 단축 효과가 큼");
		System.out.println("  - 🈵 정원 초과 실패 (BAD_REQUEST): " + capacityExceededCount.get() + " <--- 이 수치가 높을수록 락이 빨리 해제되었음을 의미");
		System.out.println("  - ❓ 기타 실패: " + otherExceptionCount.get());
		System.out.println("  - 🔄 총 시도 수: " + (successCount.get() + conflictCount.get() + capacityExceededCount.get() + otherExceptionCount.get()));

		System.out.println("\n🏢 스케줄 최종 상태:");
		System.out.println("  - 스케줄 용량: " + updatedSchedule.getCapacity());
		System.out.println("  - 최종 예약 인원 (currentRes): " + updatedSchedule.getCurrentRes());
		System.out.println("  - 스케줄 상태: " + updatedSchedule.getStatus());

		System.out.println("\n📋 예약 레코드 분석:");
		System.out.println("  - 생성된 예약 record 개수: " + reservationList.size());
		if (!reservationList.isEmpty()) {
			System.out.println("    - 예약 ID: " + reservationList.get(0).getId() + ", 상태: " + reservationList.get(0).getStatus());
		}

		boolean isOverbooking = reservationList.size() > updatedSchedule.getCapacity();
		boolean isCurrentResInconsistent = updatedSchedule.getCurrentRes() != reservationList.size();

		System.out.println("\n🔍 데이터 정합성 검증:");
		System.out.println("  - 오버부킹 발생 여부: " + isOverbooking);
		System.out.println("  - currentRes 불일치 여부: " + isCurrentResInconsistent);

		if (!responseTimes.isEmpty()) {
			double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
			long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
			System.out.println("\n⏱️ 성능 지표:");
			System.out.println("  - 평균 응답 시간: " + String.format("%.2f ms", avgResponseTime));
			System.out.println("  - 최대 응답 시간: " + maxResponseTime + " ms");
		}

		if (!isOverbooking && !isCurrentResInconsistent && successCount.get() == 1) {
			System.out.println("\n[ 최종 결론: 동시성 문제 해결 및 데이터 정합성 확보 확인 ✅ ]");
		} else {
			System.out.println("\n[ 최종 결론: 동시성 문제 또는 데이터 불일치 발생 ❌ ]");
		}
	}
}
