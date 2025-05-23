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
import com.ice.studyroom.domain.reservation.infrastructure.redis.QRCodeService;
import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import com.ice.studyroom.global.security.service.TokenService;
import com.ice.studyroom.domain.reservation.infrastructure.util.QRCodeUtil;
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
import com.ice.studyroom.domain.reservation.presentation.dto.response.QrEntranceResponse;
import com.ice.studyroom.global.dto.request.EmailRequest;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.service.EmailService;
import com.ice.studyroom.global.type.StatusCode;
import com.ice.studyroom.global.util.SecureTokenUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
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

	private final Clock clock;

	public List<Schedule> getSchedule() {
		LocalDate today = LocalDate.now(clock);
		return scheduleRepository.findByScheduleDate(today);
	}

	public Optional<GetMostRecentReservationResponse> getMyMostRecentReservation(String authorizationHeader) {
		String reservationOwnerEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);
		return reservationRepository.findLatestReservationByMemberEmail(Email.of(reservationOwnerEmail))
			.map(GetMostRecentReservationResponse::from);
	}

	@Transactional(readOnly = true)
	public List<GetReservationsResponse> getReservations(String authorizationHeader) {
		String reservationOwnerEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);

		Member reservationOwner = memberRepository.findByEmail(Email.of(reservationOwnerEmail))
			.orElseThrow(() -> {
				ReservationLogUtil.logWarn("내 예약 정보 조회 실패 - 존재하지 않는 회원", "email: " + reservationOwnerEmail);
				return new BusinessException(StatusCode.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다.");
			});

		List<Reservation> reservations = reservationRepository.findByMember(reservationOwner);

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

	@Transactional
	public String getMyReservationQrCode(Long reservationId, String authorizationHeader) {
		String reservationOwnerEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);

		ReservationLogUtil.log("QR코드 요청 수신", "예약 ID: " + reservationId);

		// 예약이 유효한지 확인
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> {
				ReservationLogUtil.logWarn("QR코드 요청 실패 - 존재하지 않는 예약","예약 ID: " + reservationId);
				return new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 예약입니다.");
			});

		if(reservation.getStatus() != ReservationStatus.RESERVED){
			ReservationLogUtil.logWarn("QR코드 요청 실패 - 예약 상태 아님", "예약 ID: " + reservationId);
			throw new BusinessException(StatusCode.BAD_REQUEST, "예약 상태가 아닙니다.");
		}

		// 해당 사용자의 예약인지 확인
		if (!reservation.isOwnedBy(reservationOwnerEmail)) {
			ReservationLogUtil.logWarn("QR코드 요청 실패 - 예약 접근 권한 없음", "예약 ID: " + reservationId);
			throw new BusinessException(StatusCode.FORBIDDEN, "해당 예약에 접근할 수 없습니다.");
		}

		String token = reservation.getQrToken();

		if (token == null) {
			token = SecureTokenUtil.generate(10);
			reservation.assignQrToken(token);
			reservationRepository.save(reservation);
			ReservationLogUtil.log("QR코드 생성 완료", "예약 ID: " + reservationId);
		} else {
			ReservationLogUtil.log("QR코드 재사용", "예약 ID: " + reservationId);
		}

		qrCodeService.storeToken(token, reservation.getId());
		String qrCode = qrCodeUtil.generateQRCodeFromToken(token);
		ReservationLogUtil.log("QR 토큰 저장 및 생성 완료", "예약 ID: " + reservationId);
		return qrCode;
	}

	@Transactional
	public QrEntranceResponse qrEntrance(QrEntranceRequest request) {
		// 클라이언트로부터 전달된 토큰 저장
		String token = request.qrToken();

		// 토큰으로 예약 ID 조회
		Long reservationId = qrCodeService.getReservationIdByToken(token);
		if (reservationId == null) {
			ReservationLogUtil.logWarn("QR 입장 실패 - 유효하지 않은 QR 토큰", token);
			throw new BusinessException(StatusCode.BAD_REQUEST, "만료되었거나 유효하지 않은 QR 코드입니다.");
		}

		ReservationLogUtil.log("QR 입장 요청 수신", "예약 ID: " + reservationId);

		// RDB에 존재하지않는 예약의 경우
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> {
				ReservationLogUtil.logWarn("QR 입장 실패 - 존재하지 않는 예약", "예약 ID: " + reservationId);
				return new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 예약입니다.");
			});

		Member reservationOwner = reservation.getMember();

		// 예약의 상태에 따른 에러 처리
		if(reservation.getStatus() == ReservationStatus.CANCELLED){
			ReservationLogUtil.logWarn("QR 입장 실패 - 취소된 예약", "예약 ID: " + reservationId);
			throw new BusinessException(StatusCode.BAD_REQUEST, "취소된 예약입니다.");
		} else if(reservation.getStatus() == ReservationStatus.ENTRANCE || reservation.getStatus() == ReservationStatus.LATE){
			ReservationLogUtil.logWarn("QR 입장 실패 - 이미 입장 처리된 예약", "예약 ID: " + reservationId);
			throw new BusinessException(StatusCode.BAD_REQUEST, "이미 입실처리 된 예약입니다.");
		}

		LocalDateTime now = LocalDateTime.now(clock);
		ReservationStatus status = reservation.checkAttendanceStatus(now);

		reservation.updateEnterTime(now);
		reservation.markStatus(status);

		// qr 무효화
		qrCodeService.invalidateToken(token);

		if (status == ReservationStatus.RESERVED) {
			ReservationLogUtil.logWarn("QR 입장 실패 - 출석 시간 아님", "예약 ID: " + reservationId);
			throw new BusinessException(StatusCode.BAD_REQUEST, "출석 시간이 아닙니다.");
		} else if (status == ReservationStatus.NO_SHOW) {
			ReservationLogUtil.logWarn("QR 입장 실패 - 출석 시간 만료", "예약 ID: " + reservationId);
			throw new BusinessException(StatusCode.BAD_REQUEST, "출석 시간이 만료되었습니다.");
		} else if(status == ReservationStatus.LATE){
			ReservationLogUtil.logWarn("지각 입장 - 패널티 부여", "예약 ID: " + reservationId,
				"userId: " + reservationOwner.getEmail().getValue());
			penaltyService.assignPenalty(reservationOwner, reservationId, PenaltyReasonType.LATE);
		}

		ReservationLogUtil.log("QR 입장 처리 완료", "예약 ID: " + reservationId);
		return new QrEntranceResponse(status, reservationOwner.getName(), reservationOwner.getStudentNum());
	}

	@Transactional
	public String createIndividualReservation(String authorizationHeader, CreateReservationRequest request) {
		// Access Token 에서 예약자 이메일 추출
		String reservationOwnerEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);
		ReservationLogUtil.log("개인 예약 생성 요청", "스케줄 ID: " + Arrays.toString(request.scheduleId()), "참여자 이메일: " + Arrays.toString(request.participantEmail()));

		// 예약자 확인
		Member reservationOwner = memberRepository.findByEmail(Email.of(reservationOwnerEmail))
			.orElseThrow(() -> {
				ReservationLogUtil.logWarn("개인 예약 실패 - 존재하지 않는 예약자", "예약자 이메일: " + reservationOwnerEmail);
				return new BusinessException(StatusCode.NOT_FOUND, "예약자 이메일이 존재하지 않습니다: " + reservationOwnerEmail);
			});

		// 예약 가능 여부 확인
		List<Long> idList = Arrays.stream(request.scheduleId()).toList();
		List<Schedule> schedules = scheduleRepository.findAllByIdIn(idList);

		if (schedules.size() != idList.size()) {
			ReservationLogUtil.logWarn("개인 예약 실패 - 존재하지 않는 스케줄 포함됨", "스케줄 ID: " + idList);
			throw new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 스케줄이 포함되어 있습니다.");
		}

		validateSchedulesAvailable(schedules);

		RoomType roomType = schedules.get(0).getRoomType();
		if(roomType == RoomType.GROUP) {
			ReservationLogUtil.logWarn("개인 예약 실패 - 그룹 전용 방 예약 시도","방 번호: " + schedules.get(0).getRoomNumber());
			throw new BusinessException(StatusCode.FORBIDDEN, "해당 방은 단체예약 전용입니다.");
		}

		// 패널티 상태 확인 (예약 불가)
		if (reservationOwner.isPenalty()) {
			ReservationLogUtil.logWarn("개인 예약 실패 - 패널티 상태의 사용자", "예약자 이메일: " + reservationOwnerEmail);
			throw new BusinessException(StatusCode.FORBIDDEN, "사용정지 상태입니다.");
		}

		// 예약 중복 방지
		checkDuplicateReservation(Email.of(reservationOwnerEmail));

		Reservation reservation = Reservation.from(schedules, true, reservationOwner);
		reservationRepository.save(reservation);

		// 스케줄 업데이트 (currentRes 증가 및 상태 변경)
		for (Schedule schedule : schedules) {
			if (!schedule.isCurrentResLessThanCapacity()) {
					ReservationLogUtil.logWarn("개인 예약 실패 - 수용 인원 초과", "스케줄 ID: " + schedule.getId());
				throw new BusinessException(StatusCode.BAD_REQUEST, "예약 가능한 자리가 없습니다.");
			}

			schedule.reserve(); // 개인예약은 현재사용인원에서 +1 진행
			if (schedule.getCurrentRes().equals(schedule.getCapacity())) { //예약 후 현재인원 == 방수용인원 경우 RESERVE
				schedule.updateStatus(ScheduleSlotStatus.RESERVED);
			}
		}

		scheduleRepository.saveAll(schedules);
		ReservationLogUtil.log("개인 예약 생성 성공", "예약자: " + reservationOwnerEmail, "예약 ID: " + reservation.getId());
		sendReservationSuccessEmail(roomType, reservationOwnerEmail, new HashSet<>(), schedules);

		return "Success";
	}

	@Transactional
	public String createGroupReservation(String authorizationHeader, CreateReservationRequest request) {
		// JWT에서 예약자 이메일 추출
		String reservationOwnerEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);
		ReservationLogUtil.log("단체 예약 생성 요청", "스케줄 ID: " + Arrays.toString(request.scheduleId()), "참여자 이메일: " + Arrays.toString(request.participantEmail()));

		// 예약자(User) 확인 및 user_name 가져오기
		Member reservationOwner = memberRepository.findByEmail(Email.of(reservationOwnerEmail))
			.orElseThrow(() -> {
				ReservationLogUtil.logWarn("단체 예약 실패 - 존재하지 않는 예약자", "예약자 이메일: " + reservationOwnerEmail);
				return new BusinessException(StatusCode.NOT_FOUND, "예약자 이메일이 존재하지 않습니다: " + reservationOwnerEmail);
			});

		// 예약 가능 여부 확인
		List<Long> idList = Arrays.stream(request.scheduleId()).toList();
		List<Schedule> schedules = scheduleRepository.findAllByIdIn(idList);

		if (schedules.size() != idList.size()) {
			ReservationLogUtil.logWarn("단체 예약 실패 - 존재하지 않는 스케줄 포함됨", "스케줄 ID: " + idList);
			throw new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 스케줄이 포함되어 있습니다.");
		}

		validateSchedulesAvailable(schedules);

		// 스케줄에서 Type을 저장해야하며, Type에 따른 RES 처리가 필요하다.
		RoomType roomType = schedules.get(0).getRoomType();
		if(roomType == RoomType.INDIVIDUAL) {
			ReservationLogUtil.logWarn("단체 예약 실패 - 개인 전용 방 예약 시도", "방 번호: " + schedules.get(0).getRoomNumber());
			throw new BusinessException(StatusCode.FORBIDDEN, "해당 방은 개인예약 전용입니다.");
		}

		if(reservationOwner.isPenalty()) {
			ReservationLogUtil.logWarn("단체 예약 실패 - 예약자가 패널티 상태", "예약자 이메일: " + reservationOwnerEmail);
			throw new BusinessException(StatusCode.FORBIDDEN, "예약자가 패널티 상태입니다. 예약이 불가능합니다.");
		}

		// 예약 중복 방지
		checkDuplicateReservation(Email.of(reservationOwnerEmail));

		// 중복된 이메일 검사 (예약자 포함)
		Set<String> uniqueEmails = new HashSet<>();
		uniqueEmails.add(reservationOwnerEmail); // 예약자 이메일 포함

		// 예약자와 참여자의 이메일을 저장 (이름 포함)
		Map<String, Member> emailToMemberMap = new HashMap<>();
		emailToMemberMap.put(reservationOwnerEmail, reservationOwner);

		// 참여자 리스트 추가 (중복 검사 및 user_name 조회)
		if (!ObjectUtils.isEmpty(request.participantEmail())) {
			for (String email : request.participantEmail()) {
				if (!uniqueEmails.add(email)) {
						ReservationLogUtil.logWarn("단체 예약 실패 - 중복된 참여자 이메일", "이메일: " + email);
					throw new BusinessException(StatusCode.BAD_REQUEST, "중복된 참여자 이메일이 존재합니다: " + email);
				}
				Member participant = memberRepository.findByEmail(Email.of(email))
					.orElseThrow(() -> {
						ReservationLogUtil.logWarn("단체 예약 실패 - 존재하지 않는 참여자 이메일", "이메일: " + email);
						return new BusinessException(StatusCode.NOT_FOUND, "참여자 이메일이 존재하지 않습니다: " + email);
					});

				//참여자 패널티 상태 확인
				if (participant.isPenalty()) {
					ReservationLogUtil.logWarn("단체 예약 실패 - 패널티 상태 참여자 존재", "이메일: " + email);
					throw new BusinessException(StatusCode.FORBIDDEN, "참여자 중 패널티 상태인 사용자가 있습니다. 예약이 불가능합니다. (이메일: " + email + ")");
				}

				//참여자 최근 예약 상태 확인
				Optional<Reservation> recentReservationOpt = reservationRepository.findLatestReservationByMemberEmail(Email.of(email));
				if(recentReservationOpt.isPresent()) {
					ReservationStatus recentStatus = recentReservationOpt.get().getStatus();
					if(recentStatus == ReservationStatus.RESERVED || recentStatus == ReservationStatus.ENTRANCE) {
						ReservationLogUtil.logWarn("단체 예약 실패 - 참여자가 이미 예약 중", "이메일: " + email);
						throw new BusinessException(StatusCode.CONFLICT, "참여자 중 현재 예약이 진행 중인 사용자가 있어 예약이 불가능합니다. (이메일: " + email + ")");
					}
				}

				emailToMemberMap.put(email, participant);
			}
		}

		// 최소 예약 인원(minRes) 검사 (예약자 + 참여자 수 체크)
		int totalParticipants = uniqueEmails.size(); // 예약자 + 참여자 수
		int minRes = schedules.get(0).getMinRes(); // 모든 Group 전용 schedule의 min_res는 2로 동일
		int capacity = schedules.get(0).getCapacity(); // 같은 방의 schedule은 capacity는 동일
		if (totalParticipants < minRes) {
			ReservationLogUtil.logWarn("단체 예약 실패 - 최소 인원 미달", "최소 인원: " + minRes, "현재 인원: " + totalParticipants);
			throw new BusinessException(StatusCode.BAD_REQUEST,
				"최소 예약 인원 조건을 만족하지 않습니다. (필요 인원: " + minRes + ", 현재 인원: " + totalParticipants + ")");
		}else if (totalParticipants > capacity) {
			ReservationLogUtil.logWarn("단체 예약 실패 - 최대 인원 초과", "최대 수용 인원: " + capacity, "현재 인원: " + totalParticipants);
			throw new BusinessException(StatusCode.BAD_REQUEST,
				"방의 최대 수용 인원을 초과했습니다. (최대 수용 인원: " + capacity + ", 현재 인원: " + totalParticipants + ")");
		}

		// 예약 리스트 생성
		List<Reservation> reservations = new ArrayList<>();

		// 예약 생성 및 저장
		for (String email : uniqueEmails) {
			Member member = emailToMemberMap.get(email);
			boolean isHolder = email.equals(reservationOwnerEmail);
			reservations.add(Reservation.from(schedules, isHolder, member));
		}
		reservationRepository.saveAll(reservations);

		for (Schedule schedule : schedules) {
			schedule.updateGroupCurrentRes(totalParticipants); // 현재 사용 인원을 예약자 + 참여자 숫자로 지정
			schedule.updateStatus(ScheduleSlotStatus.RESERVED);
		}
		ReservationLogUtil.log("단체 예약 생성 성공", "예약자: " + reservationOwnerEmail, "참여자 수: " + (uniqueEmails.size()-1), "예약 ID: " + reservations.get(0).getId());

		//전 인원에게 예약 확정 메일 발송
		sendReservationSuccessEmail(roomType, reservationOwnerEmail, uniqueEmails, schedules);

		return "Success";
	}

	@Transactional
	public CancelReservationResponse cancelReservation(Long reservationId, String authorizationHeader) {
		String reservationOwnerEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);
		ReservationLogUtil.log("예약 취소 요청 수신", "예약 ID: " + reservationId);

		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> {
				ReservationLogUtil.logWarn("예약 취소 실패 - 존재하지 않는 예약", "예약 ID: " + reservationId);
				return new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 예약입니다.");
			});

		Member reservationOwner = reservation.getMember();

		// JWT 를 통한 사용자 정보를 토대로, 본인의 예약인지 확인
		if (!reservation.isOwnedBy(reservationOwnerEmail)) {
			ReservationLogUtil.logWarn("예약 취소 실패 - 사용자 예약 아님","예약 ID: " + reservationId, "예약자 이메일: " + reservationOwnerEmail);
			throw new BusinessException(StatusCode.NOT_FOUND, "이전에 예약이 되지 않았습니다.");
		}

		LocalDateTime now = LocalDateTime.now(clock);

		Schedule firstSchedule = scheduleRepository.findById(reservation.getFirstScheduleId())
			.orElseThrow(() -> {
				ReservationLogUtil.logWarn("예약 취소 실패 - 예약에 연결된 스케줄 없음", "예약 ID: " + reservationId);
				return new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 스케줄입니다.");
			});

		LocalDateTime startTime = LocalDateTime.of(now.toLocalDate(), firstSchedule.getStartTime());

		if (now.isAfter(startTime)) {
			ReservationLogUtil.logWarn("예약 취소 실패 - 입실 시간 초과", "예약 ID: " + reservationId, "입실 시간: " + startTime);
			throw new BusinessException(StatusCode.BAD_REQUEST, "입실 시간이 초과하였기에 취소할 수 없습니다.");
		}

		if (!now.isBefore(startTime.minusHours(1))) {
			// 취소 패널티 부여
			ReservationLogUtil.log("예약 취소 - 패널티 부여", "예약 ID: " + reservationId, "예약자 이메일: " + reservationOwnerEmail);
			penaltyService.assignPenalty(reservationOwner, reservationId, PenaltyReasonType.CANCEL);
		}

		/*
		 * 취소 프로세스 시작
		 * 몇 개의 스케줄을 취소해야하는가?
		 * secondSchedule도 존재한다면 cancel로 currentRes를 -1을 더해준다.
		 */

		firstSchedule.cancel();

		Optional.ofNullable(reservation.getSecondScheduleId())
			.flatMap(scheduleRepository::findById)
			.ifPresent(schedule -> {
				schedule.cancel();
			});

		reservation.markStatus(ReservationStatus.CANCELLED);
		ReservationLogUtil.log("예약 상태 CANCELLED 처리 완료", "예약 ID: " + reservationId);

		return new CancelReservationResponse(reservationId);
	}

	@Transactional
	public String extendReservation(Long reservationId, String authorizationHeader) {
		ReservationLogUtil.log("예약 연장 요청 수신", "예약 ID: " + reservationId);

		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> {
				ReservationLogUtil.logWarn("예약 연장 실패 - 존재하지 않는 예약", "예약 ID: " + reservationId);
				return new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 예약입니다.");
			});

		String reservationOwnerEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);

		if (!reservation.isOwnedBy(reservationOwnerEmail)) {
			ReservationLogUtil.logWarn("예약 연장 실패 - 예약자 불일치", "예약 ID: " + reservationId, "예약자 이메일: " + reservationOwnerEmail);
			throw new BusinessException(StatusCode.NOT_FOUND, "해당 예약 정보가 존재하지 않습니다.");
		}

		//연장 가능 시간 검증
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDateTime endTime = LocalDateTime.of(reservation.getScheduleDate(), reservation.getEndTime());
		if (now.isAfter(endTime)) {
			ReservationLogUtil.logWarn("예약 연장 실패 - 퇴실 시간 이후 요청", "예약 ID: " + reservationId);
			throw new BusinessException(StatusCode.BAD_REQUEST, "연장 가능한 시간이 지났습니다.");
		} else if (now.isBefore(endTime.minusMinutes(10))) {
			ReservationLogUtil.logWarn("예약 연장 실패 - 연장 가능 시간 아님", "예약 ID: " + reservationId);
			throw new BusinessException(StatusCode.BAD_REQUEST, "연장은 퇴실 시간 10분 전부터 가능합니다.");
		}

		Long lastScheduleId = reservation.getSecondScheduleId() != null ?
			reservation.getSecondScheduleId() : reservation.getFirstScheduleId();

		//예약의 마지막 스케줄 ID를 통해 다음 스케줄을 찾는다.
		Schedule nextSchedule = scheduleRepository.findById(lastScheduleId + 1)
			.orElseThrow(() -> {
				ReservationLogUtil.logWarn("예약 연장 실패 - 다음 스케줄 없음", "예약 ID: " + reservationId);
				return new BusinessException(StatusCode.NOT_FOUND, "스터디룸 이용 가능 시간을 확인해주세요.");
			});

		if (!nextSchedule.getRoomNumber().equals(reservation.getRoomNumber())){
			ReservationLogUtil.logWarn("예약 연장 실패 - 다른 방 스케줄 연결 시도", "예약방: " + reservation.getRoomNumber(), "다음스케줄 방: " + nextSchedule.getRoomNumber());
			throw new BusinessException(StatusCode.BAD_REQUEST, "스터디룸 이용 가능 시간을 확인해주세요.");
		}

		if (!nextSchedule.isCurrentResLessThanCapacity() || !nextSchedule.isAvailable()) {
			ReservationLogUtil.logWarn("예약 연장 실패 - 다음 시간대 이용 불가 또는 인원 초과", "다음 스케줄 ID: " + nextSchedule.getId());
			throw new BusinessException(StatusCode.BAD_REQUEST, "다음 시간대가 이미 예약이 완료되었거나, 이용이 불가능한 상태입니다.");
		}

		if (nextSchedule.getRoomType() == RoomType.GROUP) {
			ReservationLogUtil.log("그룹 예약 연장 검증 시작", "예약 ID: " + reservationId);
			// 그룹 예약 이기에 같은 시간대의 예약 레코드를 모두 가져온다(참여자 검증 필요).
			List<Reservation> reservations = reservationRepository.findByFirstScheduleId(reservation.getFirstScheduleId());

			// 패널티를 부여받고 있는 참여자가 존재할 경우 예약 연장 진행 불가
			for (Reservation res : reservations) {
				//취소된 예약의 건에 대해서는 제외
				if (res.getStatus() == ReservationStatus.CANCELLED) continue;

				if (res.getMember().isPenalty()) {
					ReservationLogUtil.logWarn("그룹 예약 연장 실패 - 패널티 상태 참여자 존재",
						"이메일: " + res.getMember().getEmail());
					throw new BusinessException(StatusCode.FORBIDDEN, "패널티가 있는 멤버로 인해 연장이 불가능합니다.");
				}

				//1명이라도 입실하지 않은 경우
				if (!res.isEntered()){
					//지각 입실은 앞서 패널티 체킹으로 연장 불가 처리
					ReservationLogUtil.logWarn("그룹 예약 연장 실패 - 입실하지 않은 참여자 존재",
						"이메일: " + res.getMember().getEmail());
					throw new BusinessException(StatusCode.BAD_REQUEST, "입실 처리 되어있지 않은 유저가 있어 연장이 불가능합니다.");
				}
			}

			for (Reservation res : reservations) {
				if (res.getStatus() == ReservationStatus.CANCELLED) continue;
				res.extendReservation(nextSchedule.getId(), nextSchedule.getEndTime());
				nextSchedule.reserve();
				ReservationLogUtil.log("그룹 예약 연장 완료", "참여자 이메일: " + res.getMember().getEmail(), "예약 ID: " + res.getId());
			}
			nextSchedule.updateStatus(ScheduleSlotStatus.RESERVED);

		} else {
			if(reservation.getMember().isPenalty()){
				ReservationLogUtil.logWarn("개인 예약 연장 실패 - 패널티 상태 사용자", "예약자 이메일: " + reservationOwnerEmail);
				throw new BusinessException(StatusCode.BAD_REQUEST, "패넡티 상태이므로, 연장이 불가능합니다.");
			}

			if(!reservation.isEntered()){
				ReservationLogUtil.logWarn("개인 예약 연장 실패 - 입실하지 않은 사용자", "예약자 이메일: " + reservationOwnerEmail);
				throw new BusinessException(StatusCode.BAD_REQUEST, "예약 연장은 입실 후 가능합니다.");
			}
			reservation.extendReservation(nextSchedule.getId(), nextSchedule.getEndTime());
			nextSchedule.reserve();
			ReservationLogUtil.log("개인 예약 연장 완료", "예약자 이메일: " + reservationOwnerEmail, "예약 ID: " + reservation.getId());

			if (!nextSchedule.isCurrentResLessThanCapacity()){
				nextSchedule.updateStatus(ScheduleSlotStatus.RESERVED);
			}
		}

		ReservationLogUtil.log("예약 연장 최종 완료", "예약자 이메일: " + reservationOwnerEmail);
		return "Success";
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
		LocalDateTime now = LocalDateTime.now(clock);

		if (schedules.stream().anyMatch(schedule -> {
			LocalDateTime scheduleStartDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
			return !schedule.isAvailable() ||
				!schedule.isCurrentResLessThanCapacity() ||
				!scheduleStartDateTime.isAfter(now); // 현재 시간보다 이전이면 예외 발생
		})) {
			ReservationLogUtil.logWarn("스케줄 유효성 검증 실패",
				"현재 시간: " + now,
				"대상 스케줄 ID 목록: " + schedules.stream().map(Schedule::getId).toList());
			throw new BusinessException(StatusCode.BAD_REQUEST, "예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다.");
		}
	}

	private void checkDuplicateReservation(Email reservationOwnerEmail) {
		Optional<Reservation> recentReservation = reservationRepository.findLatestReservationByMemberEmail(
			reservationOwnerEmail);
		if (recentReservation.isPresent()) {
			ReservationStatus recentStatus = recentReservation.get().getStatus();
			if (recentStatus == ReservationStatus.RESERVED || recentStatus == ReservationStatus.ENTRANCE) {
				ReservationLogUtil.logWarn("중복 예약 시도",
					"userEmail: " + reservationOwnerEmail.getValue() + "현재 상태: " + recentStatus);
				throw new BusinessException(StatusCode.CONFLICT, "현재 예약이 진행 중이므로 새로운 예약을 생성할 수 없습니다.");
			}
		}
	}

	protected void sendReservationSuccessEmail(RoomType type, String reservationOwnerEmail, Set<String> participantsEmail,
		List<Schedule> schedules) {

		String subject = "[ICE-STUDYRES] 스터디룸 예약이 완료되었습니다.";
		List<Email> participantsEmailList = participantsEmail.stream().map(Email::of).toList();
		String body = buildReservationSuccessEmailBody(type, schedules, reservationOwnerEmail, participantsEmailList);

		emailService.sendEmail(new EmailRequest(reservationOwnerEmail, subject, body));

		if(type == RoomType.GROUP){
			for (String uniqueEmail : participantsEmail) {
				if(!uniqueEmail.equals(reservationOwnerEmail)){
					emailService.sendEmail(new EmailRequest(uniqueEmail, subject, body));
				}
			}
		}
	}

	private String buildReservationSuccessEmailBody(RoomType type, List<Schedule> schedules, String reservationOwnerEmail, List<Email> participantsEmail) {
		String participantsSection = "";
		Member reservationOwner = memberDomainService.getMemberByEmail(reservationOwnerEmail);
		List<Member> participantsMember = memberDomainService.getMembersByEmail(participantsEmail);
		if (type == RoomType.GROUP) {
			participantsSection = "<h3>참여자 명단</h3><ul>";

			for (Member member : participantsMember) {
				if(!member.getEmail().getValue().equals(reservationOwnerEmail)){
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
			reservationOwner.getName(),
			reservationOwner.getStudentNum(),
			schedules.get(0).getRoomNumber(),
			LocalDate.now(),
			schedules.get(0).getStartTime(),
			schedules.size() > 1 ? schedules.get(1).getEndTime() : schedules.get(0).getEndTime(),
			participantsSection
		);
	}
}
