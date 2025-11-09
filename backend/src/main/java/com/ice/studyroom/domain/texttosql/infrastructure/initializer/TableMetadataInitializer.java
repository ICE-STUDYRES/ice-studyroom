package com.ice.studyroom.domain.texttosql.infrastructure.initializer;

import com.ice.studyroom.domain.texttosql.application.LocalEmbeddingService;
import com.ice.studyroom.domain.texttosql.domain.entity.TableMetadata;
import com.ice.studyroom.domain.texttosql.infrastructure.redis.RedisVectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TableMetadataInitializer implements CommandLineRunner {

	private final RedisVectorRepository redisVectorRepository;
	private final LocalEmbeddingService localEmbeddingService;

	@Override
	public void run(String... args) {
		if (redisVectorRepository.count() > 0) {
			log.info("테이블 메타데이터가 이미 존재합니다. 초기화 스킵.");
			return;
		}

		log.info("테이블 메타데이터 초기화 시작...");

		// reservation 테이블
		createTableMetadata(
			"reservation",
			"예약 정보를 저장하는 테이블. 사용자의 스터디룸 예약, 입실, 퇴실, 취소 등의 상태를 관리합니다.",
			"예약,booking,reserve,신청,취소,입실,퇴실,노쇼,지각,완료,상태,status,QR",
			List.of("id", "member_id", "first_schedule_id", "second_schedule_id", "schedule_date", "room_number", "start_time", "end_time", "status", "enter_time", "exit_time", "qr_code"),
			List.of("member", "schedule")
		);

		// member 테이블
		createTableMetadata(
			"member",
			"회원 정보를 저장하는 테이블. 사용자의 이름, 이메일, 학번, 패널티 여부 등을 관리합니다.",
			"회원,member,사용자,user,학생,student,이름,name,이메일,email,학번",
			List.of("id", "name", "email", "student_num", "password", "is_penalty"),
			List.of("reservation", "penalty")
		);

		// schedule 테이블
		createTableMetadata(
			"schedule",
			"일정 정보를 저장하는 테이블. 특정 날짜의 예약 가능한 시간대와 방 정보를 관리합니다.",
			"일정,schedule,시간표,타임,슬롯,예약가능,available,날짜,date",
			List.of("id", "schedule_date", "room_number", "room_time_slot_id", "start_time", "end_time", "current_res", "capacity", "status"),
			List.of("room_time_slot", "reservation")
		);

		// room_time_slot 테이블
		createTableMetadata(
			"room_time_slot",
			"방의 시간대 정보를 저장하는 테이블. 방 번호, 타입, 수용 인원, 요일별 운영 시간을 관리합니다.",
			"방,room,스터디룸,studyroom,호실,시간대,타임슬롯,운영시간,요일,개인실,그룹실",
			List.of("id", "room_number", "room_type", "capacity", "min_res", "start_time", "end_time", "day_of_week", "status"),
			List.of("schedule")
		);

		// penalty 테이블
		createTableMetadata(
			"penalty",
			"회원의 패널티 정보를 저장하는 테이블. 취소, 지각, 노쇼 등의 사유와 패널티 종료 시간을 관리합니다.",
			"패널티,penalty,제재,벌점,노쇼,지각,취소,경고",
			List.of("id", "member_id", "reservation_id", "reason", "penalty_end", "status"),
			List.of("member", "reservation")
		);

		log.info("테이블 메타데이터 초기화 완료! (총 {}개)", redisVectorRepository.count());
	}

	private void createTableMetadata(
		String tableName,
		String description,
		String keywords,
		List<String> columns,
		List<String> relatedTables
	) {
		try {
			String textForEmbedding = description + " " + keywords;
			byte[] embeddingBytes = localEmbeddingService.createEmbedding(textForEmbedding);
			float[] embedding = localEmbeddingService.byteArrayToFloatArray(embeddingBytes);

			TableMetadata metadata = TableMetadata.builder()
				.id("table:" + tableName)
				.tableName(tableName)
				.description(description)
				.keywords(keywords)
				.embedding(embedding)
				.columns(columns)
				.relatedTables(relatedTables)
				.build();

			redisVectorRepository.save(metadata);
			log.info("테이블 메타데이터 생성: {}", tableName);

		} catch (Exception e) {
			log.error("테이블 메타데이터 생성 실패: {}", tableName, e);
		}
	}
}
