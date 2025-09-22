package client;

import client.Interfaces.INoteCollectionCtrl;
import client.Interfaces.IVariables;
import client.utils.ServerUtils;
import commons.Collection;
import commons.Note;
import commons.UndoItem;
import commons.UndoRenameNote;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.*;
import java.util.regex.Pattern;

public class NoteCollectionCtrl implements INoteCollectionCtrl {

    private IVariables var;
    private ServerUtils server;

    /**
     * Injection-Specific Constructor for NoteCollectionCtrl
     * @param var Variables interface
     * @param server ServerUtils reference
     */
    @Inject
    public NoteCollectionCtrl(IVariables var, ServerUtils server) {
        this.var = var;
        this.server = server;
    }

    /**
     * This method is used to handle repeating titles logic
     *
     * @param repeats The amount of repeats
     * @param popup   This is a popup, but i don't know which one...
     */
    public void handleRepeatingTitles(long repeats, Stage popup) {
        if (repeats >= 1) {
            boolean choice = var.createPopup(var.getBundle().getString("confirm_proceed_with_indexed_title"));
            if (!choice) {
                popup.close();
                return;
            }
        }
    }

    /**
     * This method is used to handle moving a note to a different collection
     *
     * @param selectedCollection collection that was selected
     */
    public void handleMovingNote(Collection selectedCollection) {
        server.handleMovingNote(var.getCurrentPortNumber(), var.getCurrentCollection(),
                var.getSelectedNote(), selectedCollection);

        Note temp = var.getSelectedNote();
        var.selectCollection(selectedCollection.getId().toString());
        temp.setTitle(generateNewTitle(temp.getTitle()));
        temp.setPosition(selectedCollection.getNotes().size() + 1);
        updateNoteInDatabase(temp);
        var.refreshNotes();
    }

    /**
     * Called by collectionSceneController, in order to minimise having to make variable public
     * Deletes the current collection iff the current collection is not default.
     * Warns the client of deleting a collection.
     * By deleting a collection all notes within that collection are lost.
     */
    public void updatePrivateCollectionname() {
        var.getCollectionMenu().setText(var.getCurrentCollection().getName());
    }

    /**
     * this method updates the positions of notes
     * in a collection and pushes them to the database
     */
    public void updatePosition() {
        server.updatePosition(var.getCurrentPortNumber(), var.getNoteContainer().getItems());
    }

    /**
     * This method is used to delete a collection
     *
     * @param selectedNote1 The selected note
     * @param temp          The temporary string
     * @param tempBoolean   The temporary boolean
     */
    public void handleDeletionOfCollection(Note selectedNote1, String temp, boolean tempBoolean) {
        int currentSelectionIndex = var.getNoteContainer().
                getSelectionModel().getSelectedIndex();
        if (currentSelectionIndex > 0) {
            var.getNoteContainer().getSelectionModel().select(currentSelectionIndex - 1);
        } else if (currentSelectionIndex == 0) {
            var.getNoteContainer().getSelectionModel().select(1);
        }
        var.setSelectedNote(selectedNote1);
        System.out.println("Selected:\n" + var.getSelectedNote() +
                " ID: " + var.getSelectedNote().getId());
        var.getSearchField().setText("");
        var.setSelectedNote(selectedNote1);
        deleteNote(var.getSelectedNote());
        var.getSearchField().setText(temp);
        if (currentSelectionIndex > 0) {
            var.getNoteContainer().getSelectionModel().select(currentSelectionIndex - 1);
        } else if (currentSelectionIndex == 0) {
            var.getNoteContainer().getSelectionModel().select(0);
        }
        if (var.isSearching()) {
            var.getSearchField().clear();
            var.getSearchField().setText(temp);
            var.getSearchInAllCheckbox().setSelected(tempBoolean);
        }
    }

    /**
     * Deletes the specified note from the database and refreshes the notes table.
     * It sends a request to delete only one, specific note.
     *
     * @param note The note to be deleted.
     */
    public void deleteNote(Note note) {

        if (!var.getSelectedTags().isEmpty()) {
            var.createWarning("error_delete_note_while_tag_filtering");
            return;
        }

        long id = note.getId();

        var.removeNoteFromCurrentCollection(note); // fixes an important problem

        server.deleteNote(id, var.getCurrentPortNumber(), var.getCurrentCollection(), var.getLogger());
        var.refreshNotes();
    }

