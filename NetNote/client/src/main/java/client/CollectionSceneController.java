package client;

import client.Interfaces.IVariables;
import client.utils.ServerUtils;
import commons.Collection;
import commons.Note;
import commons.ServerItem;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;


import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static client.Main.INJECTOR;

public class CollectionSceneController{

    @FXML
    private Button addButton;

    @FXML
    private TableColumn<Collection, String> collectionColumn;

    @FXML
    private TextField collectionNameBox;

    @FXML
    private Button delButton;

    @FXML
    private VBox optionsVbox;

    @FXML
    private Pane selectMessageText;

    @FXML
    private MenuButton serverMenuDropdown;

    @FXML
    private TableView<Collection> collectionTableView;

    @FXML
    private Text serverStatusText;

    @FXML
    private Button saveButton;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Text saveStatusText;

    private ArrayList<ServerItem> serverItems = new ArrayList<>();

    private Collection selectedCollection;

    private IVariables var;

    private Boolean hasUnsavedChanges = false;

    private ResourceBundle bundle;
    private Locale currentLocale;
    private Boolean isChosenServerOnline = true;

    private String portOfServerInCheck = "8080";

    private ServerUtils server;

    /**
     * Specific construction for injection
     * @param var Variables Interface
     * @param server ServerUtils class
     */
    @Inject
    public CollectionSceneController(IVariables var, ServerUtils server) {
        this.var = var;
        this.server = server;
    }


    /**
     * General constructor for this Class
     */
    public CollectionSceneController() {

    }
    private String nameOfCurrentServer = "MainServer";

    private int serverClicked = 0;


    /**
     * This method creates a warning dialog using localized messages.
     * @param messageKey The key for the message in the resource bundle.
     */
    public void createWarning(String messageKey) {
        String warningMessage;
        try {
            // Attempt to fetch the localized text using the key
            warningMessage = bundle.getString(messageKey);
        } catch (MissingResourceException e) {
            // Fallback: Use the input string directly if the key is not found
            warningMessage = messageKey;
        }
        String title = bundle.getString("warning_title");
        String content = warningMessage;

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
    }

    /**
     * Function that sets the bundle and locale of a file on call
     * @param bundle Bundle
     * @param currentLocale (locale = language)
     */
    public void setBundleAndLocale(ResourceBundle bundle, Locale currentLocale) {
        this.bundle = bundle;
        this.currentLocale = currentLocale;
    }


    /**
     * Neatly prints the error from try-catches
     * @param e exception which is called
     */
    public void printErrorTrace(Exception e) {
        System.out.println("\033[31m"+"Error: " + e.getClass().getName() + "\033[0m");
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        System.out.println("Called from: " + stackTrace[2].getClassName() + "." + stackTrace[2].getMethodName()
                + " at line: " + stackTrace[2].getLineNumber());
    }

    /**
     * This is a manual injector for var. It manually injects the other controller
     * @param var a var that is being used in the other scene
     */
    public void setVar(IVariables var) {
        this.var = var;
    }

    /**
     *
     * @param server
     */
    public void setServer(ServerUtils server){
        this.server = server;
    }

    /**
     * Sets the port
     * @param port the port of the server
     */
    public void setPort(String port){
        this.portOfServerInCheck = port;
    }

    /**
     *  Sets the name of the server
     * @param name name of the server
     */
    public void setNameOfServer(String name){
        this.nameOfCurrentServer = name;
    }

    /**
     * updates the list of collections by getting all collections from the database
     * @return returns the length of all collections, which can then be used to update the selection model
     */
    public int updateCollectionsList() {
        System.out.println("\033[35m"+"Updated all collections!"+"\033[0m");

        collectionTableView.getItems().clear(); // clear items beforehand
        Collection[] allCollections = var.getAllCollections();
        ObservableList<Collection> collectionList = FXCollections.observableArrayList(allCollections);
        collectionColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        collectionTableView.setItems(collectionList);
        var.refreshCollections();
        return allCollections.length;
    }

