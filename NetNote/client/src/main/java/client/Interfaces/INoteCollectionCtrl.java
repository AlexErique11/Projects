package client.Interfaces;

import commons.Collection;
import commons.Note;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.stage.Stage;


public interface INoteCollectionCtrl {

    /**
     * Handles repeating titles logic by prompting the user and taking action based on the response.
     *
     * @param repeats The number of repeats of the title.
     * @param popup   The popup window for user interaction.
     */
    void handleRepeatingTitles(long repeats, Stage popup);

    /**
     * Moves a selected note to a different collection.
     *
     * @param selectedCollection The collection to which the note will be moved.
     */
    void handleMovingNote(Collection selectedCollection);

    /**
     * Updates the private collection name in the UI.
     */
    void updatePrivateCollectionname();

    /**
     * Updates the positions of all notes in the current collection and syncs them with the database.
     */
    void updatePosition();

    /**
     * Deletes a note and refreshes the UI and database.
     *
     * @param note The note to be deleted.
     */
    void deleteNote(Note note);

    /**
     * Generates a unique name for a collection based on the provided prompt.
     *
     * @param prompt The base name for the collection.
     * @return The generated unique name for the collection.
     */
    String generateNewName(String prompt);

    /**
     * Generates a unique title for a note based on the provided prompt.
     *
     * @param prompt The base title for the note.
     * @return The generated unique title for the note.
     */
    String generateNewTitle(String prompt);

    /**
     * Sets the selected note and updates the corresponding UI elements.
     */
    void getSelectedNote();

    /**
     * Handles the action when the user edits the note's title in a text field.
     *
     * @param actionEvent The action event triggered when the user presses Enter.
     */
    void handleEditTextFieldAction(ActionEvent actionEvent);

    /**
     * Creates a default collection when the app starts, if no collections exist.
     */
    void createDefaultCollection();

    /**
     * Finds a note by its title.
     *
     * @param title The title of the note to find.
     * @return The note matching the title, or null if not found.
     */
    Note findNoteByTitle(String title);

    /**
     * Fetches a note from the database by its unique ID.
     *
     * @param id The ID of the note to fetch.
     * @return The note with the specified ID.
     */
    Note getNoteById(Long id);

    /**
     * Configures the notes column in a TableView for inline editing.
     */
    void setupEditableNotesColumn();

    /**
     * Updates a note's references in the database when its title changes.
     *
     * @param oldTitle The old title of the note.
     * @param newTitle The new title of the note.
     */
    void updateNoteReferences(String oldTitle, String newTitle);

    /**
     * Updates the specified note in the database.
     *
     * @param note The note to update.
     */
    void updateNoteInDatabase(Note note);

    /**
     * Updates the notes list asynchronously and refreshes the UI.
     *
     * @param notes The updated list of notes to display.
     */
    void setNotes(ObservableList<Note> notes);

    /**
     * Deletes a collection and its associated notes.
     *
     * @param selectedNote1 The selected note to be used in the deletion.
     * @param temp          A temporary string used for restoring search field state.
     * @param tempBoolean   A temporary boolean used for restoring the search checkbox state.
     */
    void handleDeletionOfCollection(Note selectedNote1, String temp, boolean tempBoolean);
}
