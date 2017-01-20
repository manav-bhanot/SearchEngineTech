package edu.csulb.set.documentclassifier;

import java.util.ArrayList;
import java.util.List;

public class DocumentClass {

	private String name;
	private List<DocumentVector> docVectors;
	private DocumentVector centroid;
	
	public DocumentClass() {
		docVectors = new ArrayList<DocumentVector>();
		centroid = new DocumentVector();
	}

	public DocumentClass(String name) {
		this();
		this.name = name;
	}
	
	/**
	 * Receives the summed vector of a class and calculates the centroid of the class
	 * @param totalDocuments
	 * @return
	 */
	public void calculateCentroid() {
		
		// Vector addition of all the document vectors of this class
		for (DocumentVector docVector : this.docVectors) {
			this.centroid = this.centroid.add(docVector);
		}
		
		// Divide the summed vector by |Dc|
		for (int i=0; i < this.centroid.size(); i++) {
			this.centroid.set(i, this.centroid.get(i) / this.getDocVectors().size());
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the docVectors
	 */
	public List<DocumentVector> getDocVectors() {
		return docVectors;
	}

	/**
	 * @param docVectors
	 *            the docVectors to set
	 */
	public void setDocVectors(List<DocumentVector> docVectors) {
		this.docVectors = docVectors;
	}	

	/**
	 * @return the centroid
	 */
	public DocumentVector getCentroid() {
		return centroid;
	}

	/**
	 * @param centroid
	 *            the centroid to set
	 */
	public void setCentroid(DocumentVector centroid) {
		this.centroid = centroid;
	}

}
