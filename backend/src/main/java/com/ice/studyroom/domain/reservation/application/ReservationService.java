package com.ice.studyroom.domain.reservation.application;

import java.sql.SQLOutput;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.domain.identity.domain.service.QRCodeService;
import com.ice.studyroom.domain.identity.domain.service.TokenService;
import com.ice.studyroom.domain.identity.infrastructure.security.QRCodeUtil;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.service.MemberDomainService;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.penalty.application.PenaltyService;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.request.QrEntranceRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.response.CancelReservationResponse;
import com.ice.studyroom.domain.reservation.presentation.dto.response.GetMostRecentReservationResponse;
import com.ice.studyroom.domain.reservation.presentation.dto.response.QRDataResponse;
import com.ice.studyroom.global.exception.AttendanceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

	private final QRCodeUtil qrCodeUtil;
	private final TokenService tokenService;
	private final MemberRepository memberRepository;
	private final ReservationRepository reservationRepository;
	private final ScheduleRepository scheduleRepository;
	private final QRCodeService qrCodeService;
	private final PenaltyService penaltyService;

	public List<Reservation> getMyAllReservation(String authorizationHeader) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		return reservationRepository.findByUserEmail(email);
	}

	public Optional<GetMostRecentReservationResponse> getMyMostRecentReservation(String authorizationHeader) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		return reservationRepository.findFirstByUserEmailOrderByCreatedAtDesc(email)
			.map(GetMostRecentReservationResponse::from);
	}

	public String getMyReservationQrCode(String resId, String authorizationHeader) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		String qrKey = qrCodeService.concatEmailResId(email, resId);
		return qrCodeService.getQRCode(qrKey);
	}

	public List<Schedule> getSchedule() {
		LocalDate today = LocalDate.now();
		return scheduleRepository.findByScheduleDate(today);
	}

	@Transactional
	public ReservationStatus qrEntrance(QrEntranceRequest request) {
		// 이미지를 저장.
		String qrCode = request.qrCode();
		// 이 qr 코드가 유효한지와 정보를 얻는다.
		// 이미지 BASE64디코딩 -> 디코딩 된 이미지 -> 텍스트 추출
		String qrKey = qrCodeUtil.decryptQRCode(qrCode);
		// 얻은 memer_id와 reservation_id를 토대로 예약 레코드를 찾는다.
		QRDataResponse qrData = qrCodeService.getQRData(qrKey);
		Long reservationId = qrData.getReservationId();
		String memberEmail = qrData.getEmail();

		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

		LocalDateTime now = LocalDateTime.now();
		ReservationStatus status = reservation.checkAttendanceStatus(now);

		if (status == ReservationStatus.RESERVED) {
			throw new AttendanceException("출석 시간이 아닙니다.", HttpStatus.FORBIDDEN);
		} else if (status == ReservationStatus.NO_SHOW) {
			throw new AttendanceException("출석 시간이 만료되었습니다.", HttpStatus.GONE);
		} else if(status == ReservationStatus.LATE){
			//해당 멤버에게 패널티 부여
			penaltyService.assignPenalty(memberRepository.getMemberByEmail(Email.of(memberEmail)), PenaltyReasonType.LATE);
		}
		return status;
	}

	@Transactional
	public String createIndividualReservation(String authorizationHeader, CreateReservationRequest request) {
		// 예약 가능 여부 확인
		List<Schedule> schedules = findSchedules(request.scheduleId());
		validateSchedulesAvailable(schedules);

		// JWT에서 예약자 이메일 추출
		String reserverEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);

		// 예약자 확인
		Member reserver = memberRepository.findByEmail(Email.of(reserverEmail))
			.orElseThrow(() -> new IllegalArgumentException("예약자 이메일이 존재하지 않습니다: " + reserverEmail));

		// 패널티 상태 확인 (예약 불가)
		if (reserver.isPenalty()) {
			throw new IllegalStateException("패널티 누적으로 사용불가 상태입니다.");
		}

		// 예약 객체 생성 및 저장
		String userName = reserver.getName();
		String userEmail = reserver.getEmail().getValue();
		Reservation reservation = Reservation.from(schedules, userEmail, userName);
		reservationRepository.save(reservation);

		// QR 코드 생성 및 저장
		String qrCodeBase64 = qrCodeUtil.generateQRCode(userEmail, reservation.getId().toString());
		qrCodeService.saveQRCode(userEmail, reservation.getId(), request.scheduleId().toString(), qrCodeBase64);

		// 스케줄 업데이트 (currentRes 증가 및 상태 변경)
		for (Schedule schedule : schedules) {
			if (!schedule.isCurrentResLessThanCapacity()) {
				throw new IllegalStateException("예약 가능한 자리가 없습니다.");
			}

			schedule.setCurrentRes(schedule.getCurrentRes() + 1); // 개인예약은 현재사용인원에서 +1 진행
			if (schedule.getCurrentRes() == schedule.getCapacity()) { //예약 후 현재인원 == 방수용인원 경우 RESERVE
				schedule.reserve();
			}
		}

		scheduleRepository.saveAll(schedules);

		return "Success";
	}

	@Transactional
	public String createGroupReservation(String authorizationHeader, CreateReservationRequest request) {
		// 예약 가능 여부 확인
		List<Schedule> schedules = findSchedules(request.scheduleId());
		validateSchedulesAvailable(schedules);

		// 단체 스케줄 -> 예약 시 RES
		// 개인 스케줄 -> 예약 인원 만큼 채워지면 RES
		// 스케줄에서 Type을 저장해야하며, Type에 따른 RES 처리가 필요하다.
		RoomType roomType = schedules.get(0).getRoomType();
		// if (roomType == schedules.getCapacity)

		// JWT에서 예약자 이메일 추출
		String reserverEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);

		// 예약자(User) 확인 및 user_name 가져오기
		Member reserver = memberRepository.findByEmail(Email.of(reserverEmail))
			.orElseThrow(() -> new IllegalArgumentException("예약자 이메일이 존재하지 않습니다: " + reserverEmail));

		// 중복된 이메일 검사 (예약자 포함)
		Set<String> uniqueEmails = new HashSet<>();
		uniqueEmails.add(reserverEmail); // 예약자 이메일 포함

		// 예약자와 참여자의 이메일을 저장 (이름 포함)
		Map<String, String> emailToNameMap = new HashMap<>();
		emailToNameMap.put(reserverEmail, reserver.getName());

		// 참여자 리스트 추가 (중복 검사 및 user_name 조회)
		if (!ObjectUtils.isEmpty(request.participantEmail())) {
			for (String email : request.participantEmail()) {
				if (!uniqueEmails.add(email)) {
					throw new IllegalArgumentException("중복된 참여자 이메일이 존재합니다: " + email);
				}
				Member participant = memberRepository.findByEmail(Email.of(email))
					.orElseThrow(() -> new IllegalArgumentException("참여자 이메일이 존재하지 않습니다: " + email));
				emailToNameMap.put(email, participant.getName());
			}
		}

		// 최소 예약 인원(minRes) 검사 (예약자 + 참여자 수 체크)
		int totalParticipants = uniqueEmails.size(); // 예약자 + 참여자 수
		int minRes = schedules.get(0).getMinRes(); // 모든 schedule의 minRes는 동일하다고 가정
		int capacity = schedules.get(0).getCapacity(); // 모든 연속된 schedule의 capacity는 동일하다고 가정
		if (totalParticipants < minRes) {
			throw new IllegalArgumentException(
				"최소 예약 인원 조건을 만족하지 않습니다. (필요 인원: " + minRes + ", 현재 인원: " + totalParticipants + ")");
		}else if (totalParticipants > capacity) {
			throw new IllegalArgumentException(
				"방의 최대 수용 인원을 초과했습니다. (최대 수용 인원: " + capacity + ", 현재 인원: " + totalParticipants +")");
		}

		// 예약 리스트 생성
		List<Reservation> reservations = new ArrayList<>();
		Map<String, String> qrCodeMap = new HashMap<>();

		// 예약 생성 및 저장
		for (String email : uniqueEmails) {
			String userName = emailToNameMap.get(email);
			Reservation reservation = Reservation.from(schedules, email, userName);
			reservations.add(reservation);
			reservationRepository.save(reservation);

			// QR 코드 생성 (예약 ID + 이메일 조합)
			String qrCodeBase64 = qrCodeUtil.generateQRCode(email, reservation.getId().toString());
			qrCodeService.saveQRCode(email, reservation.getId(), request.scheduleId().toString(), qrCodeBase64);
			qrCodeMap.put(email, qrCodeBase64);
		}

		// 예약 저장
		reservationRepository.saveAll(reservations);

		for (Schedule schedule : schedules) {
			schedule.setCurrentRes(totalParticipants); // 현재 사용 인원을 예약자 + 참여자 숫자로 지정
			schedule.reserve();
		}

		scheduleRepository.saveAll(schedules);

		return "Success";
	}


	@Transactional
	public CancelReservationResponse cancelReservation(Long id, String authorizationHeader) {
		Reservation reservation = findReservationById(id);
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		// JWT를 통한 사용자 정보를 토대로, 본인의 예약인지 확인
		if (!reservation.matchEmail(email)) {
			throw new IllegalStateException("이전에 예약이 되지 않았습니다.");
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startTime = LocalDateTime.of(LocalDate.now(), reservation.getStartTime());

		// 입실 1 시간 전 일 경우 패널티
		if (!now.isAfter(startTime)) {
			throw new IllegalStateException("입실 시간이 초과하였기에 취소할 수 없습니다.");
		}

		if (!now.isBefore(startTime.minus(1, ChronoUnit.HOURS))) {
			// 취소 패널티 부여
			penaltyService.assignPenalty(memberRepository.getMemberByEmail(Email.of(email)), PenaltyReasonType.CANCEL);
		}

		/*
		 * 취소 프로세스 시작
		 * 몇 개의 스케줄을 취소해야하는가?
		 * 시간 비교를 하면 몇 개의 스케줄을 신청했는지 알 수 있다.
		 */
		long hourDifference = Duration.between(reservation.getStartTime(), reservation.getEndTime()).toHours();

		scheduleRepository.findById(reservation.getFirstScheduleId()).ifPresent(Schedule::cancel);
		if (hourDifference == 2) {
			scheduleRepository.findById(reservation.getSecondScheduleId()).ifPresent(Schedule::cancel);
		}

		reservationRepository.delete(reservation);
		return new CancelReservationResponse(id);
	}

	@Transactional
	public String extendReservation(Long scheduleId, String authorizationHeader) {
		Reservation reservation = findReservationById(scheduleId);
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		if (!reservation.matchEmail(email)) {
			throw new IllegalStateException("이전에 예약이 되지 않았습니다.");
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime endTime = LocalDateTime.of(reservation.getScheduleDate(), reservation.getEndTime());


		if (now.isAfter(endTime)) {
			throw new IllegalStateException("연장 가능한 시간이 지났습니다.");
		} else if (now.isBefore(endTime.minusMinutes(10))) {
			throw new IllegalStateException("연장은 퇴실 시간 10분 전부터 가능합니다.");
		}

		//방 번호, 오늘 날짜, 시작하는 시간으로 다음 스케줄을 찾는다.
		Schedule nextSchedule = scheduleRepository.findByRoomNumberAndScheduleDateAndStartTime(
			reservation.getRoomNumber(), reservation.getScheduleDate(), reservation.getEndTime());

		if (nextSchedule == null) {
			throw new IllegalArgumentException("스터디룸 이용 가능 시간을 확인해주세요.");
		}

		if (!nextSchedule.isCurrentResLessThanCapacity() || !nextSchedule.isAvailable()) {
			throw new IllegalStateException("이미 예약이 완료된 스터디룸입니다.");
		}

		List<String> reservationEmails = reservationRepository.findEmailsByRoomNumberAndScheduleDateAndStartTime(
			reservation.getRoomNumber(), reservation.getScheduleDate(), reservation.getStartTime());

		for (String reservationEmail : reservationEmails) {
			Member member = memberRepository.getMemberByEmail(Email.of(reservationEmail));
			if(member.isPenalty()){
				throw new IllegalStateException("패널티가 있는 멤버로 인해 연장이 불가능합니다.");
			}
		}

		if (nextSchedule.getRoomType() == RoomType.GROUP) {
			List<Reservation> reservations = reservationRepository.findByRoomNumberAndScheduleDateAndStartTime(
				reservation.getRoomNumber(), reservation.getScheduleDate(), reservation.getStartTime());

			for (Reservation res : reservations) {
				res.extendReservation(nextSchedule.getId(), nextSchedule.getEndTime());
			}

			nextSchedule.reserve();
		}else{
			reservation.extendReservation(nextSchedule.getId(), nextSchedule.getEndTime());
			nextSchedule.setCurrentRes(nextSchedule.getCurrentRes() + 1);
		}
		return "Success";
	}

	// TODO: 추후 Jpa에 종합할 예정
	private Reservation findReservationById(Long reservationId) {
		return reservationRepository.findById(reservationId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));
	}

	private List<Schedule> findSchedules(Long[] scheduleIds) {
		return Arrays.stream(scheduleIds)
			.map(id -> scheduleRepository.findById(id)
				.filter(schedule -> schedule.getStatus() == ScheduleStatus.AVAILABLE) // AVAILABLE 상태 체크
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 사용 불가능한 스케줄입니다.")))
			.collect(Collectors.toList());
	}

	private void validateConsecutiveSchedules(CreateReservationRequest request) {
		Schedule firstSchedule = scheduleRepository.findById(request.scheduleId()[0])
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄입니다."));
		Schedule secondSchedule = scheduleRepository.findById(request.scheduleId()[1])
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄입니다."));

		// 같은 방인지 확인
		if (!firstSchedule.getRoomNumber().equals(secondSchedule.getRoomNumber())) {
			throw new IllegalArgumentException("연속된 예약은 같은 방에서만 가능합니다.");
		}

		// 시간이 연속되는지 확인
		if (!firstSchedule.getEndTime().equals(secondSchedule.getStartTime())) {
			throw new IllegalArgumentException("연속되지 않은 시간은 예약할 수 없습니다.");
		}
	}

	private void validateSchedulesAvailable(List<Schedule> schedules) {
		if (schedules.stream().anyMatch(schedule -> !schedule.isAvailable() || !schedule.isCurrentResLessThanCapacity())) {
			throw new IllegalStateException(("예약이 불가능합니다."));
		}
	}
}
