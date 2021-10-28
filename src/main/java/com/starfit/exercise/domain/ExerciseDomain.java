package com.starfit.exercise.domain;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starfit.exercise.data.ExerciseRepository;
import com.starfit.exercise.model.AllRankList;
import com.starfit.exercise.model.ExerciseHistory;
import com.starfit.exercise.model.Rank;


@Service
public class ExerciseDomain {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	//private static RestTemplate restTemplate = getRestTemplate();
	
	private final String baseUrl = "http://teng.169.56.174.139.nip.io";
		
	@Autowired
	private ExerciseRepository exerciseRepo;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public ResponseEntity<String> insertExer(ExerciseHistory exercise) throws Exception {
		// TODO : resttemplate 함수로 빼기
		// "http://teng.169.56.174.139.nip.io/starfitgoal/v1/usergoalsbyid/"
		log.info("Start insertExer");
		
		ResponseEntity<String> userGoal = doRestTemplate(new JSONObject(), "/starfitgoal/v1/usergoalsbyid/"+exercise.getUserId(), HttpMethod.GET) ;
		if (userGoal.getStatusCode().isError()) throw new ResponseStatusException(HttpStatus.CREATED, "데이터를 가져오지 못했습니다.");
		
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
		
		list = objectMapper.readValue(userGoal.getBody(), ArrayList.class);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		int userGoalId = 0;
		int period=0;
		int dayExerAmt=0;
		String endDateString = null;
		//LocalDateTime endDate=null;
		log.info("findgoal");
		HashMap userGoalMap = null;
		JSONObject updateUserGoalJson = null;
		for(HashMap userGoalJson:list){
			if ("0".equals(userGoalJson.get("statusCode"))) {
				period = (int) userGoalJson.get("period");
				//userGoalId = Long.parseLong(userGoalJson.get("id").toString());
				dayExerAmt = (int) userGoalJson.get("dayExerAmt");
				userGoalId = (int) userGoalJson.get("id");
				System.out.println((userGoalJson.get("endDate").toString()).split("T")[0]);
				// endDate = LocalDateTime.parse(userGoalJson.get("endDate").toString().split("T")[0], formatter);
				endDateString = userGoalJson.get("endDate").toString().split("T")[0];
				//endDate = (LocalDateTime) userGoalJson.get("endDate");
				//System.out.println(endDate);
				userGoalMap = (HashMap) userGoalJson.clone();
				updateUserGoalJson =  new JSONObject(userGoalMap);
				break;
			} 
		}
		
		if(endDateString==null) return new ResponseEntity<String> ("진행중인 목표가 없습니다", HttpStatus.CREATED);


		exercise.setUserGoalId(userGoalId);
		ExerciseHistory maxDateExerHist = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		maxDateExerHist = exerciseRepo.selectMaxDateByUserId(exercise.getUserId(), exercise.getUserGoalId());
		Optional<ExerciseHistory> maxDateExer = Optional.ofNullable(maxDateExerHist);

		LocalDateTime date = LocalDateTime.now(); 
		String currDateString = date.format(formatter);
		//String maxDateString = maxDateExerHist.getDate().format(formatter);
		String maxDateString = maxDateExerHist==null? "" : maxDateExerHist.getDate().format(formatter);
		//String endDateString = endDate.format(formatter);
		
		Date curr = sdf.parse(currDateString);
		Date end = sdf.parse(endDateString);
		
	    if(curr.after(end)){
	    	updateUserGoalJson.put("statusCode", "1");
//	    	JSONObject updateJsonObject = new JSONObject();
//	        updateJsonObject.put("userGoalId", userGoalId);
//	        updateJsonObject.put("statusCode", "1");
	        	    	
			ResponseEntity<String> updateResult = doRestTemplate(updateUserGoalJson, "/starfitgoal/v1/usergoal", HttpMethod.PUT);
			if (updateResult.getStatusCode().isError()) throw new ResponseStatusException(HttpStatus.CREATED, "목표를 종료시키지 못했습니다.");
			return new ResponseEntity<String> ("이미 종료된 목표입니다. 새로운 목표를 등록하세요", HttpStatus.OK);
	    }

		int prevExer = 0;
		if(currDateString.equals(maxDateString)) {
			exercise.setId(maxDateExerHist.getId());
			prevExer = maxDateExerHist.getExerAmt();
		}
		
		boolean pointFlg = false;
		
		if (prevExer < dayExerAmt && exercise.getExerAmt() >= dayExerAmt) {
			JSONObject insertJsonObject = new JSONObject();
			insertJsonObject.put("userId", exercise.getUserId());
			ResponseEntity<String> stdPointEntity = doRestTemplate(new JSONObject(), "/starfitgoal/v1/goal/"+updateUserGoalJson.get("goalId"), HttpMethod.GET);
			int stdPoint = (int) (objectMapper.readValue(stdPointEntity.getBody(), JSONObject.class).get("stdPoint"));
			insertJsonObject.put("point", stdPoint);
			insertJsonObject.put("description", "일일 목표 달성");
			ResponseEntity<String> result = doRestTemplate(insertJsonObject, "/starfitpoint/v1/point", HttpMethod.POST);	
			if (result.getStatusCode().isError()) throw new ResponseStatusException(HttpStatus.CREATED, "일일 포인트를 적립하지 못했습니다.");
			pointFlg = true;
			
		}
		
		exercise.setDate(date);
		ExerciseHistory re  = exerciseRepo.save(exercise);
		log.debug("result :"+ re);
		
		List<ExerciseHistory> exerHistList = exerciseRepo.findByUserIdAndUserGoalId(exercise.getUserId(), userGoalId);
		
		int exerAmtTotal = 0; 
		for(ExerciseHistory exerHist : exerHistList) {
			exerAmtTotal += exerHist.getExerAmt();
		}
		
		boolean goalFlg = false;
		
		if(exerAmtTotal >= period*dayExerAmt) {
			updateUserGoalJson.put("statusCode", "1");
//	        JSONObject updateJsonObject = new JSONObject();
//	        System.out.println(userGoalId);
//	        updateJsonObject.put("id", userGoalId);
//	        updateJsonObject.put("statusCode", "1");
			ResponseEntity<String> updateResult = doRestTemplate(updateUserGoalJson, "/starfitgoal/v1/usergoal", HttpMethod.PUT);

	        JSONObject insertJsonObject = new JSONObject();
	        insertJsonObject.put("userId", exercise.getUserId());
			ResponseEntity<String> stdPointEntity = doRestTemplate(new JSONObject(), "/starfitgoal/v1/goal/"+updateUserGoalJson.get("goalId"), HttpMethod.GET);
			int stdPoint = (int) (objectMapper.readValue(stdPointEntity.getBody(), JSONObject.class).get("stdPoint"));
	        insertJsonObject.put("point", stdPoint*period);
	        insertJsonObject.put("description", "목표 달성 완료");
			updateResult = doRestTemplate(insertJsonObject, "/starfitpoint/v1/point", HttpMethod.POST); 
			
			if (updateResult.getStatusCode().isError()) throw new ResponseStatusException(HttpStatus.CONTINUE, "목표 달성에 따른 스타포인트를 지급하지 못했습니다.");
			goalFlg = true;
		}
		String msg = "";
		if (goalFlg) msg = "최종목표를 달성하셨습니다";
		else if (pointFlg) msg = "일일 포인트 지급이 완료되었습니다";
		else msg = "운동량이 기록되었습니다.";
			
		return new ResponseEntity<String> (msg, HttpStatus.OK);
	}
	