    /**
     * This method returns a unique name for a collection.
     *
     * @param prompt (requested title)
     * @return title
     */
    public String generateNewName(String prompt) {
        List<Collection> collectionList = List.of(var.getAllCollections());
        List<String> titlesWithPrompt = new ArrayList<>(collectionList.stream().map(Collection::getName)
                .filter(collection -> collection.contains(prompt)).toList());
        List<String> titlesBeingPrompt = new ArrayList<>(collectionList.stream().map(Collection::getName)
                .filter(collection -> collection.equals(prompt)).toList());
        // If the prompt is already taken (exact match exists), handle the numbering
        if (titlesBeingPrompt.size() > 0) {
            return var.getNoteService().handleNumberAssigning(prompt, titlesWithPrompt);
        }

        // If the prompt isn't taken or if no collection contains it, return the prompt as is
        return prompt;
    }

    /**
     * This method returns a unique title.
     *
     * @param prompt (requested title)
     * @return title
     */
    public String generateNewTitle(String prompt) {
        if (prompt == null) {
            throw new IllegalArgumentException("Prompt cannot be null");
        }
        List<Note> notesList = List.of(var.getNotesCollection());
        List<String> titlesWithPrompt = new ArrayList<>(notesList.stream().map(Note::getTitle)
                .filter(note -> note.contains(prompt)).toList());
        List<String> titlesBeingPrompt = new ArrayList<>(notesList.stream().map(Note::getTitle)
                .filter(note -> note.equals(prompt)).toList());
        if (titlesBeingPrompt.size() <= 1) {
            return prompt;
        }
        return var.getNoteService().handleNumberAssigning(prompt, titlesWithPrompt);
    }

