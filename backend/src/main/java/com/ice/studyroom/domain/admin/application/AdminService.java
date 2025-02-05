package com.ice.studyroom.domain.admin.application;

import com.ice.studyroom.domain.admin.presentation.dto.request.AdminCreateOccupyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.request.AdminPenaltyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminCreateOccupyResponse;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminDeleteOccupyResponse;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminPenaltyControlResponse;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminPenaltyRecordResponse;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.domain.type.RoomTimeSlotStatus;
import com.ice.studyroom.domain.admin.infrastructure.persistence.RoomTimeSlotRepository;
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

	public AdminCreateOccupyResponse adminOccupyRoom (AdminCreateOccupyRequest request) {

		RoomTimeSlot roomTimeSlot = roomTimeSlotRepository.findById(request.roomTimeSlotId())
			.orElseThrow(() -> new IllegalStateException("해당 ID에 일치하는 RoomTimeSlot이 없습니다."));

		//상태를 RESERVED로 변경
		if (roomTimeSlot.getStatus() == RoomTimeSlotStatus.RESERVED) {
			throw new IllegalStateException("해당 시간에는 이미 선점되어있습니다.");
		}

		//방 상태 변경
		roomTimeSlot.updateStatus(RoomTimeSlotStatus.RESERVED);
		//변경 사항 저장
		roomTimeSlotRepository.save(roomTimeSlot);

		return AdminCreateOccupyResponse.of("관리자가 특정 요일, 방, 시간대를 선점했습니다.");
	}

	public List<Long> getReservedRoomIds() {
		// 선점된 방 엔티티만 가져오기
		List<RoomTimeSlot> reservedRoomTimeSlots = roomTimeSlotRepository.findByStatus(RoomTimeSlotStatus.RESERVED);

		//ID만 추출하여 반환
		return reservedRoomTimeSlots.stream().map(RoomTimeSlot::getId).toList();
	}

	public AdminDeleteOccupyResponse adminDeleteOccupy(AdminCreateOccupyRequest request) {

		RoomTimeSlot roomTimeSlot = roomTimeSlotRepository.findById(request.roomTimeSlotId())
			.orElseThrow(() -> new IllegalStateException("해당 ID에 일치하는 RoomTimeSlot이 없습니다."));

		if(roomTimeSlot.getStatus() == RoomTimeSlotStatus.AVAILABLE) {
			throw new IllegalStateException("해당 시간은 현재 사용가능 상태입니다.");
		}

		//방 상태 변경
		roomTimeSlot.updateStatus(RoomTimeSlotStatus.AVAILABLE);
		//변경 사항 저장
		roomTimeSlotRepository.save(roomTimeSlot);

		return AdminDeleteOccupyResponse.of("관리자가 특정 요일, 방, 시간대의 선점을 해지했습니다.");
	}

	public List<AdminPenaltyRecordResponse> adminGetPenaltyRecords(AdminPenaltyRequest request) {
		Member member = memberRepository.findByEmail(Email.of(request.email()))
			.orElseThrow(() -> new IllegalArgumentException("해당 이메일로 회원을 찾을 수 없습니다."));

		// 조건에 맞는 패널티 리스트 조회
		List<Penalty> penaltyList = penaltyRepository.findByMemberIdAndPenaltyEndAfter(
			member.getId(), LocalDateTime.now()
		);

		if (penaltyList.isEmpty()) {
			throw new IllegalArgumentException("해당 회원의 사용 정지 이력이 존재하지 않습니다.");
		}

		// 패널티 리스트를 AdminPenaltyRecordResponse로 변환하여 반환
		return penaltyList.stream()
			.map(penalty -> AdminPenaltyRecordResponse.of(penalty.getReason(), penalty.getPenaltyEnd()))
			.toList();
	}

	public AdminPenaltyControlResponse adminSubtractPenalty(AdminPenaltyRequest request) {
		Member member = memberRepository.findByEmail(Email.of(request.email()))
			.orElseThrow(() -> new IllegalArgumentException("해당 이메일로 회원을 찾을 수 없습니다."));

		// //패널티 횟수 차감
		// if(member.getPenaltyCount() >= 3) {
		// 	member.subPenalty(member.getPenaltyCount());
		// 	member.updatePenalty(false);
		// } else if(member.getPenaltyCount() == 1) {
		// 	member.subPenalty(member.getPenaltyCount());
		// } else {throw new IllegalStateException("현재 패널티 횟수가 0입니다.");}

		//변경상태 저장
		memberRepository.save(member);

		//return
		return AdminPenaltyControlResponse.of("관리자가 패널티 횟수를 차감했습니다.", 1);
	}

	public AdminPenaltyControlResponse adminAddPenalty(AdminPenaltyRequest request) {
		Member member = memberRepository.findByEmail(Email.of(request.email()))
			.orElseThrow(() -> new IllegalArgumentException("해당 이메일로 회원을 찾을 수 없습니다."));

		// //패널티 횟수 증가
		// if(member.isPenalty()) {
		// 	throw new IllegalStateException("현재 이미 사용불가 상태입니다.");
		// } else if(member.getPenaltyCount() >= 3) {
		// 	member.updatePenalty(true);
		// 	member.addPenalty(member.getPenaltyCount());
		// } else{member.addPenalty(member.getPenaltyCount());}

		//변경상태 저장
		memberRepository.save(member);

		//return
		return AdminPenaltyControlResponse.of("관리자가 패널티 횟수를 증가했습니다.", 1);
	}
}
