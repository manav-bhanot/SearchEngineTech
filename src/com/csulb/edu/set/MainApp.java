package com.csulb.edu.set;

import java.io.IOException;
import com.csulb.edu.set.ui.view.SearchOverviewController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {

	private Stage primaryStage;
	private AnchorPane rootLayout;
	private SearchOverviewController controller;
	/**
	 * Constructor
	 */
	public MainApp() {		
	}

	@Override
	public void start(Stage primaryStage) {		
		
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Search Engine");

		// Now the index has been created. Initializing the search application
		// window layout
		initRootLayout();
	}

	/**
	 * Initializes the root layout.
	 */
	public void initRootLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("ui/view/SearchOverview.fxml"));
			rootLayout = (AnchorPane) loader.load();

			// Give the controller access to the main app.
			controller = loader.getController();
			controller.setMainApp(this);
			
			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			//primaryStage.show();
			primaryStage.setMaximized(true);
			
			controller.promptUserForDirectoryToIndex();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Returns the main stage.
	 * 
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}