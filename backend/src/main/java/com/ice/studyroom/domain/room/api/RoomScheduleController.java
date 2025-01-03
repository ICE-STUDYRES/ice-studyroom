// package com.ice.studyroom.domain.room.api;
//
// import java.time.LocalDate;
//
// import org.springframework.format.annotation.DateTimeFormat;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
//
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import lombok.RequiredArgsConstructor;
//
// @RestController
// @RequestMapping("/api")
// @Tag(name = "Room Schedule", description = "스터디룸 스케줄 조회 API")
// @RequiredArgsConstructor
// public class RoomScheduleController {
//
// 	private final RoomScheduleService roomScheduleService;
//
// 	@Operation(summary = "스터디룸 전체 시간표 조회", description = "특정 날짜의 스터디룸 예약 현황을 조회합니다.")
// 	@GetMapping("/rooms/schedules")
// 	public ResponseEntity<List<RoomScheduleResponse>> getRoomSchedules(
// 		@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
// 	) {
// 		return ResponseEntity.ok(roomScheduleService.getRoomSchedules(date));
// 	}
//
// 	@Operation(summary = "특정 스터디룸 시간표 조회", description = "특정 스터디룸의 예약 현황을 조회합니다.")
// 	@GetMapping("/rooms/{roomId}/schedules")
// 	public ResponseEntity<RoomScheduleResponse> getRoomSchedule(
// 		@PathVariable Long roomId,
// 		@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
// 	) {
// 		return ResponseEntity.ok(roomScheduleService.getRoomSchedule(roomId, date));
// 	}
// }
