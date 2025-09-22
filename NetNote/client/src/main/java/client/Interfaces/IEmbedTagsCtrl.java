package client.Interfaces;

import commons.EmbeddedFile;
import commons.Note;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.springframework.util.MultiValueMap;

import org.springframework.http.*;

import java.util.Set;

public interface IEmbedTagsCtrl {

    // Method to handle the click event on a note link
    void handleNoteLinkClick(String noteTitle);

    // Method to refresh tags and preview for the selected note
    void refreshTagsAndPreview();

    // Method to parse and replace tags and links in the provided content
    String parseAndReplaceTagsInText(String content);

    // Method to initialize and populate tags for the UI elements (ComboBoxes)
    void initializeTags();

    // Method to preserve the state of selected tags in ComboBoxes
    void preserveComboBoxState();

    // Method to add a new ComboBox for selecting tags
    void addTagComboBox();

    // Method to handle tag selection in the ComboBox
    void handleTagSelection(ComboBox<String> selectedComboBox);

    // Method to apply the selected filters to the notes based on selected tags
    void applyFilteredNotes(ObservableList<Note> filteredNotes);

    // Method to apply the selected filters across all notes
    void applyFilters();

    // Method to extract unique tags from the given text
    Set<String> extractTags(String text);

    // Method to fetch file metadata by ID from a remote service
    EmbeddedFile getFileById(Long id);

    // Method to upload a file associated with the selected note
    long uploadFile(HttpHeaders headers, MultiValueMap<String, Object> body);

    // Method to refresh the list of embedded files for the selected note
    void refreshEmbeddedFiles();

    // Method to move a file to another note
    void moveFileToAnotherNote(Long fileId, Long targetNoteId);

    // Method to remove a file entry from the description text of the selected note
    void removeEntryFromDescriptionTextBox(Long fileId);

    // Method to add a file entry to the description text of a target note
    void addEntryToTargetNoteDescription(Note targetNote, EmbeddedFile file);

    // Method to rename a file entry in the description text box of the selected note
    void renameEntryInDescriptionTextBox(Long fileId, String newName);

    // Method to load content into the WebView with styling and scripting for interaction
    void loadContentIntoWebView(String styledContent);

    // Method to refresh the WebView with updated content
    void refreshWebView(String styledContent);

    // Method to replace a note reference in the selected note with a new one
    void replaceNoteReference(String oldReference, String newReference);

    // Method to refresh the HTML preview in the WebView
    void refreshHTMLPreview();


}
