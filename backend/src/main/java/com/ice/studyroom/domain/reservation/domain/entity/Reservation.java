package com.ice.studyroom.domain.reservation.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Supplier;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.*;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.global.entity.BaseTimeEntity;

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

	public static Reservation from(List<Schedule> schedules, boolean isReservationHolder, Member member) {
		if (schedules == null || schedules.isEmpty()) {
			throw new InvalidReservationCreationException("Schedules List가 비어있습니다.");
		}

		Schedule firstSchedule = schedules.get(0);
		Schedule secondSchedule = schedules.size() > 1 ? schedules.get(1) : null;

		if (firstSchedule.getRoomNumber() == null) {
			throw new InvalidReservationCreationException("firstSchedule은 null일  수 없습니다.");
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

	public boolean isEntered() {return status == ReservationStatus.ENTRANCE;}

	private void updateEnterTime(LocalDateTime now) {
		this.enterTime = now;
	}

	/**
	 * 전달받은 토큰을 qrToken 필드에 할당합니다.
	 * 이 메서드는 qrToken이 null일 때만 호출되는 것을 전제로 합니다.
	 * @param newQrToken 새로 생성된 QR 토큰
	 */
	private void assignQrToken(String newQrToken) {
		this.qrToken = newQrToken;
	}

	/**
	 * 이 예약의 소유자가 맞는지 검증합니다.
	 * 소유자가 아닐 경우 ReservationAccessDeniedException을 발생시킵니다.
	 * @param rawEmail 검증할 사용자의 이메일
	 */
	public void validateOwnership(String rawEmail) {
		if (this.member == null || !this.member.getEmail().equals(Email.of(rawEmail))) {
			throw new ReservationAccessDeniedException("유효하지 않은 사용자는 해당 예약에 접근할 수 없습니다.");
		}
	}

	// TODO: private로 외부공개하지않게 수정할 예정
	public void markStatus(ReservationStatus status) {
		this.status = status;
	}

	public void extendReservation(Long secondScheduleId, LocalTime endTime) {
		this.secondScheduleId = secondScheduleId;
		this.endTime = endTime;
	}

	/**
	 * QR 토큰을 발급하거나 기존 토큰을 반환합니다.
	 * 토큰이 없는 경우에만 새로운 토큰을 생성하여 할당합니다.
	 * @param tokenGenerator 토큰 생성 로직을 제공하는 Supplier
	 * @return 발급되거나 기존에 존재하던 QR 토큰
	 */
	public String issueQrToken(Supplier<String> tokenGenerator) {
		if (this.qrToken == null) {
			assignQrToken(tokenGenerator.get());
		}

		return this.qrToken;
	}

	/**
	 * QR 코드 발급이 가능한 상태인지 검증합니다.
	 * 발급이 불가능한 상태일 경우, 상태에 맞는 구체적인 InvalidReservationStatusException 발생시킵니다.
	 */
	public void validateForQrIssuance() {
		switch (this.status) {
			case RESERVED:
				// RESERVED 상태만 QR 코드를 발급받을 수 있다.
				return;
			case ENTRANCE, LATE:
				throw new QrIssuanceNotAllowedException("이미 입실 처리된 예약입니다.");
			case CANCELLED:
				throw new QrIssuanceNotAllowedException("이미 취소된 예약입니다.");
			case COMPLETED:
				throw new QrIssuanceNotAllowedException("이미 정상 퇴실된 예약입니다.");
			case NO_SHOW:
				throw new QrIssuanceNotAllowedException("노쇼 처리된 예약입니다.");
			default:
				throw new QrIssuanceNotAllowedException("QR 코드를 발급할 수 없는 예약 상태입니다.");
		}
	}

	/**
	 * 입장을 시도하기에 유효한 상태인지 검증합니다.
	 * 유효하지 않은 경우, InvalidEntranceAttemptException 예외를 발생키십니다.
	 */
	public void validateForEntrance() {
		switch (this.status) {
			case RESERVED:
				return;
			case ENTRANCE, LATE:
				throw new InvalidEntranceAttemptException("이미 입실 처리 된 예약입니다.");
			case CANCELLED:
				throw new InvalidEntranceAttemptException("취소된 예약입니다.");
			case COMPLETED:
				throw new InvalidEntranceAttemptException("이미 사용 완료된 예약입니다.");
			case NO_SHOW:
				throw new InvalidEntranceAttemptException("이미 노쇼 처리 된 예약입니다.");
			default:
				throw new InvalidEntranceAttemptException("입장 처리를 할 수 없는 상태의 예약입니다.");
		}
	}

	/**
	 * 실제 입장 처리를 수행하고, 처리 결과 상태를 반환합니다.
	 * @param entranceTime 실제 입장 시간
	 * @return 처리 결과 상태 (ENTRANCE, LATE, NO_SHOW 등)
	 */
	public ReservationStatus processEntrance(LocalDateTime entranceTime) {

		ReservationStatus newStatus = this.checkAttendanceStatus(entranceTime);

		if (newStatus == ReservationStatus.NO_SHOW) {
			throw new InvalidEntranceTimeException("출석 시간이 만료되었습니다.");
		} else if (newStatus == ReservationStatus.RESERVED) {
			throw new InvalidEntranceTimeException("출석 시간이 아닙니다.");
		}

		this.updateEnterTime(entranceTime);
		this.markStatus(newStatus);

		return newStatus;
	}

	/**
	 * 입실 시도 시간을 기준으로 처리 결과를 판별합니다.
	 * RESERVED 입실 가능 시간 이전, ENTRANCE 입실 가능, LATE 지각, NO_SHOW 노쇼
	 * @param entranceTime 실제 입장 시간
	 * @return ReservationStatus
	 */
	public ReservationStatus checkAttendanceStatus(LocalDateTime entranceTime) {
		LocalDateTime startDateTime = LocalDateTime.of(scheduleDate, startTime);
		LocalDateTime endDateTime = LocalDateTime.of(scheduleDate, endTime);

		if (entranceTime.isBefore(startDateTime)) {
			return ReservationStatus.RESERVED;
		}

		if (entranceTime.isAfter(startDateTime.plusMinutes(30))) {
			if (entranceTime.isBefore(endDateTime)) {
				return ReservationStatus.LATE;
			} else {
				return ReservationStatus.NO_SHOW;
			}
		}

		return ReservationStatus.ENTRANCE;
	}
}
