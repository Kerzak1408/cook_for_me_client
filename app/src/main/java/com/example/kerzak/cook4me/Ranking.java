package com.example.kerzak.cook4me;

import java.util.HashMap;
import java.util.List;

public class Ranking {
	// Key: who, Value: comment itself
	private HashMap<String, String> comments;
	// Key: who, Value: stars
	private HashMap<String, Integer> rankings;
	private float ranking;

	public Ranking(HashMap<String, String> comments, HashMap<String, Integer> rankings, float ranking) {
		this.comments = comments;
		this.rankings = rankings;
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

	public HashMap<String, Integer> getRankings() {
		return rankings;
	}

	public void setRankings(HashMap<String, Integer> rankings) {
		this.rankings = rankings;
	}

}

