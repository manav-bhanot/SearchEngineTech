package com.csulb.edu.set.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.csulb.edu.set.exception.InvalidQueryException;
import com.csulb.edu.set.indexes.Index;
import com.csulb.edu.set.indexes.diskindex.DiskInvertedIndex;
import com.csulb.edu.set.indexes.kgram.KGramIndex;
import com.csulb.edu.set.indexes.pii.PositionalPosting;
import com.csulb.edu.set.utils.PorterStemmer;
import com.csulb.edu.set.utils.Utils;

/**
 * Run Queries using a positional inverted index and a bi-word index
 *
 */
public class QueryRunner {

	/**
	 * Parse query input into Query objects and execute the queries
	 * 
	 * @param queryInput
	 *            query input
	 * @param invertedIndex
	 *            positional inverted index
	 * @param biWordIndex
	 *            bi-word index
	 * @return a list of document ids that match the queries
	 * @throws InvalidQueryException
	 *             when query input is invalid
	 */
	public static List<Integer> runBooleanQueries(String queryInput, Index<PositionalPosting> invertedIndex,
			Index<Integer> biWordIndex, KGramIndex kGramIndex) throws InvalidQueryException {
		System.out.println("Running the query");
		List<Integer> docIds = new ArrayList<Integer>();
		// parse query input into a list of query objects
		List<Query> queries = QueryParser.parseBooleanQuery(queryInput);

		for (Query query : queries) {
			// get the union of the results returned from each individual query
			// (Qi)
			docIds = getUnion(docIds, getdocIdsMatchingQuery(query, invertedIndex, biWordIndex, kGramIndex));
		}

		return docIds;
	}
	
	
	public static List<RankedDocuments> runRankedQueries(String queryInput, Index<PositionalPosting> invertedIndex,
			Index<Integer> biWordIndex, KGramIndex kGramIndex) throws InvalidQueryException {
		System.out.println("Running the query");
		
		int k = 10;
		
		PriorityQueue<RankedDocuments> pQueue = new PriorityQueue<RankedDocuments>();
		
		List<RankedDocuments> rankedDocumentsList = new ArrayList<RankedDocuments>();
		
		DiskInvertedIndex diskInvertedIndex = (DiskInvertedIndex) invertedIndex;
		
		// parse query input into a list of query objects
		List<Query> queries = QueryParser.parseBooleanQuery(queryInput);

		Map<Integer, RankedDocuments> rankedDocs = new HashMap<Integer, RankedDocuments>();
		
		for (Query query : queries) {
			 for (QueryLiteral queryLiterals : query.getQueryLiterals()) {
				 for (String term : queryLiterals.getTokens()) {
					 
					 List<PositionalPosting> termPostingsList = diskInvertedIndex.getPostings(term);
					 
					 // Calculate wqt for this term
					 double wqt = Math.log((1 + (diskInvertedIndex.getFileNames().size() / termPostingsList.size())));
					 
					 for (PositionalPosting pPosting : termPostingsList) {
						 float newScore = 0f;
						 RankedDocuments rankedDoc = null;
						 if (rankedDocs.containsKey(pPosting.getDocumentId())) {
							 rankedDoc = rankedDocs.get(pPosting.getDocumentId());
							 newScore = (float) (rankedDoc.getScoreAccumulator() + (wqt * pPosting.getWdt()));
							 rankedDoc.setScoreAccumulator(newScore);							 
						 } else {							 
							 newScore =  (float) (wqt * pPosting.getWdt());
							 rankedDoc = new RankedDocuments(pPosting.getDocumentId(), newScore);
						 }
						 rankedDocs.put(pPosting.getDocumentId(), rankedDoc);
						 //pPosting.setScoreAccumulator(pPosting.getScoreAccumulator() + (wqt * pPosting.getWdt()));
					 }					 
				 }
			 }
		}
		
		// Sort the ranked documents in the decreasing order
		for (RankedDocuments rd : rankedDocs.values()) {
			pQueue.add(rd);
		}
		
		// Return the top k documents;
		for (int i=0; i < k; i++) {
			rankedDocumentsList.add(pQueue.poll());
		}

		return rankedDocumentsList;
	}

