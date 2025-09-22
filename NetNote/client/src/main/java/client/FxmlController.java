package client;

import client.Interfaces.IFxmlController;
import client.Interfaces.IVariables;
import client.utils.ServerUtils;
import commons.*;
import commons.Collection;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;


public class FxmlController implements IFxmlController {

    private final IVariables var;
    private final ServerUtils server;
    private boolean runningThread = false;


    /**
     * A constructor for FXMLController
     * @param variables -> An instance of IVariables
     * @param server -> An instance of the ServerUtils
     */
    @Inject
    public FxmlController(IVariables variables, ServerUtils server) {
        this.var = variables;
        this.server = server;
    }


    private Locale loadSavedLanguage() {
        try (InputStream input = new FileInputStream("config.properties")) {
            Properties props = new Properties();
            props.load(input);
            String lang = props.getProperty("language");
            return Locale.forLanguageTag(lang.replace('_', '-'));
        } catch (IOException e) {
            return Locale.ENGLISH; // Fallback to English
        }
    }
    /**
     * This method sets up all the undoItems into a visible list
     * where user can click elements and undo some of his actions
     */
    public void undoClick(){
        Stage popup = new Stage();
        popup.setTitle("Undo Actions");
        popup.initModality(Modality.APPLICATION_MODAL);

        ListView<String> listOfUndoItems = new ListView<>();
        //Collection[] collections = getAllCollections();

        ObservableList<String> undoItemsListed = FXCollections.observableArrayList();
        UndoItem[] undoActions = server.getUndoActions(var.getCurrentPortNumber());
        if(undoActions == null){
            var.createWarning("No Undo Actions created yet!");
            return;
        }
        for (int i = 0; i < undoActions.length; i++) {
            UndoItem undoItem = undoActions[i]; // Access the current UndoItem
            if (undoItem.getType().equals("move")) {
                changeDescriptionMove(undoItem);
            }
            if (undoItem.getType().equals("addFile")) {
                changeDescriptionAddFile(undoItem);
            }
            if (undoItem.getType().equals("delFile")) {
                changeDescriptionDelFile(undoItem);
            }
            if (undoItem.getType().equals("moveCol")) {
                changeDescriptionMoveCol(undoItem);
            }
            if (undoItem.getType().equals("renameFile")) {
                changeDescriptionRenameFile(undoItem);
            }
            if (undoItem.getType().equals("renameNote")) {
                changeDescriptionRenameNote(undoItem);
            }
            if (undoItem.getType().equals("typing")) {
                changeDescriptionTyping(undoItem);
            }
        }
        var.setUndoList((List.of(undoActions)));
        for(UndoItem undo : var.getUndoList()){
            undoItemsListed.add(undo.getDescription());
        }
        ObservableList<UndoItem> reversedList = FXCollections.observableArrayList(var.getUndoList());
        java.util.Collections.reverse(reversedList);
        undoItemsListed.clear();
        for (UndoItem undo : reversedList) {
            undoItemsListed.add(undo.getDescription());
        }
        listOfUndoItems.setItems(undoItemsListed);
        listOfUndoItems.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String selectedUndoItem = null;
                try {
                    int selectedIndex = listOfUndoItems.getSelectionModel().getSelectedIndex();
                    boolean check = true;
                    while(check && selectedIndex > 0){
                        check = var.resolve();
                        selectedIndex -= 1;
                    }
                } catch (Exception e) {
                    popup.close();
                }
                var.resolve();
                popup.close();
            }
        });
        VBox listUndoItems = new VBox(listOfUndoItems);
        Button deleteAll = new Button("\uD83D\uDDD1");
        deleteAll.setOnAction(event -> {
            server.deleteUndoAll(var.getCurrentPortNumber());
            popup.close();
        });
        listUndoItems.getChildren().add(deleteAll);
        Scene scene = new Scene(listUndoItems, 400, 300);

        scene.getStylesheets().add(var.getCurrentMode());
        popup.setScene(scene);
        popup.showAndWait();
    }

    private void changeDescriptionMove(UndoItem undoItem){
        int position = undoItem.getPosition();

        Locale locale = loadSavedLanguage();

        ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);

        String descriptionTemplate = bundle.getString("undoMove.description");

        String description = MessageFormat.format(descriptionTemplate, position);

        undoItem.setDescription(description); // Update the UndoItem's field
    }

    private void changeDescriptionAddFile(UndoItem undoItem){
        String name = undoItem.getFileName();

        Locale locale = loadSavedLanguage();

        ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);

        String descriptionTemplate = bundle.getString("undoAddFile.description");

        String description = MessageFormat.format(descriptionTemplate, name);

        undoItem.setDescription(description); // Update the UndoItem's field
    }

    private void changeDescriptionDelFile(UndoItem undoItem){
        String name = undoItem.getFileName();

        Locale locale = loadSavedLanguage();

        ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);

        String descriptionTemplate = bundle.getString("undoDelFile.description");

        String description = MessageFormat.format(descriptionTemplate, name);

        undoItem.setDescription(description); // Update the UndoItem's field
    }

    private void changeDescriptionMoveCol(UndoItem undoItem){
        String collection = undoItem.getCollectionName();

        Locale locale = loadSavedLanguage();

        ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);

        String descriptionTemplate = bundle.getString("undoMoveFromCollection.description");

        String description = MessageFormat.format(descriptionTemplate, collection);

        undoItem.setDescription(description); // Update the UndoItem's field
    }

    private void changeDescriptionRenameFile(UndoItem undoItem){
        Locale locale = loadSavedLanguage();

        ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);
        String descriptionTemplate = bundle.getString("undoRenameFile.description");
        // Format the description with dynamic values
        String description = MessageFormat.format(descriptionTemplate, undoItem.getFileId(), undoItem.getOldName());

        undoItem.setDescription(description); // Update the UndoItem's field
    }

    private void changeDescriptionRenameNote(UndoItem undoItem){
        Locale locale = loadSavedLanguage();

        ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);

        String descriptionTemplate = bundle.getString("undoRenameNote.description");

        String description = MessageFormat.format(descriptionTemplate, undoItem.getOldName());

        undoItem.setDescription(description); // Update the UndoItem's field
    }

    private void changeDescriptionTyping(UndoItem undoItem){
        Locale locale = loadSavedLanguage();

        ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);

        String descriptionTemplate = bundle.getString("undoTyping.description");

        String description = MessageFormat.format(descriptionTemplate, undoItem.getTextChanges());

        undoItem.setDescription(description); // Update the UndoItem's field
    }
    /**
     * This method moves a note to a different collection.
     * Right after clicking the button, the previously selected note is saved.
     * Afterward, a list of all collections is shown in a pop-up.
     * When the user chooses a collection, the note relation is deleted
     * from the previous collection. The note for a moment does not
     * belong to any collection. Then, a relation with the chosen
     * collection is created.
     */
    public void arrowRight(){
        Note selectedNote = var.getNoteContainer().getSelectionModel().getSelectedItem();
        Stage popup = new Stage();
        popup.setTitle("Collections");
        popup.initModality(Modality.APPLICATION_MODAL);
        //A ListView used to display all Collections in DB
        ListView<String> listOfCollections = new ListView<>();
        Collection[] collections = var.getAllCollections();
        //An Observable list is required for the List View, but
        //it is an interface, so FXCollections is used.
        ObservableList<String> collectionsListed = FXCollections.observableArrayList();
        for(Collection collectionInDB : collections){
            collectionsListed.add(collectionInDB.getName());}
        listOfCollections.setItems(collectionsListed);
        // 2 Click action
        listOfCollections.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String selectedCollectionName = null;
                Collection selectedCollection = null;
                try {
                    selectedCollectionName = listOfCollections.getSelectionModel().
                            getSelectedItem();
                } catch (Exception e) {
                    popup.close();}

                if (selectedCollectionName != null) {
                    for(Collection collectionInDB : collections){
                        if(collectionInDB.getName().equals(selectedCollectionName)){
                            selectedCollection = collectionInDB;}}

                    if (selectedCollection != null && selectedNote != null) {

                        boolean choice = var.createPopup(var.getBundle().getString("confirm_move_note") + " \"" +
                                selectedNote.getTitle() + "\"?");
                        if(choice) {
                            List<Note> notesList = selectedCollection.getNotes();
                            long repeats = notesList.stream().map(Note::getTitle)
                                    .filter(note -> note.equals(selectedNote.getTitle())).count();
                            var.handleRepeatingTitles(repeats, popup);
                            popup.close();
                            UndoItem undoAction = new UndoMoveFromCollection(var.getCurrentCollection()
                                    , selectedNote.getId()).getUndo();
                            server.updateUndoItem(undoAction, var.getCurrentPortNumber());

                            var.handleMovingNote(selectedCollection);
                            var.getStatusBar().setStyle("-fx-text-fill: #17a2b8;");
                            String statusBar1 = String.format(var.getBundle().getString("movedTo")
                                    , selectedNote.getTitle(),selectedCollection.getName());
                            var.getStatusBar().setText(statusBar1 + " →");}
                    }else{
                        var.createWarning("error_note_not_selected");}
                    popup.close();}
                popup.close();}});
        //A VBox is used as a base for the pop-up
        VBox listCollection = new VBox(listOfCollections);
        Scene scene = new Scene(listCollection, 300, 350);
        scene.getStylesheets().add(var.getCurrentMode());
        popup.setScene(scene);
        popup.showAndWait();
    }

    /**
     * This method moves a note one place down in the collection list
     */
    public void arrowDown() {
        if(var.isSearching()){
            var.createWarning("error_cannot_move_notes_down");
            return;
        }
        ObservableList<Note> notes = var.getNoteContainer().getItems();
        Note selectedNote = var.getNoteContainer().getSelectionModel().getSelectedItem();

        int notesSize = notes.size();
        if(selectedNote == null){return;}
        int currentSelectionIndex = var.getNoteContainer().getSelectionModel().getSelectedIndex();
        if (currentSelectionIndex + 1 < notes.size()) {
            notes.set(currentSelectionIndex, notes.get(currentSelectionIndex + 1));
            notes.set(currentSelectionIndex + 1, selectedNote);
        }
        UndoItem undoAction = new UndoMove(selectedNote.getPosition(), selectedNote.getId()).getUndo();
        var.updatePosition();
        refreshNotes();
        var.getStatusBar().setStyle("-fx-text-fill: blue;");
        String statusBar1 = String.format(var.getBundle().getString("movedDown"), selectedNote.getTitle());
        var.getStatusBar().setText(statusBar1 + " ↓");
        if(currentSelectionIndex == notesSize - 1){
            var.getNoteContainer().getSelectionModel().select(currentSelectionIndex);
        }else if(currentSelectionIndex >= 0){
            var.getNoteContainer().getSelectionModel().select(currentSelectionIndex + 1);
        }
        server.updateUndoItem(undoAction, var.getCurrentPortNumber());
    }

    /**
     * This method moves a note one place up in the collection list
     */
    public void arrowUp() {
        if(var.isSearching()){
            var.createWarning("error_cannot_prioritize_notes");
            return;
        }
        ObservableList<Note> notes = var.getNoteContainer().getItems();
        Note selectedNote = var.getNoteContainer().getSelectionModel().getSelectedItem();
        if(selectedNote == null){return;}
        int currentSelectionIndex = var.getNoteContainer().getSelectionModel().getSelectedIndex();
        if (currentSelectionIndex > 0) {
            notes.set(currentSelectionIndex, notes.get(currentSelectionIndex - 1));
            notes.set(currentSelectionIndex - 1, selectedNote);
        }
        UndoItem undoAction = new UndoMove(selectedNote.getPosition(), selectedNote.getId()).getUndo();
        var.updatePosition();
        refreshNotes();
        var.getStatusBar().setStyle("-fx-text-fill: blue;");
        String statusBar1 = String.format(var.getBundle().getString("movedUp"), selectedNote.getTitle());
        var.getStatusBar().setText(statusBar1 + " ↑");
        if(currentSelectionIndex == 0){
            var.getNoteContainer().getSelectionModel().select(0);
        }else if(currentSelectionIndex >= 0){
            var.getNoteContainer().getSelectionModel().select(currentSelectionIndex - 1);
        }
        server.updateUndoItem(undoAction, var.getCurrentPortNumber());
    }

    /**
     * The methods open a new scene for collections, it sets up the scene, the color,
     *  and it adds all necessary collection to a list of collections
     */
    public void clickCollectionSettings() {
        System.out.println("ccs");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/newScene.fxml"), var.getBundle());

            Parent root = loader.load();
            var.getCollectionSettingsButton().setDisable(true);

            CollectionSceneController csc = loader.getController();
            csc.setBundleAndLocale(var.getBundle(), var.getCurrentLocale());
            csc.setVar(this.var);

            csc.setPort(var.getCurrentPortNumber());
            csc.setNameOfServer(var.getNameOfCurrentServer());


            Stage newStage = new Stage();
            newStage.setTitle("Collections");
            newStage.setScene(new Scene(root));
            newStage.initModality(Modality.APPLICATION_MODAL);

            newStage.setResizable(false);
            newStage.getScene().getStylesheets().add(var.getCurrentMode());

            newStage.show();

            newStage.setOnCloseRequest(event -> {
                System.out.println("Second scene is closed.");
                var.getCollectionSettingsButton().setDisable(false);
                System.out.println(var.getCurrentPortNumber());
                var.getCollectionMenu().setText("default");
                refreshNotes();
                refreshCollections();
            });

            csc.updateCollectionsList(); // show all collections in menu before launch.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to delete a chosen note. It also helps with resetting the selected note index
     * after deletion to the next index (so that a few notes can be deleted in a row easily)
     */
    public void clickDelNote() {
        if (!var.getSelectedTags().isEmpty()) {
            var.createWarning("error_delete_note_while_tag_filtering");
            return;
        }
        String temp;
        boolean tempBoolean = var.getSearchInAllCheckbox().isSelected();
        Note selectedNote1 = var.getNoteContainer().getSelectionModel().getSelectedItem();
        if(var.isSearching()){
            if(var.getSearchInAllCheckbox().isSelected()){
                var.createWarning("error_delete_note_while_searching_in_all");
                return;
            }
            if (var.getNoteContainer().getItems().isEmpty()){return;}
            temp = var.getSearchField().getText();
            var.getSearchField().clear();
            var.setSearching(true);
        } else {
            temp = "";
            if (var.getNoteContainer().getItems().isEmpty()){return;}
            var.setSelectedNote(var.getNoteContainer().getSelectionModel().getSelectedItem());
        }
        var.setSelectedNote(selectedNote1);
        if (var.getSelectedNote() == null){return;}
        var.getSearchField().setText(temp);
        var.getSearchInAllCheckbox().setSelected(tempBoolean);
        boolean choice = var.createPopup(var.getBundle().getString("confirm_delete_note") + " \"" +
                var.getSelectedNote().getTitle() + "\"?");
        if(choice){
            var.getStatusBar().setStyle("-fx-text-fill: red;");
            String warningMessage = "Deleted note \uD83D\uDDD1 " + selectedNote1.getTitle();

            try {
                // Attempt to fetch the localized text using the key
                warningMessage = var.getBundle().getString("deletedStatus");
            } catch (MissingResourceException e) {
            }
            var.getStatusBar().setText(warningMessage +" " + selectedNote1.getTitle() + " \uD83D\uDDD1");
            var.handleDeletionOfCollection(selectedNote1, temp, tempBoolean);
        }
    }

    /**
     * This method refreshes the collections that should
     * be shown in the collectionMenu component,
     * this functionality should still be added to UI
     */
    public void refreshCollections(){
        Long currentId = var.getCurrentCollection().getId();
        String s = var.getCollectionMenu().getText();
        var.setIsWaitingForAnswerAfterDeletingCollection(false);
        if(!var.getSearchField().getText().isEmpty() && !s.equals(var.getCollectionMenu().getText())){
            var.getSearchField().clear();
        }

        var.getCollectionMenu().getItems().clear();

        MenuItem allItem = new MenuItem("ALL");
        allItem.setOnAction(event -> selectAllNotes());

        var.getCollectionMenu().getItems().add(allItem);


        List<Collection> collections = Arrays.stream(var.getAllCollections()).toList();

        for(Collection c : collections){
            MenuItem item = new MenuItem(c.getName());
            item.setId(c.getId().toString());
            item.setOnAction(event -> selectCollection(item.getId()));
            var.getCollectionMenu().getItems().add(item);
        }
        System.out.println(var.getCurrentCollection().getId());
        refreshNotes();
        if(var.getCurrentCollection().getId().equals(currentId)){
            String s1 = var.getSearchField().getText();
            var.getSearchField().setText("");
            var.getSearchField().setText(s1);
        }
    }

    /**
     * This method handles clicking a ALL option in Collection menu,
     * it gets all notes from DB and sets current collection to All collection
     * with ID 0, which cannot be created automatically by DB.
     * It also disables button for adding/ deleting/ moving a note
     */
    public void selectAllNotes(){
        Note[] notes = var.getNotesDB();
        Collection collection = new Collection();
        collection.setName("All");
        collection.setNotes(List.of(notes));
        collection.setId(0L);
        var.setCurrentCollection(collection);
        var.setCurrentCollectionId(collection.getId());
        var.getCollectionMenu().setText("All");

        var.getNewNoteButton().setDisable(true);
        var.getClickDelNote().setDisable(true);
        var.getArrowRight().setDisable(true);

        var.setMarkDownTutorial("tutorial");
        var.getStatusBar().setStyle("-fx-text-fill: purple;");
        String statusBar1 = String.format(var.getBundle().getString("switchTo"), var.getCurrentCollection().getName());
        var.getStatusBar().setText(statusBar1);

        refreshNotes();
        var.initializeTags();
    }


    /**
     * The methods handle clicking a collection in a collectionMenu item.
     * It sets up the CurrentCollection and calls necessary initialize and refresh methods.
     * It also sets up the text of collectionMenu for the name of chosen collection
     * @param id -> The id of the note
     */
    public void selectCollection(String id){
        if (id == null){
            throw new NullPointerException("id must be not null");
        }

        var.setCurrentCollection(server.getNoteFromColelction(var.getCurrentPortNumber(),id));
        var.setCurrentCollectionId((long) Integer.parseInt(id));
        var.getCollectionMenu().setText(var.getCurrentCollection().getName());

        // Reinitialize tags for the newly selected collection
        refreshOnCollectionChange();

        refreshNotes();
        var.getNoteContainer().getSelectionModel().select(null);
        var.setMarkDownTutorial("tutorial");
        var.getStatusBar().setStyle("-fx-text-fill: purple;");
        String statusBar1 = String.format(var.getBundle().getString("switchTo"), var.getCurrentCollection().getName());
        var.getStatusBar().setText(statusBar1);

        var.getNewNoteButton().setDisable(false);
        var.getClickDelNote().setDisable(false);
        var.getArrowRight().setDisable(false);
        var.getSearchField().setText("");
    }

    /**
     * This method clears all the selected tags and refreshes the search criteria
     */
    public void refreshOnCollectionChange() {
        // 1) Clear all selected tags
        var.clearSelectedTags();

        // 2) Reset all existing ComboBoxes safely using a copy of the list
        List<ComboBox<String>> comboBoxCopy = new ArrayList<>(var.getTagComboBoxes());
        for (ComboBox<String> cb : comboBoxCopy) {
            cb.setValue(null);  // No tag selected
            cb.setItems(FXCollections.emptyObservableList());  // Clear items
        }

        // 5) Clear the description text box & reset the WebView
        var.getDescriptionTextBox().clear();

        // 6) Rebuild tags
        var.initializeTags();  // Re-populates ComboBox items from the new note set
    }


    /**
     * This method creates a new Note with the
     * title provided when the New Note button is clicked
     */
    public void clickNewNote() {
        if(var.isSearching()){
            var.createWarning("error_add_note_while_searching");
            return;
        }
        if (!var.getSelectedTags().isEmpty()) {
            var.createWarning("error_add_note_while_tag_filtering");
            return;
        }

        String title = var.generateNewTitle("Untitled Note");
        System.out.println(title);
        String text = "";

        Note newNote = new Note();
        newNote.setText(text);
        newNote.setTitle(title);
        newNote.setPosition(var.getNotesDB().length + 1);
        System.out.println("Creating " + newNote.toString());


        //I used a RestTemplate to post (and get) the Note Object https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html
        //String dataBase2 = "http://localhost:"+currentPortNumber+"/notes";
        //restTemplate.postForObject(dataBase2, newNote, Void.class);

        title = var.generateNewTitle("Untitled Note");
        newNote.setTitle(title);

        server.saveNoteToDatabase(var.getCurrentPortNumber(), newNote);

        Note[] notes = var.getNotesDB();
        int nextId = (int) notes[notes.length-1].getId();
        newNote.setId(nextId);
        server.updateNoteInCollection(var.getCurrentPortNumber(), var.getCurrentCollection().getId(), newNote);

        title = var.generateNewTitle("Untitled Note");
        newNote.setTitle(title);

        server.saveNoteToDatabase(var.getCurrentPortNumber(), newNote);

        refreshNotes();

        var.addNoteToCurrentCollection(newNote);


        //   var.getNoteContainer().getSelectionModel().select(var.getNotesCollection().length - 1);
     //   var.getNoteContainer().edit(var.getNotesCollection().length - 1, var.getNotesColumn());
        var.getStatusBar().setStyle("-fx-text-fill: green;");
        String warningMessage = "Added a new note ✎";

        try {
            // Attempt to fetch the localized text using the key
            warningMessage = var.getBundle().getString("addedStatus");
        } catch (MissingResourceException e) {
        }
        var.getStatusBar().setText(warningMessage + " ✎");

        //System.out.println(currentCollection.getNotes().size());
    }

    /**
     * This method accepts a list of titles and prints them out as
     * labels into the NotesContainer when the refresh button is clicked
     */
    public void refreshNotes() {
        if(var.getCurrentCollectionId() > 0){
            var.setAllNotes( FXCollections.observableArrayList(var.getNotesFromCollectionsDatabase()));
        }
        else{
            var.setAllNotes( FXCollections.observableArrayList(var.getNotesDB()));
        }

        List<Note> noteList;
        if (!var.getSelectedTags().isEmpty()) {
            noteList = var.getAllNotes().filtered(note ->
                    var.getSelectedTags().stream().allMatch(tag -> note.getText().contains("#" + tag))
            );
        } else {
            noteList = var.getAllNotes();
        }

        server.undoUpdate(var.getCurrentPortNumber());


        // Create a mutable list to avoid UnsupportedOperationException
        List<Note> modifiableNoteList = new ArrayList<>(noteList);

        modifiableNoteList.sort(Comparator.comparingInt(Note::getPosition));

        Note previousSelection = null;
        if(var.getNoteContainer().isFocused()) {
            previousSelection = var.getNoteContainer().getSelectionModel().getSelectedItem();
        }
        var.getNoteContainer().setItems(FXCollections.observableArrayList(modifiableNoteList));

        if (previousSelection != null) {
            var.getNoteContainer().getSelectionModel().select(previousSelection);
            var.refreshTagsAndPreview();  // Refresh markdown after selection
        } else if (!modifiableNoteList.isEmpty()) {
            //noteContainer.getSelectionModel().select(0);
            var.refreshTagsAndPreview();
        }


        var.getNotesColumn().setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTitle()));

        var.initializeTags();

