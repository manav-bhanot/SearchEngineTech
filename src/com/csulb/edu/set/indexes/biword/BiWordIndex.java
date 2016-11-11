package com.csulb.edu.set.indexes.biword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.csulb.edu.set.indexes.Index;

/**
 * BiWord Index 
 * This is used to search the phrase queries having two tokens inside the phrase.
 *
 */
public class BiWordIndex extends Index<Integer>{
	
	public BiWordIndex(){
		super();
	}

	/**
	 * Accepts a term and the documentId in which the term exits.
	 * The term is a combination of two tokens - "token1token2"
	 * Creates an entry in the biword index for this term
	 * If the index already exists, adds the current document id to the list of documents
	 * @param term
	 * @param documentID
	 */
	public void addTerm(String term, int documentID) {
		// add the term to the index hashtable. If the table does not have
		// an entry for the term, initialize a new ArrayList<Integer>, add the
		// docID to the list, and put it into the map. Otherwise add the docID
		// to the list that already exists in the map, but ONLY IF the list does
		// not already contain the docID.
		if (index.containsKey(term)) {
			List<Integer> docIDs = index.get(term);
			
			// Comparing the new document id only with the last document id added in the posting list till now.
			if (docIDs.get(docIDs.size() - 1) != documentID) {
				docIDs.add(documentID);
			}
		} else {
			List<Integer> docIDs = new ArrayList<Integer>();
			docIDs.add(documentID);
			index.put(term, docIDs);
		}
	}
}
