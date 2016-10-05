package com.csulb.edu.set.indexes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * An abstract class.
 * Extended by BiWordIndex and PositionalInvertedIndex
 * 
 *
 * @param <T>
 */
public abstract class Index<T> {

	protected HashMap<String, List<T>> index;

	public Index() {
		index = new HashMap<String, List<T>>();
	}
	
	public List<T> getPostings(String term) {
		return index.get(term);
	}

	public int getTermCount() {
		return index.keySet().size();
	}

	public String[] getDictionary() {
		String[] dict = index.keySet().toArray(new String[0]);
		Arrays.sort(dict);
		
		return dict;
	}
}
