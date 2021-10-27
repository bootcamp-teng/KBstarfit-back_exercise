package com.starfit.exercise.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AllRankList {
	private List<Rank> rankList;
	private int myExerAmt;
	private int myRank;
	private String myName;
}
