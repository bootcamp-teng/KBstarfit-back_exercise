package com.starfit.exercise.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.starfit.exercise.model.AllRankList;
import com.starfit.exercise.model.ExerciseHistory;
import com.starfit.exercise.service.ExerciseService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="Exercise API")
@RestController
public class UserController {
	@Autowired
	private ExerciseService exerService;
	
	@GetMapping("/v1/exerciselist/{userId}")
	@ApiOperation(value="운동 리스트 조회하기")
	public ResponseEntity <List<ExerciseHistory>> getExerList(
				@PathVariable(name="userId", required = true) int userId
			) throws Exception {
		return exerService.getExerList(userId);
	}
	
	@GetMapping("/v1/exercise/{Id}")
	@ApiOperation(value = "운동 상세 조회")
	public ResponseEntity <Optional<ExerciseHistory>> getExer(
			@PathVariable(name="Id", required = true) Long Id
		) throws Exception {
	return exerService.getExer(Id);
	}
	
	@PostMapping("/v1/exercise")
	@ApiOperation(value="운동 추가하기")
	public ResponseEntity<String> insertUser(
			@RequestBody ExerciseHistory exer
			) throws Exception {
			return exerService.insertExer(exer);
	}
	
	@DeleteMapping("/v1/exercise/{userGoalId}")
	@ApiOperation(value="운동 삭제하기")
	public ResponseEntity<String> deleteUser(
			@PathVariable(name="userGoalId", required = true) int userGoalId
			) throws Exception {
			return exerService.deleteExer(userGoalId);
	}	
	
	@PutMapping("/v1/exercise")
	@ApiOperation(value="운동 정보 수정하기")
	public ResponseEntity<String> updateUser(
			@RequestBody ExerciseHistory exer
			) throws Exception{
		return exerService.updateExer(exer);
	}
	@GetMapping("/v1/exercisebygoal/{UserGoalId}")
	@ApiOperation(value = "운동 히스토리 조회")
	public ResponseEntity <List<ExerciseHistory>> getExerListByGoal(
			@PathVariable(name="UserGoalId", required = true) int UserGoalId
			) throws Exception{
		return exerService.getListByUserGoalId(UserGoalId);
	}
//	@GetMapping("/createtestusers/{startUserId}/{userCount}")
//	@ApiOperation(value="테스트 사용자를 userCount명 등록하기 ")
//	public ResponseEntity <String > createTestUsers(
//			@PathVariable (name="startUserId", required = true) int startUserId,
//			@PathVariable (name="userCount", required = true) int userCount
//		) throws Exception { 
//		
//		return userService.createTestUsers(startUserId, userCount);
//	}	
	
	@GetMapping("/v1/rank/{userId}")
	@ApiOperation(value = "랭킹 조회(+userId 등수)")
	public ResponseEntity <Optional<AllRankList>> getRank(
			@PathVariable(name="userId", required = true) int userId
		) throws Exception {
	return exerService.getRank(userId);
	}	
	
}
