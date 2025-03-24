package com.ice.studyroom.domain.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.request.QrEntranceRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.response.CancelReservationResponse;
import com.ice.studyroom.domain.reservation.presentation.dto.response.GetMostRecentReservationResponse;
import com.ice.studyroom.domain.reservation.presentation.dto.response.GetReservationsResponse;
import com.ice.studyroom.domain.reservation.presentation.dto.response.ParticipantResponse;
import com.ice.studyroom.domain.reservation.presentation.dto.response.QRDataResponse;
import com.ice.studyroom.domain.reservation.presentation.dto.response.QrEntranceResponse;
import com.ice.studyroom.global.dto.request.EmailRequest;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.service.EmailService;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

	private final QRCodeUtil qrCodeUtil;
	private final TokenService tokenService;
	private final MemberRepository memberRepository;
	private final ReservationRepository reservationRepository;
	private final ScheduleRepository scheduleRepository;
	private final QRCodeService qrCodeService;
	private final PenaltyService penaltyService;
	private final MemberDomainService memberDomainService;
	private final EmailService emailService;

	// 테스트 코드 작성을 위해 필요한 clock 속성
	private final Clock clock;

	//todo : N+1 문제 해결
	public List<GetReservationsResponse> getReservations(String authorizationHeader) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		Member member = memberRepository.findByEmail(Email.of(email))
			.orElseThrow(() -> new BusinessException(StatusCode.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다."));

		List<Reservation> reservations = reservationRepository.findByMember(member);

		Map<String, List<ParticipantResponse>> reservationParticipantsMap = new HashMap<>();

		for (Reservation reservation : reservations) {
			String key = reservation.getRoomNumber() + "_" + reservation.getScheduleDate() + "_" + reservation.getStartTime();

			if (!reservationParticipantsMap.containsKey(key)) {

				Schedule schedule = scheduleRepository.findById(reservation.getFirstScheduleId())
					.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 스케줄입니다."));

				if (schedule.getRoomType() == RoomType.GROUP) {
					List<Reservation> sameReservations = reservationRepository.findByRoomNumberAndScheduleDateAndStartTime(
						reservation.getRoomNumber(), reservation.getScheduleDate(), reservation.getStartTime());

					List<ParticipantResponse> participants = sameReservations.stream()
						.map(sameRes -> ParticipantResponse.from(sameRes.getMember(), sameRes.isHolder()))
						.collect(Collectors.toList());

					reservationParticipantsMap.put(key, participants);
				}
				// 그룹예약인지 개인 예약인지 확인
				// 그룹 예약이면 findByFirstScheduleId
				// 참여자를 찾기 위해서 아래 로직이 수행됨
				// 개인 예약이면
				// 참여자를 찾을 필요가 없다.
			}
		}

		return reservations.stream()
			.map(reservation -> GetReservationsResponse.from(reservation, reservationParticipantsMap.get(
				reservation.getRoomNumber() + "_" + reservation.getScheduleDate() + "_" + reservation.getStartTime())))
			.collect(Collectors.toList());
	}

	public Optional<GetMostRecentReservationResponse> getMyMostRecentReservation(String authorizationHeader) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		return reservationRepository.findLatestReservationByUserEmail(email)
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
	public QrEntranceResponse qrEntrance(QrEntranceRequest request) {
		// 이미지를 저장.
		String qrCode = request.qrCode();
		// 이 qr 코드가 유효한지와 정보를 얻는다.
		// 이미지 BASE64디코딩 -> 디코딩 된 이미지 -> 텍스트 추출
		String qrKey = qrCodeUtil.decryptQRCode(qrCode);
		// 얻은 memer_id와 reservation_id를 토대로 예약 레코드를 찾는다.
		QRDataResponse qrData = qrCodeService.getQRData(qrKey);
		Long reservationId = qrData.getReservationId();
		String memberEmail = qrData.getEmail();
		Member member = memberDomainService.getMemberByEmail(memberEmail);

		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND,"존재하지 않는 예약입니다."));

		if(reservation.getStatus() == ReservationStatus.CANCELLED){
			throw new BusinessException(StatusCode.BAD_REQUEST, "취소된 예약입니다.");
		} else if(reservation.getStatus() == ReservationStatus.ENTRANCE || reservation.getStatus() == ReservationStatus.LATE){
			throw new BusinessException(StatusCode.BAD_REQUEST, "이미 입실처리 된 예약입니다.");
		}

		LocalDateTime now = LocalDateTime.now();
		ReservationStatus status = reservation.checkAttendanceStatus(now);
		reservation.markStatus(status);

		if (status == ReservationStatus.RESERVED) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "출석 시간이 아닙니다.");
		} else if (status == ReservationStatus.NO_SHOW) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "출석 시간이 만료되었습니다.");
		} else if(status == ReservationStatus.LATE){
			penaltyService.assignPenalty(memberRepository.getMemberByEmail(Email.of(memberEmail)), reservationId, PenaltyReasonType.LATE);
		}
		return new QrEntranceResponse(status, member.getName(), member.getStudentNum());
	}

	@Transactional
	public String createIndividualReservation(String authorizationHeader, CreateReservationRequest request) {
		// 예약 가능 여부 확인
		List<Schedule> schedules = findSchedules(request.scheduleId());
		validateSchedulesAvailable(schedules);
		RoomType roomType = schedules.get(0).getRoomType();
		if(roomType == RoomType.GROUP) throw new BusinessException(StatusCode.FORBIDDEN, "해당 방은 단체예약 전용입니다.");

		// JWT에서 예약자 이메일 추출
		String reserverEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);

		// 예약자 확인
		Member reserver = memberRepository.findByEmail(Email.of(reserverEmail))
			.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND,"예약자 이메일이 존재하지 않습니다: " + reserverEmail));

		// 패널티 상태 확인 (예약 불가)
		if (reserver.isPenalty()) {
			throw new BusinessException(StatusCode.FORBIDDEN, "사용정지 상태입니다.");
		}

		// 예약 중복 방지
		checkDuplicateReservation(reserverEmail);

		// 예약 객체 생성 및 저장
		String userName = reserver.getName();
		String userEmail = reserver.getEmail().getValue();
		Reservation reservation = Reservation.from(schedules, userEmail, userName, true, reserver);
		reservationRepository.save(reservation);

		// QR 코드 생성 및 저장
		String qrCodeBase64 = qrCodeUtil.generateQRCode(userEmail, reservation.getId().toString());
		qrCodeService.saveQRCode(userEmail, reservation.getId(), request.scheduleId().toString(), qrCodeBase64);

		// 스케줄 업데이트 (currentRes 증가 및 상태 변경)
		for (Schedule schedule : schedules) {
			if (!schedule.isCurrentResLessThanCapacity()) {
				throw new BusinessException(StatusCode.BAD_REQUEST, "예약 가능한 자리가 없습니다.");
			}

			schedule.setCurrentRes(schedule.getCurrentRes() + 1); // 개인예약은 현재사용인원에서 +1 진행
			if (schedule.getCurrentRes() == schedule.getCapacity()) { //예약 후 현재인원 == 방수용인원 경우 RESERVE
				schedule.updateStatus(ScheduleSlotStatus.RESERVED);
			}
		}

		scheduleRepository.saveAll(schedules);
		sendReservationSuccessEmail(roomType, reserverEmail, new HashSet<>(), schedules.get(0));

		return "Success";
	}

	@Transactional
	public String createGroupReservation(String authorizationHeader, CreateReservationRequest request) {
		// 예약 가능 여부 확인
		List<Schedule> schedules = findSchedules(request.scheduleId());
		validateSchedulesAvailable(schedules);

		// 스케줄에서 Type을 저장해야하며, Type에 따른 RES 처리가 필요하다.
		RoomType roomType = schedules.get(0).getRoomType();
		if(roomType == RoomType.INDIVIDUAL) throw new BusinessException(StatusCode.FORBIDDEN, "해당 방은 개인예약 전용입니다.");

		// JWT에서 예약자 이메일 추출
		String reserverEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);

		// 예약자(User) 확인 및 user_name 가져오기
		Member reserver = memberRepository.findByEmail(Email.of(reserverEmail))
			.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "예약자 이메일이 존재하지 않습니다: " + reserverEmail));

		if(reserver.isPenalty()) {
			throw new BusinessException(StatusCode.FORBIDDEN, "예약자가 패널티 상태입니다. 예약이 불가능합니다.");
		}

		// 예약 중복 방지
		checkDuplicateReservation(reserverEmail);

		// 중복된 이메일 검사 (예약자 포함)
		Set<String> uniqueEmails = new HashSet<>();
		uniqueEmails.add(reserverEmail); // 예약자 이메일 포함

		// 예약자와 참여자의 이메일을 저장 (이름 포함)
		Map<String, Member> emailToMemberMap = new HashMap<>();
		emailToMemberMap.put(reserverEmail, reserver);

		// 참여자 리스트 추가 (중복 검사 및 user_name 조회)
		if (!ObjectUtils.isEmpty(request.participantEmail())) {
			for (String email : request.participantEmail()) {
				if (!uniqueEmails.add(email)) {
					throw new BusinessException(StatusCode.BAD_REQUEST, "중복된 참여자 이메일이 존재합니다: " + email);
				}
				Member participant = memberRepository.findByEmail(Email.of(email))
					.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "참여자 이메일이 존재하지 않습니다: " + email));

				//참여자 패널티 상태 확인
				if (participant.isPenalty()) {
					throw new BusinessException(StatusCode.FORBIDDEN, "참여자 중 패널티 상태인 사용자가 있습니다. 예약이 불가능합니다. (이메일: " + email + ")");
				}

				//참여자 최근 예약 상태 확인
				Optional<Reservation> recentReservationOpt = reservationRepository.findLatestReservationByUserEmail(email);
				if(recentReservationOpt.isPresent()) {
					ReservationStatus recentStatus = recentReservationOpt.get().getStatus();
					if(recentStatus == ReservationStatus.RESERVED || recentStatus == ReservationStatus.ENTRANCE) {
						throw new BusinessException(StatusCode.CONFLICT, "참여자 중 현재 예약이 진행 중인 사용자가 있어 예약이 불가능합니다. (이메일: " + email + ")");
					}
				}

				emailToMemberMap.put(email, participant);
			}
		}

		// 최소 예약 인원(minRes) 검사 (예약자 + 참여자 수 체크)
		int totalParticipants = uniqueEmails.size(); // 예약자 + 참여자 수
		int minRes = schedules.get(0).getMinRes(); // 모든 schedule의 minRes는 동일하다고 가정
		int capacity = schedules.get(0).getCapacity(); // 모든 연속된 schedule의 capacity는 동일하다고 가정
		if (totalParticipants < minRes) {
			throw new BusinessException(StatusCode.BAD_REQUEST,
				"최소 예약 인원 조건을 만족하지 않습니다. (필요 인원: " + minRes + ", 현재 인원: " + totalParticipants + ")");
		}else if (totalParticipants > capacity) {
			throw new BusinessException(StatusCode.BAD_REQUEST,
				"방의 최대 수용 인원을 초과했습니다. (최대 수용 인원: " + capacity + ", 현재 인원: " + totalParticipants + ")");
		}

		// 예약 리스트 생성
		List<Reservation> reservations = new ArrayList<>();
		Map<String, String> qrCodeMap = new HashMap<>();

		// 예약 생성 및 저장
		for (String email : uniqueEmails) {
			Member member = emailToMemberMap.get(email);
			boolean isHolder = email.equals(reserverEmail);
			reservations.add(Reservation.from(schedules, email, member.getName(), isHolder, member));
		}

		reservationRepository.saveAll(reservations);

		// QR 코드 생성 (예약 ID + 이메일 조합)
		for (Reservation reservation : reservations) {
			String qrCodeBase64 = qrCodeUtil.generateQRCode(reservation.getUserEmail(), reservation.getId().toString());
			qrCodeService.saveQRCode(reservation.getUserEmail(), reservation.getId(), request.scheduleId().toString(), qrCodeBase64);
			qrCodeMap.put(reservation.getUserEmail(), qrCodeBase64);
		}

		for (Schedule schedule : schedules) {
			schedule.setCurrentRes(totalParticipants); // 현재 사용 인원을 예약자 + 참여자 숫자로 지정
			schedule.updateStatus(ScheduleSlotStatus.RESERVED);
		}

		scheduleRepository.saveAll(schedules);
		sendReservationSuccessEmail(roomType, reserverEmail, uniqueEmails, schedules.get(0));

		return "Success";
	}

	@Transactional
	public CancelReservationResponse cancelReservation(Long id, String authorizationHeader) {
		Reservation reservation = reservationRepository.findById(id)
			.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 예약입니다."));

		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		// JWT 를 통한 사용자 정보를 토대로, 본인의 예약인지 확인
		if (!reservation.isOwnedBy(email)) {
			throw new BusinessException(StatusCode.NOT_FOUND, "이전에 예약이 되지 않았습니다.");
		}

		LocalDateTime now = LocalDateTime.now(clock);

		Schedule firstSchedule = scheduleRepository.findById(reservation.getFirstScheduleId())
			.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 스케줄입니다."));

		LocalDateTime startTime = LocalDateTime.of(now.toLocalDate(), firstSchedule.getStartTime());

		// 입실 1 시간 전 일 경우 패널티
		if (now.isAfter(startTime)) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "입실 시간이 초과하였기에 취소할 수 없습니다.");
		}

		if (!now.isBefore(startTime.minusHours(1))) {
			// 취소 패널티 부여
			penaltyService.assignPenalty(memberRepository.getMemberByEmail(Email.of(email)), id, PenaltyReasonType.CANCEL);
		}

		/*
		 * 취소 프로세스 시작
		 * 몇 개의 스케줄을 취소해야하는가?
		 * secondSchedule도 존재한다면 cancel로 currentRes를 -1을 더해준다.
		 */

		firstSchedule.cancel();

		Optional.ofNullable(reservation.getSecondScheduleId())
			.flatMap(scheduleRepository::findById)
			.ifPresent(Schedule::cancel);

		reservation.markStatus(ReservationStatus.CANCELLED);
		return new CancelReservationResponse(id);
	}

	@Transactional
	public String extendReservation(Long reservationId, String authorizationHeader) {
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 예약입니다."));

		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		if (!reservation.isOwnedBy(email)) {
			throw new BusinessException(StatusCode.NOT_FOUND, "해당 예약 정보가 존재하지 않습니다.");
		}

		//연장 가능 시간 검증
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDateTime endTime = LocalDateTime.of(reservation.getScheduleDate(), reservation.getEndTime());
		if (now.isAfter(endTime)) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "연장 가능한 시간이 지났습니다.");
		} else if (now.isBefore(endTime.minusMinutes(10))) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "연장은 퇴실 시간 10분 전부터 가능합니다.");
		}

		Long lastScheduleId = reservation.getSecondScheduleId() != null ?
			reservation.getSecondScheduleId() : reservation.getFirstScheduleId();

		//예약의 마지막 스케줄 ID를 통해 다음 스케줄을 찾는다.
		Schedule nextSchedule = scheduleRepository.findById(lastScheduleId + 1).orElseThrow(
			() -> new BusinessException(StatusCode.NOT_FOUND, "스터디룸 이용 가능 시간을 확인해주세요."));

		if(!nextSchedule.getRoomNumber().equals(reservation.getRoomNumber())) {
			throw new BusinessException(StatusCode.NOT_FOUND, "스터디룸 이용 가능 시간을 확인해주세요.");
		}

		if (!nextSchedule.isCurrentResLessThanCapacity() || !nextSchedule.isAvailable()) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "다음 시간대가 이미 예약이 완료되었거나, 이용이 불가능한 상태입니다.");
		}

		if (nextSchedule.getRoomType() == RoomType.GROUP) {
			// 그룹 예약 이기에 같은 시간대의 예약 레코드를 모두 가져온다(참여자 검증 필요). member를 통해 검증 예정
			List<Reservation> reservations = reservationRepository.findByFirstScheduleId(reservation.getFirstScheduleId());

			// 패널티를 부여받고 있는 참여자가 존재할 경우 예약 연장 진행 불가
			for (Reservation res : reservations) {
				if (res.getMember().isPenalty()) {
					throw new BusinessException(StatusCode.FORBIDDEN, "패널티가 있는 멤버로 인해 연장이 불가능합니다.");
				}
			}

			for (Reservation res : reservations) {
				//1명이라도 입실하지 않은 경우
				if (!res.isEntered()){
					//지각 입실은 앞서 패널티 체킹으로 연장 불가 처리
					throw new BusinessException(StatusCode.BAD_REQUEST, "입실 처리 되어있지 않은 유저가 있어 연장이 불가능합니다.");
				}
			}

			for (Reservation res : reservations) {
				res.extendReservation(nextSchedule.getId(), nextSchedule.getEndTime());
			}

			nextSchedule.updateStatus(ScheduleSlotStatus.RESERVED);
			nextSchedule.setCurrentRes(reservations.size());
		} else {
			if(reservation.getMember().isPenalty()){
				throw new BusinessException(StatusCode.FORBIDDEN, "패넡티 상태이므로, 연장이 불가능합니다.");
			}

			if(!reservation.isEntered()){
				throw new BusinessException(StatusCode.BAD_REQUEST, "예약 연장은 입실 후 가능합니다.");
			}

			reservation.extendReservation(nextSchedule.getId(), nextSchedule.getEndTime());
			nextSchedule.setCurrentRes(nextSchedule.getCurrentRes() + 1);
			if (!nextSchedule.isCurrentResLessThanCapacity()){
				nextSchedule.updateStatus(ScheduleSlotStatus.RESERVED);
			}
		}

		return "Success";
	}

	private List<Schedule> findSchedules(Long[] scheduleIds) {
		return Arrays.stream(scheduleIds)
			.map(id -> scheduleRepository.findById(id)
				.filter(schedule -> schedule.getStatus() == ScheduleSlotStatus.AVAILABLE) // AVAILABLE 상태 체크
				.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "존재하지 않거나 사용 불가능한 스케줄입니다.")))
			.collect(Collectors.toList());
	}

	private void validateConsecutiveSchedules(CreateReservationRequest request) {
		Schedule firstSchedule = scheduleRepository.findById(request.scheduleId()[0])
			.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 스케줄입니다."));
		Schedule secondSchedule = scheduleRepository.findById(request.scheduleId()[1])
			.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 스케줄입니다."));

		// 같은 방인지 확인
		if (!firstSchedule.getRoomNumber().equals(secondSchedule.getRoomNumber())) {
			throw new BusinessException(StatusCode.BAD_REQUEST,"연속된 예약은 같은 방에서만 가능합니다.");
		}

		// 시간이 연속되는지 확인
		if (!firstSchedule.getEndTime().equals(secondSchedule.getStartTime())) {
			throw new BusinessException(StatusCode.BAD_REQUEST,"연속되지 않은 시간은 예약할 수 없습니다.");
		}
	}

	private void validateSchedulesAvailable(List<Schedule> schedules) {
		LocalDateTime now = LocalDateTime.now();

		if (schedules.stream().anyMatch(schedule -> {
			LocalDateTime scheduleStartDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
			return !schedule.isAvailable() ||
				!schedule.isCurrentResLessThanCapacity() ||
				scheduleStartDateTime.isBefore(now); // 현재 시간보다 이전이면 예외 발생
		})) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "예약이 불가능합니다.");
		}
	}

	private void checkDuplicateReservation(String reserverEmail){
		Optional<Reservation> recentReservation = reservationRepository.findLatestReservationByUserEmail(reserverEmail);
		if (recentReservation.isPresent()) {
			ReservationStatus recentStatus = recentReservation.get().getStatus();
			if (recentStatus == ReservationStatus.RESERVED || recentStatus == ReservationStatus.ENTRANCE) {
				throw new BusinessException(StatusCode.CONFLICT, "현재 예약이 진행 중이므로 새로운 예약을 생성할 수 없습니다.");
			}
		}
	}

	private void sendReservationSuccessEmail(RoomType type, String reserverEmail, Set<String> participantsEmail,
		Schedule schedule) {

		String subject = "[ICE-STUDYRES] 스터디룸 예약이 완료되었습니다.";
		List<Email> participantsEmailList = participantsEmail.stream().map(Email::new).toList();
		String body = buildReservationSuccessEmailBody(type, schedule, reserverEmail, participantsEmailList);

		emailService.sendEmail(new EmailRequest(reserverEmail, subject, body));

		if(type == RoomType.GROUP){
			for (String uniqueEmail : participantsEmail) {
				if(!uniqueEmail.equals(reserverEmail)){
					emailService.sendEmail(new EmailRequest(uniqueEmail, subject, body));
				}
			}
		}
	}

	private String buildReservationSuccessEmailBody(RoomType type, Schedule schedule, String reserverEmail, List<Email> participantsEmail) {
		String participantsSection = "";
		Member reserver = memberDomainService.getMemberByEmail(reserverEmail);
		List<Member> participantsMember = memberDomainService.getMembersByEmail(participantsEmail);
		if (type == RoomType.GROUP) {
			participantsSection = "<h3>참여자 명단</h3><ul>";

			for (Member member : participantsMember) {
				if(!member.getEmail().getValue().equals(reserverEmail)){
					participantsSection += "<li>" + member.getName() + "(" + member.getStudentNum() + ")" + "</li>";
				}
			}
			participantsSection += "</ul>";
		}

		return String.format(
			"<html><body>" +
				"<h2>스터디룸 예약이 완료되었습니다!</h2>" +
				"<p>아래 예약 정보를 확인해주세요.</p>" +
				"<hr>" +
				"<h3>예약 정보</h3>" +
				"<p><strong>예약자:</strong> %s(%s)</p>" +
				"<p><strong>스터디룸:</strong> %s</p>" +
				"<p><strong>예약 날짜:</strong> %s</p>" +
				"<p><strong>이용 시간:</strong> %s ~ %s</p>" +
				"%s" +  // 그룹 예약 시 참여자 목록 포함
				"<hr>" +
				"<h3>⚠ 예약 패널티 안내 ⚠</h3>" +
				"<p>예약 시간 미준수 시 패널티가 부여되며, 해당 기간 동안 예약 기능이 제한됩니다.</p>" +
				"<ul>" +
				"<li><strong>No Show</strong> 시 <strong>7일간 패널티 부여</strong></li>" +
				"<li><strong>예약 시간 30분 초과 입장</strong> 시 <strong>지각 처리</strong> 및 <strong>3일간 패널티 부여</strong></li>" +
				"<li><strong>입장 시간 1시간 전 취소</strong> 시 <strong>2일간 패널티 부여</strong></li>" +
				"</ul>" +
				"<p>감사합니다.</p>" +
				"</body></html>",
			reserver.getName(),
			reserver.getStudentNum(),
			schedule.getRoomNumber(),
			LocalDate.now(),
			schedule.getStartTime(),
			schedule.getEndTime(),
			participantsSection
		);
	}
}