	private ResponseEntity<String> doRestTemplate(JSONObject jsonObject, String url, HttpMethod method) {
		log.info("jsonObject : {}" ,jsonObject.toString());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<String>(jsonObject.toString(), headers);
		ResponseEntity<String> result = getRestTemplate().exchange(
		        baseUrl + url,
		        method,
		        request,
		        String.class);
		//if (result.getStatusCode().isError()) throw new ResponseStatusException(HttpStatus.CONTINUE, "에러");
		log.info("result : {}", result.toString());
		return result;
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

	public ResponseEntity<List<ExerciseHistory>> getExerList(int userId) {
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
	
	public ResponseEntity<List<ExerciseHistory>> getListByUserGoalId(int usergoalid) throws Exception{
		List<ExerciseHistory> exerlist = null;
		try {
			log.info("Start db select");
			exerlist = exerciseRepo.findByUserGoalId(usergoalid);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<List<ExerciseHistory>> (exerlist, HttpStatus.OK);
	}
	@Bean
	public RestTemplate getRestTemplate(){
	    return new RestTemplate();
	}


	public ResponseEntity<Optional<AllRankList>> getRank(int userId) throws Exception{
		ResponseEntity<String> result = doRestTemplate(new JSONObject(), "/starfituser/v1/user/all", HttpMethod.GET);
		if (result.getStatusCode().isError()) throw new ResponseStatusException(HttpStatus.CREATED, "사용자목록을 가져오지 못했습니다");
		ArrayList<HashMap<String,Object>> list = objectMapper.readValue(result.getBody(), ArrayList.class);
		LocalDateTime date = LocalDateTime.now();
		date = date.minusDays(1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String yesterday = date.format(formatter);

		List<ExerciseHistory> rankExer = exerciseRepo.selectYesterRank(yesterday);
		if (rankExer.isEmpty()) return new ResponseEntity<Optional<AllRankList>> (Optional.ofNullable(new AllRankList()), HttpStatus.OK);
		AllRankList allRankList = new AllRankList();
		List<Rank> rankList = new ArrayList<>(); 
		int rank = 1;
		for(ExerciseHistory exer : rankExer) {
			Rank currRank = new Rank();
			List<HashMap<String,Object>> username
				= list.stream()
					.filter(t->(int) t.get("id")==exer.getUserId()) // 유저테이블 -> 유저 아이디 -> 유저 이름 불러옴
					.collect(Collectors.toList());
			if (!username.isEmpty()) {
				currRank.setName((String) username.get(0).get("name"));
				currRank.setCharacterId((int) username.get(0).get("characterId"));
			}
			currRank.setExerHist(exer);
			if (userId==exer.getUserId()) {
				allRankList.setMyRank(rank);
				allRankList.setMyExerAmt(exer.getExerAmt());
				allRankList.setMyName((String) username.get(0).get("name"));
				allRankList.setMyCharacterId((int) username.get(0).get("characterId"));
			}
			rankList.add(currRank);
			currRank.setRank(rank++);
		}
		allRankList.setRankList(rankList);
		
		return new ResponseEntity<Optional<AllRankList>> (Optional.ofNullable(allRankList), HttpStatus.OK);
	}

	@Transactional
	public ResponseEntity<String> deleteExer(int userGoalId) throws Exception {
		Long deletedRecords = exerciseRepo.deleteByUserGoalId(userGoalId);
		return new ResponseEntity<String> ("deleted items : "+deletedRecords, HttpStatus.OK);
	}
}
