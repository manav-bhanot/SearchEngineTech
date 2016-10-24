package com.csulb.edu.set.indexes.kgram;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.csulb.edu.set.utils.Utils;

/**
 * This class maps the generated 1,2,3 grams of the tokens to the corresponding tokens.
 */
public class KGramIndex extends HashMap<String, SortedSet<String>> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3417192736882759969L;

	public void processToken(String word) {
		
		// Do initial processing on this word which removes the special characters from the beginning and the end of the string
		word = Utils.processWord(word);
		
		/**
		 * Check if the length of the token is greater than 2
		 * We create all the 1-,2- and 3- grams if the length of the token >= 2, otherwise we only create 1-gram
		 */
		if (word.length() > 1) {
			// Append $ to mark the beginning and end of the string
			String kGramString = '$' + word + '$';
			
			// Put the word in all the corresponding k-grams
			for (int i = 0; i < word.length() ; i++) {
				
				// Get the one gram and add the word to the corresponding oneGram list
				String oneGram = word.charAt(i)+"";
				checkAndPopulateKGrams(oneGram, word);
				
				// Get the two gram and add the word to the corresponding twoGram list
				String twoGram = kGramString.substring(i, i+2);
				checkAndPopulateKGrams(twoGram, word);
				
				// Get the three gram and add the word to the corresponding threeGram list
				String threeGram = kGramString.substring(i, i+3);
				checkAndPopulateKGrams(threeGram, word);
			}
			
			// The above loop does not process the last 2-gram of this word.
			// The below line process that last 2-gram
			checkAndPopulateKGrams(kGramString.substring(kGramString.length()-2), word);
		} else if (word.length() == 1) {			
			if (!this.containsKey(word)) {
				checkAndPopulateKGrams(word, word);
				checkAndPopulateKGrams('$' + word, word);
				checkAndPopulateKGrams(word + '$', word);
			}
		}
	}

	private void checkAndPopulateKGrams(String key, String value) {
		if (this.containsKey(key)) {
			this.get(key).add(value);
		} else {
			SortedSet<String> list = new TreeSet<String>();
			list.add(value);
			this.put(key, list);
		}
	}
}
