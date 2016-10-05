package com.csulb.edu.set.query;
import java.util.List;

/**
 * A QueryLiteral Object represents either a single token or a phrase.
 *
 */
public class QueryLiteral {
	
	// if the query literal is a phrase
	private boolean isPhrase;
	// the list of tokens in the literal
	// a single token literal only has 1 item in this list
	private List<String> tokens;
	// is the query literal a positive literal
	private boolean isPositive;
	
	public boolean isPositive() {
		return isPositive;
	}

	public void setPositive(boolean isPositive) {
		this.isPositive = isPositive;
	}

	public boolean isPhrase() {
		return isPhrase;
	}

	public void setPhrase(boolean isPhrase) {
		this.isPhrase = isPhrase;
	}

	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

}