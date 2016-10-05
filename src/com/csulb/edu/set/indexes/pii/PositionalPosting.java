package com.csulb.edu.set.indexes.pii;

import java.util.ArrayList;
import java.util.List;

/**
 * PositionalPosting class
 * 
 * The objects of this class are used for the creation of PositionalInvertedIndex.
 * This maps a documentId to the list of all the positions of a particular term in the document
 *
 */
public class PositionalPosting {
	
	// stores the documentId
	private int documentId;
	
	// stores ths list of postions
	private List<Integer> positions;	

	public PositionalPosting() {
		this.positions = new ArrayList<Integer>();
	}
	
	public PositionalPosting(int documentId, List<Integer> positions) {
		this.documentId = documentId;
		this.positions = positions;
	}

	public int getDocumentId() {
		return this.documentId;
	}

	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	public List<Integer> getPositions() {
		return this.positions;
	}

	public void setPositions(List<Integer> positions) {
		this.positions = positions;
	}
}
