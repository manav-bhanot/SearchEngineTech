package com.csulb.edu.set.ui.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Document {

	private StringProperty documentName;
	private DoubleProperty docScore;

	/**
	 * Default constructor.
	 */
	public Document() {
		this(null, null);
	}

	/**
	 * Constructor with some initial data.
	 * 
	 * @param firstName
	 * @param lastName
	 */
	public Document(String documentName, Double docScore) {
		this.documentName = new SimpleStringProperty(documentName);
		this.docScore = new SimpleDoubleProperty(docScore);
	}

	/**
	 * @return the documentName
	 */
	public String getDocumentName() {
		return documentName.get();
	}

	/**
	 * @param documentName the documentName to set
	 */
	public void setDocumentName(StringProperty documentName) {
		this.documentName = documentName;
	}
	
	/**
	 * 
	 * @return
	 */
	public StringProperty documentNameProperty() {
		return documentName;
	}

	/**
	 * @return the docScore
	 */
	public Double getDocScore() {
		return docScore.get();
	}

	/**
	 * @param docScore the docScore to set
	 */
	public void setDocScore(DoubleProperty docScore) {
		this.docScore = docScore;
	}
	
	/**
	 * 
	 * @return
	 */
	public DoubleProperty documentScoreProperty() {
		return docScore;
	}
}