	/**
	 * Get document IDs that match the given individual query Qi
	 * 
	 * The overall idea is to get the doc IDs matching each query literal and
	 * get the union of the results (or difference for a negative literal).
	 * Bi-word index is used for phrases of size-2. Positional Inverted Index is
	 * used for single tokens.
	 * 
	 * There is a special algorithm to find doc IDs matching phrases of size
	 * greater than 2: for the ith word in the phrase, we find all the
	 * positional postings, each of which include a doc ID and a list of
	 * positions, and minus each position by i. Then we return a document id
	 * only if all of the words in the phrase match the document id and there is
	 * have at least one position that all the words share in their postings.
	 * 
	 * @param query
	 *            Query object
	 * @param invertedIndex
	 *            positional inverted index
	 * @param biWordIndex
	 *            bi-word index
	 * @return a list of document IDs that match the query
	 */
	private static List<Integer> getdocIdsMatchingQuery(Query query, Index<PositionalPosting> invertedIndex,
			Index<Integer> biWordIndex, KGramIndex kGramIndex) {
		// final results
		List<Integer> results = new ArrayList<Integer>();
		
		for (QueryLiteral queryLiteral : query.getQueryLiterals()) {
			// docIds that match the current query literal that is being
			// processed
			List<Integer> docIds = new ArrayList<Integer>();
			if (!queryLiteral.isPhrase()) {
				String word = queryLiteral.getTokens().get(0);
				String wordRegex = word.replace("*", ".*");
				if (word.contains("*")) {
					// use K-Gram Index and positional inverted index
					if (word.charAt(0) != '*'){
						word = '$' + word;
					}
					if (word.charAt(word.length() - 1) != '*'){
						word = word + '$';
					}
					String[] sequences = word.split("\\*");
					Set<String> candidates = new HashSet<String>();
					for (String sequence : sequences) {
						if (sequence.length() > 3) {
							for (int i = 0; i < sequence.length() - 3; i++) {
							    String substr = sequence.substring(i, i+3);
							    for (String candidate : kGramIndex.get(substr)){
							    	// do post filter
							    	if (candidate.matches(wordRegex)) candidates.add(candidate);
							    }
							}
						} else if (sequence.length() > 0) {
							for (String candidate : kGramIndex.get(sequence)){
						    	// do post filter
						    	if (candidate.matches(wordRegex)) candidates.add(candidate);
						    }
						}
					}
					// find positional postings of all candidates and OR the results
					for (String candidate : candidates) {
						List<Integer> candidateDocIds = new ArrayList<Integer>();
						List<PositionalPosting> positionalPostings = invertedIndex.getPostings(PorterStemmer
								.processToken(Utils.processWord(candidate, true)));
						if (positionalPostings != null) {
							for (PositionalPosting positionalPosting : positionalPostings) {
								// add the docId in each posting to docIds
								candidateDocIds.add(positionalPosting.getDocumentId());
								// TODO: remove duplicate docIds
							}
						}
						docIds = getUnion(docIds, candidateDocIds);
					}	
				} else {
					// use positional inverted index for single tokens
					// get all the postings that match the token
					List<PositionalPosting> positionalPostings = invertedIndex.getPostings(PorterStemmer
							.processToken(word));
					if (positionalPostings != null) {
						for (PositionalPosting positionalPosting : positionalPostings) {
							// add the docId in each posting to docIds
							docIds.add(positionalPosting.getDocumentId());
						}
					}
				}
			} else if (queryLiteral.isPhrase() && queryLiteral.getTokens().size() == 2) {
				// use bi-word index for 2-word-phrases
				List<Integer> postings = biWordIndex.getPostings(
						PorterStemmer.processToken(queryLiteral.getTokens().get(0)) + PorterStemmer
								.processToken(queryLiteral.getTokens().get(1)));
				if (postings != null) {
					docIds.addAll(postings);
				}
			} else {
				// use positional inverted index for other phrases
				// postings: postings that match all the query literals that
				// have been processed
				List<PositionalPosting> postings = new ArrayList<PositionalPosting>();
				for (int i = 0; i < queryLiteral.getTokens().size(); i++) {
					String token = queryLiteral.getTokens().get(i);
					// currentPostings: postings that match the query literals
					// that is currently being processed
					List<PositionalPosting> currentPostings = invertedIndex
							.getPostings(PorterStemmer.processToken(Utils.removeHyphens(token)));
					if (currentPostings == null) {
						// no possible results
						break;
					}

					if (postings.isEmpty()) {
						// when we are processing the first literal
						postings = currentPostings;
					} else {
						// Use two pointers, j and k, to point to postings and
						// currentPostings, respectively. Increment the pointers
						// until they point to postings with the same
						// documentID, and repeat.
						int j = 0, k = 0;
						List<PositionalPosting> newPostings = new ArrayList<PositionalPosting>();
						while (j < postings.size() && k < currentPostings.size()) {
							if (postings.get(j).getDocumentId() < currentPostings.get(k).getDocumentId()) {
								j++;
							} else if (postings.get(j).getDocumentId() > currentPostings.get(k).getDocumentId()) {
								k++;
							} else {
								// if postings have the same documentId
								List<Integer> postingsPositions = postings.get(j).getPositions();
								List<Integer> currentPostingsPositions = new ArrayList<Integer>(
										currentPostings.get(k).getPositions());
								for (int l = 0; l < currentPostingsPositions.size(); l++) {
									// minus each position in
									// currentPostingsPositions by i
									currentPostingsPositions.set(l, currentPostingsPositions.get(l) - i);
								}
								// get the intersection of the positions
								List<Integer> newPostingsPositions = getIntersection(postingsPositions,
										currentPostingsPositions);
								if (!newPostingsPositions.isEmpty()) {
									// create a new PositionalPosting object and
									// save it in newPostings
									newPostings.add(new PositionalPosting(postings.get(j).getDocumentId(),
											newPostingsPositions));
								}
								j++;
								k++;
							}
						}
						postings = newPostings;
					}
				}
				for (PositionalPosting posting : postings) {
					// add document id in each posting
					docIds.add(posting.getDocumentId());
				}
			}

			if (results.isEmpty()) {
				results = docIds;
			} else {
				if (queryLiteral.isPositive()) {
					// get the intersection of existing results and result of
					// the current query literal and if query literal is
					// positive
					results = getIntersection(results, docIds);
				} else {
					// get the difference of existing results and result of
					// the current query literal and if query literal is
					// negative
					// a negative query literal cannot be the first literal in a query
					results = getDifference(results, docIds);
				}
			}
		}

		return results;
	}

