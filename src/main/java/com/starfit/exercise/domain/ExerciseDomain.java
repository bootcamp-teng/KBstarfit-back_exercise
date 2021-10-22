package com.starfit.exercise.domain;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.starfit.exercise.data.ExerciseRepository;
import com.starfit.exercise.model.ExerciseHistory;

@Service
public class ExerciseDomain {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ExerciseRepository exerciseRepo;
	
	public ResponseEntity<String> insertExer(ExerciseHistory exercise) throws Exception {
		log.info("Start db insert");
		ExerciseHistory re  = exerciseRepo.save(exercise);
		log.debug("result :"+ re);
		
		return new ResponseEntity<String> (re+"", HttpStatus.OK);
	}

	public ResponseEntity<String> updateExer(ExerciseHistory exer) throws Exception {
		log.info("Start db update==>"+exer.getId());
		Optional<ExerciseHistory> entity = exerciseRepo.findById(exer.getId());
		
		if(entity.isPresent()) {
			entity.get().setExerAmt(exer.getExerAmt());
			entity.get().setDate(exer.getDate());
			entity.get().setTotal(exer.getTotal());
			entity.get().setUserGoalId(exer.getUserGoalId());
			entity.get().setUserId(exer.getUserId());
			ExerciseHistory re  = exerciseRepo.save(exer);
		}

		
		log.debug("result :"+ entity);
		
		return new ResponseEntity<String> (entity+"", HttpStatus.OK);
	}

	public ResponseEntity<List<ExerciseHistory>> getexerList(int userId) {
		List<ExerciseHistory> re = null;
		try {
			log.info("Start db select");
			re = exerciseRepo.findByuserId(userId);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<List<ExerciseHistory>> (re, HttpStatus.OK);
	}

	public ResponseEntity<Optional<ExerciseHistory>> getExer(Long Id) throws Exception {
		Optional<ExerciseHistory> re = null;
		try {
			log.info("Start db select");
			re = exerciseRepo.findById(Id);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Optional<ExerciseHistory>> (re, HttpStatus.OK);
	}
}
