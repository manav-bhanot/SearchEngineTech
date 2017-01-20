package edu.csulb.set.documentclassifier;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class DocumentVector extends ArrayList<Double> {
	
	private Integer documentId;
	private double ld;
	
	public DocumentVector() {
		super();
	}
	
	public DocumentVector(int length) {
		super(length);
	}

	/**
	 * @return the documentId
	 */
	public Integer getDocumentId() {
		return documentId;
	}
	
	/**
	 * @param documentId the documentId to set
	 */
	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}



	/**
	 * @return the ld
	 */
	public double getLd() {
		return ld;
	}

	/**
	 * @param ld the ld to set
	 */
	public void setLd(double ld) {
		this.ld = ld;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DocumentVector add(DocumentVector docVector) {

		if (!(this instanceof DocumentVector)) {
			// throws an invalid documentVectorException
			return null;
		}
		for (int i = 0; i < docVector.size(); i++) {
			this.set(i, this.get(i) + docVector.get(i));
		}

		return this;
	}

	public void normalizeVector() {

		if (!(this instanceof DocumentVector)) {
			// throws an invalid documentVectorException
			return;
		}
		
		for (int i = 0; i < this.size(); i++) {
			this.set(i, this.get(i) / Math.sqrt(this.getLd()));
		}
		
		/*DoubleStream ds = this.parallelStream().mapToDouble(i -> i / Math.sqrt(this.getLd()));
		ds.coll*/
	}
	

	public static double findEuclidianDistance(DocumentVector v1, DocumentVector v2) {
		
		double sum = 0;
		for (int i=0; i < v1.size(); i++) {
			sum += Math.pow((v1.get(i) - v2.get(i)), 2);
		}
		
		return Math.sqrt(sum);
	}

}
