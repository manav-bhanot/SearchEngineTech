package com.csulb.edu.set.indexes.diskindex;

public enum DiskIndexEnum {
	POSITIONAL_INDEX("positionalVocab.bin", "positionalVocabTable.bin", "positionalPostings.bin"),
	BI_WORD_INDEX("biWordVocab.bin", "biWordVocabTable.bin", "biWordPostings.bin");
	
	private String vocabFileName;
	private String vocabTableFileName;
	private String postingsFileName;
	
	DiskIndexEnum(String vocabFileName, String vocabTableFileName, String postingsFileName) {
		this.vocabFileName = vocabFileName;
		this.vocabTableFileName = vocabTableFileName;
		this.postingsFileName = postingsFileName;
	}

	public String getVocabFileName() {
		return vocabFileName;
	}

	public String getVocabTableFileName() {
		return vocabTableFileName;
	}

	public String getPostingsFileName() {
		return postingsFileName;
	}

	
};
