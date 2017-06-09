package com.example.kerzak.cook4me.DataStructures;

import java.util.HashMap;

public class Ranking {
	// Key: who, Value: comment itself
	private HashMap<String, String> comments;
	private float ranking;
	
	public Ranking(HashMap<String, String> comments, float ranking) {
		this.comments = comments;
		this.ranking = ranking;
	}
	
	public HashMap<String, String> getComments() {
		return comments;
	}
	public void setComments(HashMap<String, String> comments) {
		this.comments = comments;
	}
	public float getRanking() {
		return ranking;
	}
	public void setRanking(float ranking) {
		this.ranking = ranking;
	}
	
}
