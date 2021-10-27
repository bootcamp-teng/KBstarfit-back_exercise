package com.starfit.exercise.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.starfit.exercise.domain.ExerciseDomain;
import com.starfit.exercise.model.AllRankList;
import com.starfit.exercise.model.ExerciseHistory;

@Service
public class ExerciseService {
	@Autowired
	private ExerciseDomain exerDomain;
//	
//	@Autowired
//	private TestDomain testDomain;
	
	public ResponseEntity<String> insertExer(ExerciseHistory exer) throws Exception {
		return exerDomain.insertExer(exer);
	}

	public ResponseEntity<String> updateExer( ExerciseHistory exer) throws Exception {
		return exerDomain.updateExer(exer);
	}

	public ResponseEntity<List<ExerciseHistory>> getExerList(int userId) throws Exception {
		return exerDomain.getExerList(userId);
	}

	public ResponseEntity<Optional<ExerciseHistory>> getExer(Long Id) throws Exception {
		return exerDomain.getExer(Id);
	}

	public ResponseEntity<Optional<AllRankList>> getRank(int userId) throws Exception {
		return exerDomain.getRank(userId);
	}
	
	public ResponseEntity<List<ExerciseHistory>> getListByUserGoalId(int usergoalid) throws Exception{
		return exerDomain.getListByUserGoalId(usergoalid);
	}

	public ResponseEntity<String> deleteExer(int userGoalId) throws Exception{
			return exerDomain.deleteExer(userGoalId);
	}
		
}
	
//	public ResponseEntity <String > createTestGoals(int startGoalId, int goalCount) throws Exception { 
//		return goalDomain.createTestGoals(startGoalId, goalCount);
//	}