/*        collectionColumn.setCellValueFactory(cellData -> {
            int id = (int) cellData.getValue().getId();
            return new SimpleStringProperty(getCollectionFromNoteInefficient(id).getName());
        });*/
    }

    /**
     * Switch to English language.
     */
    public void switchToEnglish() {
        Locale englishLocale = new Locale("en", "US");
        var.switchLanguage(englishLocale);
    }


    /**
     * Switch to Dutch language.
     */
    public void switchToDutch() {
        Locale dutchLocale = new Locale("nl", "NL");
        var.switchLanguage(dutchLocale);
    }

    /**
     * Switch to German language.
     */
    public void switchToGerman() {
        Locale germanLocale = new Locale("de", "DE");
        var.switchLanguage(germanLocale);
    }

    /**
     * This is run when the NoteController is initialised.
     * The app is loaded, refreshed and everything is fetched
     */
    public void initialize() {// Load the saved language for the initial state
        Locale savedLocale = var.loadSavedLanguage();
        var.setBundle(ResourceBundle.getBundle("languages", savedLocale));
        var.setCurrentLocale(savedLocale);
//        Locale savedLocale = loadSavedLanguage();
//        loadResourceBundle(savedLocale);
        var.applyLocalizedText();

        var.getHtmlPreview().getEngine().setJavaScriptEnabled(true);

        var.createDefaultCollection();

        var.setCurrentCollection(var.getDefaultCollection());

        var.getCollectionMenu().setText("default");

        var.setRefreshThread(new RefreshThread(this.var));
        if(!runningThread) {
            var.getRefreshThread().startThread();
            runningThread = true;
        }

        //setupCollectionColumn();
        var.setupEditableNotesColumn(); // Call the method to set up the editable column
        refreshNotes();
        var.setupKeybindListener();
        var.getSearchInAllCheckbox().setOnAction(event -> var.filterNotes());
        // Listen for changes in the search field
        var.getSearchField().textProperty().addListener((observable, oldValue, newValue) -> {
            var.filterNotes(); // Filter and highlight notes dynamically
        });

        // Listen for changes on FocusProperty of searchField
        // (true when client is focused on textField and false if it is otherwise)
        var.getSearchField().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (var.getSearchField().getText().isEmpty()) {
                refreshCollections();
            }
        });

        // Deleting temporary HTML files when the app closes
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (File file : var.getGeneratedHtmlFiles()) {
                // Ensure the file is not already deleted
                if (file.exists()) {
                    if (file.delete()) {
                        System.out.println("Deleted temporary file: " + file.getAbsolutePath());
                    } else {
                        System.err.println("Failed to delete file: " + file.getAbsolutePath());
                    }
                } else {
                    System.out.println("File already deleted: " + file.getAbsolutePath());
                }
            }
        }));
        var.getRootVBox().addEventFilter(KeyEvent.KEY_PRESSED, var::keyBoardShortcuts);


        // Automatically saves the note and updates the preview
        // when the description changes
        var.getDescriptionTextBox().textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if(var.getSelectedNote() != null && Objects.equals(var.getSelectedNote().getText(), oldValue)
                            && !Objects.equals(oldValue, newValue)){
                        String database2 = "http://localhost:"+var.getCurrentPortNumber()+"/undo";
                        var.setTextFieldCounter(var.getTextFieldCounter() + 1);
                        if(var.getTextFieldCounter() % 2 == 0) {
                            UndoItem undoAction = new UndoTyping(var.getSelectedNote().getText(),
                                    var.getSelectedNote().getId(), 2).getUndo();
                            server.updateUndoItem2(undoAction, var.getCurrentPortNumber());

                        }
                        if(var.getTextFieldCounter() >= 10){
                            UndoItem[] undoActions = server.getUndoActions(var.getCurrentPortNumber());
                            var.setUndoList(List.of(undoActions));
                            int x = 0;
                            while(x < 4 && var.getUndoList().size() >= 3 && var.getUndoList()
                                    .getLast().getType().equals("typing")
                                    && var.getUndoList().get(var.getUndoList().size() - 2).getType().equals("typing")){
                                server.deleteUndoItem(var.getCurrentPortNumber());
                                undoActions = server.getUndoActions(var.getCurrentPortNumber());
                                var.setUndoList(List.of(undoActions));
                                x ++;
                            }
                            x ++;
                            UndoItem undoMore = new UndoTyping(var.getUndoList().getLast().getOldName(),
                                    var.getSelectedNote().getId(), x*2).getUndo();
                            server.undoMore(undoMore, var.getCurrentPortNumber());
                            var.setTextFieldCounter(0);
                        }
                    }
                    assert var.getSelectedNote() != null;
                    if(var.getSelectedNote() != null) {
                        var.setSelectedNoteText(newValue);
                    }// Update note text immediately in memory
                    var.saveNoteToDatabase();            // Save to database
                    var.refreshTagsAndPreview();         // Update tags dynamically and refresh WebView
                });


        // Add listener to note selection to toggle file UI elements
        var.getNoteContainer().getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    var.setSelectedNote(newValue);
                    var.refreshEmbeddedFiles();

            // Show/Hide file-related UI elements based on whether a note is selected
                    boolean noteSelected = (var.getSelectedNote() != null);
                    var.getEmbedFileButton().setVisible(noteSelected);
                    var.getToggleFilesButton().setVisible(noteSelected);
                    var.getEmbeddedFilesList().setVisible(noteSelected && var.getToggleFiles());});

        var.getNoteContainer().getSelectionModel().selectedItemProperty()
                .addListener((observable, oldNote, newNote) -> {
                    if (newNote != null) {
                        var.setSelectedNote(newNote);
                        var.getTitleLabel().setText(newNote.getTitle()); // Load selected note's title
                        var.getDescriptionTextBox().setText(newNote.getText()); // Load selected note's text
                        var.refreshEmbeddedFiles();}
                    else {
                        String warningMessage = "Select a note";

                        try {
                            // Attempt to fetch the localized text using the key
                            warningMessage = var.getBundle().getString("noTitle");
                        } catch (MissingResourceException e) {}
                        var.getTitleLabel().setText(warningMessage);}});


        // Initially hide the file-related UI elements
        var.getEmbedFileButton().setVisible(false);
        var.getEmbeddedFilesList().setVisible(false);
        var.getToggleFilesButton().setVisible(false);

        // Intercept hyperlink clicks in the WebView
        var.getHtmlPreview().getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    // Open the link in the system's default browser
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(newValue));
                } catch (Exception e) {
                }
            }
        });

        var.getToggleFilesButton().setOnAction(event -> {
            if (var.getEmbeddedFilesList().isVisible()) {
                var.getEmbeddedFilesList().setVisible(false);
                var.setToggleFiles(false);
            } else {
                var.getEmbeddedFilesList().setVisible(true);
                var.setToggleFiles(true);
            }
        });

        var.getTitleLabel().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.equals("Select a note") || newValue.equals("Eine Notiz auswählen")||
                        newValue.equals("Selecteer een notitie")) {
                    if(!var.getSelectedTags().isEmpty() && var.getNoteContainer().getItems().isEmpty()) {
                        var.setMarkDownTutorial("tutorial");
                        var.getDescriptionTextBox().setVisible(false);
                    }
                    else if(var.getSelectedTags().isEmpty()){
                        var.setMarkDownTutorial("tutorial");
                        var.getDescriptionTextBox().setVisible(false);
                    }
                }
                else{
                    var.getDescriptionTextBox().setVisible(true);
                }
            }
        });

        // Setup the tag filter`````
        var.initializeTags();
        var.addTagComboBox();  // Add the first tag selection box


        // Add a listener or onAction in FXML for the clear button
        var.getClearTagsButton().setOnAction(event -> clearAllTags());

        initializeColors();
        var.readSavedColor();
        var.getSearchField().requestFocus();
        var.getNoteContainer().getSelectionModel().clearSelection();
        var.setMarkDownTutorial("tutorial");
        String warningMessage = "Select a note";

        try {
            // Attempt to fetch the localized text using the key
            warningMessage = var.getBundle().getString("noTitle");
        } catch (MissingResourceException e) {
        }
        var.getTitleLabel().setText(warningMessage);
    }

    /**
     * Handles the embedding of a file into the currently selected note.
     * Displays a file chooser to select a file, uploads it to the backend,
     * updates the UI, and refreshes the embedded files list.
     */
    public void embedFile() {
        if (var.getSelectedNote() == null) {
            var.createWarning("error_select_note_embed_file");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null); // Opens file dialog

        if (selectedFile != null) {
            try {


                // Create headers and body for the request
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", new FileSystemResource(selectedFile));
                ResponseEntity<EmbeddedFile> response = server.getResponse(var.getCurrentPortNumber(),
                        var.getSelectedNote().getId(),
                        body, headers);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    EmbeddedFile uploadedFile = response.getBody();
                    var.addUndoFileDelList(body);
                    var.addUndoFileDelList(headers);
                    var.addUndoFileDelList(uploadedFile);

                    // Add the uploaded file to the local embedded files list
                    var.addSelectedNoteEmbeddedFile(uploadedFile);

                    // Append the file name and ID to the descriptionTextBox
                    String newEntry = "\"" + uploadedFile.getName() + "\"[" + uploadedFile.getId() + "]\n";
                    var.getDescriptionTextBox().appendText("\n" + newEntry);

                    // Refresh the embedded files list immediately
                    var.refreshEmbeddedFiles();
                    UndoItem undoAction = new UndoAddFile(uploadedFile, var.getSelectedNote().getId()).getUndo();
                    server.postUndoItem(undoAction, var.getCurrentPortNumber());
                } else {
                    var.createWarning("error_failed_file_upload");
                }
            } catch (Exception e) {
                String errorMessage = String.format(var.getBundle().getString("error_failed_file_upload_error")
                        , e.getMessage());
                //var.createFormattedWarning(errorMessage);
            }
        }
    }

    /**
     * Deletes a specified embedded file from the selected note.
     * Sends a DELETE request to the backend, removes the file locally,
     * updates the UI, and removes the file entry from the description text box.
     * @param file The EmbeddedFile to be deleted.
     */
    public void deleteFile(EmbeddedFile file) {
        if (var.getSelectedNote() == null) {
            var.createWarning("error_no_note_selected_delete");
            return;
        }

        var.setCriticalOperationInProgress(true); // Flag set to true

        // Create a confirmation dialog
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle(var.getBundle().getString("delete_file_confirmation_title"));
        confirmationAlert.setHeaderText(var.getBundle().getString("file_deletion_warning"));
        confirmationAlert.setContentText("File: " + file.getName());

        // Show the confirmation dialog and wait for user response
        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {

                // Send DELETE request

                ResponseEntity<Void> response = server.getResponseDelete(var.getCurrentPortNumber(), file.getId());


                if (response.getStatusCode() == HttpStatus.OK) {
                    // Remove the file locally from the note
                    var.removeSelectedNoteEmbeddedFile(file);

                    // Update the description text box to remove the file reference
                    var.removeEntryFromDescriptionTextBox(file.getId());


                    // Refresh the embedded files list in the UI
                    var.refreshEmbeddedFiles();

                    UndoItem undoAction = new UndoDelFile(file, var.getSelectedNote().getId()).getUndo();
                    server.postUndoItem(undoAction, var.getCurrentPortNumber());
                } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                    var.createWarning("error_file_not_exist");
                } else {
                    var.createWarning("error_failed_file_delete_unexpected");
                }
            } catch (Exception e) {
                String errorMessage = String.format(var.getBundle().getString("error_failed_file_delete")
                        , e.getMessage());
                var.createFormattedWarning(errorMessage);
            }
        } else {
            // If the user cancels, do nothing
            System.out.println("File deletion canceled by user.");
        }
        var.setCriticalOperationInProgress(false); // Reset flag
    }



    /**
     * Renames an embedded file within the selected note.
     * Prompts the user for a new name, sends a PUT request to the backend to rename the file,
     * updates the local file name, and refreshes the embedded files list.
     * @param file The EmbeddedFile to be renamed.
     */
    public void renameFile(EmbeddedFile file) {
        if (var.getSelectedNote() == null) return;
        var.setRenamingFile(true); // Set flag to prevent UI refresh

        String oldName2 = file.getName();
        // Prompt user for a new file name
        TextInputDialog dialog = new TextInputDialog(file.getName());
        dialog.setTitle("Rename File");
        dialog.setHeaderText(var.getBundle().getString("file_rename_prompt"));
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newFileName -> {
            try {
                // Prepare request payload
                HttpEntity<String> request = new HttpEntity<>(newFileName);

                // Send PUT request to rename the file
                ResponseEntity<EmbeddedFile> response = server.getResponsePut(request, var.getCurrentPortNumber()
                        , file.getId());


                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    file.setName(newFileName); // Update the local file object

                    // Call the updated renameEntryInDescriptionTextBox
                    var.renameEntryInDescriptionTextBox(file.getId(), newFileName);

                    // Refresh the embedded files list in the UI
                    var.refreshEmbeddedFiles();
                    UndoItem undoAction = new UndoRenameFile(file, oldName2, var.getSelectedNote().getId()).getUndo();
                    server.postUndoItem(undoAction, var.getCurrentPortNumber());
                }

            } catch (Exception e) {
                String errorMessage = String.format(var.getBundle().getString("error_failed_file_rename")
                        , e.getMessage());
                var.createFormattedWarning(errorMessage);
            }
            finally {
                var.setRenamingFile(false); // Reset flag after renaming is complete
            }
        });
        if (result.isEmpty()) {
            var.setRenamingFile(false); // Reset flag if the dialog is canceled
        }
    }

    /**
     * Handles the move file action for a specific file.
     *
     * @param file The file to be moved.
     */
    public void handleMoveFile(EmbeddedFile file) {
        if (file == null) {
            var.createWarning("Please select a file to move.");
            return;
        }

        var.setIsMovingFile(true);
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Select Target Note");

        ListView<String> notesListView = new ListView<>();
        ObservableList<String> noteTitles = FXCollections.observableArrayList(
                var.getAllNotes().stream()
                        .map(Note::getTitle)
                        .filter(title -> !title.equals(var.getSelectedNote().getTitle())) // Exclude current note
                        .toList()
        );
        notesListView.setItems(noteTitles);

        Button okButton = new Button("OK");
        okButton.setOnAction(event -> {
            String targetNoteTitle = notesListView.getSelectionModel().getSelectedItem();
            if (targetNoteTitle != null) {
                var.setAllNotes(FXCollections.observableArrayList(var.getNoteContainer().getItems()));
                Note targetNote = var.getNoteService().findNoteByTitle(targetNoteTitle, var.getAllNotes());
                if (targetNote != null) {
                    var.moveFileToAnotherNote(file.getId(), targetNote.getId());
                    System.out.println(file.getId());
                    var.removeEntryFromDescriptionTextBox(file.getId());

                    // Add file entry to the target note's description
                    var.addEntryToTargetNoteDescription(targetNote, file);

                    popupStage.close();
                }
            }
        });

        VBox layout = new VBox(10, notesListView, okButton);
        Scene scene = new Scene(layout, 300, 400);
        popupStage.setScene(scene);
        popupStage.showAndWait();

        refreshCollections();

        var.setIsMovingFile(false);
    }

    /**
     * This method initialize the DropDown Menu with available color versions
     * It adds new Items to the colorButton and sets the onAction function for the items
     */
    public void initializeColors(){

        var.getColorButton().getItems().clear();

        MenuItem lightMode = new MenuItem("Light Mode");
        lightMode.setStyle(
                "-fx-background-color: #e0e0e0;"
        );
        var.getColorButton().getItems().add(lightMode);
        MenuItem darkMode = new MenuItem("Dark Mode");
        darkMode.setStyle(
                "-fx-background-color: #3c3f41;" +
                        "-fx-border-color: #e0e0e0;"
        );
        var.getColorButton().getItems().add(darkMode);
        MenuItem blueMode = new MenuItem("Blue Mode");
        blueMode.setStyle(
                "-fx-background-color: #3b87e3;" +
                        "-fx-border-color: silver;"
        );
        var.getColorButton().getItems().add(blueMode);
        MenuItem greenMode = new MenuItem("Green Mode");
        greenMode.setStyle(
                "-fx-background-color: #51ff43;" +
                        "-fx-border-color: black;"
        );
        var.getColorButton().getItems().add(greenMode);
        MenuItem redMode = new MenuItem("Red Mode");
        redMode.setStyle(
                "-fx-background-color: #ef0606;" +
                        "-fx-border-color: black;"
        );
        var.getColorButton().getItems().add(redMode);


        lightMode.setOnAction(e -> var.changeColor("light"));
        darkMode.setOnAction(e -> var.changeColor("dark"));
        blueMode.setOnAction(e -> var.changeColor("blue"));
        greenMode.setOnAction(e -> var.changeColor("green"));
        redMode.setOnAction(e -> var.changeColor("red"));
    }


    /**
     * The method requests a focus on the searchField after clicking escape
     * @param event -> An event
     */
    public void escapeButtonSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            var.getSearchField().requestFocus();
        }
    }

    /**
     *  This method clears all the tags in use,
     *  it sets the string in the searchField to empty string
     *  It sets the method in note container to default set
     */
    public void clearAllTags() {
        if (var.getSelectedTags().isEmpty()) {
            var.createWarning("error_clear_tags_no_tags");
            return;
        }
        String s = var.getSearchField().getText();
        // Clear the selected tags and reset the UI
        var.clearSelectedTags();
        var.getTagContainer().getChildren().clear();
        var.getTagComboBoxes().clear();
        var.addTagComboBox();

        if(var.getSearchField().getText().isEmpty()) {
            var.setAllNotes(FXCollections.observableArrayList(var.getNotesCollection()));
        }
        // Apply search query without resetting the search field
        ObservableList<Note> filteredNotes = var.getAllNotes().filtered(note ->
                note.getTitle().toLowerCase().contains(var.getSearchField().getText().toLowerCase()) ||
                        note.getText().toLowerCase().contains(var.getSearchField().getText().toLowerCase())
        );
        int selected = -1;
        int caret = 0;
        if(!var.getNoteContainer().getItems().isEmpty()) {
            selected = var.getNoteContainer().getSelectionModel().getSelectedIndex();
            caret = var.getDescriptionTextBox().getCaretPosition();
        }

        // Update the notes in the TableView while maintaining search results
        var.getNoteContainer().setItems(filteredNotes);
        var.getNoteContainer().refresh();
        refreshNotes();

        // Restore selection and caret position
        if(selected >= 0) {
            var.getNoteContainer().getSelectionModel().select(selected);
            var.getDescriptionTextBox().positionCaret(caret);
        }
        else{
            String warningMessage = "Select a note";

            try {
                // Attempt to fetch the localized text using the key
                warningMessage = var.getBundle().getString("noTitle");
            } catch (MissingResourceException e) {
            }
            var.getTitleLabel().setText(warningMessage);
        }
        var.getSearchField().setText("");
        var.getSearchField().setText(s);
    }
}
