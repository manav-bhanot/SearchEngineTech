package com.csulb.edu.set.ui.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.csulb.edu.set.MainApp;
import com.csulb.edu.set.exception.InvalidQueryException;
import com.csulb.edu.set.indexes.Index;
import com.csulb.edu.set.indexes.TokenStream;
import com.csulb.edu.set.indexes.biword.BiWordIndex;
import com.csulb.edu.set.indexes.diskindex.DiskIndexWriter;
import com.csulb.edu.set.indexes.diskindex.DiskInvertedIndex;
import com.csulb.edu.set.indexes.kgram.KGramIndex;
import com.csulb.edu.set.indexes.pii.PositionalInvertedIndex;
import com.csulb.edu.set.indexes.pii.PositionalPosting;
import com.csulb.edu.set.query.QueryRunner;
import com.csulb.edu.set.utils.PorterStemmer;
import com.csulb.edu.set.utils.Utils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;

public class SearchOverviewController {

	/**
	 * Holds the documents returned as result of query
	 */
	
	// List of documents that matches the search query
	private ObservableList<String> documents;
	
	// List containing all the vocabulary terms of the corpus
	private ObservableList<String> vocab;
	
	// Flag to check if the directory entered by the user is a valid directory or not
	boolean isValidDirectory;
	
	// Stores the path of the directory
	// Used to fetch the contents of the file to be displayed on the screen
	private String dirPath;

	// The main anchorpane or the root window of the application
	@FXML
	private AnchorPane parentWindow;
	
	@FXML
	private Label corpusVocabSize;

	@FXML
	private Label numberOfDocsIndexed;
	
	@FXML
	private Label numberOfDocsMatchingQuery;

	@FXML
	private ListView<String> listView;

	@FXML
	private TextField userQuery;

	@FXML
	private Button search;

	@FXML
	private Button findStem;

	@FXML
	private Button printVocab;

	@FXML
	private TextArea jsonBodyContents;

	// Declare an object of PositionalInvertedIndex
	private Index<PositionalPosting> pInvertedIndex;

	// Declare an object of biWordIndex
	private Index<Integer> biWordIndex;
	
	//Declare an object of KGramIndex
	private KGramIndex kGramIndex;
	
	// Declare an object of Disk Inverted Index
	// private Index<PositionalPosting> diskInvertedIndex;
	
	// Declare a reference of the parent class of all the indexes
	private Index<PositionalPosting> invertedIndex;
	
	// Create a list of double value to store the document weights
	private List<Double> docWeights;
	
	// Flag to choose from DiskInvertedIndex or InMemoryIndex
	boolean useDiskIndex = true;
	
	private List<String> fileNames = new ArrayList<String>();

	// Reference to the main application.
	private MainApp mainApp;

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	/**
	 * Opens up a Text Dialog prompting the user to enter the path of the
	 * directory to index
	 */
	public void promptUserForDirectoryToIndex() {
		
		/*if (this.mainApp.getPrimaryStage().isShowing()) {
			this.search.setDisable(true);
			this.printVocab.setDisable(true);
		}*/
		
		TextInputDialog dialog = new TextInputDialog("Enter the path here");
		dialog.setTitle("Index A Directory");
		dialog.setHeaderText("Kindly enter the path of the directory to index");
		dialog.setContentText("Directory Path :");

		final ButtonType browseButton = new ButtonType("Browse", ButtonData.OTHER);
		dialog.getDialogPane().getButtonTypes().add(browseButton);

		final Button browse = (Button) dialog.getDialogPane().lookupButton(browseButton);
		browse.addEventFilter(ActionEvent.ACTION, event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setInitialDirectory(new File(System.getProperty("user.home")));
			File dir = chooser.showDialog(this.mainApp.getPrimaryStage());
			if (dir == null) {
				return;
			}
			this.dirPath = Paths.get(dir.getAbsolutePath()).toString();
			dialog.getEditor().setText(this.dirPath != null ? this.dirPath : "");
		});

		// Handles the cancel button action
		final Button cancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
		cancel.addEventFilter(ActionEvent.ACTION, event -> {
			this.isValidDirectory = true;
			if (mainApp.getPrimaryStage() != null && !mainApp.getPrimaryStage().isShowing()) {
				Platform.exit();
			}
			this.search.setDisable(false);
			this.printVocab.setDisable(false);
		});

