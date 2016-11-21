package edu.csulb.set.indexes.pii;

import java.util.ArrayList;
import java.util.List;

import edu.csulb.set.indexes.Index;

/**
 * A positional inverted index created from the corpus terms
 * This index maps each term of the corpus to the list of documents. Each entry of the document contains a list of
 * all the positions in the document where the word exists
 *
 */
public class PositionalInvertedIndex extends Index<PositionalPosting> {

	public PositionalInvertedIndex() {
		super();
	}

	/**
	 * Accepts the term to be indexed alongwith is position in the document and documentId
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

			// Gets the current list of the positional posting associated with this term.
			List<PositionalPosting> positionalPostingList = index.get(term);
			
			// Gets the last posting object containing the document id and the list of positions
			PositionalPosting lastPosting = positionalPostingList.get(positionalPostingList.size() - 1);

			// Checks if the current documentId matches the last indexed document for this term
			// If yes, then appends the current position of this term in the last document added
			// If no adds this document in the document list along with the position of this term
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
}
