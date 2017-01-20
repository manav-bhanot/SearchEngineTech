/**
 * 
 */
package edu.csulb.set.documentclassifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.csulb.set.indexes.pii.PositionalPosting;

/**
 * 
 *
 */
public class ClassifyDocuments {

	// Create all the directory paths
	String allDocs = "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\ALL";
	String hamiltonDocs = "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\HAMILTON";
	String jayDocs = "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\JAY";
	String madisonDocs = "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\MADISON";
	// String hamiltonAndMadison =
	// "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\HAMILTON
	// AND MADISON";

	String toBeClassified = "C:\\Users\\Manav\\Documents\\CECS-529_Search_Engine_Technology\\Project\\Federalist_ByAuthors\\HAMILTON OR MADISON";

	List<String> fileNames = new ArrayList<String>();

	// Now index all the documents in the ALL folder
	PositionalInvertedIndex pInvertedIndex;

	Set<String> setOfHamiltonDocs;
	Set<String> setOfMadisonDocs;
	Set<String> setOfJayDocs;

	String[] corpusVocab;

	/**
	 * 1. Index the documents 
	 * 2. Calculate wdt
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		ClassifyDocuments docsClassification = new ClassifyDocuments();

		// Create the invertedIndex
		docsClassification.createIndex();

		// List<DocumentVector> docVectorsList = new
		// ArrayList<DocumentVector>(85);

		// Call doRocchioClassification
		docsClassification.doRocchioClassification();

		// Call doBayesianClassification

	}

	private void createIndex() {
		// Now index all the documents in the ALL folder
		pInvertedIndex = new PositionalInvertedIndex();

		/**
		 * Indexing the documents in the ALL folder. Currently the ALL folder
		 * contains a total of 74 document items
		 */
		PositionalInvertedIndex.createIndex(allDocs, pInvertedIndex, fileNames, 0);

		/**
		 * Indexing the documents in the HAMILTON OR MADISON folder. Adding it
		 * to the same index This folder has a total of 11 document items
		 */
		PositionalInvertedIndex.createIndex(toBeClassified, pInvertedIndex, fileNames, fileNames.size());

		corpusVocab = pInvertedIndex.getDictionary();

		setOfHamiltonDocs = pInvertedIndex.populateAlreadyClassifiedDocsList(hamiltonDocs);
		setOfMadisonDocs = pInvertedIndex.populateAlreadyClassifiedDocsList(madisonDocs);
		setOfJayDocs = pInvertedIndex.populateAlreadyClassifiedDocsList(jayDocs);
	}

	private void doRocchioClassification() {

		// Create the document vectors of all the 85 documents available		
		// List<DocumentVector> docVectorsList = new ArrayList<DocumentVector>(85);

		List<DocumentVector> docVectorsList = Stream.generate(DocumentVector::new).limit(85)
				.collect(Collectors.toList());		

		for (int i = 0; i < corpusVocab.length; i++) {

			for (PositionalPosting pPosting : pInvertedIndex.getPostings(corpusVocab[i])) {

				DocumentVector docVector = docVectorsList.get(pPosting.getDocumentId());
				if (docVector.getDocumentId() == null) {
					
					// Creates a T-dimensional document vector having all the vector components initialized to 0.0
					docVector.addAll(Collections.nCopies(corpusVocab.length, 0.0));
					
					// Set the documentId of this document to the particular docId to which this vector belongs
					docVector.setDocumentId(pPosting.getDocumentId());
				}
				double wdt = 1 + Math.log(pPosting.getPositions().size());
				docVector.set(i, wdt);

				docVector.setLd(docVector.getLd() + Math.pow(wdt, 2));
			}
		}

		DocumentClass hamiltonDocClass = new DocumentClass("H");
		DocumentClass madisonDocClass = new DocumentClass("M");
		DocumentClass jayDocClass = new DocumentClass("J");

		List<DocumentVector> docsToBeClassified = new ArrayList<DocumentVector>();

		// Normalize each of the document vectors by the Euclidian distance Ld
		// Add the normalized vector to their corresponding classes
		for (DocumentVector documentVector : docVectorsList) {

			documentVector.normalizeVector();

			boolean isDocClassified = false;

			if (setOfHamiltonDocs.contains(fileNames.get(documentVector.getDocumentId()))) {
				hamiltonDocClass.getDocVectors().add(documentVector);
				isDocClassified = true;
			}
			if (setOfJayDocs.contains(fileNames.get(documentVector.getDocumentId()))) {
				jayDocClass.getDocVectors().add(documentVector);
				isDocClassified = true;
			}
			if (setOfMadisonDocs.contains(fileNames.get(documentVector.getDocumentId()))) {
				madisonDocClass.getDocVectors().add(documentVector);
				isDocClassified = true;
			}

			if (!isDocClassified) {
				docsToBeClassified.add(documentVector);
			}
		}

		// Initialize the centroid vector of all the three classes to 0.0
		hamiltonDocClass.getCentroid().addAll(Collections.nCopies(corpusVocab.length, 0.0));
		madisonDocClass.getCentroid().addAll(Collections.nCopies(corpusVocab.length, 0.0));
		jayDocClass.getCentroid().addAll(Collections.nCopies(corpusVocab.length, 0.0));
		
		// Find the centroid of all the three classes of documents
		hamiltonDocClass.calculateCentroid();
		madisonDocClass.calculateCentroid();
		jayDocClass.calculateCentroid();

		for (DocumentVector toBeClassifiedDocVector : docsToBeClassified) {
			double distanceFromHClass = DocumentVector.findEuclidianDistance(toBeClassifiedDocVector,
					hamiltonDocClass.getCentroid());
			double distanceFromMClass = DocumentVector.findEuclidianDistance(toBeClassifiedDocVector,
					madisonDocClass.getCentroid());
			// double distanceFromJClass =
			// DocumentVector.findEuclidianDistance(toBeClassifiedDocVector,
			// jayDocClass.getCentroid());

			if (distanceFromHClass < distanceFromMClass) {
				System.out.println("Document : " + fileNames.get(toBeClassifiedDocVector.getDocumentId())
						+ " : belongs to Hamilton class");
			} else if (distanceFromMClass < distanceFromHClass) {
				System.out.println("Document : " + fileNames.get(toBeClassifiedDocVector.getDocumentId())
						+ " : belongs to Madison class");
			} else {
				System.out.println("Document : " + fileNames.get(toBeClassifiedDocVector.getDocumentId())
						+ " : belongs to both Hamilton and Madison class");
			}
		}

	}

	private void doBayesianClassification() {

	}
}