	/**
	 * Get the union of two sorted integer lists.
	 * 
	 * @param intListA sorted integer list
	 * @param intListB sorted integer list
	 * @return integer list that has the union of two lists
	 */
	private static List<Integer> getUnion(List<Integer> intListA, List<Integer> intListB) {
		int i = 0, j = 0;
		List<Integer> results = new ArrayList<Integer>();
		while (i < intListA.size() && j < intListB.size()) {
			if (intListA.get(i) < intListB.get(j)) {
				results.add(intListA.get(i++));
			} else if (intListA.get(i) > intListB.get(j)) {
				results.add(intListB.get(j++));
			} else {
				results.add(intListA.get(i++));
				j++;
			}
		}
		while (i < intListA.size()) {
			results.add(intListA.get(i++));
		}
		while (j < intListB.size()) {
			results.add(intListB.get(j++));
		}

		return results;
	}

	/**
	 * Get the intersection of two sorted integer lists.
	 * 
	 * @param intListA sorted integer list
	 * @param intListB sorted integer list
	 * @return integer list that has the intersection of two lists
	 */
	private static List<Integer> getIntersection(List<Integer> intListA, List<Integer> intListB) {
		int i = 0, j = 0;
		List<Integer> results = new ArrayList<Integer>();
		while (i < intListA.size() && j < intListB.size()) {
			if (intListA.get(i) < intListB.get(j)) {
				i++;
			} else if (intListA.get(i) > intListB.get(j)) {
				j++;
			} else {
				results.add(intListA.get(i++));
				j++;
			}
		}

		return results;
	}

	/**
	 * Get the difference of two sorted integer lists.
	 * 
	 * @param intListA sorted integer list
	 * @param intListB sorted integer list
	 * @return integer list that has the difference of two lists
	 */
	private static List<Integer> getDifference(List<Integer> intListA, List<Integer> intListB) {
		int i = 0, j = 0;
		List<Integer> results = new ArrayList<Integer>();
		while (i < intListA.size() && j < intListB.size()) {
			if (intListA.get(i) < intListB.get(j)) {
				results.add(intListA.get(i++));
			} else if (intListA.get(i) > intListB.get(j)) {
				j++;
			} else {
				i++;
				j++;
			}
		}

		while (i < intListA.size()) {
			results.add(intListA.get(i++));
		}

		return results;
	}

}
