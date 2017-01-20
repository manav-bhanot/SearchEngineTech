package edu.csulb.set.documentclassifier;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.csulb.set.indexes.Index;
import edu.csulb.set.indexes.TokenStream;
import edu.csulb.set.indexes.pii.PositionalPosting;
import edu.csulb.set.utils.Utils;

/**
 * A positional inverted index created from the corpus terms This index maps
 * each term of the corpus to the list of documents. Each entry of the document
 * contains a list of all the positions in the document where the word exists
 *
 */
public class PositionalInvertedIndex extends Index<PositionalPosting> {

	public PositionalInvertedIndex() {
		super();
	}

	public static void createIndex(String dirPath, PositionalInvertedIndex positionalInvertedIndex, List<String> fileNames, int startingDocId) {
		// Indexing the documents in the ALL folder
		try {
			Path currentWorkingPath = Paths.get(dirPath).toAbsolutePath();

			// This is our standard "walk through all .txt files" code.
			Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
				int mDocumentID = startingDocId;

				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					// make sure we only process the current working directory
					if (currentWorkingPath.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

					if (file.toString().endsWith(".txt")) {

						fileNames.add(file.getFileName().toString());

						// Get the contents of the body element of the file name
						/*InputStream in = null;
						try {
							in = new FileInputStream(file.toFile().getAbsolutePath());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}*/

						TokenStream tokenStream = null;
						try {
							tokenStream = Utils.getTokenStreams(file.toFile());
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						int position = 0;
						// String prevToken = null;
						while (tokenStream.hasNextToken()) {

							String token = Utils.processWord(tokenStream.nextToken().trim(), false);

							// Check if the token is hyphenized
							// Then index the terms = # of hyphens + 1
							/*
							 * if (token.contains("-")) { for (String term :
							 * token.split("-")) {
							 * pInvertedIndex.addTerm(PorterStemmer.processToken
							 * (Utils.processWord(term, false)), position,
							 * mDocumentID); position++; } position--; }
							 */

							/*
							 * pInvertedIndex.addTerm(PorterStemmer.processToken
							 * (Utils.removeHyphens(token)), position,
							 * mDocumentID);
							 */

							positionalInvertedIndex.addTerm(token, position, mDocumentID);
							position++;
						}
						mDocumentID++;
					}
					return FileVisitResult.CONTINUE;
				}

				// don't throw exceptions if files are locked/other errors occur
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Accepts the term to be indexed alongwith is position in the document and
	 * documentId
	 * 
	 * @param term
	 * @param pos
	 * @param documentID
	 */
	public void addTerm(String term, int pos, int documentID) {

		// Checks if there was only a single special character which got
		// removed as part of processing
		// and only the empty string "" is left
		if (term.length() == 0) {
			return;
		}

		// Checks if the term has already been indexed
		if (index.containsKey(term)) {

			// Gets the current list of the positional posting associated with
			// this term.
			List<PositionalPosting> positionalPostingList = index.get(term);

			// Gets the last posting object containing the document id and the
			// list of positions
			PositionalPosting lastPosting = positionalPostingList.get(positionalPostingList.size() - 1);

			// Checks if the current documentId matches the last indexed
			// document for this term
			// If yes, then appends the current position of this term in the
			// last document added
			// If no adds this document in the document list along with the
			// position of this term
			if (lastPosting.getDocumentId() == documentID) {
				lastPosting.getPositions().add(pos);
			} else {
				PositionalPosting newPosting = new PositionalPosting();
				newPosting.setDocumentId(documentID);
				newPosting.getPositions().add(pos);
				index.get(term).add(newPosting);
			}
		} else {
			PositionalPosting posting = new PositionalPosting();
			posting.setDocumentId(documentID);
			posting.getPositions().add(pos);

			List<PositionalPosting> postingList = new ArrayList<PositionalPosting>();
			postingList.add(posting);

			// Puts the term and its corresponding posting list in the index
			index.put(term, postingList);
		}
	}
	
	public String[] getDictionary() {
		// TO-DO: fill an array of Strings with all the keys from the hashtable.
		// Sort the array and return it.
		
		String[] dict = this.index.keySet().toArray(new String[0]);
		Arrays.sort(dict);
		
		return dict;
	}
	
	public Set<String> populateAlreadyClassifiedDocsList(String dirPath) {
		Set<String> classifiedFileNames = new HashSet<String>();
		try {
			Path currentWorkingPath = Paths.get(dirPath).toAbsolutePath();
			// This is our standard "walk through all .txt files" code.
			Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {

				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					// make sure we only process the current working directory
					if (currentWorkingPath.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

					if (file.toString().endsWith(".txt")) {
						classifiedFileNames.add(file.getFileName().toString());					}
					return FileVisitResult.CONTINUE;
				}

				// don't throw exceptions if files are locked/other errors occur
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return classifiedFileNames;
	}
}
