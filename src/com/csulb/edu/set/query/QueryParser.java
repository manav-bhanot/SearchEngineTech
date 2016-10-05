package com.csulb.edu.set.query;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.csulb.edu.set.exception.InvalidQueryException;
import com.csulb.edu.set.utils.Utils;

/**
 * Seach Query Parser.
 * 
 * Definitions: 
 * Query list/query input: user queries in the form of Q1 + Q2 + ... + Qk
 * query: each Qi in query list, a sequence of query literals seperated
 * by white space
 * query literal: a single token or a phrase
 * phrase: a sequence of single tokens that are within double quotes
 */
public class QueryParser {

	// a single token can consist of any characters except double quotes and space
	private static final String singleToken = "[^\" ]+";
	private static final String phrase = "-?\"(" + singleToken + " +)*(" + singleToken + ")\"";
	private static final String literal = singleToken + "|" + phrase;
	private static final String queries = "((" + literal + ") +)*" + "(" + literal + ")";

	/**
	 * Parse query input into a list of Query objects.
	 * 
	 * @param queryInput: user queries in the form of Q1 + Q2 + ... + Qk
	 * @return a List of Query objects
	 * @throws InvalidQueryException
	 */
	public static List<Query> parseQuery(String queryInput) throws InvalidQueryException {
		
		// throw exception if queries are invalid
		if (!queryInput.matches(queries)) {
			throw new InvalidQueryException();
		}

		List<Query> queryList = new ArrayList<Query>();
		// remove extra spaces and split query input by ' + '
		String[] queryStrings = queryInput.replaceAll(" {2,}", " ").split(" \\+ ");
		for (String queryString : queryStrings) {
			// parse each query
			Query query = new Query();
			List<QueryLiteral> queryLiterals = new LinkedList<QueryLiteral>();
			// split query by space to get query literals
			String[] literalStrings = queryString.split(" ");

			int i = 0;
			while (i < literalStrings.length) {
				// parse each query literal
				QueryLiteral queryLiteral = new QueryLiteral();
				queryLiteral.setTokens(new ArrayList<String>());

				// Checking if this token has a '-' sign in front of it in which
				// case it will be a NOT query
				if (literalStrings[i].startsWith("-")) {
					queryLiteral.setPositive(false);
					// remove "-"
					literalStrings[i] = literalStrings[i].substring(1).trim();
				} else {
					queryLiteral.setPositive(true);
				}

				// Checks if the token is actually a phrase.
				// It's a phrase if it is enclosed within " "
				if (literalStrings[i].startsWith("\"")) {
					queryLiteral.setPhrase(true);
					if (literalStrings[i].endsWith("\"")) {
						// phrase with only one token
						queryLiteral.getTokens()
								.add(Utils.processWord(literalStrings[i].substring(1, literalStrings[i].length() - 1)));
					} else {
						// phrase with more than one tokens
						queryLiteral.getTokens().add(Utils.processWord(literalStrings[i].substring(1)));
						i++;

						while (!literalStrings[i].endsWith("\"")) {
							queryLiteral.getTokens().add(Utils.processWord(literalStrings[i]));
							i++;
						}

						// Adding the last token of the phrase to the list of tokens
						queryLiteral.getTokens()
								.add(Utils.processWord(literalStrings[i].substring(0, literalStrings[i].length() - 1)));
					}
					i++;
				} else {
					queryLiteral.setPhrase(false);
					queryLiteral.getTokens().add(Utils.processWord(literalStrings[i]));
					i++;
				}

				// add query literal to the front of the list if positive, end
				// of the list otherwise
				if (queryLiteral.isPositive()) {
					queryLiterals.add(0, queryLiteral);
				} else {
					queryLiterals.add(queryLiteral);
				}
			}

			// throw exception if there isn't at least one positive literal
			if (!queryLiterals.get(0).isPositive()) {
				throw new InvalidQueryException();
			}
			query.setQueryLiterals(queryLiterals);
			queryList.add(query);
		}

		return queryList;
	}
}
