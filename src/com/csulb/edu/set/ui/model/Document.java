package com.csulb.edu.set.ui.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Document {

	private StringProperty documentName;
	private FloatProperty docScore;

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
	public Document(String documentName, Float docScore) {
		this.documentName = new SimpleStringProperty(documentName);
		this.docScore = new SimpleFloatProperty(docScore);
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
	public Float getDocScore() {
		return docScore.get();
	}

	/**
	 * @param docScore the docScore to set
	 */
	public void setDocScore(FloatProperty docScore) {
		this.docScore = docScore;
	}
	
	/**
	 * 
	 * @return
	 */
	public FloatProperty documentScoreProperty() {
		return docScore;
	}
}