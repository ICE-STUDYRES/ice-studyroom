// package com.ice.studyroom.domain.user.domain.entity;
//
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
//
// import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
// import com.ice.studyroom.global.entity.BaseTimeEntity;
//
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.OneToMany;
// import jakarta.persistence.Table;
// import lombok.AccessLevel;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
//
// @Entity
// @Table(name = "user")
// @Getter
// @NoArgsConstructor(access = AccessLevel.PROTECTED)
// public class User extends BaseTimeEntity {
// 	@Id
// 	@GeneratedValue(strategy = GenerationType.IDENTITY)
// 	private Long id;
//
// 	@Column(name = "user_name", nullable = false)
// 	private String userName;
//
// 	@Column(name = "join_date", nullable = false)
// 	private LocalDateTime joinDate;
//
// 	@Column(name = "cancel_chance", nullable = false)
// 	private int cancelChance;
//
// 	@OneToMany(mappedBy = "user")
// 	private List<Reservation> reservations = new ArrayList<>();
//
// 	@Builder
// 	public User(String userName) {
// 		this.userName = userName;
// 		this.joinDate = LocalDateTime.now();
// 		this.cancelChance = 3;
// 	}
// }
