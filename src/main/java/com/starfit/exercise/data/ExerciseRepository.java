package com.starfit.exercise.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.starfit.exercise.model.ExerciseHistory;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseHistory, Long> {
	List<ExerciseHistory> findByuserId(String userId);
}
