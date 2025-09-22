package client;

import client.Interfaces.IVariables;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;

public class WebBridgeImpl {
    private final IVariables controller;

    /**
     * Injector-Specific Constructor for this class
     * @param controller The Variables Interface
     */
    @Inject
    public WebBridgeImpl(IVariables controller) {
        this.controller = controller;
    }

    /**
     * Called when a tag is clicked
     * @param noteTitle The title of the note
     */
    public void onNoteLinkClicked(String noteTitle) {
        System.out.println("JS to Java bridge received title: " + noteTitle);
        Platform.runLater(() -> controller.handleNoteLinkClick(noteTitle));
    }

    /**
     * Modify WebBridgeImpl to clear search and apply tag filter
     * @param tag Tag that was clicked
     */
    public void onTagClicked(String tag) {
        Platform.runLater(() -> {
            String cleanTag = tag.startsWith("#") ? tag.substring(1) : tag;

            // If the tag is already selected, prevent unnecessary filtering
            if (controller.getSelectedTags().contains(cleanTag)) {
                return;
            }

            // Locate an empty ComboBox or add a new one if necessary
            ComboBox<String> targetComboBox = controller.getTagComboBoxes().stream()
                    .filter(cb -> cb.getValue() == null)
                    .findFirst()
                    .orElseGet(() -> {
                        controller.addTagComboBox();  // Add new ComboBox if none are available
                        return controller.getTagComboBoxes().get(controller.getTagComboBoxes().size() - 1);
                    });

            // Set the selected value and trigger UI update
            targetComboBox.setValue(cleanTag);
            controller.handleTagSelection(targetComboBox);
        });
    }

    /**
     * Replace references of notes in text with their new name
     * @param oldReference Old name
     * @param newReference New name
     */
    public void onReferenceReplace(String oldReference, String newReference) {
        Platform.runLater(() -> controller.replaceNoteReference(oldReference, newReference));
    }


}