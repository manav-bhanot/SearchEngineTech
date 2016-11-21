package edu.csulb.set.indexes.pii;

import java.util.ArrayList;
import java.util.List;

/**
 * PositionalPosting class
 * 
 * The objects of this class are used for the creation of
 * PositionalInvertedIndex. This maps a documentId to the list of all the
 * positions of a particular term in the document
 *
 */
public class PositionalPosting {

	// stores the documentId
	private int documentId;

	// stores ths list of postions
	private List<Integer> positions;
	
	// stores the wdt weights of this document corresponding to the term in which posting's list this document exist
	private double wdt;
	
	public PositionalPosting() {
		this.positions = new ArrayList<Integer>();
	}
	
	public PositionalPosting(int documentId, List<Integer> positions) {
		this.documentId = documentId;
		this.positions = positions;
	}
	
	/**
	 */
	public PositionalPosting(int documentId, List<Integer> positions, double wdt) {
		super();
		this.documentId = documentId;
		this.positions = positions;
		this.wdt = wdt;
	}
	
	@Override
	public String toString() {
		return "PositionalPosting [documentId=" + documentId + ", positions=" + positions + "]";
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

	/**
	 * @return the wdt
	 */
	public double getWdt() {
		return wdt;
	}

	/**
	 * @param wdt the wdt to set
	 */
	public void setWdt(double wdt) {
		this.wdt = wdt;
	}
}
