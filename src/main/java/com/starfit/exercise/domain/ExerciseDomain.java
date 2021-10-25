package com.starfit.exercise.domain;

import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starfit.exercise.data.ExerciseRepository;
import com.starfit.exercise.model.ExerciseHistory;


@Service
public class ExerciseDomain {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	//private static RestTemplate restTemplate = getRestTemplate();
	
	@Autowired
	private ExerciseRepository exerciseRepo;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public ResponseEntity<String> insertExer(ExerciseHistory exercise) throws Exception {
		// TODO : resttemplate 함수로 빼기
		
		log.info("Start insertExer");
		ResponseEntity<String> userGoal = getRestTemplate().exchange(
			            "http://teng.169.56.174.139.nip.io/starfitgoal/v1/usergoalsbyid/"+exercise.getUserId(),
			            HttpMethod.GET,
			            null,
			            String.class);
		
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
		list = objectMapper.readValue(userGoal.getBody(),ArrayList.class);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		int userGoalId = 0;
		int period=0;
		int dayExerAmt=0;
		String endDateString = null;
		//LocalDateTime endDate=null;
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
				break;
			} 
		}
		
		if(endDateString==null) return new ResponseEntity<String> ("진행중인 목표가 있습니다", HttpStatus.CONTINUE);


		exercise.setUserGoalId(userGoalId);
		ExerciseHistory maxDateExerHist = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		maxDateExerHist = exerciseRepo.selectMaxDateByUserId(exercise.getUserId(), exercise.getUserGoalId());
		Optional<ExerciseHistory> maxDateExer = Optional.ofNullable(maxDateExerHist);

		LocalDateTime date = LocalDateTime.now(); 
		String currDateString = date.format(formatter);
		//String maxDateString = maxDateExerHist.getDate().format(formatter);
		String maxDateString = Optional.ofNullable(maxDateExerHist.getDate().format(formatter)).orElse("");
		//String endDateString = endDate.format(formatter);
		
		Date curr = sdf.parse(currDateString);
		Date end = sdf.parse(endDateString);
		
	    if(curr.after(end)){
	    	HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);

	    	JSONObject updateJsonObject = new JSONObject();
	        updateJsonObject.put("userGoalId", userGoalId);
	        updateJsonObject.put("statusCode", "1");
	        
	    	HttpEntity<String> request = new HttpEntity<String>(updateJsonObject.toString(), headers);
	    	
			ResponseEntity<String> updateResult = getRestTemplate().exchange(
		            "http://teng.169.56.174.139.nip.io/starfitgoal/v1/usergoal",
		            HttpMethod.PUT,
		            request,
		            String.class);
			if (updateResult.getStatusCode().isError()) throw new ResponseStatusException(HttpStatus.CONTINUE, "업데이트 실패");
			
			log.info("업데이트 결과 : {}",updateResult.getStatusCode().toString());
			
	    	return new ResponseEntity<String> ("끝난 목표입니다. 새로운 목표를 등록해주세요.", HttpStatus.CONTINUE);
	    }

		int prevExer = 0;
		if(currDateString.equals(maxDateString)) {
			exercise.setId(maxDateExerHist.getId());
			prevExer = maxDateExerHist.getExerAmt();
		}
		
		
		if (prevExer < dayExerAmt && exercise.getExerAmt() >= dayExerAmt) {
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        JSONObject insertJsonObject = new JSONObject();
	        insertJsonObject.put("userId", exercise.getUserId());
	        // TODO : stdPoint 가져오기
	        int stdPoint = 10;
	        insertJsonObject.put("point", stdPoint);
	        insertJsonObject.put("description", "일일 목표 달성");
	        HttpEntity<String> request = new HttpEntity<String>(insertJsonObject.toString(), headers);
	        ResponseEntity<String> updateResult = getRestTemplate().exchange(
		            "http://teng.169.56.174.139.nip.io/starfitpoint/v1/point",
		            HttpMethod.POST,
		            request,
		            String.class);
			if (updateResult.getStatusCode().isError()) throw new ResponseStatusException(HttpStatus.CONTINUE, "업데이트 실패");		
			
		}
		
		exercise.setDate(date);
		ExerciseHistory re  = exerciseRepo.save(exercise);
		log.debug("result :"+ re);
		
		List<ExerciseHistory> exerHistList = exerciseRepo.findByUserIdAndUserGoalId(exercise.getUserId(), userGoalId);
		
		int exerAmtTotal = 0; 
		for(ExerciseHistory exerHist : exerHistList) {
			exerAmtTotal += exerHist.getExerAmt();
		}
		
		if(exerAmtTotal >= period*dayExerAmt) {
	    	HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        JSONObject updateJsonObject = new JSONObject();
	        System.out.println(userGoalId);
	        updateJsonObject.put("id", userGoalId);
	        updateJsonObject.put("statusCode", "1");
	    	HttpEntity<String> request = new HttpEntity<String>(updateJsonObject.toString(), headers);
			ResponseEntity<String> updateResult = getRestTemplate().exchange(
		            "http://teng.169.56.174.139.nip.io/starfitgoal/v1/usergoal",
		            HttpMethod.PUT,
		            request,
		            String.class);
			if (updateResult.getStatusCode().isError()) throw new ResponseStatusException(HttpStatus.CONTINUE, "업데이트 실패");			
//			{
//				  "date": "2021-10-25T07:11:19.891Z",
//				  "description": "string",
//				  "id": 0,
//				  "point": 0,
//				  "userId": 0
//				}
//			
			// 포인트 업데이트
	    	headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        JSONObject insertJsonObject = new JSONObject();
	        insertJsonObject.put("userId", exercise.getUserId());
	        // TODO : stdPoint 가져오기
	        int stdPoint = 10;
	        insertJsonObject.put("point", stdPoint*period);
	        insertJsonObject.put("description", "목표 달성 완료");
	    	request = new HttpEntity<String>(insertJsonObject.toString(), headers);
			updateResult = getRestTemplate().exchange(
		            "http://teng.169.56.174.139.nip.io/starfitpoint/v1/point",
		            HttpMethod.POST,
		            request,
		            String.class);
			if (updateResult.getStatusCode().isError()) throw new ResponseStatusException(HttpStatus.CONTINUE, "업데이트 실패");			
		}
		
		
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
	@Bean
	public RestTemplate getRestTemplate(){
	    return new RestTemplate();
	}
}