		Optional<String> result = null;
		while (!isValidDirectory) {
			result = dialog.showAndWait();
			// The Java 8 way to get the response value (with lambda
			// expression).
			result.ifPresent(dir -> {
				if ((new File(dir).isDirectory())) {
					isValidDirectory = true;
					this.dirPath = dir;
					
					// Show a message saying indexing in progress
					//this.numberOfDocsIndexed.setText("Indexing in Progress....");
					
					// Clears the previous state of UI
					this.jsonBodyContents.clear();
					this.listView.getItems().clear();
					this.vocab.clear();
					this.documents.clear();
					
					/**
					 * TODO
					 * Checks if we already have a disk index created.
					 * If yes then tell the user that a disk index was already created alongwith the timestamp
					 * and ask him whether he wants to re-create a new index or proceed with the existing index.
					 */
					boolean isVocabFilePresent = false;
					boolean isVocabTablePresent = false;
					boolean isPostingsFilePresent = false;
					
					Path vocab = Paths.get(this.dirPath + "\\vocab.bin");
					Path postings = Paths.get(this.dirPath + "\\postings.bin");
					Path vocabTable = Paths.get(this.dirPath + "\\vocabTable.bin");
					
					if (vocab.toFile().exists() && !vocab.toFile().isDirectory() && postings.toFile().exists()
							&& !postings.toFile().isDirectory() && vocabTable.toFile().exists()
							&& !vocabTable.toFile().isDirectory()) {
						
						BasicFileAttributes attributes = null;
						try {
							attributes = Files.readAttributes(postings, BasicFileAttributes.class);
						} catch (IOException e) {
							e.printStackTrace();
						}
						// Get the creation time of this file
						FileTime creationTime = attributes.lastModifiedTime();
						
						// Prompt the user here if he want to select the disk index or the in memory index
						Alert confirmationBox = showAlertBox("The Disk index for this corpus was already created at " + creationTime + " Do you want to "
								+ "update the existing disk index ? ", AlertType.CONFIRMATION);
						Optional<ButtonType> isUserOk = confirmationBox.showAndWait();
						
						if (isUserOk.get() == ButtonType.CANCEL) {
							isVocabFilePresent = true;
							isVocabTablePresent = true;
							isPostingsFilePresent = true;
						}					
					} 
					if (!isVocabFilePresent && !isPostingsFilePresent && !isVocabTablePresent) {
						// go ahead and create the new index
						// Begin creating the index
						createIndexes(this.dirPath);
						this.numberOfDocsIndexed.setText("Total documents indexed = " + fileNames.size());
						// Displaying a message about the total number of words in the vocabulary
						this.vocab.clear();
						this.vocab.addAll(Arrays.asList(pInvertedIndex.getDictionary()));
						this.corpusVocabSize.setText("Size of Corpus Vocabulary is : " + this.vocab.size());
					}
					
					// Show the search app window 
					if (!this.mainApp.getPrimaryStage().isShowing()) {
						this.mainApp.getPrimaryStage().show();
					}
				} else {
					Alert invalidDirectoryPathAlert = showAlertBox("Invalid Directory path! Please enter a valid directory", AlertType.ERROR);
					invalidDirectoryPathAlert.showAndWait();
				}
			});
		}
		this.isValidDirectory = false;
	}

	/**
	 * Displays an error dialog box to indicate the user that the directory path
	 * entered is invalid
	 */
	private Alert showAlertBox(String msg, AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(null);
		alert.setHeaderText(null);
		alert.setContentText(msg);
		return alert;
	}

	@FXML
	private void indexNewDirectory() {
		promptUserForDirectoryToIndex();
	}

	/**
	 * Called when the user clicks on the search button.
	 */
	@FXML
	private void searchCorpus() {
		// Get the query entered by the user in the query text box in the
		// queryString variable
		String queryString = userQuery.getText();

		// Check if the user has actually entered a query. If it is blank ask
		// the user to enter a query
		if (queryString == null || queryString.isEmpty()) {
			// Nothing selected.
			Alert alert = new Alert(AlertType.WARNING);
			alert.initOwner(mainApp.getPrimaryStage());
			alert.setTitle("No Query Entered");
			alert.setHeaderText(
					"Oh come on!!! You don't want me to return every document in the universe. Do you think you have time to read all of that");
			alert.setContentText("Please enter the text that you want to search for in the corpus");
			alert.showAndWait();

			userQuery.requestFocus();
		} else {
			// Call the Query Processing API to parse the query and get the
			// tokens

			// For each token apply the Porter Stemmer Algorithm to get the stem

			// Now apply the AND, OR operations
			// Carefully check for phrase queries

			// Apply all the operations and return the list of chapters and
			// store it
			// in documentsList variable
			System.out.println("Searching for " + queryString);
			
			// Instantiates an object based on whether the user want to use DiskIndex or InMemoryIndex
			if (useDiskIndex) {				
				this.invertedIndex = new DiskInvertedIndex(this.dirPath);
//				this.kGramIndex = new KGramIndex(this.dirPath);
			} else  {
				this.invertedIndex = this.pInvertedIndex;
			}

			if (this.invertedIndex != null && this.biWordIndex != null) {
				if (!documents.isEmpty())
					documents.clear();
				try {
					List<Integer> docIds = QueryRunner.runQueries(queryString, invertedIndex, biWordIndex, kGramIndex);					
					numberOfDocsMatchingQuery.setText("Total documents found for this query = "+docIds.size());
					if (!this.numberOfDocsMatchingQuery.isVisible()) this.numberOfDocsMatchingQuery.setVisible(true);
					
					// Show an info box saying no results found
					if (docIds.isEmpty()) {
						showAlertBox("Sorry. Your search results does not fetch any documents from the corpus", AlertType.INFORMATION);
					}
					
					for (int docId : docIds) {
						documents.add(fileNames.get(docId));
						//System.out.println(fileNames.get(docId));
					}
				} catch (InvalidQueryException e) {
					// Show an Error Alert box saying the Query is invalid
					showAlertBox("Invalid Query Format. Kindly re enter the query", AlertType.ERROR);
				}
				listView.setItems(documents);
				listView.scrollTo(0);
			} else {
				/**
				 * TODO
				 * 
				 * The invertedIndex or biwordIndex could probably be null because the user choose to query the corpus 
				 * with the disk index and hence the applcation never creates an in-memory index
				 */
			}
		}
	}

	/**
	 * Prints the vocabulary i.e all the terms in the vocabulary of the corpus,
	 * one item per line
	 */
	@FXML
	private void printVocabulary() {
		// Prints all the terms in the dictionary of corpus
		this.numberOfDocsMatchingQuery.setText("");
		listView.setItems(vocab);
		listView.scrollTo(0);
	}

	/**
	 * Output the stem of the entered word in a new alert box
	 */
	@FXML
	private void findStem() {
		System.out.println("Findnig the stem");

		// Fetch the word entered by the user in the textbox
		String word = userQuery.getText();
		if (word == null) {
			// TO-DO :: Show an error box prompting user to enter a word to stem
		} else {
			Alert stemInfo = new Alert(AlertType.INFORMATION);
			stemInfo.setTitle("Finding the stem using Porter-Stemmer Algorithm");
			stemInfo.setHeaderText("Below is the stem of the word: " + word);

			// Call PorterStemmer
			String stem = PorterStemmer.processToken(word);

			stemInfo.setContentText(stem);
			stemInfo.showAndWait();
		}
	}

	@FXML
	private void onTextFieldClick() {
		userQuery.setEditable(true);
		userQuery.requestFocus();
	}
	
	/**
	 * Saving the In-Memory index on disk
	 */
	private void saveIndexesOnDisk() {
		// TODO Auto-generated method stub
		
		// Persist the PositionalInvertedIndex on disk
		DiskIndexWriter.buildIndexForDirectory(this.dirPath, this.pInvertedIndex);
		DiskIndexWriter.storeKGramIndexOnDisk(this.dirPath, this.kGramIndex);
	}

	/**
	 * Create the in-memory inverted index of the corpus
	 * @param dirPath
	 */
	public void createIndexes(String dirPath) {
		System.out.println("In Controller :: Begin creation of index");
		// Begin indexing of all the files present at the directory location

		// TO-DO (May be) :: Make this operation happen asynchronously
		// Keep the search text box disabled till the time index is created
		// If the user clicks on the search text box, show him a message saying
		// :: Index creation in progress
		try {
			System.out.println("Begin creating index at : " + Calendar.getInstance().getTime());
			this.fileNames.clear();
			this.vocab.clear();
			this.numberOfDocsMatchingQuery.setVisible(false);
			
			this.pInvertedIndex = new PositionalInvertedIndex();
			this.biWordIndex = new BiWordIndex();
			this.kGramIndex = new KGramIndex();
			
			Path currentWorkingPath = Paths.get(dirPath).toAbsolutePath();

			// This is our standard "walk through all .txt files" code.
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
					// only process .json files
					//System.out.println(file.toString());
					if (file.toString().endsWith(".json")) {
						// we have found a .json file; add its name to the fileName
						// list,
						// then index the file and increase the document ID counter.
						// System.out.println("Indexing file " +
						// file.getFileName());

						fileNames.add(file.getFileName().toString());

						// Get the contents of the body element of the file name
						InputStream in = null;
						try {
							in = new FileInputStream(file.toFile().getAbsolutePath());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						
						// Create a hashmap to count term frequency
						Map<String, Integer> termFreq = new HashMap<String, Integer>();
						
						TokenStream tokenStream = Utils.getTokenStreams(in);
						
						int position = 0;
						String prevToken = null;
						while (tokenStream.hasNextToken()) {

							String token = Utils.processWord(tokenStream.nextToken().trim(), false);
							
							// Token sent to be added in kGramIndex
							kGramIndex.processToken(token);

							// Check if the token is hyphenized
							// Then index the terms = # of hyphens + 1
							if (token.contains("-")) {
								for (String term : token.split("-")) {
									((PositionalInvertedIndex)pInvertedIndex).addTerm(PorterStemmer.processToken(Utils.processWord(term, false)), position, mDocumentID);
									position++;
								}
								position--;
							}
							((PositionalInvertedIndex)pInvertedIndex).addTerm(PorterStemmer.processToken(Utils.removeHyphens(token)), position,
									mDocumentID);
							if (prevToken != null) {
								((BiWordIndex)biWordIndex).addTerm(PorterStemmer.processToken(Utils.removeHyphens(prevToken))
										+ PorterStemmer.processToken(Utils.removeHyphens(token)), mDocumentID);
							}
							
							// Add to the termFrequency
							/*if (termFreq.containsKey(PorterStemmer.processToken(token))) {
								//int freq = termFreq.get(PorterStemmer.processToken(token)) + 1;
								termFreq.put(PorterStemmer.processToken(token), termFreq.get(PorterStemmer.processToken(token)) + 1);
							} else {
								termFreq.put(PorterStemmer.processToken(token), 1);
							}		*/					
							prevToken = token;
							position++;
						}
						
						// Calculate Ld for this document and store it in the docWeights arraylist
						/*double sum = 0;
						for (Integer tf : termFreq.values()) {
							double wt = 1 + Math.log10(tf);							
							sum = sum + Math.pow(wt, 2);
						}						
						docWeights.add(Math.sqrt(sum));*/						
						mDocumentID++;
					}
					return FileVisitResult.CONTINUE;
				}

				// don't throw exceptions if files are locked/other errors occur
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});			
			System.out.println("In Memory Index creation finished at : " + Calendar.getInstance().getTime());
			System.out.println("In MemoryIndexes created successfully");
			
			// Saving the indexes on disk
			this.saveIndexesOnDisk();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {

		// Attach an event listener to the list items which handles the click on the list items that contains the document names 
		// as part of the search query result
		listView.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.getClickCount() == 2) {
				SelectionModel<String> selectionModel = listView.getSelectionModel();
				String itemSelected = selectionModel.getSelectedItem();
				if (itemSelected.contains("json")) {
					this.jsonBodyContents.setText(Utils.getDocumentText(dirPath + "\\" + itemSelected));
				}
			}
		});

	}

	/**
	 * The constructor. The constructor is called before the initialize()
	 * method.
	 */
	public SearchOverviewController() {
		documents = FXCollections.observableArrayList();
		listView = new ListView<String>();
		vocab = FXCollections.observableArrayList();
		jsonBodyContents = new TextArea();
		corpusVocabSize = new Label();
		numberOfDocsIndexed = new Label();
		numberOfDocsMatchingQuery = new Label();
	}

}
