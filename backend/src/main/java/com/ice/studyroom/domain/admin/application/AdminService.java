package com.ice.studyroom.domain.admin.application;

import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.admin.presentation.dto.request.AdminOccupyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.request.AdminPenaltyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.response.*;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.domain.type.RoomTimeSlotStatus;
import com.ice.studyroom.domain.admin.infrastructure.persistence.RoomTimeSlotRepository;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

	private final RoomTimeSlotRepository roomTimeSlotRepository;
	private final PenaltyRepository penaltyRepository;
	private final MemberRepository memberRepository;

	public AdminOccupyResponse adminOccupyRooms(AdminOccupyRequest request) {
		// 요청된 roomTimeSlotId들을 기반으로 RoomTimeSlot을 조회
		List<RoomTimeSlot> roomTimeSlots = roomTimeSlotRepository.findAllById(request.roomTimeSlotId());

		if (roomTimeSlots.isEmpty()) {
			throw new BusinessException(StatusCode.NOT_FOUND, "해당 ID에 일치하는 RoomTimeSlot이 없습니다.");
		}

		// 상태 변경 전, 이미 예약된 항목 검사
		for (RoomTimeSlot roomTimeSlot : roomTimeSlots) {
			if (request.setOccupy() && roomTimeSlot.getStatus() == RoomTimeSlotStatus.RESERVED) {
				throw new BusinessException(StatusCode.BAD_REQUEST, "일부 시간대가 이미 선점되어 있습니다.");
			}
			if (!request.setOccupy() && roomTimeSlot.getStatus() == RoomTimeSlotStatus.AVAILABLE) {
				throw new BusinessException(StatusCode.BAD_REQUEST, "일부 시간대는 이미 사용 가능한 상태입니다.");
			}
		}

		// 상태 변경
		for (RoomTimeSlot roomTimeSlot : roomTimeSlots) {
			roomTimeSlot.updateStatus(request.setOccupy() ? RoomTimeSlotStatus.RESERVED : RoomTimeSlotStatus.AVAILABLE);
		}

		// 변경 사항 일괄 저장
		roomTimeSlotRepository.saveAll(roomTimeSlots);

		String message = request.setOccupy() ?
			"관리자가 선택한 시간대를 선점했습니다." :
			"관리자가 선택한 시간대의 선점을 해지했습니다.";

		return AdminOccupyResponse.of(message);
	}

	public List<AdminRoomResponse> getRoomByDayOfWeek(DayOfWeekStatus dayOfWeekStatus) {
		List<RoomTimeSlot> roomTimeSlots = roomTimeSlotRepository.findByDayOfWeek(dayOfWeekStatus);

		return roomTimeSlots.stream().map(AdminRoomResponse::from).toList();
	}

	public List<AdminGetReservedResponse> getReservedRoomIds() {
		// 선점된 방 엔티티만 가져오기
		List<RoomTimeSlot> reservedRooms = roomTimeSlotRepository.findByStatus(RoomTimeSlotStatus.RESERVED);

//		//ID만 추출하여 반환
//		return reservedRoomTimeSlots.stream().map(RoomTimeSlot::getId).toList();
		return reservedRooms.stream().map(AdminGetReservedResponse::from).toList();
	}

	public List<AdminPenaltyRecordResponse> adminGetPenaltyRecords(AdminPenaltyRequest request) {
		Member member = memberRepository.findByEmail(Email.of(request.email()))
			.orElseThrow(() -> new BusinessException(StatusCode.BAD_REQUEST, "해당 이메일로 회원을 찾을 수 없습니다."));

		// 조건에 맞는 패널티 리스트 조회
		List<Penalty> penaltyList = penaltyRepository.findByMemberIdAndPenaltyEndAfter(
			member.getId(), LocalDateTime.now()
		);

		if (penaltyList.isEmpty()) {
			throw new BusinessException(StatusCode.NOT_FOUND, "해당 회원의 사용 정지 이력이 존재하지 않습니다.");
		}

		// 패널티 리스트를 AdminPenaltyRecordResponse로 변환하여 반환
		return penaltyList.stream()
			.map(penalty -> AdminPenaltyRecordResponse.of(penalty.getReason(), penalty.getPenaltyEnd()))
			.toList();
	}

	public AdminPenaltyControlResponse adminSetPenalty(AdminPenaltyRequest request) {
		Member member = memberRepository.findByEmail(Email.of(request.email()))
			.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "해당 이메일로 회원을 찾을 수 없습니다."));

		member.updatePenalty(request.setPenalty());
		memberRepository.save(member);

		String message = request.setPenalty() ? "해당 유저에게 패널티가 부여되었습니다." : "해당 유저의 패널티가 해제되었습니다.";
		return AdminPenaltyControlResponse.of(message);
	}
}
