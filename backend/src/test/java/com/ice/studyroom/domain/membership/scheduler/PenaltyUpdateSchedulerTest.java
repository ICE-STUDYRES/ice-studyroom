// package com.ice.studyroom.domain.membership.scheduler;
//
// import static org.assertj.core.api.AssertionsForClassTypes.*;
//
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;
// import java.util.List;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.annotation.Rollback;
//
// import com.ice.studyroom.domain.admin.domain.type.RoomType;
// import com.ice.studyroom.domain.membership.domain.entity.Member;
// import com.ice.studyroom.domain.penalty.application.PenaltyService;
// import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
// import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
// import com.ice.studyroom.domain.membership.domain.vo.Email;
// import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
// import com.ice.studyroom.domain.penalty.domain.type.PenaltyStatus;
// import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
// import com.ice.studyroom.domain.penalty.scheduler.PenaltyUpdateScheduler;
// import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
// import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
// import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
// import com.ice.studyroom.domain.reservation.domain.type.ScheduleStatus;
// import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
// import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
//
// import jakarta.transaction.Transactional;
//
// @SpringBootTest
// @Transactional
// @Rollback
// class PenaltyUpdateSchedulerTest {
//
// 	@Autowired
// 	private MemberRepository memberRepository;
//
// 	@Autowired
// 	private PenaltyRepository penaltyRepository;
//
// 	@Autowired
// 	private PenaltyUpdateScheduler penaltyUpdateScheduler;
//
// 	@Autowired
// 	private ReservationRepository reservationRepository;
//
// 	@Autowired
// 	private ScheduleRepository scheduleRepository;
//
// 	@Autowired
// 	private PenaltyService penaltyService;
//
// 	@Test
// 	@DisplayName("패널티 스케줄러를 통해 Member의 패널티 여부가 갱신되어야한다.")
// 	void testUpdatePenaltyCounts() {
// 		// 1. 테스트 데이터를 생성
// 		Member member1 = Member.builder()
// 			.email(new Email("test1@hufs.ac.kr"))
// 			.name("User1")
// 			.password("password1")
// 			.studentNum("12345")
// 			.roles(List.of("ROLE_USER"))
// 			.createdAt(LocalDateTime.now())
// 			.updatedAt(LocalDateTime.now())
// 			.isPenalty(false)
// 			.build();
//
// 		Member member2 = Member.builder()
// 			.email(new Email("test2@hufs.ac.kr"))
// 			.name("User2")
// 			.password("password2")
// 			.studentNum("67890")
// 			.roles(List.of("ROLE_USER"))
// 			.createdAt(LocalDateTime.now())
// 			.updatedAt(LocalDateTime.now())
// 			.isPenalty(false)
// 			.build();
//
// 		memberRepository.saveAll(List.of(member1, member2));
//
// 		Penalty penalty1 = Penalty.builder()
// 			.member(member1)
// 			.reason(PenaltyReasonType.LATE)
// 			.penaltyEnd(LocalDateTime.now().plusDays(1))
// 			.build();
//
// 		Penalty penalty2 = Penalty.builder()
// 			.member(member2)
// 			.reason(PenaltyReasonType.LATE)
// 			.penaltyEnd(LocalDateTime.now().minusHours(1)) // 만료된 패널티
// 			.build();
//
// 		penaltyRepository.saveAll(List.of(penalty1, penalty2));
//
// 		// 2. 스케줄러 실행
// 		penaltyUpdateScheduler.updateMemberPenalty();
//
// 		// 3. 검증
// 		Member updatedMember1 = memberRepository.findById(member1.getId()).orElseThrow();
// 		Member updatedMember2 = memberRepository.findById(member2.getId()).orElseThrow();
//
// 		assertThat(updatedMember1.isPenalty()).isTrue();
// 		assertThat(updatedMember2.isPenalty()).isFalse();
// 	}
//
// 	@Test
// 	@DisplayName("expireOldPenalties 메서드는 패널티 기간이 끝난 패널티의 상태를 'EXPIRED' 로 변경해준다.")
// 	void testExpireOldPenalties(){
//
// 		Member member1 = Member.builder()
// 			.email(new Email("test1@hufs.ac.kr"))
// 			.name("User1")
// 			.password("password1")
// 			.studentNum("12345")
// 			.roles(List.of("ROLE_USER"))
// 			.createdAt(LocalDateTime.now())
// 			.updatedAt(LocalDateTime.now())
// 			.isPenalty(false)
// 			.build();
//
// 		Member member2 = Member.builder()
// 			.email(new Email("test2@hufs.ac.kr"))
// 			.name("User2")
// 			.password("password2")
// 			.studentNum("67890")
// 			.roles(List.of("ROLE_USER"))
// 			.createdAt(LocalDateTime.now())
// 			.updatedAt(LocalDateTime.now())
// 			.isPenalty(false)
// 			.build();
//
// 		memberRepository.saveAll(List.of(member1, member2));
//
// 		//만료되지 않은 패널티
// 		Penalty penalty1 = Penalty.builder()
// 			.member(member1)
// 			.reason(PenaltyReasonType.LATE)
// 			.penaltyEnd(LocalDateTime.now().plusDays(1))
// 			.build();
//
// 		// 만료된 패널티
// 		Penalty penalty2 = Penalty.builder()
// 			.member(member2)
// 			.reason(PenaltyReasonType.LATE)
// 			.penaltyEnd(LocalDateTime.now().minusDays(1))
// 			.build();
//
// 		penaltyRepository.saveAll(List.of(penalty1, penalty2));
//
// 		penaltyUpdateScheduler.expireOldPenalties();
//
// 		Penalty updatedPenalty1 = penaltyRepository.findById(penalty1.getId()).orElseThrow();
// 		Penalty updatedPenalty2 = penaltyRepository.findById(penalty2.getId()).orElseThrow();
//
// 		assertThat(updatedPenalty1.getStatus()).isEqualTo(PenaltyStatus.VALID);  // 만료되지 않음
// 		assertThat(updatedPenalty2.getStatus()).isEqualTo(PenaltyStatus.EXPIRED); // 만료됨
// 	}
//
//
// 	@Test
// 	@DisplayName("processNoShowPenalties 메서드는 종료된 예약을 확인하고 노쇼 패널티를 부여해야 한다.")
// 	void testProcessNoShowPenalties() {
// 		// 1. 테스트 시간 설정
// 		LocalTime now = LocalTime.of(11, 0).withSecond(0).withNano(0);
//
// 		// 2. 테스트 데이터 생성 (스케줄)
// 		Schedule schedule1 = Schedule.builder()
// 			.scheduleDate(LocalDate.now())
// 			.roomNumber("Room A")
// 			.roomType(RoomType.GROUP)
// 			.roomTimeSlotId(1L)
// 			.startTime(LocalTime.of(10, 0))
// 			.endTime(LocalTime.of(11, 0))
// 			.capacity(10)
// 			.currentRes(0)
// 			.minRes(1)
// 			.status(ScheduleStatus.AVAILABLE)
// 			.build();
//
// 		scheduleRepository.save(schedule1);
//
// 		// 3. 테스트 데이터 생성 (멤버)
// 		Member member1 = Member.builder()
// 			.email(new Email("test1@hufs.ac.kr"))
// 			.name("User1")
// 			.password("password1")
// 			.studentNum("12345")
// 			.roles(List.of("ROLE_USER"))
// 			.createdAt(LocalDateTime.now())
// 			.updatedAt(LocalDateTime.now())
// 			.isPenalty(false)
// 			.build();
//
// 		memberRepository.save(member1);
//
// 		// 4. 테스트 데이터 생성 (예약)
// 		Reservation reservation1 = Reservation.from(List.of(schedule1), member1.getEmail().getValue(), member1.getName());
// 		reservationRepository.save(reservation1);
//
//
// 		Reservation beforeUpdateReservation = reservationRepository.findById(reservation1.getId()).orElseThrow();
// 		assertThat(beforeUpdateReservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
//
// 		// 5. 스케줄러 실행
// 		//penaltyUpdateScheduler.processNoShowPenalties() 과 동일한 코드입니다.
// 		List<Reservation> expiredReservations = reservationRepository.findByEndTimeBetween(
// 			now.minusMinutes(2), now);
// 		assertThat(expiredReservations.size()).isEqualTo(1);
//
// 		// LocalDateTime 생성 시 LocalDate + LocalTime 사용
// 		// 스케줄러와 동일한 시간에 맞추기 위해 +1 분에 예약 상태를 확인
// 		expiredReservations.forEach(reservation -> {
// 			penaltyService.checkReservationNoShow(reservation, LocalDateTime.of(LocalDate.now(), now.plusMinutes(1)));
// 		});
//
// 		// 6. 검증 - 예약 상태 확인
// 		Reservation afterUpdateReservation = reservationRepository.findById(reservation1.getId()).orElseThrow();
// 		assertThat(afterUpdateReservation.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
//
// 		// 7. 검증 - 해당 예약을 한 member의 패널티 확인
// 		List<Penalty> penalties = penaltyRepository.findAll();
// 		assertThat(penalties.get(0).getMember().getId()).isEqualTo(member1.getId());
// 		assertThat(penalties.get(0).getReason()).isEqualTo(PenaltyReasonType.NO_SHOW);
// 	}
//
// }
