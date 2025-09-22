package client;

import client.Interfaces.IEmbedTagsCtrl;
import client.Interfaces.IVariables;
import client.utils.ServerUtils;
import commons.EmbeddedFile;
import commons.Note;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import netscape.javascript.JSObject;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import org.springframework.http.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class EmbedTagsCtrl implements IEmbedTagsCtrl {


    private IVariables var;
    private NoteService noteService;
    private ServerUtils server;

    /**
     * A constructor for the EmbedTagsCtrl item
     * @param var -> Instance of IVariables interface
     * @param noteService -> An instance of noteService
     * @param server -> An instance of ServerUtils
     */
    @Inject
    public EmbedTagsCtrl(IVariables var, NoteService noteService, ServerUtils server) {
        this.var = var;
        this.noteService = noteService;
        this.server = server;
    }

    /**
     * Parameterless constructor for EmbedTagsCtrl
     */
    public EmbedTagsCtrl() {}

    /**
     * The methods handle clicking a reference to a different note
     * it checks whether the linked note is null, if yes then it gives an error
     * if no, then it sets the selected note to this note.
     * It finds a note by title
     * @param noteTitle -> A title of a linked note
     */
    public void handleNoteLinkClick(String noteTitle) {
        // Find and select the note based on its title
        var.setAllNotes(FXCollections.observableArrayList(var.getNoteContainer().getItems()));
        Note linkedNote = noteService.findNoteByTitle(noteTitle, var.getAllNotes());
        if (linkedNote != null) {
            // Update the selected note
            var.setSelectedNote(linkedNote);

            // Update UI elements
            var.getDescriptionTextBox().setText(linkedNote.getText());
            var.getTitleLabel().setText(linkedNote.getTitle());
            var.getNoteContainer().getSelectionModel().select(linkedNote);

            // Re-parse and refresh the WebView content
            var.setAllNotes(FXCollections.observableArrayList(var.getNoteContainer().getItems()));
            String parsedContent = noteService.parseAndReplaceTagsInText(
                    var.markdownToHtml(linkedNote.getText()),var.getAllNotes());
            loadContentIntoWebView(parsedContent);

            // Refresh tags dynamically
            initializeTags();
        } else {
            String errorMessage = String.format(var.getBundle().getString("error_note_not_found"), noteTitle);
            var.createFormattedWarning(errorMessage);
        }
    }

    /**
     * Refreshes the tags and HTML preview for the selected note.
     * This method performs the following steps:
     * 1. Converts the note's text to HTML using markdown parsing.
     * 2. Replaces and formats tags (#tag) by wrapping them in span elements.
     * 3. If a search query is present, highlights occurrences of the search term within the note text.
     * 4. Updates the WebView with the processed HTML content.
     * 5. Dynamically refreshes the available tags in the UI.
     *
     * If the selected note is null or empty, the WebView is cleared.
     */
    public void refreshTagsAndPreview() {
        if (var.getSelectedNote() == null || var.getSelectedNote().getText().isEmpty()) {
            var.setMarkDownTutorial("tutorial");
            return;
        }

        //Convert to HTML
        String htmlContent = var.markdownToHtml(var.getSelectedNote().getText());

        //Replace tags (#tag) before highlighting
        var.setAllNotes(FXCollections.observableArrayList(var.getNoteContainer().getItems()));
        String styledContent = noteService.parseAndReplaceTagsInText(htmlContent,var.getAllNotes());

        // Apply highlighting for search query after tag replacement
        if (!var.getSearchField().getText().isEmpty()) {
            styledContent = noteService.highlightTextInNotes(var.getSelectedNote(), var.getSearchField().getText());
            var.setAllNotes(FXCollections.observableArrayList(var.getNoteContainer().getItems()));
            styledContent = noteService.parseAndReplaceTagsInText(
                    styledContent, var.getAllNotes());  // Ensure tags persist
        }

        // Update WebView
        refreshWebView(styledContent);

        // Refresh tags
        initializeTags();


        refreshWebView(var.markdownToHtml(styledContent));

    }

    /**
     * Parses and replaces specific elements within the provided HTML content.
     * This method performs three primary operations:
     * 1. Converts markdown-style headers (# Header) into proper HTML header tags.
     * 2. Identifies and transforms inline tags (#tag) into clickable HTML span elements for styling and interaction.
     * 3. Converts note links in the format [[Note Title]] into HTML anchor tags for easy navigation.
     *
     * @param content The HTML content to be parsed and modified. This is typically generated from markdown.
     * @return A String containing the modified HTML content with headers, tags, and links appropriately replaced.
     */
    public String parseAndReplaceTagsInText(String content) {
        if (content == null || content.isEmpty()) return "";

        // Handle Markdown Headers <p> tags
        content = content.replaceAll("(?m)<p>\\s*#\\s(.+?)</p>", "<h1>$1</h1>");
        content = content.replaceAll("(?m)<p>\\s*##\\s(.+?)</p>", "<h2>$1</h2>");
        content = content.replaceAll("(?m)<p>\\s*###\\s(.+?)</p>", "<h3>$1</h3>");

        // Handle inline tags (#tag)
        Pattern tagPattern = Pattern.compile("(?<!\\w)(#\\w+)");
        Matcher tagMatcher = tagPattern.matcher(content);
        StringBuffer tagBuffer = new StringBuffer();

        while (tagMatcher.find()) {
            String tag = tagMatcher.group(1);  // Full match with #
            String cleanTag = tag.substring(1);  // Remove # for display
            String replacement = "<span class='tag' data-tag='" + tag + "' onclick='onTagClicked(\"" + tag + "\")'>"
                    + cleanTag + "</span>";  // Render without #
            tagMatcher.appendReplacement(tagBuffer, replacement);

        }
        tagMatcher.appendTail(tagBuffer);

        // Handle [[other note]] links
        Pattern linkPattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
        Matcher linkMatcher = linkPattern.matcher(tagBuffer.toString());
        StringBuffer linkBuffer = new StringBuffer();

        while (linkMatcher.find()) {
            String noteTitle = linkMatcher.group(1);
            String linkReplacement = "<a href='#' class='note-link' data-note-title='" + noteTitle + "'>"
                    + noteTitle + "</a>";
            linkMatcher.appendReplacement(linkBuffer, linkReplacement);

        }
        linkMatcher.appendTail(linkBuffer);


        return linkBuffer.toString();
    }

    /**
     * Initializes and populates the tag ComboBoxes by extracting unique tags from all notes.
     *-----------------------------------------------------------------------------------------
     * This method retrieves all notes from the database, extracts tags from their content,
     * and populates the ComboBoxes used for tag-based filtering. The method ensures that
     * duplicate tags are avoided by using a LinkedHashSet, which maintains insertion order.
     *-----------------------------------------------------------------------------------------
     * - Fetches notes from the database.
     * - Extracts unique tags from the content of each note.
     * - Populates three ComboBoxes (tagComboBox1, tagComboBox2, tagComboBox3) with the tags.
     * - Dynamically updates the tags without duplication.
     *-----------------------------------------------------------------------------------------
     * This method is called during the initialization phase to ensure that the user can
     * filter notes by available tags.
     */
    public void initializeTags() {
        // Fetch all notes from the database
        List<Note> allNotes = new ArrayList<>();

        var.setAllNotes(FXCollections.observableArrayList(var.getNoteContainer().getItems()));

        // Extract tags from all notes
        Set<String> globalTags = new LinkedHashSet<>();
        for (Note note : var.getAllNotes()) {
            globalTags.addAll(extractTags(note.getText()));
        }

        // Extract tags specific to the current collection
        Set<String> collectionTags = new LinkedHashSet<>();
        for (Note note : var.getCurrentCollection().getNotes()) {
            collectionTags.addAll(extractTags(note.getText()));
        }

        // Combine tags: Current collection tags appear first, followed by global tags
        LinkedHashSet<String> combinedTags = new LinkedHashSet<>(collectionTags);
        combinedTags.addAll(globalTags);

        // Update the tag list
        var.setAllAllTags(combinedTags);
        for (ComboBox<String> comboBox : var.getTagComboBoxes()) {
            var.removeFromAllTags(comboBox.getValue());
        }


        // Refresh the tag ComboBoxes
        for (ComboBox<String> comboBox : var.getTagComboBoxes()) {
            if(comboBox.getValue() == null)
                comboBox.setItems(FXCollections.observableArrayList(var.getAllTags()));
        }
    }

    /**
     *The methods iterate over the ComboBoxes
     * It takes a value from avery ComboBox and if it's not a null
     * value, it adds it to SelectedTags in var.
     */
    public void preserveComboBoxState() {
        for (ComboBox<String> comboBox : var.getTagComboBoxes()) {
            String selectedValue = comboBox.getValue();
            if (selectedValue != null) {
                var.addSelectedTags(selectedValue);
            }
        }
    }
    /**
     * Adds a new ComboBox for selecting tags.
     */
    public void addTagComboBox() {
        ComboBox<String> tagComboBox = new ComboBox<>(var.getAllTags());
//        tagComboBox.setPromptText(bundle.getString("combo.tag.prompt"));  // Localized prompt text
        tagComboBox.setOnAction(event -> handleTagSelection(tagComboBox));

        var.getTagContainer().getChildren().add(tagComboBox);
        var.getTagComboBoxes().add(tagComboBox);
    }

    /**
     * Handles tag selection and filters the dropdown options for subsequent ComboBoxes.
     * @param selectedComboBox the current selected bombobox
     */
    public void handleTagSelection(ComboBox<String> selectedComboBox) {
        String selectedTag = selectedComboBox.getValue();
        if (selectedTag == null) return;

        var.clearSelectedTags();
        // Add the tag to the selected list
        var.addSelectedTags(selectedTag);
        preserveComboBoxState();  // Store current selections

        // Filter notes from the current collection
        ObservableList<Note> notesFromCurrentCollection = FXCollections.observableArrayList(
                var.getNoteContainer().getItems());

        ObservableList<Note> filteredNotes = notesFromCurrentCollection.filtered(note ->
                var.getSelectedTags().stream().allMatch(tag -> note.getText().contains("#" + tag))
        );

        // Extract tags from filtered notes
        Set<String> remainingTags = filteredNotes.stream()
                .flatMap(note -> extractTags(note.getText()).stream())
                .collect(Collectors.toSet());

        // Remove already selected tags
        remainingTags.removeAll(var.getSelectedTags());

        // Add another ComboBox if more tags are available
        if (!remainingTags.isEmpty() && selectedComboBox == var.getTagComboBoxes()
                .getLast()) {
            addTagComboBox();
        }

        resetUnselectedComboBoxes(remainingTags);  // Reset with unselected tags
        applyFilteredNotes(filteredNotes);
    }


    /**
     * The methods sets up the list of filtered notes, if the old selected
     * note is still in the NoteContainer then the new selected note will be the old
     * selected Note
     * @param filteredNotes -> The ObservableList of Note items
     */
    public void applyFilteredNotes(ObservableList<Note> filteredNotes) {
        Note oldSelection = var.getSelectedNote();
        var.getNoteContainer().setItems(filteredNotes);
        var.getNoteContainer().refresh();
        if(var.getNoteContainer().getItems().contains(oldSelection)) {
            var.setSelectedNote(oldSelection);
            var.getNoteContainer().getSelectionModel().select(oldSelection);
        }
        else if (!var.getNoteContainer().getItems().isEmpty()){
            var.setSelectedNote(var.getNoteContainer().getItems().getFirst());
            var.getNoteContainer().getSelectionModel().select(var.getNoteContainer().getItems().getFirst());
        }
    }

    /**
     *The method filter out already selected tags from the dropdown
     * @param remainingTags -> The set of remaining tags
     */
    public void resetUnselectedComboBoxes(Set<String> remainingTags) {
        for (ComboBox<String> comboBox : var.getTagComboBoxes()) {
            if (comboBox.getValue() == null) {
                // Filter out already selected tags from the dropdown
                ObservableList<String> filteredTags = FXCollections.observableArrayList(remainingTags);
                comboBox.setItems(filteredTags);
            }
        }
    }

    /**
     * Extracts unique tags from the provided text by identifying words prefixed with '#'.
     * ----
     * This method scans the input text for hashtags (e.g., #example) and collects them
     * without the '#' symbol. It uses regular expressions to ensure that the tags are valid
     * and not part of a URL or other context. The extracted tags are returned as a set,
     * ensuring uniqueness.
     * - Uses a regular expression to match hashtags.
     * - Captures the tag name by excluding the '#' character.
     * - Avoids extracting tags that are part of other words (e.g., 'word#tag' is ignored).
     * - Returns an empty set if the input text is null or empty.
     *------
     * @param text The input text from which tags need to be extracted. Can be null or empty.
     * @return A set of unique tags extracted from the text. Returns an empty set if no tags are found.
     */
    public Set<String> extractTags(String text) {
        Set<String> tags = new HashSet<>();
        if (text == null || text.isEmpty()) return tags;

        Pattern pattern = Pattern.compile("(?<!\\w)#(\\w+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            tags.add(matcher.group(1));  // Extract tag without #
        }
        return tags;
    }

    /**
     * The methods take all the notes from the db and filter them
     * by the tags they contains
     */
    public void applyFilters() {
        List<Note> filteredNotes = new ArrayList<>(var.getAllNotes());

        if (!var.getSelectedTags().isEmpty()) {
            filteredNotes = filteredNotes.stream()
                    .filter(note -> var.getSelectedTags().stream()
                            .allMatch(tag -> note.getText().contains("#" + tag)))
                    .collect(Collectors.toList());
        }

        // Sort and display notes
        filteredNotes.sort(Comparator.comparingInt(Note::getPosition));
        var.getNoteContainer().setItems(FXCollections.observableArrayList(filteredNotes));
        var.getNoteContainer().refresh();
    }

    /**
     * Fetches file metadata by ID from a remote service.
     * @param id the unique ID of the file to fetch
     * @return the EmbeddedFile object for the given ID, or null if an error occurs
     */
    public EmbeddedFile getFileById(Long id) {
        return server.getFileById(id, var.getCurrentPortNumber());
    }

    /**
     * Handles the embedding of a file while undoing an action.
     *
     * @param headers headers
     * @param body    body
     * @return id of the file
     */
    public long uploadFile(HttpHeaders headers, MultiValueMap<String, Object> body ){
        try{
            String url = "http://localhost:"+var.getCurrentPortNumber()+"/files/" + var.getSelectedNote().getId();

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            // Send the request to the backend and get the response
            ResponseEntity<EmbeddedFile> response = var.getRestTemplate().exchange(url, HttpMethod.POST,
                    requestEntity, EmbeddedFile.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                EmbeddedFile uploadedFile = response.getBody();
                // Add the uploaded file to the local embedded files list
                var.addSelectedNoteEmbeddedFile(uploadedFile);

                // Append the file name and ID to the descriptionTextBox
                String newEntry = "\"" + uploadedFile.getName() + "\"[" + uploadedFile.getId() + "]\n";
                var.getDescriptionTextBox().appendText("\n" + newEntry);

                // Refresh the embedded files list immediately
                refreshEmbeddedFiles();
                return uploadedFile.getId();
            } else {
                var.createWarning("Failed to upload file: Response was not OK.");
            }
        }
        catch (Exception e) {
            var.createWarning("Failed to upload file: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Refreshes the list of embedded files displayed in the UI for the selected note.
     * Clears the existing list and repopulates it with updated file data.
     */
    public void refreshEmbeddedFiles() {
        if (var.getSelectedNote() == null || var.getSelectedNote().getEmbeddedFiles() == null) {
            var.getEmbeddedFilesList().getItems().clear();
            return;
        }

        var.getEmbeddedFilesList().getItems().clear();

        // Loop through all embedded files and display them
        var.getSelectedNote().getEmbeddedFiles().forEach(file -> {
            HBox fileItem = new HBox(10); // 10px spacing between elements
            fileItem.setAlignment(Pos.CENTER_LEFT);

            // Label to display the file name
            Label fileNameLabel = new Label(file.getName());
            fileNameLabel.setPrefWidth(200);

            // Rename button
            Button renameButton = new Button("🖍");
            renameButton.setOnAction(event -> var.renameFile(file));

            // Delete button
            Button deleteButton = new Button("🗑");
//            deleteButton.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-text-fill: red;");
            deleteButton.setOnAction(event -> var.deleteFile(file));

            Button moveButton = new Button("⮫");
            moveButton.setOnAction(event -> var.handleMoveFile(file)); // Link to the move file logic

            fileItem.getChildren().addAll(fileNameLabel, deleteButton, renameButton, moveButton);

            // Add the HBox to the ListView
            var.getEmbeddedFilesList().getItems().add(fileItem);
        });
    }


    /**
     * Sends a request to move a file to another note.
     *
     * @param fileId       The ID of the file to move.
     * @param targetNoteId The ID of the target note.
     */
    public void moveFileToAnotherNote(Long fileId, Long targetNoteId) {
        server.moveFileToAnotherNote(fileId, targetNoteId, var.getCurrentPortNumber());
        refreshEmbeddedFiles();
        var.refreshNotes();
    }

    /**
     * Removes a file entry from the description text box by searching for its ID.
     * @param fileId The ID of the file to be removed.
     */
    public void removeEntryFromDescriptionTextBox(Long fileId) {
        String currentText = var.getDescriptionTextBox().getText();
        String[] lines = currentText.split("\n"); // Split description into lines
        StringBuilder updatedText = new StringBuilder();

        // Iterate over each line to find and exclude the file entry
        for (String line : lines) {
            if (!line.contains("[" + fileId + "]")) { // Skip the line containing the file ID
                updatedText.append(line).append("\n");
            }
        }

        // Update the descriptionTextBox content
        var.getDescriptionTextBox().setText(updatedText.toString().trim());
    }

    /**
     * The method ads the text being a file to the targeted note,
     * it takes a target note and a file
     * @param targetNote -> A note to which the file is moving
     * @param file -> A file
     */
    public void addEntryToTargetNoteDescription(Note targetNote, EmbeddedFile file) {
        // Append the file details to the note's description text
        String newEntry = "\n" + "\"" + file.getName() + "\"[" + file.getId() + "]\n";
        System.out.println("Note " + targetNote.getTitle() + " changed from "
                + targetNote.getText() + " to " + targetNote.getText() + newEntry);

        targetNote.setText(targetNote.getText() + newEntry);
        // Update the note in the backend
        server.updateNote(var.getCurrentPortNumber(), targetNote);

    }

    /**
     * Replaces a file entry in the description text box by searching for its ID and updating its name.
     * @param fileId The ID of the file to be renamed.
     * @param newName The new name for the file.
     */
    public void renameEntryInDescriptionTextBox(Long fileId, String newName) {
        String currentText = var.getDescriptionTextBox().getText();

        String[] lines = currentText.split("\n"); // Split description into lines
        StringBuilder updatedText = new StringBuilder();

        // Iterate over each line to find and update the file entry
        for (String line : lines) {
            if (line.contains("[" + fileId + "]")) { // Check if the line contains the file ID
                String updatedLine = line.replaceFirst("^\".*\"", "\"" + newName + "\""); // Update the file name
                updatedText.append(updatedLine).append("\n");
            } else {
                updatedText.append(line).append("\n"); // Keep other lines unchanged
            }
        }
        ;
        // Update the descriptionTextBox content
        var.getDescriptionTextBox().setText(updatedText.toString().trim());
    }


    private final String htmlString = """
<html>
<head>
<style>
    %s
    .tag {
        display: inline-block;
        background-color: #d0d0d0;
        color: #000000;
        border: 1px solid #000000;
        border-radius: 8px;
        padding: 2px 6px;
        margin: 2px;
        font-size: 12px;
        font-family: Arial, sans-serif;
        cursor: pointer;
    }
    .tag:hover {
        background-color: #a0a0a0;
        color: #ffffff;
    }
    .highlight {
        background-color: yellow !important;
        color: #000000;
    }
    a.note-link {
        color: #404040;
        text-decoration: underline;
        cursor: pointer;
    }
    a.note-link:hover {
        color: #303030;
    }
</style>
</head>
<body>
%s
<script>
    // Track registered tags and links to prevent duplicate binding
    let registeredTags = new Set();
    let registeredLinks = new Set();

    function rebindTagClickHandlers() {
        document.querySelectorAll('.tag').forEach(function(tagElement) {
            let tag = tagElement.getAttribute('data-tag') || tagElement.textContent.trim();
            
            if (!registeredTags.has(tag)) {
                tagElement.addEventListener('click', function(event) {
                    event.stopPropagation();
                    if (window.thisApplicationBridge) {
                        try {
                            window.thisApplicationBridge.onTagClicked(tag);
                        } catch (e) {
                            console.error("Tag click failed:", e);
                        }
                    }
                });
                registeredTags.add(tag);
            }
        });

        // Rebind note link click handlers
        document.querySelectorAll('.note-link').forEach(function(linkElement) {
            let noteTitle = linkElement.getAttribute('data-note-title');
            
            if (!registeredLinks.has(noteTitle)) {
                linkElement.addEventListener('click', function(event) {
                    event.preventDefault();
                    if (window.thisApplicationBridge) {
                        try {
                            window.thisApplicationBridge.onNoteLinkClicked(noteTitle);
                        } catch (e) {
                            console.error("Note link click failed:", e);
                        }
                    }
                });
                registeredLinks.add(noteTitle);
            }
        });
    }

    function highlightTags(query) {
        if (!query) return;
        document.querySelectorAll('.tag').forEach(function(element) {
            let originalText = element.textContent || "";
            if (!element.innerHTML.includes('highlight')) {
                element.innerHTML = originalText.replace(
                    new RegExp(query, 'gi'),
                    match => '<span class="highlight">' + match + '</span>'
                );
            }
        });
        rebindTagClickHandlers();
    }

    document.addEventListener('DOMContentLoaded', rebindTagClickHandlers);
</script>
</body>
</html>
        """;


    /**
     *
     * This method constructs an HTML structure with embedded CSS and JavaScript to display
     * note content inside the WebView. It styles tags, highlights search queries dynamically,
     * and enables click interactions with tags and note links. Additionally, JavaFX is bridged
     * with JavaScript to facilitate communication between the WebView and the JavaFX application.
     *
     * Key Features:
     *  - Tags (#tag) are styled as clickable HTML span elements.
     *  - Matching tags are highlighted in yellow during searches.
     *  - Click handlers trigger Java methods via JavaScript for tag and note-link interactions.
     *  - JavaScript dynamically attaches and detaches event listeners to avoid duplication.
     *
     * @param styledContent The HTML content to be injected into the WebView, typically
     *                      generated from Markdown or other text processing.
     */
    public void loadContentIntoWebView(String styledContent) {
        var.getHtmlPreview().getEngine().loadContent("");
        String fullHtml = htmlString.formatted(var.readCssForMarkdown(),styledContent);

        var.getHtmlPreview().getEngine().loadContent(fullHtml, "text/html");

        var.getHtmlPreview().getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) var.getHtmlPreview().getEngine().executeScript("window");
                window.setMember("thisApplicationBridge", new WebBridgeImpl(this.var));

                if (var.getSearchField() != null && !var.getSearchField().getText().isEmpty()) {
                    String script = """
                                        try {
                                        highlightTags('%s');
                                           rebindTagClickHandlers();
                                        } catch (e) {
                                        console.error("Highlight function error:", e);
                                        }
                        """.formatted(var.getSearchField().getText().replace("'", "\\'"));
                    var.getHtmlPreview().getEngine().executeScript(script);
                }
            }
        });
    }

    /**
     * This method is used to call loadContentIntoWebView
     * @param styledContent -> A string
     */
    // Directly updates the WebView content
    public  void refreshWebView(String styledContent) {
        loadContentIntoWebView(styledContent);
    }


    /**
     * This method is called to change a reference to another note,
     * it takes the title of old reference and change it to the new one
     * @param oldReference -> Title of the old note that was referenced
     * @param newReference -> The new title
     */
    public void replaceNoteReference(String oldReference, String newReference) {
        if (var.getSelectedNote() == null) {
            var.createWarning("error_no_note_selected_update");
            return;
        }

        // Get the current text of the selected note
        String currentText = var.getSelectedNote().getText();

        // Replace the old reference with the new one
        String updatedText = currentText.replace("[[" + oldReference + "]]", "[[" + newReference + "]]");

        if (!currentText.equals(updatedText)) {
            // Update the note's text
            var.setSelectedNoteText(updatedText);
            var.getDescriptionTextBox().setText(updatedText); // Update the UI description box

            // Save changes to the database
            var.saveNoteToDatabase();

            // Refresh the UI to reflect changes
            refreshTagsAndPreview();

            var.createWarning("status_reference_replaced");
        } else {
            var.createWarning("error_no_matching_reference");
        }
    }


    /**
     * This function refreshes the HTML preview
     */
    public void refreshHTMLPreview() {
        String styledContent = parseAndReplaceTagsInText(var.markdownToHtml(var.getSelectedNote().getText()));
        var.getHtmlPreview().getEngine().loadContent(styledContent);
    }
}
