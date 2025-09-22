package client.Interfaces;

import client.NoteService;
import client.RefreshThread;
import client.Variables;
import commons.Collection;
import commons.EmbeddedFile;
import commons.Note;
import commons.UndoItem;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.File;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface IVariables {

    public String defaultCollectionName = "default";

    // Getters and Setters for UI elements
    HBox getTagContainer();
    void clearTagComboBoxes();
    Button getClearTagsButton();
    Label getStatusBar();
    MenuButton getLanguageMenu();
    void clearSelectedTags();
    List<Object> getUndoFileDelList();
    void addUndoFileDelList(Object o);
    void removeLastUndoFileDelList();
    void removeFirstUndoFileDelList();
    void addNoteToCurrentCollection(Note note);
    void setLanguageMenu(MenuButton languageMenu);
    Label getFilterText();
    void setFilterText(Label filterText);
    ResourceBundle getBundle();
    void setBundle(ResourceBundle bundle);
    Locale getCurrentLocale();
    void setCurrentLocale(Locale currentLocale);
    long getCurrentCollectionId();
    String getNameOfCurrentServer();
    void setNameOfCurrentServer(String name);
    String readCssForMarkdown();

    // Getters and Setters for Notes and Collections
    Note getSelectedNote();
    void setSelectedNote(Note selectedNote);
    void setSelectedNoteText(String text);
    void setSelectedNoteTitle(String text);
    boolean isChoice();
    void setChoice(boolean choice);
    boolean getChoice();
    boolean isSearching();
    void setSearching(boolean searching);
    NoteService getNoteService();
    String getDefaultCollectionName();
    Collection getCurrentCollection();
    void setCurrentCollectionId(long id);
    void setCurrentCollection(Collection currentCollection);
    void removeNoteFromCurrentCollection(Note note);
    List<File> getGeneratedHtmlFiles();
    Collection getDefaultCollection();
    void setDefaultCollection(Collection defaultCollection);
    void setDefaultCollectionCollectionName(String name);
    void setDefaultCollectionNotes(List<Note> notes);
    void setDefaultCollectionId(long id);
    void initializeFXML();
    RestTemplate getRestTemplate();
    void setRestTemplate(RestTemplate restTemplate);
    List<UndoItem> getUndoList();
    void setUndoList(List<UndoItem> undoList);
    void addUndoList(UndoItem undoItem);
    void removeLastUndoList();
    List<ComboBox<String>> getTagComboBoxes();
    void setTagComboBoxes(List<ComboBox<String>> tagComboBoxes);
    ObservableList<String> getAllTags();
    void setAllTags(ObservableList<String> allTags);
    void setAllAllTags(Set<String> tags);
    void removeFromAllTags(String comboBoxIndex);
    Set<String> getSelectedTags();
    void setSelectedTags(Set<String> selectedTags);
    void addSelectedTags(String tag);
    boolean isUpdating();
    void setUpdating(boolean updating);
    Logger getLogger();
    RefreshThread getRefreshThread();
    void setRefreshThread(RefreshThread refreshThread);
    Boolean getIsEditingTitle();
    void setIsEditingTitle(Boolean isEditingTitle);
    boolean isRenamingFile();
    void setRenamingFile(boolean renamingFile);
    boolean isCriticalOperationInProgress();
    void setCriticalOperationInProgress(boolean criticalOperationInProgress);
    Boolean getIsWaitingForAnswerAfterDeletingCollection();
    void setIsWaitingForAnswerAfterDeletingCollection(Boolean isWaitingForAnswerAfterDeletingCollection);
    ObservableList<Note> getAllNotes();
    void setAllNotes(ObservableList<Note> allNotes);
    void sortAllNotes(Comparator<Note> comp);
    ObservableList<Note> getFilteredNotess();
    void setFilteredNotess(ObservableList<Note> filteredNotess);
    String getCurrentPortNumber();
    void setCurrentPortNumber(String currentPortNumber);
    void addSelectedNoteEmbeddedFile(EmbeddedFile ef);
    void removeSelectedNoteEmbeddedFile(EmbeddedFile ef);
    int getTextFieldCounter();
    void setTextFieldCounter(int textFieldCounter);
    boolean isEditing();
    void setEditing(boolean editing);

    // Getters for Colors
    String getDarkMode();
    String getLightMode();
    String getBlueMode();
    String getGreenDarkMode();
    String getRedMode();
    String getCurrentMode();
    void setCurrentMode(String currentMode);
    String getCssForMarkdown();
    void setCssForMarkdown(String cssForMarkdown);
    String getUsersCSS();
    void setUsersCSS(String usersCSS);

    // FXML Getters
    VBox getRootVBox();
    Button getArrowDown();
    Button getArrowUp();
    Button getArrowRight();
    Button getClickDelNote();
    MenuButton getCollectionMenu();
    MenuButton getColorButton();
    Button getNewCollectionButton();
    TableView<Note> getNoteContainer();
    TableColumn<Note, String> getNotesColumn();
    AnchorPane getRootPane();
    TextField getEditTitleField();
    TextArea getDescriptionTextBox();
    Label getTitleLabel();
    CheckBox getSearchInAllCheckbox();
    TextField getEditTextField();
    MenuItem getSaveNoteButton();
    TextField getSearchField();
    Button getNewNoteButton();
    WebView getHtmlPreview();
    TableColumn<Note, String> getCollectionColumn();

    // UI Component Getters for Files
    Button getEmbedFileButton();
    ListView<HBox> getEmbeddedFilesList();
    Button getCollectionSettingsButton();
    Button getToggleFilesButton();
    void setToggleFiles(boolean toggleFiles);
    boolean getToggleFiles();
    void setIsMovingFile(boolean movingFile);
    boolean getIsMovingFile();

    // Methods for Database Interactions
    Collection[] getAllCollections();
    Note[] getNotesDB();
    Note[] getNotesCollection();
    void saveNoteToDatabase();
    Note[] getNotesFromCollectionsDatabase();
    Collection getCollectionFromNoteInefficient(int noteId);

    // FXML event handlers
    void undoClick();
    void handleEditTextFieldAction(ActionEvent actionEvent);
    void arrowRight();
    void arrowDown();
    void arrowUp();
    void clickCollectionSettings();
    void clickDelNote();
    void refreshCollections();
    void selectCollection(String id);
    void clickNewNote();
    void refreshNotes();

    // Language Switch
    void switchToEnglish();
    void switchToDutch();
    void switchToGerman();
    void initialize();

    // File Handling
    void clearAllTags();
    void embedFile();
    void deleteFile(EmbeddedFile file);
    void handleMoveFile(EmbeddedFile file);
    void renameFile(EmbeddedFile file);
    void moveFileToAnotherNote(Long fileId, Long targetNoteId);
    void initializeColors();
    void escapeButtonSearch(KeyEvent event);

    // Utility Methods
    Note findNoteByTitle(String title);
    void refreshTagsAndPreview();
    void handleRepeatingTitles(long repeats, Stage popup);
    void handleMovingNote(Collection selectedCollection);
    void createWarning(String message);
    void updatePosition();
    void handleDeletionOfCollection(Note note, String string, Boolean boolean1);
    boolean createPopup(String message);
    void setMarkDownTutorial(String text);
    String generateNewTitle(String prompt);
    void loadContentIntoWebView(String content);
    void initializeTags();
    void switchLanguage(Locale locale);
    Locale loadSavedLanguage();
    void applyLocalizedText();
    void createDefaultCollection();
    void setupEditableNotesColumn();
    void setupKeybindListener();
    void keyBoardShortcuts(KeyEvent event);
    boolean resolve();
    String markdownToHtml(String markdown);
    Note getNoteById(long id);
    void updateNoteInDatabase(Note note);
    EmbeddedFile getFileById(long id);
    void setStateFromOldController(IVariables var);
    void showNonBlockingWarning(String message);
    void handleNoteLinkClick(String title);
    void addTagComboBox();
    void handleTagSelection(ComboBox<String> cb);
    void setAvailable();
    void filterNotes();
    void refreshEmbeddedFiles();
    void readSavedColor();
    String readUserCss();
    void changeColor(String color);
    void renameEntryInDescriptionTextBox(long id, String name);
    void removeEntryFromDescriptionTextBox(long id);
    void addEntryToTargetNoteDescription(Note targetNote, EmbeddedFile file);
    String generateNewName(String curName);
    void deleteNote(Note note);
    void updatePrivateCollectionName();
    void setNotes(ObservableList<Note> notes);
    void updatePrivateCollectionname();
    void createFormattedWarning(String errorMessage);
    long uploadFile(HttpHeaders header, MultiValueMap<String, Object> mvm);
    void replaceNoteReference(String oldReference, String newReference);
    Stage currentStage();
    void deleteUndoItembyId(long id);
}
