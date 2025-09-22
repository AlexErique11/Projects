package client;

import client.Interfaces.IMiscCtrl;
import client.Interfaces.IVariables;
import client.utils.ServerUtils;
import commons.*;
import jakarta.inject.Inject;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscCtrl implements IMiscCtrl {
    private IVariables var;
    private ServerUtils server;

    /**
     * General constructor for Miscelaneous Control
     * @param var Variables interface
     * @param server Server Utils Interface
     */
    @Inject
    public MiscCtrl(IVariables var, ServerUtils server) {
        this.var = var;
        this.server = server;
    }

    /**
     * This method creates a popup with a given text and yes / no choice options
     * @param text text which should be included in the popup
     * @return choice - boolean choice
     */
    public boolean createPopup(String text){
        var.setChoice(false);
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle(var.getBundle().getString("warning_title"));
        Label confirmationText = new Label(text);
        Label spaces = new Label("   ");
        Button yesButton = new Button(var.getBundle().getString("yes"));
        Button noButton = new Button(var.getBundle().getString("no"));
        yesButton.setOnMouseClicked((MouseEvent event) -> {
            popupStage.close();
            var.setChoice(true);
        });
        yesButton.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                popupStage.close();
                var.setChoice(true);
            }
        });
        noButton.setOnMouseClicked((MouseEvent event) -> {
            popupStage.close();
            var.setChoice(false);
        });
        noButton.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                popupStage.close();
                var.setChoice(false);
            }
        });
        HBox buttonLayout = new HBox();
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.getChildren().addAll(
                yesButton,
                spaces,
                noButton
        );
        VBox confirmationLayout = new VBox(20);
        confirmationLayout.setAlignment(Pos.CENTER);
        confirmationLayout.getChildren().addAll(confirmationText, buttonLayout);
        Scene scene = new Scene(confirmationLayout, 400, 100);
        popupStage.setScene(scene);
        popupStage.showAndWait();
        return var.getChoice();
    }

    private void handleDeleteFileForResolve(UndoItem undoItem, UndoItem[] undoActions) {
        if(var.getUndoFileDelList().size() >= 3) {
            var list = var.getUndoFileDelList();
            int position = 0;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getClass().equals(EmbeddedFile.class)) {
                    EmbeddedFile file = (EmbeddedFile) list.get(i);
                    if (file.getId() == undoItem.getFileId()) {
                        position = i;
                    }
                }
            }
            if(position != 0) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String, Object> body =
                        (MultiValueMap<String, Object>) var.getUndoFileDelList().get(position - 2);
                var.getUndoFileDelList().remove(position);
                var.getUndoFileDelList().remove(position - 1);
                var.getUndoFileDelList().remove(position - 2);

                long fileId = var.uploadFile(headers, body);
                String fileName = var.getFileById(fileId).getName();
                long id = undoItem.getFileId();
                for (UndoItem undo : undoActions) {
                    if (undo.getFileId() == id && undoItem.getId() != undo.getId()) {
                        var.deleteUndoItembyId(undo.getId());
                    }
                    if (undo.getType().equals("typing")) {
                        String currentText = undo.getOldName();
                        String[] lines = currentText.split("\n"); // Split description into lines
                        StringBuilder updatedText = new StringBuilder();
                        for (String line : lines) {
                            if (!line.contains("[" + undoItem.getFileId() + "]")) {
                                // Skip the line containing the file ID
                                updatedText.append(line).append("\n");
                            } else {
                                updatedText.append("\"").append(fileName).append("\"").append("[")
                                        .append(fileId).append("]").append("\n");
                            }
                        }
                        undo.setOldName(String.valueOf(updatedText));
                        server.postUndoItem(undo, var.getCurrentPortNumber());
                    }
                }
            }
            server.deleteUndoItem(var.getCurrentPortNumber());
        }else{
            createWarning("File deletion could not be undone.\n" +
                    "Deleted files are not stored after the app is closed.");
        }
    }

    private void handleRenameFileForResolve(UndoItem undoItem) {
        EmbeddedFile file = var.getFileById(undoItem.getFileId());
        var.setRenamingFile(true); // Set flag to prevent UI refresh
        try {
            HttpEntity<String> request = new HttpEntity<>(undoItem.getOldName());
            ResponseEntity<EmbeddedFile> response = server.getResponsePut(request,
                    var.getCurrentPortNumber(), file.getId());
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                file.setName(undoItem.getOldName());
                var.renameEntryInDescriptionTextBox(file.getId(), undoItem.getOldName());
                var.refreshEmbeddedFiles();
            }
        } catch (Exception e) {
            String errorMessage = String.format(var.getBundle()
                    .getString("error_failed_file_rename"), e.getMessage());
            var.createFormattedWarning(errorMessage);
        }
        finally {
            var.setRenamingFile(false); // Reset flag after renaming is complete
        }
    }

    private void handleRenameAddAndRenameForResolve(UndoItem undoItem, Note selected) {
        if(undoItem.getType().equals("renameFile")){
            handleRenameFileForResolve(undoItem); // using another function to lower the cyclo complexity.
        }
        else if(undoItem.getType().equals("addFile")){
            if(var.getFileById(undoItem.getFileId()) != null) {
                var.deleteFile(var.getFileById(undoItem.getFileId()));
                server.deleteFile(var.getCurrentPortNumber());
            }
        }
        else if(undoItem.getType().equals("renameNote")){
            selected.setTitle(undoItem.getOldName());
            var.updateNoteInDatabase(selected);
            var.getNoteContainer().refresh();
        }
    }

    /**
     * This method resolves and deletes the last (most recent) undoItem
     * Mapped to ctrl + z
     * @return check for whether the method was resolved as expected
     */
    public boolean resolve(){

        UndoItem[] undoActions = server.getUndoActions(var.getCurrentPortNumber());

        if(undoActions == null || undoActions.length == 0){
            return false;
        }
        var.setUndoList((List.of(undoActions)));
        UndoItem undoItem = var.getUndoList().getLast();
        Note selected = var.getNoteById(undoItem.getNote());
        if(undoItem.getType().equals("move")){
            var.refreshNotes();
            int currentPosition = selected.getPosition();
            ObservableList<Note> notes = var.getNoteContainer().getItems();
            try {
                if(currentPosition > undoItem.getPosition() && currentPosition - 1 >= 0) {
                    notes.set(currentPosition, notes.get(currentPosition - 1));
                    notes.set(currentPosition - 1, selected);
                }
                if(currentPosition < undoItem.getPosition() && currentPosition + 1 < notes.size()) {
                    notes.set(currentPosition, notes.get(currentPosition + 1));
                    notes.set(currentPosition + 1, selected);
                }
                var.updatePosition();
            }catch(Exception e){
                createWarning("error_request_not_resolved");
                return false;
            }
        }
        else if(undoItem.getType().equals("typing")){
            selected.setText(undoItem.getOldName());
            var.updateNoteInDatabase(selected);
        }
        else if(undoItem.getType().equals("delFile")) {
            handleDeleteFileForResolve(undoItem, undoActions); // using another function lower the cyclomatic cmpx.
        }
        else if(undoItem.getType().equals("moveCol")){
            server.resolveMove(var.getCurrentPortNumber(), selected, undoItem.getCollectionId());
            Note temp = var.getSelectedNote();
            var.selectCollection("" + undoItem.getCollectionId());
            var.updateNoteInDatabase(selected);
            var.refreshNotes();
        } else {
            handleRenameAddAndRenameForResolve(undoItem, selected);
        }
        var.refreshNotes();
        var.getNoteContainer().getSelectionModel().select(selected);
        server.deleteUndoItem(var.getCurrentPortNumber());
        return true;
    }

    /**
     * This method creates a warning that stays on the screen with the
     * provided String
     * @param s String text of a warning
     */
    public void createWarning(String s){
        String warningMessage;

        try {
            // Attempt to fetch the localized text using the key
            warningMessage = var.getBundle().getString(s);
        } catch (MissingResourceException e) {
            // Fallback: Use the input string directly if the key is not found
            warningMessage = s;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(var.getBundle().getString("warning_title")); // Localized title
        alert.setContentText(warningMessage); // Localized message
        alert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = alert.showAndWait();
    }

    /**
     * This method creates a warning that stays on the screen with the
     * provided formatted message
     * @param formattedMessage String text of a warning
     */
    public void createFormattedWarning(String formattedMessage) {
        // Directly use the formatted message
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(var.getBundle().getString("warning_title")); // Localized title
        alert.setContentText(formattedMessage); // Use the provided message
        alert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = alert.showAndWait();
    }

    /**
     * Sets the state set by the old controller and applies it to the new one
     * @param oldController The reference to the old controller
     */
    public void setStateFromOldController(IVariables oldController) {
//        var.setSelectedNote(oldController.getSelectedNote());
        var.setCurrentCollection(oldController.getCurrentCollection());
        var.setAllNotes(oldController.getAllNotes());
        var.setDefaultCollection(oldController.getDefaultCollection());
        var.setCurrentLocale(oldController.getCurrentLocale());
        var.setBundle(oldController.getBundle());
        var.setRestTemplate(oldController.getRestTemplate());

        // Restore the selected note in the UI
        if (var.getSelectedNote() != null) {
            var.getNoteContainer().getSelectionModel().select(var.getSelectedNote());
            var.getTitleLabel().setText(var.getSelectedNote().getTitle());
            var.getDescriptionTextBox().setText(var.getSelectedNote().getText());
            var.refreshTagsAndPreview();
        }
    }

    /**
     * This method handles some of the keyboard shortcuts such as moving from the search bar to
     * the notes, from the  notes to add button.
     * @param event A button clicked on the keyboard
     */
    public void keyBoardShortcuts(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            var.getSearchField().requestFocus();
            event.consume();
        }
        else if(event.getCode() == KeyCode.RIGHT && var.getSearchField().isFocused()){
            var.getTagComboBoxes().get(0).requestFocus();
        }
        else if(var.getTagComboBoxes().getFirst().isFocused()){
            tagsKeyboardShortcuts(event);
        }
        else if((event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.EQUALS) &&
                var.getCurrentCollection().getId() != 0){
            var.clickNewNote();
        }
        else if(event.getCode() == KeyCode.C && !var.getDescriptionTextBox().isFocused()
                && !var.getSearchField().isFocused() && !var.getIsEditingTitle()){
            var.getCollectionMenu().requestFocus();
        }
        else if(event.getCode() == KeyCode.Z && event.isControlDown()){
            var.resolve();
        }
        else if(event.getCode() == KeyCode.RIGHT &&
                var.getCollectionSettingsButton().isFocused()){
            var.getTagComboBoxes().getFirst().requestFocus();
            event.consume();
        } else {
            keyBoardShortcuts2(event);
        }
    }

    /**
     * Second keyboardshortcuts function
     * @param event Event that captures keyevent
     */
    private void keyBoardShortcuts2 (KeyEvent event) {
        if (event.getCode() == KeyCode.UP &&
                var.getSearchField().isFocused()) {
            var.getCollectionMenu().requestFocus();
        }
        else if(event.getCode() == KeyCode.RIGHT &&
                var.getCollectionMenu().isFocused()){
            var.getCollectionSettingsButton().requestFocus();
            event.consume();
        }
        else if (event.getCode() == KeyCode.DOWN &&
                var.getSearchField().isFocused()) {
            if(var.getNoteContainer().getItems() != null) {
                var.getNoteContainer().getSelectionModel().select(0);
                var.getNoteContainer().requestFocus();
            }
        }
        else if (event.getCode() == KeyCode.DOWN &&
                var.getNoteContainer().getItems() != null &&
                var.getNoteContainer().getSelectionModel().getSelectedIndex() ==
                        var.getNoteContainer().getItems().size() - 1) {
            var.getNewNoteButton().requestFocus();
        }
        else if(event.getCode() == KeyCode.DOWN && var.getCollectionMenu().isFocused()) {
            var.getSearchField().requestFocus();
            event.consume();
        }
        else{
            keyBoardShortcutsBugs(event);
        }
    }

    /**
     * Listens for keyboard shortcuts and fires certain events when it hears them.
     * @param event KeyEvent
     */
    public void tagsKeyboardShortcuts(KeyEvent event) {
        if(event.getCode() == KeyCode.DOWN){
            var.getDescriptionTextBox().requestFocus();
            event.consume();
        }
        else if(event.getCode() == KeyCode.RIGHT){
            var.getClearTagsButton().requestFocus();
        }
        else if(event.getCode() == KeyCode.ENTER){
            var.getTagComboBoxes().getFirst().show();
        }
        else if(event.getCode() == KeyCode.LEFT){
            var.getSearchField().requestFocus();
        }
        else{
            keyBoardShortcutsBugs(event);
        }
    }

    /**
     * This method handles different keyboardShortcuts and ways the user can try to bug them
     * It handles cycling through collections and being able to type and go back to notes.
     * @param event Event that captures keycode
     */
    public void keyBoardShortcutsBugs(KeyEvent event) {
        if ((event.getCode() == KeyCode.DELETE) &&
                !var.getNoteContainer().getItems().isEmpty() &&
                var.getNoteContainer().getSelectionModel().getSelectedIndex() != -1 &&
                var.getCurrentCollection().getId() != 0){
            var.clickDelNote();
            if(var.getNoteContainer().getItems().isEmpty()) {
                String warningMessage = "Select a note";

                try {
                    // Attempt to fetch the localized text using the key
                    warningMessage = var.getBundle().getString("noTitle");
                } catch (MissingResourceException e) {
                }
                var.getTitleLabel().setText(warningMessage);
                var.getDescriptionTextBox().setVisible(false);
            }
        }
        else if (event.getCode() == KeyCode.UP &&
                var.getDescriptionTextBox().isFocused() &&
                var.getDescriptionTextBox().getCaretPosition() == 0) {
            var.getTagComboBoxes().getFirst().requestFocus();
        }
        else if (event.getCode() == KeyCode.UP &&
                var.getNoteContainer().getItems() != null &&
                var.getNoteContainer().getSelectionModel().getSelectedIndex() == 0){
            var.getSearchField().requestFocus();
        }
        else if(event.getCode() == KeyCode.RIGHT &&
                var.getNoteContainer().getItems() != null &&
                var.getNoteContainer().getSelectionModel().getSelectedIndex()<
                        var.getNoteContainer().getItems().size()) {
            var.getDescriptionTextBox().requestFocus();
            var.setSelectedNote(var.getNoteContainer().getSelectionModel().getSelectedItem());
        }
        else if(var.getNoteContainer().isFocused() && event.getCode() == KeyCode.UP
                && var.getNoteContainer().getItems().isEmpty()){
            var.getSearchField().requestFocus();
        }
        else if(event.getCode() == KeyCode.LEFT &&
                var.getDescriptionTextBox().isFocused() &&
                var.getDescriptionTextBox().getCaretPosition() == 0){
            if(var.getNoteContainer().getItems() != null &&
                    var.getNoteContainer().getSelectionModel().getSelectedIndex()<
                            var.getNoteContainer().getItems().size()){
                var.getNoteContainer().getSelectionModel().select(var.getSelectedNote());
                var.getNoteContainer().requestFocus();
            }
            else{
                var.getArrowRight().requestFocus();
            }
        }
    }

    /**
     * Sets the markdown tutorial
     * @param s String to set the markdown tutorial to
     */
    public void setMarkDownTutorial(String s) {
        String warningMessage;

        try {
            // Attempt to fetch the localized text using the key
            warningMessage = var.getBundle().getString(s);
        } catch (MissingResourceException e) {
            // Fallback: Use the input string directly if the key is not found
            warningMessage = s;
        }
        var.getHtmlPreview().getEngine().
                loadContent(markdownToHtml(warningMessage),
                        "text/html");
    }

    /**
     * Non-blocking warning to prevent reset
     * @param message The message to show in the blocking warning
     */
    public void showNonBlockingWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Matching Notes");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    /** Markdown to HTML to String
     * Function that takes a String as input (note text) and converts this to HTML,
     * to be able to preview it in JavaFX, as it doesn't support direct Common Markdown
     * @param markdown String input by user, formatted as Common markdown
     * @return returns the HTML markdown
     */
    public String markdownToHtml(String markdown) {
        if (markdown == null) {
            return "proba";
        }

        // Match references to files like "name"[id]
        StringBuilder updatedMarkdown = new StringBuilder();
        Pattern pattern = Pattern.compile("\"(.*?)\"\\[(\\d+)]"); // Match "name"[id]
        Matcher matcher = pattern.matcher(markdown);

        int lastMatchEnd = 0;
        while (matcher.find()) {
            // Append the text before the current match
            updatedMarkdown.append(markdown, lastMatchEnd, matcher.start());

            String name = matcher.group(1); // Extract the file name
            Long id = Long.parseLong(matcher.group(2)); // Extract the file ID
            EmbeddedFile file = var.getFileById(id); // Fetch file details by ID

            if (file != null) {
                // Generate a download link for all files
                updatedMarkdown.append("[\"").append(name).append("\"](http://localhost:"
                                +var.getCurrentPortNumber()+"/files/")
                        .append(id).append("/download)");

                // Generate an <img> tag for image files
                if (file.getType().startsWith("image/")) {
                    updatedMarkdown.append("<br><img src=\"http://localhost:"+var.getCurrentPortNumber()+"/files/")
                            .append(id).append("/preview\" alt=\"").append(name)
                            .append("\" style=\"max-width: 100%;\" />");
                }
            } else {
                // Handle missing file (optional)
                updatedMarkdown.append("[\"").append(name).append("\"](http://localhost:"
                        +var.getCurrentPortNumber()+"/files/").append(id).append("/download)");
            }

            lastMatchEnd = matcher.end(); // Update the end position of the last match
        }

        // Append the remaining part of the text after the last match
        updatedMarkdown.append(markdown.substring(lastMatchEnd));

        // Parse the markdown and render as HTML
        Parser parser = Parser.builder().build();
        Node document = parser.parse(updatedMarkdown.toString());
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String htmlContent = renderer.render(document);

        if(!(var.getSelectedNote() == null)) {
            // Save the HTML content to a file
            saveHtmlToFile(htmlContent, var.getSelectedNote().getId());
        }

        return """
        <html>
        <head>
            <style>
            %s
            </style>
        </head>
        <body>
            %s
        </body>
        </html>
        """.formatted(var.readCssForMarkdown(), htmlContent);
    }

    /**
     * Save HTML to a file which is designated
     * @param htmlContent The html content to save to the file
     * @param noteId The note id
     */
    public void saveHtmlToFile(String htmlContent, Long noteId) {
        try {
            // Define the file path based on the note ID
            File file = new File("notes/" + noteId + ".html");

            // Ensure the directory exists
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            // Write the HTML content to the file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(htmlContent);
            }


            // Add the file to the list of generated files if not already present
            if (!var.getGeneratedHtmlFiles().contains(file)) {
                var.getGeneratedHtmlFiles().add(file);
                System.out.println("HTML file saved: " + file.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Failed to save HTML file: " + e.getMessage());
        }
    }

    /**
     * Sets up the KeybindListener
     */
    public void setupKeybindListener() {
        new KeyBindListener(var.getDescriptionTextBox());
    }


    /**
     * This method activates the collection button when the scene is closed. It is called
     * in the collection Scene controller
     */
    public void setAvailable() {
        var.getCollectionSettingsButton().setDisable(false);
    }


}
