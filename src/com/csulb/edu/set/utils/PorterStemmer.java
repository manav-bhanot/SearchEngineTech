package com.csulb.edu.set.utils;

import java.util.regex.Pattern;

public class PorterStemmer {

	// a single consonant
	private static final String c = "[^aeiou]";
	// a single vowel
	private static final String v = "[aeiouy]";

	// a sequence of consonants; the second/third/etc consonant cannot be 'y'
	private static final String C = c + "[^aeiouy]*";
	// a sequence of vowels; the second/third/etc cannot be 'y'
	private static final String V = v + "[aeiou]*";

	// this regex pattern tests if the token has measure > 0 [at least one VC].
	private static final Pattern mGr0 = Pattern.compile("^(" + C + ")?" + "(" + V + C + ")+");

	// add more Pattern variables for the following patterns:
	// m equals 1: token has measure == 1
	// m greater than 1: token has measure > 1
	// vowel: token has a vowel after the first (optional) C
	// double consonant: token ends in two consonants that are the same,
	// unless they are L, S, or Z. (look up "backreferencing" to help
	// with this)
	// m equals 1, cvc: token is in Cvc form, where the last c is not w, x,
	// or y.
	private static final Pattern mEq1 = Pattern.compile("^(" + C + ")?" + "(" + V + C + "){1}");
	private static final Pattern mGr1 = Pattern.compile("^(" + C + ")?" + "(" + V + C + "){2,}");
	private static final Pattern vowel = Pattern.compile("^(" + C + ")?" + V + ".+");
	private static final Pattern doubleC = Pattern.compile(".+([^lsz])\\1{1}");
	private static final Pattern Cvc = Pattern.compile("^(" + C + "){1}" + v + "[^aeiouwxy]");

