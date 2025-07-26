package com.ice.studyroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaAuditing
@EnableAsync
@SpringBootApplication
public class IceStudyRoomApplication {
	public static void main(String[] args) {
		SpringApplication.run(IceStudyRoomApplication.class, args);
	}
}