    /**
     * initialise method for setting up listeners that listen for: <br>
     * - changing data test by client  <br>
     * - selecting the correct collection
     * @noinspection checkstyle:LambdaParameterName
     */
    @FXML
    public void initialize() {// automatically invoked when FXML is loaded

        serverItems = readServerItems();

        System.out.println(serverItems.getFirst().getName());
        var = INJECTOR.getInstance(Variables.class);
        server = INJECTOR.getInstance(ServerUtils.class);
        System.out.println(serverItems.getLast().getName());


        optionsVbox.setVisible(false);
        selectMessageText.setVisible(true);


        rootPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            keyBoardShortcuts(event);
            /*if (collectionNameBox.isFocused() && event.getCode() == KeyCode.ENTER) {
                serverMenuDropdown.requestFocus();
            } else if (collectionNameBox.isFocused() && event.getCode() != KeyCode.ENTER){
                // do nothing
            } else {
                keyBoardShortcuts(event);
            }*/
        });

        collectionTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedCollection = newValue;
                showSelectedCollectionData();
            }
        });
        collectionNameBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 21) {
                collectionNameBox.setText(oldValue);
                var.createWarning("error_collection_name_max_length");
            }
            updateSaveStatus();
        });

        serverMenuDropdown.setText(var.getNameOfCurrentServer());

        System.out.println(var.getNameOfCurrentServer());
        System.out.println(var.getCurrentPortNumber());

        portOfServerInCheck = var.getCurrentPortNumber();

        serverStatusText.setText("Server Online");

        updateServersMenu();
    }

    /**
     * This method handles the keyboard shortcuts in the collections scene
     * such as deleting with DELETE button, and moving freely with the arrows
     * @param event A key press on the scene
     */
    private void keyBoardShortcuts(KeyEvent event) {

        if(event.getCode() == KeyCode.RIGHT && !selectMessageText.isVisible() && !addButton.isFocused()){
            collectionNameBox.requestFocus();
        }
        else if(event.getCode() == KeyCode.DOWN && saveButton.isFocused()){
            saveButton.requestFocus();
        }
        else if(event.getCode() == KeyCode.DOWN && serverMenuDropdown.isFocused()){
            saveButton.requestFocus();
            event.consume();
        }
        else if (event.getCode() == KeyCode.UP && serverMenuDropdown.isFocused()){
            collectionNameBox.requestFocus();
        } else {
            keyBoardShortcuts2(event);
        }
    }

    private void keyBoardShortcuts2(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE &&
                !collectionTableView.getItems().isEmpty() &&
                collectionTableView.getSelectionModel().getSelectedIndex() != -1){
            clickDeleteCollectionButton();
        }
        else if(event.getCode() == KeyCode.DOWN &&
                collectionTableView.getSelectionModel().getSelectedIndex() ==
                        collectionTableView.getItems().size() - 1 &&
                collectionTableView.isFocused()) {
            addButton.requestFocus();
        }
        else if(collectionNameBox.isFocused() && event.getCode() == KeyCode.DOWN){
            serverMenuDropdown.requestFocus();
        } else if(event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.EQUALS){ // add a collection
            clickCreateCollectionButton();
        }
        else{
            keyBoardShortcutsBugs(event);
        }
    }

    /**
     * This method handles some bugs when moving with the arrows. It also makes it so that the
     * arrows skip the delete button so no bugs occur
     * @param event KeyEvent a keypress on the rootPane
     */
    private void keyBoardShortcutsBugs(KeyEvent event) {
        if(saveButton.isFocused() && event.getCode() == KeyCode.LEFT){
            delButton.requestFocus();
            event.consume();
        }
        else if(event.getCode() == KeyCode.UP && serverMenuDropdown.isFocused()){
            collectionNameBox.requestFocus();
        }
        else if (event.getCode() == KeyCode.RIGHT && addButton.isFocused()){
            delButton.requestFocus();
            event.consume();
        }
        else if(event.getCode() == KeyCode.LEFT && collectionNameBox.isFocused()){
            collectionTableView.requestFocus();
        }
    }

    /**
     * Function that checks whether a collection name has already been taken, and if it is; returns true.
     * If it is not taken; false
     * @param name The name of the collection which should be compared with
     * @param allCollection a list of all collections
     * @return returns true/false
     */
    public boolean checkForExistingCollectionName(String name, Collection[] allCollection){
        Collection[] allCollections = allCollection;
        for (Collection collection : allCollections) {
            if (collection.getName().equals(name.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves the new options to the database, in order to change Collection names.
     */
    @FXML
    public void saveNewOptions() {
        // save collection name / id, check for duplicate name

        if(isChosenServerOnline) {
            if (hasUnsavedChanges) {
                String newName = collectionNameBox.getText().trim();
                selectedCollection.setName(newName);

                // check if there is no collection selected (for some reason)
                if (selectedCollection == null) {
                    printErrorTrace(new IllegalArgumentException("Current collection is null"));
                }

                // check if said collection name is empty or already taken
                if (newName.isEmpty()) {
                    var.createWarning("error_collection_name_empty");
                    return;
                } else if (checkForExistingCollectionName(newName, var.getAllCollections())) {
                    var.createWarning("error_collection_already_exists");
                    return;
                }

                try {
                    server.postCollections(var.getCurrentPortNumber(), selectedCollection);
                } catch (Exception e) {
                    printErrorTrace(e);
                }

                // update and get the number of collections, select the current collection after updating
                int numberOfCollections = updateCollectionsList();
                collectionTableView.getSelectionModel().select(numberOfCollections);
                updateSaveStatus();
            }

            var.setNameOfCurrentServer(nameOfCurrentServer);

            var.setCurrentPortNumber(portOfServerInCheck);
            System.out.println(serverClicked);

            if(serverClicked>0){
                var.getCollectionMenu().setText("default");
                var.createDefaultCollection();
                var.setCurrentCollection(var.getDefaultCollection());
                var.refreshCollections();
/*                var.setCurrentCollection(var.getDefaultCollection());
                var.refreshCollections();*/
            }


            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.close();
            var.setAvailable();
        }
        else{
            var.createWarning("error_server_offline");
        }
    }

    /**
     * Function which is called to show the data (right side)
     * correctly based on the selected Collection from the column.
     */
    public void showSelectedCollectionData() {
        System.out.println("\033[35mShow Selected collection data \033[0m");
        if (selectedCollection != null && !selectedCollection.getName().equals("default")) {
            optionsVbox.setVisible(true);
            selectMessageText.setVisible(false);

            serverMenuDropdown.setText(var.getNameOfCurrentServer());

            collectionNameBox.setText(selectedCollection.getName());
            updateSaveStatus();
        } else {
            optionsVbox.setVisible(false);
            selectMessageText.setVisible(true);
        }
    }

    /**
     * Updates the text "save status" which indicated if changes have not been saved yet or if they have.
     * This is depending on whether the text in the boxes has changed.
     */
    private void updateSaveStatus() {
        if (selectedCollection != null) {
            // > check if changes have occurred
            if (!collectionNameBox.getText().equals(selectedCollection.getName())) {
                hasUnsavedChanges = true;
                // > set save status text
                saveStatusText.setText(bundle.getString("unsaved_changes"));
                saveStatusText.setFill(Color.RED);
            } else {
                hasUnsavedChanges = false;

                saveStatusText.setText(bundle.getString("saved_changes"));
                saveStatusText.setFill(Color.GREEN);
            }
        }
    }

    /**
     * This method takes the text from
     * the collectionText component and creates
     * new empty collection with the name that was specified
     * in this component.
     */
    @FXML
    void clickCreateCollectionButton() {
        String name = var.generateNewName("Untitled Collection");

        if(name.trim().equals(var.defaultCollectionName)) {
            var.createWarning("error_collection_name_all");
        }
        else{
            Collection[] allCollections = var.getAllCollections();

            // make a new collection, and set it's name
            Collection c = new Collection();
            c.setName(name);
            c.setNotes(new ArrayList<>());

            // adding httpHeaders (done before) to stop big errors from happening
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Collection> requestEntity = new HttpEntity<>(c, headers);

            try {
                server.postCollectionsEntity(requestEntity, var.getCurrentPortNumber());
            } catch (Exception e) {
                printErrorTrace(e);
            }
            System.out.println("\033[35mAdded collection!\033[0m");
            updateCollectionsList();
            var.refreshNotes();

            // select the new collection
            collectionTableView.getSelectionModel().select(allCollections.length);
            Collection[] allCollections2 = var.getAllCollections();
            selectedCollection = allCollections2[allCollections2.length - 1];
        }
    }

    /**
     * Function for getting the current selected collection
     * @return returns the collection that was selected by the user.
     */
    private Collection getSelectedCollectionFromTableView(){
        Collection currentSelectedCollection = collectionTableView.getSelectionModel().getSelectedItem();
        if (currentSelectedCollection != null) {
            selectedCollection = currentSelectedCollection;
            return currentSelectedCollection;
        } else {
            System.out.println("Selected Collection is NULL");
            return null;
        }
    }

    /**
     * Primarily refactored function which was called "clickDeleteCollection"
     * but modified so it can be used within this class
     * Permanently deletes selected collection from database,
     * including all notes that are in it.
     */
    @FXML
    void clickDeleteCollectionButton() {
        getSelectedCollectionFromTableView();
        if (selectedCollection == null) {
            var.createWarning("No collection was selected"); // Show warning if no collection is selected
            return;
        }
        if (Objects.equals(selectedCollection.getId(), var.getDefaultCollection().getId())) {
            createWarning("delete_default_collection"); // Use the key from the ResourceBundle
            return;
        } else {
            if (selectedCollection == null) {
                var.createWarning("error_selected_collection_null");
                return;
            }
            String message = bundle.getString("confirm_delete_collection") + "\n\""
                    + selectedCollection.getName() + "\"?";
            boolean choice = var.createPopup(message);
            if(choice){
                var.setCurrentCollection(selectedCollection);
                Note[] notes = var.getNotesCollection();
                ArrayList<Note> notesToDelete = new ArrayList<>(List.of(notes));
                for(Note note : notesToDelete){
                    var.deleteNote(note);
                }

                try {
                    server.deleteCollection(var.getCurrentPortNumber(), selectedCollection.getId());
                    System.out.println("Successfully deleted collection with ID: "
                            + var.getCurrentCollection().getId() );
                }catch (Exception e) {
                    printErrorTrace(e);
                }
                var.setCurrentCollection(var.getDefaultCollection());
                var.setCurrentCollectionId(var.getDefaultCollection().getId());

                var.updatePrivateCollectionname();
                var.refreshCollections();
                var.refreshNotes();
            } else {
                return;
            }
        }

        System.out.println("\033[35mDeleted current collection! \033[0m");
        // give warning and delete a collection from the list
        collectionTableView.getSelectionModel().select(0); // select default
        updateCollectionsList();
    }

    /**
     * Updates the servers menu in the client view, so all proper servers are stated
     */
    public void updateServersMenu(){
        serverMenuDropdown.getItems().clear();
        for(int i=0;i<serverItems.size();i++){
            MenuItem item = new MenuItem(serverItems.get(i).getName());
            item.setId(serverItems.get(i).getPort());
            item.setOnAction(event -> handleServerClick(item.getId(), item.getText()));
            serverMenuDropdown.getItems().add(item);
        }

        for(int i=0;i<serverItems.size();i++) {
            if(Objects.equals(serverItems.get(i).getPort(), portOfServerInCheck)){
                System.out.println(var.getCurrentPortNumber());
                System.out.println(serverItems.get(i).getName());
                serverMenuDropdown.setText(serverItems.get(i).getName());
            }
        }
    }

    /**
     *
     * @param port port that should be changed to
     * @param name name of the port
     */
    private void handleServerClick(String port, String name) {

        System.out.println("XXXXXXXXXXXXXXXXX");
        serverMenuDropdown.setText(name);
        nameOfCurrentServer = name;
        portOfServerInCheck = port;
        if(isThisServerOnline(port)){
            serverStatusText.setText("Server is online.");
            isChosenServerOnline = true;
        }
        else{
            serverStatusText.setText("Server is offline.");
            isChosenServerOnline = false;
        }
        serverClicked++;
    }

    /**
     * This method will read all servers from servers.txt file and create ArrayList of
     * ServerItems from them. If you added a new server configuration then please add it also to the servers.txt file
     * in the format of
     * SERVER_NAME PORT
     * The method will also send a health request to all the specified servers to see which of them are available,
     * so see their status.
     * @return the ArrayList of ServerItems that are read from the servers.txt file
     */
    public ArrayList<ServerItem> readServerItems() {
        ArrayList<ServerItem> items = new ArrayList<ServerItem>();
        try {
            Scanner sc = new Scanner(new File("commons/servers.txt"));
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(" ");

                String port = parts[1];
                String database = "http://localhost:"+port+"/notes/healthCheck";
                boolean checked;
                try{
                    checked = Boolean.TRUE.equals(var.getRestTemplate().getForObject(database, Boolean.class));
                }catch(Exception e){
                    checked = false;
                }
                ServerItem item = new ServerItem(parts[1], parts[0], checked);
                String info = "not active";
                if(checked) {
                    info = "Active";
                }
                System.out.println(parts[1] + "   " + info + "  " + parts[0]);
                items.add(item);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }

        return items;
    }

    /**
     * Check if "this server" on this port is online
     * @param port The port of the server to check
     * @return Returns True when the server is online, and false when it isn't
     */
    public boolean isThisServerOnline(String port){
        String database = "http://localhost:"+port+"/notes/healthCheck";
        boolean checked;
        try{
            checked = Boolean.TRUE.equals(var.getRestTemplate().getForObject(database, Boolean.class));
        }catch(Exception e){
            checked = false;
        }
        return checked;
    }


}
