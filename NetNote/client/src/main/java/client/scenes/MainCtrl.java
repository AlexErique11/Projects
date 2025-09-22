package client.scenes;

import client.Variables;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MainCtrl {

    private Stage primaryStage;
    private Variables var;
    private Scene overview;
    private Scene add;

    /**
     * Method to initialise Primary Stage
     * @param primaryStage The primary Stage to initialise
     * @param overview The Overview Pair to initialise
     */
    public void initialize(Stage primaryStage, Pair<Variables, Parent> overview) {
        this.primaryStage = primaryStage;
        this.var = overview.getKey();
        this.overview = new Scene(overview.getValue());
        var.readSavedColor();
        var.setUsersCSS(var.readUserCss());
        this.overview.getStylesheets().add(var.getCurrentMode());

        //var.refreshNotes();
        //var.getSelectedNote();
        showOverview();
        primaryStage.show();
        var.initializeFXML();
    }

    /**
     * New Initialise method
     * @param overview The overview pair to initialise
     */
    public void innitializeNew(Pair<Variables, Parent> overview){
        this.var = overview.getKey();

        // Get the current scene of the primary stage
        Scene currentScene = primaryStage.getScene();

        // If the current scene exists, update the root node
        if (currentScene != null) {
            currentScene.setRoot(overview.getValue());  // Switch to the new root
        } else {
            // If the scene does not exist, create a new scene with the new root
            this.overview = new Scene(overview.getValue());
            primaryStage.setScene(this.overview);  // Set the scene on the primary stage
        }

        // Show the updated primary stage (if needed)
        primaryStage.show();
        var.initializeFXML();
    }

    /**
     * Method to show Overview
     */
    public void showOverview() {
        primaryStage.setTitle("NetNote");
        primaryStage.setScene(overview);
    }
}
