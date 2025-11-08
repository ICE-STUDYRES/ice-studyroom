package com.ice.studyroom.domain.texttosql.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sql_example")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SqlExample {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 500)
	private String userQuery;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String correctSql;

	@Column(nullable = false, length = 50)
	private String category;

	@Column(length = 1000)
	private String description;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	public SqlExample(String userQuery, String correctSql, String category, String description) {
		this.userQuery = userQuery;
		this.correctSql = correctSql;
		this.category = category;
		this.description = description;
	}

	public static SqlExample of(String userQuery, String correctSql, String category, String description) {
		return new SqlExample(userQuery, correctSql, category, description);
	}
}
