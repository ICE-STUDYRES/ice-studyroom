package com.ice.studyroom.domain.reservation.concurrency;

import static org.mockito.Mockito.*;

import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.global.exception.BusinessException;
import org.hibernate.LazyInitializationException;
import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.global.security.jwt.JwtTokenProvider;
import com.ice.studyroom.global.security.service.TokenService;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationExtendOverbookingTest {

	@Autowired private ReservationService reservationService;
	@Autowired private ScheduleRepository scheduleRepository;
	@Autowired private ReservationRepository reservationRepository;
	@Autowired private MemberRepository memberRepository;

	@MockitoBean private TokenService tokenService;
	@MockitoBean private JwtTokenProvider jwtTokenProvider;
	@MockitoBean private Clock clock; // Clock을 Mock으로 설정


	@Test
	void 예약_연장_오버부킹_테스트() throws Exception {
		LocalDateTime testTime = LocalDateTime.now().plusDays(1).withHour(9).withMinute(55);

		when(clock.instant()).thenReturn(testTime.atZone(ZoneId.systemDefault()).toInstant());
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		// Schedule 생성 시 ID를 설정하지 않음 (자동 생성)
		Schedule scheduleA = createSchedule("101", 9, 10);  // 기존 예약용
		Schedule scheduleB = createSchedule("101", 10, 1);  // 연장 대상 (capacity = 1)

		// 각각 저장하여 실제 ID 획득
		scheduleA = scheduleRepository.save(scheduleA);
		scheduleB = scheduleRepository.save(scheduleB);

		System.out.println("Schedule A ID: " + scheduleA.getId());
		System.out.println("Schedule B ID: " + scheduleB.getId());

		// 테스트용 회원 생성 및 예약 등록
		List<Member> members = createTestMembers(10);
		memberRepository.saveAll(members);
		setupTokenMocks(members);

		List<Reservation> reservations = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Reservation res = createReservation(List.of(scheduleA), false, members.get(i), ReservationStatus.ENTRANCE);
			reservationRepository.save(res);
			reservations.add(res);
		}

		// 동시성 테스트 실행
		ExecutorService executor = Executors.newFixedThreadPool(10);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(10);

		List<String> successes = Collections.synchronizedList(new ArrayList<>());
		List<Exception> failures = Collections.synchronizedList(new ArrayList<>());
		List<Exception> staleObjectExceptions = Collections.synchronizedList(new ArrayList<>());

		for (int i = 0; i < reservations.size(); i++) {
			final int idx = i;
			executor.submit(() -> {
				try {
					start.await();
					String token = "Bearer test-token-" + idx;
					String result = reservationService.extendReservation(reservations.get(idx).getId(), token);
					successes.add(result);
				} catch (StaleObjectStateException | ObjectOptimisticLockingFailureException e) {
					staleObjectExceptions.add(e);
					System.out.println("StaleObjectException 발생 (사용자 " + idx + "): " + e.getMessage());
				} catch (BusinessException e) {
					System.out.println("비즈니스 예외 발생 (사용자 " + idx + "): " + e.getMessage());
					failures.add(e);
				} catch (Exception e) {
					System.out.println("기타 예외 발생 (사용자 " + idx + "): " + e.getClass().getSimpleName() + " - " + e.getMessage());
					failures.add(e);
				} finally {
					done.countDown();
				}
			});
		}

		start.countDown();
		done.await(15, TimeUnit.SECONDS);
		executor.shutdown();

		// 결과 확인
		Schedule updatedScheduleB = scheduleRepository.findById(scheduleB.getId()).orElseThrow();
		List<Reservation> extended = reservationRepository.findAll()
			.stream().filter(r -> r.getSecondScheduleId() != null).toList();

		System.out.println("=== 오버부킹 테스트 결과 ===");
		System.out.println("연장 성공 수: " + successes.size());
		System.out.println("비즈니스 예외 수: " + failures.size());
		System.out.println("StaleObject 예외 수: " + staleObjectExceptions.size());
		System.out.println("Schedule B currentRes: " + updatedScheduleB.getCurrentRes());
		System.out.println("실제 연장된 예약 개수: " + extended.size());
		System.out.println("Schedule B capacity: " + scheduleB.getCapacity());

		// 오버부킹 발생 확인
		if (updatedScheduleB.getCurrentRes() > scheduleB.getCapacity()) {
			System.out.println("🚨 오버부킹 발생! 용량: " + scheduleB.getCapacity() +
				", 실제 예약: " + updatedScheduleB.getCurrentRes());
		}

		if (successes.size() > 1) {
			System.out.println("🚨 오버부킹 발생: " + successes.size() + "개 요청이 성공했습니다.");
		}

		// 성공한 예약들의 상세 정보 출력
		for (Reservation reservation : extended) {
			String memberEmail = "N/A";
			try {
				memberEmail = reservation.getMember().getEmail().getValue();
			} catch (LazyInitializationException e) {
				Member member = memberRepository.findById(reservation.getMember().getId()).orElse(null);
				if (member != null) {
					memberEmail = member.getEmail().getValue();
				}
			}

			System.out.println("연장된 예약 - ID: " + reservation.getId() +
				", 사용자: " + memberEmail +
				", 두 번째 스케줄 ID: " + reservation.getSecondScheduleId());
		}
	}

	private Schedule createSchedule(String room, int startHour, int capacity) {
		return Schedule.builder()
			.roomNumber(room)
			.roomTimeSlotId((long) startHour)
			.scheduleDate(LocalDate.now().plusDays(1)) // 내일 날짜
			.startTime(LocalTime.of(startHour, 0))
			.endTime(LocalTime.of(startHour + 1, 0))
			.status(ScheduleSlotStatus.AVAILABLE)
			.roomType(RoomType.INDIVIDUAL)
			.capacity(capacity)
			.currentRes(0)
			.minRes(1)
			.dayOfWeek(DayOfWeekStatus.MONDAY)
			.build();
	}

	private Reservation createReservation(List<Schedule> schedules, boolean isReservationHolder, Member member, ReservationStatus status) {
		Schedule schedule = schedules.get(0);
		return Reservation.builder()
			.firstScheduleId(schedule.getId())
			.secondScheduleId(null)
			.member(member)
			.scheduleDate(schedule.getScheduleDate())
			.roomNumber(schedule.getRoomNumber())
			.startTime(schedule.getStartTime())
			.endTime(schedule.getEndTime())
			.status(status)
			.isHolder(isReservationHolder)
			.build();
	}

	private List<Member> createTestMembers(int count) {
		List<Member> members = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			members.add(Member.create(
				Email.of("test" + i + "@hufs.ac.kr"),
				EncodedPassword.of("pw"),
				"사용자" + i,
				"202400" + String.format("%02d", i)
			));
		}
		return members;
	}

	private void setupTokenMocks(List<Member> members) {
		for (int i = 0; i < members.size(); i++) {
			String token = "Bearer test-token-" + i;
			String email = members.get(i).getEmail().getValue();
			when(tokenService.extractEmailFromAccessToken(token)).thenReturn(email);
		}
	}
}
