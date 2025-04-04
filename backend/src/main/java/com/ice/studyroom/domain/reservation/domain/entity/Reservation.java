package com.ice.studyroom.domain.reservation.domain.entity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.global.entity.BaseTimeEntity;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reservation extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(name = "first_schedule_id")
	private Long firstScheduleId;

	@Column(name = "second_schedule_id")
	private Long secondScheduleId;

	@Column(name = "schedule_date", nullable = false)
	private LocalDate scheduleDate;

	@Column(name = "room_number", nullable = false, length = 20)
	private String roomNumber;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	@Column(name = "enter_time")
	private LocalDateTime enterTime;

	@Column(name = "exit_time")
	private LocalDateTime exitTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ReservationStatus status = ReservationStatus.RESERVED;

	@Column(name = "is_holder", nullable = false)
	private boolean isHolder;

	@Column(name = "qr_token", length = 32, unique = true)
	private String qrToken;

	public boolean isEntered() {
		return status == ReservationStatus.ENTRANCE;
	}

	public boolean isOwnedBy(String rawEmail) {
		return this.member.getEmail().equals(Email.of(rawEmail));
	}

	// 정상 입실인지 지각인지 노쇼인지 판단하는 코드
	public ReservationStatus checkAttendanceStatus(LocalDateTime now) {
		LocalDateTime startDateTime = LocalDateTime.of(scheduleDate, startTime);
		LocalDateTime endDateTime = LocalDateTime.of(scheduleDate, endTime);

		if (now.isBefore(startDateTime)) {
			return ReservationStatus.RESERVED;
		}

		if (now.isAfter(startDateTime.plusMinutes(30))) {
			if (now.isBefore(endDateTime)) {
				return ReservationStatus.LATE;
			} else {
				return ReservationStatus.NO_SHOW;
			}
		}

		return ReservationStatus.ENTRANCE;
	}

	public void assignQrToken(String generatedToken) {
		this.qrToken = generatedToken;
	}

	public void updateEnterTime(LocalDateTime now) {
		this.enterTime = now;
	}

	public void markStatus(ReservationStatus status) {
		this.status = status;
		if(status != ReservationStatus.CANCELLED && status != ReservationStatus.NO_SHOW
			&& status != ReservationStatus.COMPLETED) {
			this.enterTime = LocalDateTime.now();
		}
	}

	public void extendReservation(Long secondScheduleId, LocalTime endTime) {
		this.secondScheduleId = secondScheduleId;
		this.endTime = endTime;
	}

	public static Reservation from(List<Schedule> schedules, boolean isReservationHolder, Member member) {
		if (schedules == null || schedules.isEmpty()) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "Schedules List가 비어있습니다.");
		}

		Schedule firstSchedule = schedules.get(0);
		Schedule secondSchedule = schedules.size() > 1 ? schedules.get(1) : null;

		if (firstSchedule.getRoomNumber() == null) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "firstSchedule은 null일  수 없습니다.");
		}

		return Reservation.builder()
			.firstScheduleId(firstSchedule.getId())
			.secondScheduleId(secondSchedule != null ? secondSchedule.getId() : null)
			.member(member)
			.scheduleDate(firstSchedule.getScheduleDate())
			.roomNumber(firstSchedule.getRoomNumber())
			.startTime(firstSchedule.getStartTime())
			.endTime(secondSchedule != null ? secondSchedule.getEndTime() : firstSchedule.getEndTime())
			.status(ReservationStatus.RESERVED)
			.isHolder(isReservationHolder)
			.build();
	}
}
