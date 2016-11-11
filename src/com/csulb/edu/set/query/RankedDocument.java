/**
 * 
 */
package com.csulb.edu.set.query;

public class RankedDocument implements Comparable<RankedDocument> {	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int documentId;
	
	private double scoreAccumulator;

	public RankedDocument(int documentId) {
		this.documentId = documentId;
	}

	public RankedDocument(int documentId, double score) {
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
	public double getScoreAccumulator() {
		return scoreAccumulator;
	}

	/**
	 * @param scoreAccumulator the scoreAccumulator to set
	 */
	public void setScoreAccumulator(double scoreAccumulator) {
		this.scoreAccumulator = scoreAccumulator;
	}


	@Override
	public int compareTo(RankedDocument o) {
		if (this.scoreAccumulator < o.scoreAccumulator) {
			return 1;
		} else if (this.scoreAccumulator > o.scoreAccumulator) {
			return -1;
		} else {
			return 0;
		}
	}
}
