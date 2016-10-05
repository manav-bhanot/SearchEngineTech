package com.csulb.edu.set.ui.model;

import java.util.ArrayList;
import java.util.List;

public class Result {

	private List<Document> documents = new ArrayList<Document>();

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public Result() {
	}

	/**
	 * 
	 * @param documents
	 */
	public Result(List<Document> documents) {
		this.documents = documents;
	}

	/**
	 * 
	 * @return The documents
	 */
	public List<Document> getDocuments() {
		return documents;
	}

	/**
	 * 
	 * @param documents
	 *            The documents
	 */
	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	/*@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(documents).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if ((other instanceof Result) == false) {
			return false;
		}
		Result rhs = ((Result) other);
		return new EqualsBuilder().append(documents, rhs.documents).isEquals();
	}
*/
}
