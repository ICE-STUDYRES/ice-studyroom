package com.ice.studyroom.domain.reservation.application;

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
import com.ice.studyroom.domain.reservation.presentation.dto.request.DeleteReservationRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.request.QrEntranceRequest;
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

	// @Transactional
	// public String createIndividualReservation(String authorizationHeader, CreateReservationRequest request) {
	// 	// 종합된 스케줄의 예약 자리가 비어있는지, 예약 가능 상태인지 확인
	// 	List<Schedule> schedules = findSchedules(request.scheduleId());
	// 	validateSchedulesAvailable(schedules);
	//
	// 	String reserverEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);
	//
	//
	// }

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
		if (totalParticipants < minRes) {
			throw new IllegalArgumentException(
				"최소 예약 인원 조건을 만족하지 않습니다. (필요 인원: " + minRes + ", 현재 인원: " + totalParticipants + ")");
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
			schedule.reserve();
		}

		scheduleRepository.saveAll(schedules);

		return "Success";
	}


	@Transactional
	public void cancelReservation(DeleteReservationRequest request) {
		// TODO: 추후에는 JWT를 통한 사용자 정보를 토대로, 본인의 예약인지 확인하고 예약을 취소할 예정

		// 예약 데이터를 가져온다.
		Reservation reservation = findReservationById(request.getReservationId());

		// 예약 상태 확인
		if (!reservation.isReserved()) {
			throw new IllegalStateException("이전에 예약이 되지 않았습니다.");
		}

		List<Schedule> schedules = new ArrayList<>();

		// first schedule은 항상 존재
		Schedule firstSchedule = scheduleRepository.findById(reservation.getFirstScheduleId())
			.orElseThrow(() -> new IllegalStateException("스케줄을 찾지 못했습니다."));
		firstSchedule.available();
		schedules.add(firstSchedule);

		// second schedule이 있는 경우 (2시간 예약인 경우)
		if (reservation.getSecondScheduleId() != null) {
			Schedule secondSchedule = scheduleRepository.findById(reservation.getSecondScheduleId())
				.orElseThrow(() -> new IllegalStateException("스케줄을 찾지 못했습니다."));
			secondSchedule.available();
			schedules.add(secondSchedule);
		}

		// TODO: 사용자 취소 횟수 차감 코드 구현 예정

		scheduleRepository.saveAll(schedules);
		reservationRepository.delete(reservation);
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
