package client.Interfaces;

import commons.EmbeddedFile;
import commons.Note;
import commons.UndoItem;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.control.TextInputDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.Optional;

public interface IFxmlController {

    void undoClick();

    void arrowRight();

    void arrowDown();

    void arrowUp();

    void clickCollectionSettings();

    void clickDelNote();

    void selectCollection(String id);

    void clickNewNote();

    void refreshNotes();

    void switchToEnglish();

    void switchToDutch();

    void switchToGerman();

    void selectAllNotes();

    // Methods related to file management
    void embedFile();

    void deleteFile(EmbeddedFile file);

    void renameFile(EmbeddedFile file);

    void handleMoveFile(EmbeddedFile file);

    // Methods related to color theme initialization
    void initializeColors();

    // Methods related to tag handling
    void escapeButtonSearch(KeyEvent event);

    void clearAllTags();


    void refreshCollections();

    void initialize();

}
