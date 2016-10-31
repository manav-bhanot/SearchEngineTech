package com.csulb.edu.set.indexes.diskindex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

import com.csulb.edu.set.indexes.Index;
import com.csulb.edu.set.indexes.pii.PositionalPosting;

public class DiskInvertedIndex extends Index<PositionalPosting> {

	private String mPath;
	private RandomAccessFile mVocabList;
	private RandomAccessFile mPostings;
	private long[] mVocabTable;
	private List<String> mFileNames;

	public DiskInvertedIndex(String path) {
		try {
			mPath = path;
			mVocabList = new RandomAccessFile(new File(path, "vocab.bin"), "r");
			mPostings = new RandomAccessFile(new File(path, "postings.bin"), "r");
			mVocabTable = readVocabTable(path);
			mFileNames = readFileNames(path);
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		}
	}

	public List<PositionalPosting> getPostings(String term) {
		long postingsPosition = binarySearchVocabulary(term);
		if (postingsPosition >= 0) {
			return readPostingsFromFile(mPostings, postingsPosition);
		}
		return null;
	}

	private static List<PositionalPosting> readPostingsFromFile(RandomAccessFile postings, long postingsPosition) {
		try {
			// initialize the array that will hold the postings.
			List<PositionalPosting> docList = new ArrayList<PositionalPosting>();

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
			byte positionsBuffer[] = new byte[4];

			for (int docIdIndex = 0; docIdIndex < documentFrequency; docIdIndex++) {

				// Reads the 4 bytes of the docId into docIdsBuffer
				postings.read(docIdsBuffer, 0, docIdsBuffer.length);

				// Convert the byte representation of the docId into the integer
				// representation
				// Current docId is the difference between the lastDocId and the
				// currentDocId
				// So add the lastDocId to the current number read from the
				// postings file to get the currentDocId
				docId = ByteBuffer.wrap(docIdsBuffer).getInt() + lastDocId;
				
				// Next 8 bytes is the document weight corresponding to the 
				postings.skipBytes(8);
				
				// Allocate a buffer for the 4 byte term frequency value
				buffer = new byte[4];
				
				// Read the term frequency
				postings.read(buffer, 0, buffer.length);
				int termFreq = ByteBuffer.wrap(buffer).getInt();

				// Create a positions list storing the position of each occurence of this term in this document
				int[] positions = new int[termFreq];
				
				// Iterate through the postings file and get the positions of this term into the positions array
				for (int positionIndex = 0; positionIndex < termFreq; positionIndex++) {
					postings.read(positionsBuffer, 0, positionsBuffer.length);
					positions[positionIndex] = ByteBuffer.wrap(positionsBuffer).getInt();
				}

				lastDocId = docId;
				PositionalPosting positionalPosting = new PositionalPosting(docId,
						Arrays.stream(positions).boxed().collect(Collectors.toList()));

				docList.add(positionalPosting);
			}
			return docList;
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	/**
	 * Search the vocab.bin file for the corresponding term.
	 * 
	 * @param term
	 * @return
	 */
	private long binarySearchVocabulary(String term) {
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

	private static List<String> readFileNames(String indexName) {
		try {
			final List<String> names = new ArrayList<String>();
			final Path currentWorkingPath = Paths.get(indexName).toAbsolutePath();

			Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
				int mDocumentID = 0;

				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					// make sure we only process the current working directory
					if (currentWorkingPath.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					// only process .txt files
					if (file.toString().endsWith(".txt")) {
						names.add(file.toFile().getName());
					}
					return FileVisitResult.CONTINUE;
				}

				// don't throw exceptions if files are locked/other errors occur
				public FileVisitResult visitFileFailed(Path file, IOException e) {

					return FileVisitResult.CONTINUE;
				}

			});
			return names;
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	private static long[] readVocabTable(String indexName) {
		try {
			long[] vocabTable;

			RandomAccessFile tableFile = new RandomAccessFile(new File(indexName, "vocabTable.bin"), "r");

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

			// That's why the size should be multiplied by 2
			vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];

			int tableIndex = 0;

			byteBuffer = new byte[8];

			while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { // while
																			// we
																			// keep
																			// reading
																			// 8
																			// bytes
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

	public List<String> getFileNames() {
		return mFileNames;
	}

	public int getTermCount() {
		return mVocabTable.length / 2;
	}
}
