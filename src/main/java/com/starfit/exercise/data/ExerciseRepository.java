package com.starfit.exercise.data;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.starfit.exercise.model.ExerciseHistory;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseHistory, Long> {
	List<ExerciseHistory> findByuserId(int userId);
	
	List<ExerciseHistory> findByUserIdAndUserGoalId(int userId, int userGoalId);
	
	List<ExerciseHistory> findByUserGoalId(int userGoalId);
	
	@Query(value= "select eh from ExerciseHistory eh where eh.userId = :userId"
	+ " and date = (select max(date) from ExerciseHistory where userGoalId= :userGoalId)")
	ExerciseHistory selectMaxDateByUserId(@Param("userId")int userId, @Param("userGoalId")int userGoalId);
	
	@Query(value= "select eh from ExerciseHistory eh where  DATE_FORMAT(date, '%Y-%m-%d') = :yesterday"
			+ " order by exerAmt desc")
	List<ExerciseHistory> selectYesterRank(@Param("yesterday") String yesterday);
	
	Long deleteByUserGoalId(int userGoalId);

//	@Query(value = "select id, max(date) as date, exerAmt, total, userGoalId, userId from ExerciseHistory eh where eh.userId = :userId and userGoalId= :userGoalId")
//	ExerciseHistory findMaxDateByUserId(@Param("userId")int userId, @Param("userGoalId")int userGoalId);

//	@Query(value = "select id, to_char(date, 'YYYY-MM-DD'), exerAmt, total, userGoalId, userId from ExerciseHistory eh where eh.userId = :userId and userGoalId= :userGoalId order by date desc")
//	List<ExerciseHistory> selectMaxDateByUserId(int userGoalId);
//	
//	
//	public ExerciseHistory findMaxDateByUserId(@Param("userId")int userId, @Param("userGoalId")int userGoalId);

	
}
