package com.csulb.edu.set.indexes.diskindex;

import java.util.List;
import java.util.Scanner;

import com.csulb.edu.set.indexes.pii.PositionalPosting;
import com.csulb.edu.set.utils.PorterStemmer;

public class DiskEngine {

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);

		System.out.println("Menu:");
		System.out.println("1) Build index");
		System.out.println("2) Read and query index");
		System.out.println("Choose a selection:");
		int menuChoice = scan.nextInt();
		scan.nextLine();

		switch (menuChoice) {
		case 1:
			System.out.println("Enter the name of a directory to index: ");
			String folder = scan.nextLine();

			IndexWriter writer = new IndexWriter(folder);
			writer.buildIndex();
			break;

		case 2:
			System.out.println("Enter the name of an index to read:");
			String indexName = scan.nextLine();

			DiskInvertedIndex index = new DiskInvertedIndex(indexName);

			while (true) {
				System.out.println("Enter one or more search terms, separated " + "by spaces:");
				String input = scan.nextLine();

				if (input.equals("EXIT")) {
					break;
				}

				List<PositionalPosting> postingsList = index.getPostings(PorterStemmer.processToken(input.toLowerCase()));
				int[] docIds = new int[postingsList.size()];
				
				int i=0;
				for (PositionalPosting posting : postingsList) {
					docIds[i] = posting.getDocumentId();
					i++;
				}
				
				System.out.print("Docs: ");
				for (int post : docIds) {
					System.out.print(index.getFileNames().get(post) + " ");
				}
				System.out.println();
				System.out.println();
			}

			break;
		}
	}
}
