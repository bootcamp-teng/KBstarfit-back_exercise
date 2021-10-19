package com.starfit.exercise.model;


import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity(name="ExerciseHistory")
public class ExerciseHistory {
	public ExerciseHistory() {
		super();
	}
	public ExerciseHistory(Long id, int amount, int userId, int usergoalId, LocalDateTime date, int total) {
		super();
		this.id = id;
		this.amount = amount;
		this.userId = userId;
		this.usergoalId = usergoalId;
		this.date = date;
		this.total = total;
	}
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // id 는 ai pk이다.
	private Long id; 			// 운동 아이디 (pk)
	private int amount; 		// 운동량
	private int userId; 		// 회원 아이디(fk).
	private int usergoalId; 	// 회원별목표 아이디(fk)
	private LocalDateTime date; // 날짜
	private int total; 			// 운동 총량
}
