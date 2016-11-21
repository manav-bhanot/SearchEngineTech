package edu.csulb.set.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import edu.csulb.set.indexes.SimpleTokenStream;
import edu.csulb.set.indexes.TokenStream;

/**
 * Contains the general utility methods used by the application.
 * All the methods in the class are declared static so as to be called easily
 *
 */
public class Utils {

	// Regex to remove the special chars from the beginning of the word
	private static final String specialCharsRegexStart = "^[^\\w*]*";
	
	// Regex to remove the special chars from the end of the word
	private static final String specialCharsRegexEnd = "[^\\w*]*$";
	
	// Created a json parser for parsing json files
	private static final JsonParser jsonParser = new JsonParser();	
	
	//private static final Gson gson = new GsonBuilder().create();

	/**
	 * Processes the word. Removes all the special characters at the beginning and at the end of the word
	 * @param token
	 * @return
	 */
	public static String processWord(String word, boolean removeHyphens) {
		word = word.trim().replaceAll(specialCharsRegexStart, "").replaceAll(specialCharsRegexEnd, "").replaceAll("'", "");
		if (removeHyphens) word = word.replaceAll("-", "");
		return word.toLowerCase();
	}

	/**
	 * Removes the hyphens present in the word
	 * @param token
	 * @return
	 */
	public static String removeHyphens(String word) {
		return word.replaceAll("-", "").toLowerCase();
	}

	
	/**
	 * Processes the json files and creates a token stream on the body element's value of the json and return it to the caller
	 * 
	 * @param jsonFile
	 * @return
	 */
	public static TokenStream getTokenStreams(InputStream jsonFile) {
		JsonReader reader = new JsonReader(new InputStreamReader(jsonFile));
		return new SimpleTokenStream(jsonParser.parse(reader).getAsJsonObject().get("body").getAsString());		
	}
	

	/**
	 * Gets the contents of the body element to be displayed on the screen
	 * The method is called when the user double clicks on the file name to display the contents of the file on the window
	 * @param docLocation
	 * @return
	 */
	public static String getDocumentText(String docLocation) {
		
		try {
			return jsonParser.parse(new InputStreamReader(new FileInputStream(docLocation))).getAsJsonObject().get("body").getAsString();
		} catch (JsonIOException e1) {
			e1.printStackTrace();
		} catch (JsonSyntaxException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		return "Unable to read the json file "+docLocation;
	}
	
	public static List<String> readFileNames(String indexName) {
		try {
			final List<String> names = new ArrayList<String>();
			final Path currentWorkingPath = Paths.get(indexName).toAbsolutePath();

			Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {

				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					// make sure we only process the current working directory
					if (currentWorkingPath.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					// only process .txt files
					if (file.toString().endsWith(".json")) {
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
}
