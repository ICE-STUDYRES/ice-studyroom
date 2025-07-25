package com.ice.studyroom.domain.schedule.application;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.exception.member.MemberNotFoundException;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.schedule.domain.exception.schedule.ScheduleNotFoundException;
import com.ice.studyroom.domain.schedule.infrastructure.redis.ScheduleVacancyAlertService;
import com.ice.studyroom.global.security.service.TokenService;

import com.ice.studyroom.global.type.ActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

	private final TokenService tokenService;
	private final MemberRepository memberRepository;
	private final ScheduleRepository scheduleRepository;
	private final ScheduleVacancyAlertService scheduleVacancyAlertService;

	@Transactional(readOnly = true)
	public String registerVacancyAlert(Long scheduleId, String authorizationHeader) {
		String requesterEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);

		Member member = memberRepository.findByEmail(Email.of(requesterEmail))
			.orElseThrow(() -> new MemberNotFoundException(requesterEmail, ActionType.VACANCY_ALERT));
		Schedule schedule = scheduleRepository.findById(scheduleId)
				.orElseThrow(() ->  new ScheduleNotFoundException(scheduleId, requesterEmail, ActionType.VACANCY_ALERT));

		scheduleVacancyAlertService.registerVacancyAlert(scheduleId, requesterEmail, member.getName());

		return "빈자리 알림이 등록되었습니다.";
	}
}