	public static String processToken(String token) {
		if (token.length() <= 3) {
			return token; // token must be at least 3 chars
		}
		// step 1a
		if (token.endsWith("sses") || token.endsWith("ies")) {
			token = token.substring(0, token.length() - 2);
		} else if (token.endsWith("s") && !token.endsWith("ss")) {
			token = token.substring(0, token.length() - 1);
		}
		// program the other steps in 1a.
		// note that Step 1a.3 implies that there is only a single 's' as the
		// suffix; ss does not count. you may need a regex pattern here for
		// "not s followed by s".

		// step 1b
		boolean doStep1bb = false;
		// step 1b
		if (token.endsWith("eed")) { // 1b.1
			// token.substring(0, token.length() - 3) is the stem prior to
			// "eed".
			// if that has m>0, then remove the "d".
			String stem = token.substring(0, token.length() - 3);
			if (mGr0.matcher(stem).matches()) { // if the pattern matches the
												// stem
				token = stem + "ee";
			}
		} else if (token.endsWith("ed")) {
			String stem = token.substring(0, token.length() - 2);
			if (vowel.matcher(stem).matches()) {
				token = stem;
				doStep1bb = true;
			}
		} else if (token.endsWith("ing")) {
			String stem = token.substring(0, token.length() - 3);
			if (vowel.matcher(stem).matches()) {
				token = stem;
				doStep1bb = true;
			}
		}
		// program the rest of 1b. set the boolean doStep1bb to true if Step 1b*
		// should be performed.

		// step 1b*, only if the 1b.2 or 1b.3 were performed.
		if (doStep1bb) {
			if (token.endsWith("at") || token.endsWith("bl") || token.endsWith("iz")
					|| (Cvc).matcher(token).matches()) {
				token = token + "e";
				// use the regex patterns you wrote for 1b*.4 and 1b*.5
			} else if (doubleC.matcher(token).matches()) {
				token = token.substring(0, token.length() - 1);
			}
		}

		// step 1c
		// program this step. test the suffix of 'y' first, then test the
		// condition *v* on the stem.
		if (token.endsWith("y")) {
			String stem = token.substring(0, token.length() - 1);
			if (vowel.matcher(stem).matches()) {
				token = stem + "i";
			}
		}

		// step 2
		// program this step. for each suffix, see if the token ends in the
		// suffix.
		// * if it does, extract the stem, and do NOT test any other suffix.
		// * take the stem and make sure it has m > 0.
		// * if it does, complete the step and do not test any others.
		// if it does not, attempt the next suffix.

		// you may want to write a helper method for this. a matrix of
		// "suffix"/"replacement" pairs might be helpful. It could look like
		// string[][] step2pairs = { new string[] {"ational", "ate"},
		// new string[] {"tional", "tion"}, ....

		String[][] step2pairs = { new String[] { "ational", "ate" }, new String[] { "tional", "tion" },
				new String[] { "enci", "ence" }, new String[] { "anci", "ance" }, new String[] { "izer", "ize" },
				new String[] { "abli", "able" }, new String[] { "alli", "al" }, new String[] { "entli", "ent" },
				new String[] { "eli", "e" }, new String[] { "ousli", "ous" }, new String[] { "ization", "ize" },
				new String[] { "ation", "ate" }, new String[] { "ator", "ate" }, new String[] { "alism", "al" },
				new String[] { "iveness", "ive" }, new String[] { "fulness", "ful" }, new String[] { "ousness", "ous" },
				new String[] { "aliti", "al" }, new String[] { "iviti", "ive" }, new String[] { "biliti", "ble" } };

		token = replaceString(token, step2pairs, mGr0);
		// step 3
		// program this step. the rules are identical to step 2 and you can use
		// the same helper method. you may also want a matrix here.
		String[][] step3pairs = { new String[] { "icate", "ic" }, new String[] { "ative", "" },
				new String[] { "alize", "al" }, new String[] { "iciti", "ic" }, new String[] { "ical", "ic" },
				new String[] { "ful", "" }, new String[] { "ness", "" } };

		token = replaceString(token, step3pairs, mGr0);

		// step 4
		// program this step similar to step 2/3, except now the stem must have
		// measure > 1.
		// note that ION should only be removed if the suffix is SION or TION,
		// which would leave the S or T.
		// as before, if one suffix matches, do not try any others even if the
		// stem does not have measure > 1.
		String[][] step4pairs = { new String[] { "al", "" }, new String[] { "ance", "" }, new String[] { "ence", "" },
				new String[] { "er", "" }, new String[] { "ic", "" }, new String[] { "alble", "" },
				new String[] { "ible", "" }, new String[] { "ant", "" }, new String[] { "ement", "" },
				new String[] { "ment", "" }, new String[] { "ent", "" }, new String[] { "sion", "s" },
				new String[] { "tion", "t" }, new String[] { "ou", "" }, new String[] { "ism", "" },
				new String[] { "ate", "" }, new String[] { "iti", "" }, new String[] { "ous", "" },
				new String[] { "ive", "" }, new String[] { "ize", "" } };

		token = replaceString(token, step4pairs, mGr1);

		// step 5
		// program this step. you have a regex for m=1 and for "Cvc", which
		// you can use to see if m=1 and NOT Cvc.
		// all your code should change the variable token, which represents
		// the stemmed term for the token.
		if (token.endsWith("e")) {
			String stem = token.substring(0, token.length() - 1);
			if (mGr1.matcher(stem).matches() || (mEq1.matcher(stem).matches() && !Cvc.matcher(stem).matches())) {
				token = stem;
			}
		}

		if (token.endsWith("ll")) {
			String stem = token.substring(0, token.length() - 2);
			if (mGr1.matcher(token).matches()) { // use token not stem here
				token = token.substring(0, token.length() - 1);
			}
		}

		return token;
	}

	private static String replaceString(String token, String[][] pairs, Pattern pattern) {
		for (String[] pair : pairs) {
			if (token.endsWith(pair[0])) {
				String stem = token.substring(0, token.length() - pair[0].length());
				if (pattern.matcher(stem).matches()) {
					token = stem + pair[1];
					break;
				}
			}
		}

		return token;
	}

	public static void main(String[] args) {
		// System.out.println(mGr1);
		String[] strings = { "cats", "feed", "agreed", "plastered", "bled", "motoring", "sing", "conflated", "troubled",
				"sized", "hopping", "tanned", "falling", "hissing", "hizzed", "failing", "filing", "happy", "sky",
				"probate", "rate", "cease", "controll", "roll", "activate", "replicate", "rational", "organization",
				"organize", "organizer", "really", "reed", "red", "argument" };
		for (String string : strings) {
			System.out.println(PorterStemmer.processToken(string));
		}

	}
}
