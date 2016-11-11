package com.csulb.edu.set.indexes.diskindex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DiskBiWordIndex extends DiskIndex<Integer> {

	public DiskBiWordIndex(String path) {
		try {
			mVocabList = new RandomAccessFile(new File(path, DiskIndexEnum.BI_WORD_INDEX.getVocabFileName()), "r");
			mPostings = new RandomAccessFile(new File(path, DiskIndexEnum.BI_WORD_INDEX.getPostingsFileName()), "r");
			mVocabTable = readVocabTable(path);
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		}
	}

	public List<Integer> getPostings(String term) {
		long postingsPosition = binarySearchVocabulary(term);
		if (postingsPosition >= 0) {
			return readPostingsFromFile(mPostings, postingsPosition);
		}
		return null;
	}
	
	private static List<Integer> readPostingsFromFile(RandomAccessFile postings, long postingsPosition) {
		try {
			// initialize the array that will hold the postings.
			List<Integer> docList = new ArrayList<Integer>();

			// seek to the position in the file where the postings start.
			postings.seek(postingsPosition);

			// read the 4 bytes for the document frequency
			byte[] buffer = new byte[4];
			postings.read(buffer, 0, buffer.length);

			// use ByteBuffer to convert the 4 bytes into an int.
			int documentFrequency = ByteBuffer.wrap(buffer).getInt();

			// write the following code:
			// read 4 bytes at a time from the file, until you have read as many
			// postings as the document frequency promised.
			//
			// after each read, convert the bytes to an int posting. this value
			// is the GAP since the last posting. decode the document ID from
			// the gap and put it in the array.
			//
			// repeat until all postings are read.

			int docId = 0;
			int lastDocId = 0;

			byte docIdsBuffer[] = new byte[4];

			for (int docIdIndex = 0; docIdIndex < documentFrequency; docIdIndex++) {

				// Reads the 4 bytes of the docId into docIdsBuffer
				postings.read(docIdsBuffer, 0, docIdsBuffer.length);

				docId = ByteBuffer.wrap(docIdsBuffer).getInt() + lastDocId;
				lastDocId = docId;
				docList.add(docId);
			}
			return docList;
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}


	private static long[] readVocabTable(String indexName) {
		try {
			long[] vocabTable;

			RandomAccessFile tableFile = new RandomAccessFile(new File(indexName, DiskIndexEnum.BI_WORD_INDEX.getVocabTableFileName()), "r");

			// Creates a byte array named "byteBuffer" of size 4
			byte[] byteBuffer = new byte[4];

			// Reads the first 4 bytes of data from the vocabTable.bin file into
			// the byteBuffer array
			// Recall that while creating the vocabTable.bin file we stored the
			// size of the dictionary as the first element in the vocab table
			// Thus the first 4 bytes of data in the vocab table represents the
			// size of the corpus vocabulary
			tableFile.read(byteBuffer, 0, byteBuffer.length);

			// Initializes the size of the vocabTable long array to the value =
			// vocabSize * 2. Why ??
			// Recall that when we store the location of each term in the
			// dictionary as a 8 bytes value in the vocabTable
			// Next to each 8 byte position of the word in the vocab file, we
			// store the 8 bytes location in the postings file where the
			// postings list entry <df, docId, tf, pos1, ....> for this term
			// begins in the postings file

			// That's why the size shogetTermCountuld be multiplied by 2
			vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];

			int tableIndex = 0;

			byteBuffer = new byte[8];

			while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { 
				// while we keep reading 8 bytes
				vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
				tableIndex++;
			}
			tableFile.close();
			return vocabTable;
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}
}