    /**
     * This method checks if the user have pressed on a note from the table
     * If he pressed on something set the text of the label to the title of the note.
     * Everytime he presses on a new note it resets the visual effects
     */
    public void getSelectedNote() {
        var.getDescriptionTextBox().setVisible(false);
        TableView<Note> tableView = var.getNoteContainer();
        tableView.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        var.setSelectedNote(newValue);
                        var.getEditTitleField().setVisible(false);
                        var.getEditTitleField().setText("");
                        // Update titleLabel when a new note is selected
                        var.getTitleLabel().setText(newValue.getTitle());
                        var.getTitleLabel().setVisible(true);
                        // set the textField to the note's text
                        var.getDescriptionTextBox().setText(newValue.getText());
                        var.getDescriptionTextBox().setVisible(true);
                        var.setEditing(false);
                    } else {
                        var.getTitleLabel().setVisible(true);
                        String warningMessage = "Select a note";

                        try {
                            // Attempt to fetch the localized text using the key
                            warningMessage = var.getBundle().getString("noTitle");
                        } catch (MissingResourceException e) {
                        }
                        var.getTitleLabel().setText(warningMessage);
                        var.getDescriptionTextBox().setVisible(false);
                        //setMarkDownTutorial();
                    }
                });
    }


    /**
     * When a person is ready with what he wants to edit he can press enter
     * This sets the title of the current note, saves it to the database
     * resets the table view we can see the new title
     * and selects the note again so label can show up and everything is cleaned
     *
     * @param actionEvent clicking enter
     */
    public void handleEditTextFieldAction(ActionEvent actionEvent) {
        var.getTitleLabel().setText(var.getEditTitleField().getText());
        var.setSelectedNoteTitle(var.getEditTitleField().getText());

        var.saveNoteToDatabase();
        var.refreshNotes();
        var.getNoteContainer().getSelectionModel().select(var.getSelectedNote());
    }

    /**
     * This method create the default collection when starting the app
     */
    public void createDefaultCollection() {
        Collection[] collections = var.getAllCollections();
        int x = 0;
        if (collections.length == 0) {
            var.setDefaultCollection(new Collection());
            System.out.println(var.getDefaultCollectionName());
            var.setDefaultCollectionCollectionName(var.getDefaultCollectionName());
            // might cause a problem with default collections?

            Note[] notesDB = var.getNotesDB();
            if (notesDB == null) {
                notesDB = new Note[0];
            }
            List<Note> allNoteList = Arrays.stream(notesDB).toList();
            var.setDefaultCollectionNotes(allNoteList);

            server.createDefault(var.getDefaultCollection(), var.getCurrentPortNumber());
            Collection[] collections1 = var.getAllCollections();
            var.setDefaultCollectionId(collections1[0].getId());
            var.setDefaultCollection(collections1[0]);
            var.setCurrentCollection(collections1[0]);
        } else {
            for (Collection c : collections) {
                if (c.getName().equals("default")) {
                    var.setDefaultCollection(c);
                }
            }
        }
    }

    /**
     * Find the note corresponding to a title, as all titles are unique
     * @param title The title of the note to find.
     * @return Returns the note which was found
     */
    public Note findNoteByTitle(String title) {
        return var.getAllNotes().stream()
                .filter(note -> note.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);
    }

    /**
     * Fetches note from DB by Id
     *
     * @param id the unique ID of the note
     * @return the Note fetched
     */
    public Note getNoteById(Long id) {
        return server.getNoteById(id, var.getCurrentPortNumber());
    }

    /**
     * Configures the notes column in a TableView to allow in-place editing.
     * Updates the database and UI in real time as users edit note titles.
     */
    public void setupEditableNotesColumn() {

        final boolean[] isAlertShowing = {false}; // Add this at the class level

        var.getNoteContainer().setEditable(true);

        var.getNotesColumn().setCellFactory(column -> new TableCell<>() {
            private final TextField textField = new TextField();

            {
                textField.textProperty().addListener((_, _, newValue) -> {
                    if (isEditing() && getItem() != null) {
                        Note note = getTableView().getItems().get(getIndex());
                        String oldTitle = note.getTitle();
                        note.setTitle(newValue);
                        updateNoteInDatabase(note);

                        if (var.getSelectedNote() != null && var.getSelectedNote().getId() == note.getId()) {
                            var.getTitleLabel().setText(newValue);
                        }

                        // Update references across all notes
                        updateNoteReferences(oldTitle, newValue);
                    }
                });

                textField.setOnAction(event -> commitEdit(textField.getText()));

                textField.focusedProperty().addListener((_, _, newValue) -> {
                    if (!newValue) { //lost focus
                        String editedTitle = textField.getText();
                        if (editedTitle.matches(".*[#\\[\\]].*") || editedTitle.isEmpty()) {
                            // Check for special characters
                            if (!isAlertShowing[0]) {
                                isAlertShowing[0] = true; // Set flag to true
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Invalid Title");
                                alert.setHeaderText("Special Characters Detected");
                                alert.setContentText("The note title cannot contain special characters like #,"+
                                        " [ or ] and cannot be empty.");
                                if(!editedTitle.equals(generateNewTitle(editedTitle)))
                                    alert.setContentText("The note title cannot contain be the same "+
                                            "one as other notes titles");

                                // Show the alert and re-enter editing mode after it's closed
                                alert.setOnHidden(event -> {
                                    isAlertShowing[0] = false; // Reset the flag
                                    Platform.runLater(() -> {
                                        startEdit();              // Re-enter editing mode
                                        textField.requestFocus(); // Focus back on the TextField
                                    });
                                });
                                alert.show();
                            }


                            // Revert to the original title if invalid
                            textField.setText(getItem());
                        } else {
                            commitEdit(editedTitle); // Commit the edit if valid
                            var.setIsEditingTitle(false);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null); // Hide TextField for empty cells
                    setText(null);
                } else {
                    if (isEditing()) {
                        // Set TextField content to the current value
                        textField.setText(item);
                        setGraphic(textField); // Show TextField
                        setText(null); // Hide regular text
                    } else {
                        setGraphic(null); // Hide TextField
                        setText(item); // Show regular text
                    }
                }
            }

            @Override
            public void startEdit() {
                String tempValue = var.getSelectedNote().getTitle();
                String newValue = generateNewTitle(tempValue);
                if(Objects.equals(newValue, tempValue)){
                    UndoItem undoAction = new UndoRenameNote(var.getSelectedNote().getTitle(),
                            var.getSelectedNote().getId()).getUndo();
                    server.startEdit(undoAction, var.getCurrentPortNumber());
                }
                super.startEdit();
                var.setIsEditingTitle(true);
                // Set TextField content to the current value
                textField.setText(getItem());
                setGraphic(textField); // Show TextField
                setText(null); // Hide regular text
                textField.requestFocus(); // Focus on the TextField
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setGraphic(null); // Hide TextField
                setText(getItem()); // Restore the regular text
                var.setIsEditingTitle(false);
            }

            @Override
            public void commitEdit(String newValue) {
                String editedTitle = textField.getText();
                if (editedTitle.matches(".*[#\\[\\]].*") || editedTitle.isEmpty()) {
                    // Check for special characters
                    if (!isAlertShowing[0]) {
                        isAlertShowing[0] = true; // Set flag to true
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Invalid Title");
                        alert.setHeaderText("Special Characters Detected");
                        alert.setContentText("The note title cannot contain special characters "+
                                "like #, [ or ] and cannot be empty.");

                        // Temporarily cancel the edit before showing the alert
                        cancelEdit();
                        // Show the alert and re-enter editing mode after it's closed
                        alert.setOnHidden(event -> {
                            isAlertShowing[0] = false; // Reset the flag
                            Platform.runLater(() -> {
                                startEdit();              // Re-enter editing mode
                                textField.requestFocus(); // Focus back on the TextField
                            });
                        });
                        alert.show();
                    }

                    return;
                }

                String newTitle = generateNewTitle(newValue);
                super.commitEdit(newTitle);
               // I sometimes got an index out of bounds exception while testing, so I added this if statement
                if (getIndex() >= 0 && getIndex() < var.getNotesCollection().length) {
               //      Update the note title and save to the database
                    Note note = getTableView().getItems().get(getIndex());
                    if(!newTitle.equals(newValue) && !var.createPopup("Title \"" + newValue +
                            "\" already exists. Do you want to index this note?\nIf possible, "+
                            "previous title will be used.")){
                        var.resolve();
                        return;
                       // newTitle = note.getTitle();
                    }
                 //   var.getSelectedNote().setTitle(newTitle);
                    note.setTitle(newTitle);
                    updateNoteInDatabase(note);

                    // Update the titleLabel if the edited note is selected
                    if (var.getSelectedNote() != null && var.getSelectedNote().getId() == note.getId()) {
                        var.getTitleLabel().setText(newTitle);
                    }
                    var.refreshNotes();
                }
                getTableView().refresh();
                String s = var.getSearchField().getText();
                var.getSearchField().setText("");
                var.getSearchField().setText(s);
            }
        });

        var.getNotesColumn().setOnEditCommit(event -> {
            Note note = event.getRowValue();
            note.setTitle(generateNewTitle(event.getNewValue())); // Update the note's title
            updateNoteInDatabase(note); // Save the updated note to the database


            // Update titleLabel if the current note is selected
            if (var.getSelectedNote() != null && var.getSelectedNote().getId() == note.getId()) {
                var.getTitleLabel().setText(note.getTitle());
            }
        });


        var.getNotesColumn().setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTitle()));

    }

    /**
     * Updates the specified note in the database.
     * Sends a POST request to the server with the updated note data.
     *
     * @param oldTitle change from oldTitle
     * @param newTitle change to newTitle
     */
    public void updateNoteReferences(String oldTitle, String newTitle) {
        if (var.getCurrentCollection() == null) {
            return; // No current collection, so nothing to update
        }
        var.setAllNotes(FXCollections.observableArrayList(var.getNoteContainer().getItems()));

        for (Note note : var.getAllNotes()) {
            // Replace the old title reference with the new title reference in the note's text
            String updatedText = note.getText().replaceAll(
                    "\\[\\[" + Pattern.quote(oldTitle) + "\\]\\]",
                    "[[" + newTitle + "]]"
            );

            // If the text was updated, save the note to the database
            if (!updatedText.equals(note.getText())) {
                note.setText(updatedText);
                updateNoteInDatabase(note);
            }
        }
    }

    /**
     * Updates the given note in database with the new information
     * @param note The note to update.
     */
    public void updateNoteInDatabase(Note note) {
        server.updateNoteInDatabase(note, var.getCurrentPortNumber(), var.getLogger());
    }

    /**
     * Updates the note list asynchronously on the JavaFX Application Thread.
     * This method prevents overwriting filtered views when a search or tag filter is active.
     * It selectively refreshes notes while maintaining the current selection and caret position
     * to avoid disrupting the user's workflow.
     *
     * @param notes The updated list of notes to be processed and displayed in the TableView.
     * @see Platform#runLater(Runnable)
     */
    public void setNotes(ObservableList<Note> notes) {
        Platform.runLater(() -> {
            try {
                if (var.getIsEditingTitle() || var.isRenamingFile() || var.isCriticalOperationInProgress()
                        || var.getIsMovingFile()) {
                    System.out.println("You are editing or renaming a file, no changes synced.");
                    return;
                }
                // Avoid overwriting the filtered view if filtering is active
                if (var.isSearching() || !var.getSelectedTags().isEmpty() || !var.getSearchField().getText().isEmpty()){
                    System.out.println("Skipping refresh during active filtering.");
                    return;
                }
                int selected = -1;
                int caret = 0;
                if (!var.getNoteContainer().getItems().isEmpty()) {
                    selected = var.getNoteContainer().getSelectionModel().getSelectedIndex();
                    caret = var.getDescriptionTextBox().getCaretPosition();
                }

                var.refreshCollections();
                var.refreshNotes();  // Updates allNotes, but avoids overwriting noteContainer directly

                // Restore selection and caret position
                if (selected >= 0) {
                    var.getNoteContainer().getSelectionModel().select(selected);
                    var.getDescriptionTextBox().positionCaret(caret);
                } else {
                    String warningMessage = "Select a note";

                    try {
                        // Attempt to fetch the localized text using the key
                        warningMessage = var.getBundle().getString("noTitle");
                    } catch (MissingResourceException e) {
                    }
                    var.getTitleLabel().setText(warningMessage);
                }

            } catch (Exception e) {
                var.getLogger().error("Error during note refresh: {}", e.getMessage());
            }
        });
    }
}
