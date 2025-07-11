package com.ice.studyroom.domain.reservation.concurrency;

import static org.junit.jupiter.api.MethodOrderer.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.ice.studyroom.domain.reservation.application.ReservationConcurrencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.domain.vo.EncodedPassword;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.reservation.application.ReservationService;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;
import com.ice.studyroom.global.security.jwt.JwtTokenProvider;
import com.ice.studyroom.global.security.service.TokenService;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IndividualReservationOverBookingTest {

	@Autowired
	private ReservationService reservationService;

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private ReservationConcurrencyService reservationConcurrencyService;

	@MockitoBean
	private TokenService tokenService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@Test
	void 개인_예약_오버부킹_시나리오() throws InterruptedException {
		Schedule schedule = createTestSchedule();
		Schedule saved = scheduleRepository.save(schedule);
		scheduleRepository.flush();
		System.out.println(">>> 저장된 schedule ID = " + saved.getId()); // 반드시 출력해보세요

		List<Member> testMembers = createTestMembers();
		memberRepository.saveAll(testMembers);
		memberRepository.flush();
		setupTokenServiceMocking(testMembers);

		int threadCount = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		//스레드들이 동시에 시작하도록 동기화
		CountDownLatch startLatch = new CountDownLatch(1);
		//모든 스레드의 작업이 완료될 때까지 대기
		CountDownLatch endLatch = new CountDownLatch(threadCount);

		List<String> successResults = Collections.synchronizedList(new ArrayList<>());
		List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
		List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

		// When: 10명이 동시에 용량 1인 스케줄 예약 시도
		for (int i = 0; i < threadCount; i++) {
			final int userIndex = i;
			executorService.submit(() -> {
				try {
					startLatch.await(); // 모든 스레드가 동시에 시작

					long requestStart = System.nanoTime();

					CreateReservationRequest request = createTestRequest(schedule.getId());
					String authHeader = "Bearer test-token-" + userIndex;

					String result = reservationService.createIndividualReservation(authHeader, request);
					successResults.add(result);

					long requestEnd = System.nanoTime();
					responseTimes.add((requestEnd - requestStart) / 1_000_000);
				} catch (Exception e) {
					System.out.println("[예외 발생]: " + e.getClass().getSimpleName() + " - " + e.getMessage());
					e.printStackTrace();
					exceptions.add(e);
				} finally {
					endLatch.countDown();
				}
			});
		}

		startLatch.countDown(); // 모든 스레드 시작 신호
		endLatch.await(15, TimeUnit.SECONDS); // 완료 대기
		executorService.shutdown();

		// Then: 오버부킹 발생 확인
		// 해당 schedule 로 예약된 예약 목록 조회
		List<Reservation> reservationResultList = reservationRepository.findByFirstScheduleId(schedule.getId());
		printDetailedTestResults(successResults, exceptions, schedule, reservationResultList, responseTimes);
	}

	private Schedule createTestSchedule() {
		return Schedule.builder()
			.roomType(RoomType.INDIVIDUAL)
			.scheduleDate(LocalDate.now().plusDays(1))
			.roomNumber("101")
			.roomTimeSlotId(1L)
			.startTime(LocalTime.of(9, 0))
			.endTime(LocalTime.of(10, 0))
			.currentRes(0) // 초기값 0으로 설정
			.capacity(1)   // 용량 1로 설정하여 오버부킹 쉽게 재현
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

			// TokenService의 extractEmailFromAccessToken 메서드 모킹
			when(tokenService.extractEmailFromAccessToken(authHeader))
				.thenReturn(expectedEmail);
		}
	}

	private CreateReservationRequest createTestRequest(Long scheduleId) {
		//schedule, 빈 배열의 참여자
		return new CreateReservationRequest(new Long[]{scheduleId}, new String[]{});
	}

	private void printDetailedTestResults(List<String> successResults, List<Exception> exceptions,
		Schedule originalSchedule, List<Reservation> reservationList, List<Long> responseTimes) {

		Schedule updatedSchedule = scheduleRepository.findById(originalSchedule.getId()).get();

		System.out.println("=== 상세 오버부킹 테스트 결과 ===");
		System.out.println("📊 기본 통계:");
		System.out.println("  - 성공한 예약 수: " + successResults.size());
		System.out.println("  - 실패한 예약 수: " + exceptions.size());
		System.out.println("  - 총 시도 수: " + (successResults.size() + exceptions.size()));

		System.out.println("\n🏢 스케줄 정보:");
		System.out.println("  - 스케줄 용량: " + updatedSchedule.getCapacity());
		System.out.println("  - 초기 예약 인원: " + originalSchedule.getCurrentRes());
		System.out.println("  - 최종 예약 인원: " + updatedSchedule.getCurrentRes());
		System.out.println("  - 스케줄 상태: " + updatedSchedule.getStatus());

		System.out.println("\n📋 예약 레코드 분석:");
		System.out.println("  - 생성된 예약 record 개수: " + reservationList.size());

		System.out.println("  - 예약 ID 목록:");
		for (int i = 0; i < reservationList.size(); i++) {
			Reservation reservation = reservationList.get(i);
			System.out.println("    " + (i + 1) + ". 예약ID: " + reservation.getId() +
				", 상태: " + reservation.getStatus() +
				", 스케줄 ID: " + reservation.getFirstScheduleId() +
				", 방번호: " + reservation.getRoomNumber() +
				", isHolder: " + reservation.isHolder());
		}

		System.out.println("\n 동시성 문제 분석:");

		boolean isOverbooking = reservationList.size() > updatedSchedule.getCapacity();
		boolean isCurrentResInconsistent = updatedSchedule.getCurrentRes() != reservationList.size();

		System.out.println("  - 오버부킹 발생: " + isOverbooking +
			" (예약 " + reservationList.size() + "개 > 용량 " + updatedSchedule.getCapacity() + "개)");
		System.out.println("  - currentRes 불일치: " + isCurrentResInconsistent +
			" (DB예약수: " + reservationList.size() + " vs currentRes: " + updatedSchedule.getCurrentRes() + ")");

		if (!responseTimes.isEmpty()) {
			double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
			long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
			System.out.println("\n평균 응답 시간: " + String.format("%.2f ms", avgResponseTime));
			System.out.println("최대 응답 시간: " + maxResponseTime + " ms");
		}

		if (isOverbooking || isCurrentResInconsistent) {
			System.out.println("동시성 문제 확인");
		} else {
			System.out.println("동시성 문제 해결 완료");
		}
	}

}
