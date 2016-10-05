package com.csulb.edu.set.query;

import java.util.List;

/**
 * Each Query object represent a Qi in query input Q1 + Q2 + ... + Qk
 *
 */
public class Query {

	List<QueryLiteral> queryLiterals;

	public List<QueryLiteral> getQueryLiterals() {
		return queryLiterals;
	}

	public void setQueryLiterals(List<QueryLiteral> queryLiterals) {
		this.queryLiterals = queryLiterals;
	}

}
