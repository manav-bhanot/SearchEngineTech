package com.csulb.edu.set.indexes.diskindex;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.csulb.edu.set.indexes.Index;

public abstract class DiskIndex<T> extends Index<T> {
	protected RandomAccessFile mVocabList;
	protected RandomAccessFile mPostings;
	protected long[] mVocabTable;
	
	public abstract List<T> getPostings(String term);
	
	/**
	 * Search the vocab.bin file for the corresponding term.
	 * 
	 * @param term
	 * @return
	 */
	protected long binarySearchVocabulary(String term) {
		// do a binary search over the vocabulary, using the vocabTable and the
		// file vocabList.
		int i = 0, j = mVocabTable.length / 2 - 1;
		while (i <= j) {
			try {
				int m = (i + j) / 2;
				long vListPosition = mVocabTable[m * 2];
				int termLength;
				if (m == mVocabTable.length / 2 - 1) {
					termLength = (int) (mVocabList.length() - mVocabTable[m * 2]);
				} else {
					termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
				}

				mVocabList.seek(vListPosition);

				byte[] buffer = new byte[termLength];
				mVocabList.read(buffer, 0, termLength);
				String fileTerm = new String(buffer, "ASCII");

				int compareValue = term.compareTo(fileTerm);
				if (compareValue == 0) {
					// found it!
					return mVocabTable[m * 2 + 1];
				} else if (compareValue < 0) {
					j = m - 1;
				} else {
					i = m + 1;
				}
			} catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}
		return -1;
	}
}
