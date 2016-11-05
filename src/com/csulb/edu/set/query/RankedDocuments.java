/**
 * 
 */
package com.csulb.edu.set.query;

public class RankedDocuments implements Comparable<RankedDocuments> {
	
	private int documentId;
	
	private float scoreAccumulator;

	public RankedDocuments(int documentId) {
		this.documentId = documentId;
	}

	public RankedDocuments(int documentId, float score) {
		this.documentId = documentId;
		this.scoreAccumulator = score;
	}

	/**
	 * @return the documentId
	 */
	public int getDocumentId() {
		return documentId;
	}

	/**
	 * @param documentId the documentId to set
	 */
	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	/**
	 * @return the scoreAccumulator
	 */
	public float getScoreAccumulator() {
		return scoreAccumulator;
	}

	/**
	 * @param scoreAccumulator the scoreAccumulator to set
	 */
	public void setScoreAccumulator(float scoreAccumulator) {
		this.scoreAccumulator = scoreAccumulator;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int compareTo(RankedDocuments o) {
		if (this.scoreAccumulator < o.scoreAccumulator) {
			return 1;
		} else if (this.scoreAccumulator > o.scoreAccumulator) {
			return -1;
		} else {
			return 0;
		}
	}
}
