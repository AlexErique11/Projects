package client;

import client.Interfaces.*;
import client.utils.ServerUtils;
import commons.Collection;
import commons.EmbeddedFile;
import commons.Note;
import commons.UndoItem;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javafx.collections.FXCollections;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import javax.naming.directory.SearchControls;
import java.io.File;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.*;

import static client.Main.INJECTOR;

@SuppressWarnings("checkstyle:JavadocMethod")
public class Variables implements IVariables {

    @FXML
    private VBox rootVBox;

    @FXML
    private Button arrowDown;

    @FXML
    private Button arrowUp;

    @FXML
    private Button arrowRight;

    @FXML
    private Button clickDelNote;

    @FXML
    private MenuButton collectionMenu;

    @FXML
    private MenuButton colorButton;

    @FXML
    private Button newCollectionButton;

    @FXML
    private TableView<Note> noteContainer;

    @FXML
    private Label statusBar;

    @FXML
    private TableColumn<Note, String> notesColumn;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField editTitleField;

    @FXML
    private TextArea descriptionTextBox;

    @FXML
    private Label titleLabel;

    @FXML
    private CheckBox searchInAllCheckbox;

    @FXML
    private TextField editTextField;

    @FXML
    private MenuItem saveNoteButton;

    @FXML
    private TextField searchField;

    @FXML
    private Button newNoteButton;

    @FXML
    private WebView htmlPreview;

    @FXML
    private TableColumn<Note, String> collectionColumn;

    @FXML
    private Button embedFileButton;

    @FXML
    private ListView<HBox> embeddedFilesList;

    @FXML
    private Button collectionSettingsButton;

    private String nameOfCurrentServer = "MainServer";

    @FXML
    private Button toggleFilesButton;

    private boolean toggleFiles;

    private boolean isMovingFile = false;

    private Note selectedNote = null;
    private Collection currentCollection = null;
    private boolean choice = false;
    private Collection defaultCollection = null;

    static final Logger logger = LoggerFactory.getLogger(Variables.class);

    private RestTemplate restTemplate = new RestTemplate();

    private boolean isSearching = false;

    private final NoteService noteService = new NoteService();

    private String defaultCollectionName = "default";
    private List<UndoItem> undoList = new ArrayList<>();

    private boolean isUpdating = false;
    private RefreshThread refreshThread;

    private Boolean isEditingTitle = false;
    private boolean isRenamingFile = false; // Flag to track renaming operation
    private boolean isCriticalOperationInProgress = false;

    private Boolean isWaitingForAnswerAfterDeletingCollection = false;
    private ObservableList<Note> allNotes = FXCollections.observableArrayList();
    private ObservableList<Note> filteredNotess = FXCollections.observableArrayList();

    private final List<File> generatedHtmlFiles = new ArrayList<>();

    private String currentPortNumber = "8080";

    private int textFieldCounter;
    private boolean isEditing = false;

    private String darkMode = "clientCss/darkMode.css";
    private String lightMode = "clientCss/lightMode.css";
    private String blueMode = "clientCss/blueMode.css";
    private String greenDarkMode = "clientCss/greenDarkMode.css";
    private String redMode = "clientCss/redMode.css";

    private String currentColorMode = lightMode;
    private String cssForMarkdown = "background-color: #ffffff;" +
            "color: #000000";

    private String usersCSS = "";

    @FXML
    private HBox tagContainer;  // VBox to dynamically add ComboBoxes
    private List<ComboBox<String>> tagComboBoxes = new ArrayList<>();
    private ObservableList<String> allTags = FXCollections.observableArrayList();

    private List<Object> undoFileDelList = new ArrayList<>();

    // Tags Filter
    @FXML
    private Button clearTagsButton;
    private Set<String> selectedTags = new HashSet<>();

    private MenuButton languageMenu;
    private Label filterText;
    // Language
    private ResourceBundle bundle;
    private Locale currentLocale;


    // Classes
    private INoteCollectionCtrl nc;
    private IEmbedTagsCtrl embedctrl;
    private ISrchCTCtrl srchctrl;
    private IFxmlController fxmlctr;
    private IMiscCtrl miscCtrl;
    private ServerUtils server;

    @Inject
    public Variables(INoteCollectionCtrl ncProvider,
                     IEmbedTagsCtrl embedctrl,IFxmlController fxmlctr,
                     ISrchCTCtrl srchctrl, IMiscCtrl miscCtrl, ServerUtils server
                     ) {

        this.nc = ncProvider;
        this.embedctrl = embedctrl;
        this.fxmlctr = fxmlctr;
        this.srchctrl = srchctrl;
        this.miscCtrl = miscCtrl;
        this.server = server;
    }

    public Variables(){
    }


    // CHECKSTYLE.OFF

    public HBox getTagContainer(){
        return tagContainer;
    }

    public void clearTagComboBoxes(){
        tagComboBoxes.clear();
    }

    public Button getClearTagsButton(){
        return clearTagsButton;
    }

    public Label getStatusBar(){
        return statusBar;
    }

    public MenuButton getLanguageMenu() {
        return languageMenu;
    }

    public void clearSelectedTags() {
        selectedTags.clear();
    }

    public void setSelectedTags(List<String> selectedTags) {
        this.selectedTags.clear();
        this.selectedTags.addAll(selectedTags);
    }


    public List<Object> getUndoFileDelList() {return this.undoFileDelList;}
    public void addUndoFileDelList(Object o){
        this.undoFileDelList.add(o);
    }
    public void removeLastUndoFileDelList(){this.undoFileDelList.removeLast();}
    public void removeFirstUndoFileDelList(){this.undoFileDelList.removeFirst();}

    public void addNoteToCurrentCollection(Note note){
        this.currentCollection.addNote(note);
    }

    public void setLanguageMenu(MenuButton languageMenu) {
        this.languageMenu = languageMenu;
    }

    public Label getFilterText() {
        return filterText;
    }

    public void setFilterText(Label filterText) {
        this.filterText = filterText;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(Locale currentLocale) {
        this.currentLocale = currentLocale;
    }

    // Getters and Setters

    public Note getSelectedNote() {return selectedNote;}

    public void setSelectedNote(Note selectedNote) {this.selectedNote = selectedNote;}

    public void setSelectedNoteText(String text){
        this.selectedNote.setText(text);
    }

    public void setSelectedNoteTitle(String text){
        this.selectedNote.setTitle(text);
    }

    // choice
    public boolean isChoice() {return choice;}

    public void setChoice(boolean choice) {this.choice = choice;}

    public boolean getChoice(){
        return this.choice;
    }

    // isSearching
    public boolean isSearching() {return isSearching;}

    public void setSearching(boolean searching) {isSearching = searching;}

    public String getUsersCSS(){
        return usersCSS;
    }

    public void setUsersCSS(String s){
        this.usersCSS = s;
    }

    public String readUserCss(){
        return srchctrl.readUserCss();
    }

    // Note Service
    public NoteService getNoteService() {return noteService; }

    public String getDefaultCollectionName() {return defaultCollectionName;}
    // Read-only (final variable, no setter provided)}

    public Collection getCurrentCollection() {return currentCollection;}

    public void setCurrentCollectionId(long id) {this.currentCollection.setId(id);}

    public long getCurrentCollectionId(){return this.currentCollection.getId();}

    public void setCurrentCollection(Collection currentCollection) {this.currentCollection = currentCollection;}

    public void removeNoteFromCurrentCollection(Note note) {
        this.currentCollection.removeNote(note);
    }

    public List<File> getGeneratedHtmlFiles() {
        return generatedHtmlFiles;
    }

    // getDefault collection
    public Collection getDefaultCollection() {return defaultCollection;}

    public void setDefaultCollection(Collection defaultCollection) {this.defaultCollection = defaultCollection;}

    public void setDefaultCollectionCollectionName(String name){
        this.defaultCollection.setName(name);
    }

    public void setDefaultCollectionNotes(List<Note> notes) {
        this.defaultCollection.setNotes(notes);
    }

    public void setDefaultCollectionId(long id) {
        this.defaultCollection.setId(id);
    }

    // Rest template
    public RestTemplate getRestTemplate() {return restTemplate;}

    public void setRestTemplate(RestTemplate restTemplate) {this.restTemplate = restTemplate;}

    // Undo List
    public List<UndoItem> getUndoList() {return undoList;}

    public void setUndoList(List<UndoItem> undoList) {this.undoList = undoList;}

    public void addUndoList(UndoItem undoItem) {
        undoList.add(undoItem);
    }

    public void removeLastUndoList() {
        undoList.removeLast();
    }

    public List<ComboBox<String>> getTagComboBoxes() {return tagComboBoxes;}

    public void setTagComboBoxes(List<ComboBox<String>> tagComboBoxes) {this.tagComboBoxes = tagComboBoxes;}

    public ObservableList<String> getAllTags() {return allTags;}

    public void setAllTags(ObservableList<String> allTags) {this.allTags = allTags;}

    public void setAllAllTags(Set<String> tags){
        this.allTags.setAll(tags);
    }

    public void removeFromAllTags(String comboBoxIndex) {
        this.allTags.remove(comboBoxIndex);
    }

    public Set<String> getSelectedTags() {return selectedTags;}

    public void setSelectedTags(Set<String> selectedTags) {this.selectedTags = selectedTags;}

    public void addSelectedTags(String tag){
        this.selectedTags.add(tag);
    }

    public boolean isUpdating() {return isUpdating;}

    public void setUpdating(boolean updating) {isUpdating = updating;}

    public Logger getLogger(){
        return this.logger;
    }

    public RefreshThread getRefreshThread() {return refreshThread;}

    public void setRefreshThread(RefreshThread refreshThread) {
        this.refreshThread = refreshThread;
    }

    public Boolean getIsEditingTitle() {
        return isEditingTitle;
    }

    public void setIsEditingTitle(Boolean isEditingTitle) {
        this.isEditingTitle = isEditingTitle;
    }

    public boolean isRenamingFile() {
        return isRenamingFile;
    }

    public void setRenamingFile(boolean renamingFile) {
        isRenamingFile = renamingFile;
    }

    public boolean isCriticalOperationInProgress() {
        return isCriticalOperationInProgress;
    }

    public void setCriticalOperationInProgress(boolean criticalOperationInProgress) {
        isCriticalOperationInProgress = criticalOperationInProgress;
    }

    public Boolean getIsWaitingForAnswerAfterDeletingCollection() {
        return isWaitingForAnswerAfterDeletingCollection;
    }

    public void setIsWaitingForAnswerAfterDeletingCollection(Boolean isWaitingForAnswerAfterDeletingCollection) {
        this.isWaitingForAnswerAfterDeletingCollection = isWaitingForAnswerAfterDeletingCollection;
    }

    public ObservableList<Note> getAllNotes() {
        return allNotes;
    }

    public void setAllNotes(ObservableList<Note> allNotes) {
        this.allNotes = allNotes;
    }

    public void sortAllNotes(Comparator<Note> comp) {
        this.allNotes.sort(comp);
    }

    public ObservableList<Note> getFilteredNotess() {
        return filteredNotess;
    }

    public void setFilteredNotess(ObservableList<Note> filteredNotess) {
        this.filteredNotess = filteredNotess;
    }

    public String getCurrentPortNumber() {
        return currentPortNumber;
    }

    public void setCurrentPortNumber(String currentPortNumber) {
        this.currentPortNumber = currentPortNumber;
    }

    public void addSelectedNoteEmbeddedFile(EmbeddedFile ef) {
        this.selectedNote.getEmbeddedFiles().add(ef);
    }

    public void removeSelectedNoteEmbeddedFile(EmbeddedFile ef) {
        this.selectedNote.getEmbeddedFiles().remove(ef);
    }

    public int getTextFieldCounter() {
        return textFieldCounter;
    }

    public void setTextFieldCounter(int textFieldCounter) {
        this.textFieldCounter = textFieldCounter;
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setEditing(boolean editing) {
        isEditing = editing;
    }

    public void initializeFXML(){
        fxmlctr.initialize();
    }

    // Getters for Colors

    public String getDarkMode() {
        return darkMode;
    }

    public String getLightMode() {
        return lightMode;
    }

    public String getBlueMode() {
        return blueMode;
    }

    public String getGreenDarkMode() {
        return greenDarkMode;
    }

    public String getRedMode() {
        return redMode;
    }

    public String getCurrentMode() {
        return currentColorMode;
    }

    public void setCurrentMode(String currentMode) {
        currentColorMode = currentMode;
    }

    public String getCssForMarkdown() {
        return cssForMarkdown;
    }

    public void setCssForMarkdown(String cssForMarkdown) {
        this.cssForMarkdown = cssForMarkdown;
    }

    // FXML getters

    public VBox getRootVBox() {
        return rootVBox;
    }

    public Button getArrowDown() {
        return arrowDown;
    }

    public Button getArrowUp() {
        return arrowUp;
    }

    public Button getArrowRight() {
        return arrowRight;
    }

    public Button getClickDelNote() {
        return clickDelNote;
    }

    public MenuButton getCollectionMenu() {
        return collectionMenu;
    }

    public MenuButton getColorButton() {
        return colorButton;
    }

    public Button getNewCollectionButton() {
        return newCollectionButton;
    }

    public TableView<Note> getNoteContainer() {
        return noteContainer;
    }

    public TableColumn<Note, String> getNotesColumn() {
        return notesColumn;
    }

    public AnchorPane getRootPane() {
        return rootPane;
    }

    public TextField getEditTitleField() {
        return editTitleField;
    }

    public TextArea getDescriptionTextBox() {
        return descriptionTextBox;
    }

    public Label getTitleLabel() {
        return titleLabel;
    }

    public CheckBox getSearchInAllCheckbox() {
        return searchInAllCheckbox;
    }

    public TextField getEditTextField() {
        return editTextField;
    }

    public MenuItem getSaveNoteButton() {
        return saveNoteButton;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Button getNewNoteButton() {
        return newNoteButton;
    }

    public WebView getHtmlPreview() {
        return htmlPreview;
    }

    public TableColumn<Note, String> getCollectionColumn() {
        return collectionColumn;
    }

    public Button getEmbedFileButton() {
        return embedFileButton;
    }

    public ListView<HBox> getEmbeddedFilesList() {
        return embeddedFilesList;
    }

    public Button getCollectionSettingsButton() {
        return collectionSettingsButton;
    }

    public Button getToggleFilesButton() {
        return toggleFilesButton;
    }

    public void setToggleFiles(boolean toggleFiles) {
        this.toggleFiles = toggleFiles;
    }

    public boolean getToggleFiles() {
        return toggleFiles;
    }

    public void setIsMovingFile(boolean movingFile) {
        isMovingFile = movingFile;
    }

    public boolean getIsMovingFile() {
        return isMovingFile;
    }

    public String getNameOfCurrentServer(){
        return this.nameOfCurrentServer;
    }

    public void setNameOfCurrentServer(String name){
        this.nameOfCurrentServer = name;
    }


    // CHECKSTYLE.ON

    /**
     *This method sends a request to database and receive all collections
     * @return an arrayList of collections that are in the database
     */
    public Collection[] getAllCollections(){
        return server.getAllCollection(currentPortNumber);
    }

    /**
     * This method complements the refreshNotes method and fetches all the titles in DB
     * @return an array of titles of all the notes
     */
    public Note[] getNotesDB(){
        return server.getNotesDB(currentPortNumber);
    }

    /**
     * This method fetches all notes of the current collection
     * @return an array of titles of all the notes in the current collection
     */
    public Note[] getNotesCollection(){
        return server.getNotesCollection(currentPortNumber, currentCollection);
    }

    /**
     * Saves the selectedNote to database
     * (part of function taken from handleEditTextFieldAction)
     * This function can also be invoked by saving via the MenuBar > File > Save
     */
    public void saveNoteToDatabase(){
        selectedNote.setText(descriptionTextBox.getText());
        server.saveNoteToDatabase(currentPortNumber, selectedNote);
    }

    public Note[] getNotesFromCollectionsDatabase() {
        return server.getNotesFromCollectionDatabase(currentPortNumber, currentCollection);
    }

    /**
     * Function that gets the collection which contains a given note
     * @param noteId ID of the note for which the collection should be found
     * @return returns the Collection object which is parent to the note
     */
    public Collection getCollectionFromNoteInefficient(int noteId){
        Collection[] allCollt = getAllCollections();
        for (int i = 0; i < allCollt.length; i++) {
            if (allCollt[i].contains(noteId)){
                return allCollt[i];
            }
        }
        return defaultCollection;
    }


    @FXML
    public void undoClick(){
        fxmlctr.undoClick();
    }

    @FXML
    public void handleEditTextFieldAction(ActionEvent actionEvent){
        nc.handleEditTextFieldAction(actionEvent);
    }

    @FXML
    public void arrowRight(){
        fxmlctr.arrowRight();
    }

    @FXML
    public void arrowDown(){
        fxmlctr.arrowDown();
    }

    @FXML
    public void arrowUp() {
        fxmlctr.arrowUp();
    }

    @FXML
    public void clickCollectionSettings() {
        fxmlctr.clickCollectionSettings();
    }

    @FXML
    public void clickDelNote() {
        fxmlctr.clickDelNote();
    }

    @FXML
    public void refreshCollections(){
        fxmlctr.refreshCollections();
    }

    @FXML
    public void selectCollection(String id){
        fxmlctr.selectCollection(id);
    }

    @FXML
    public void clickNewNote() {
        fxmlctr.clickNewNote();
    }

    @FXML
    public void refreshNotes() {
        fxmlctr.refreshNotes();
    }

    // Language switch

    @FXML
    public void switchToEnglish() {
        fxmlctr.switchToEnglish();
    }

    @FXML
    public void switchToDutch() {
        fxmlctr.switchToDutch();
    }

    @FXML
    public void switchToGerman() {
        fxmlctr.switchToGerman();
    }

    @FXML
    public void initialize() {

        System.out.println("Initialised...");


    }

    @FXML
    public void clearAllTags() {
        fxmlctr.clearAllTags();
    }

    @FXML
    public void embedFile() {
        fxmlctr.embedFile();
    }

    @FXML
    public void deleteFile(EmbeddedFile file) {
        fxmlctr.deleteFile(file);
    }

    @FXML
    public void handleMoveFile(EmbeddedFile file) {
        fxmlctr.handleMoveFile(file);
    }


    @FXML
    public void renameFile(EmbeddedFile file) {
        fxmlctr.renameFile(file);
    }

    @FXML
    public void moveFileToAnotherNote(Long fileId, Long targetNoteId) {
        embedctrl.moveFileToAnotherNote(fileId, targetNoteId);
    }


    @FXML
    public void initializeColors(){
        fxmlctr.initializeColors();
    }

    @FXML
    public void escapeButtonSearch(KeyEvent event) {
        fxmlctr.escapeButtonSearch(event);
    }


    // Functions for funneling methods from utility classes to eachother

    /**
     * @see NoteCollectionCtrl#findNoteByTitle(String)
     * @param title Title to find
     * @return returns the Note
     */
    public Note findNoteByTitle(String title) {
        return nc.findNoteByTitle(title);
    }

    /**
     * @see EmbedTagsCtrl#refreshTagsAndPreview()
     */
    public void refreshTagsAndPreview() {
        embedctrl.refreshTagsAndPreview();
    }

    /**
     * @see NoteCollectionCtrl#handleRepeatingTitles(long, Stage)
     * @param repeats Repeats
     * @param popup The popup
     */
    public void handleRepeatingTitles(long repeats, Stage popup) {
        nc.handleRepeatingTitles(repeats, popup);
    }

    /**
     * @see NoteCollectionCtrl#handleMovingNote(Collection)
     * @param selectedCollection The collection that was selected
     */
    public void handleMovingNote(Collection selectedCollection) {
        nc.handleMovingNote(selectedCollection);
    }

    /**
     * @see MiscCtrl#createWarning(String)
     * @param message The message to warn
     */
    public void createWarning(String message) {
        miscCtrl.createWarning(message);
    }

    /**
     * @see NoteCollectionCtrl#updatePosition()
     */
    public void updatePosition() {
        nc.updatePosition();
    }

    /**
     * @see NoteCollectionCtrl#handleDeletionOfCollection(Note, String, boolean)
     * @param note The note to remove
     * @param string String?
     * @param boolean1 Boowlian
     */
    public void handleDeletionOfCollection(Note note, String string, Boolean boolean1) {
        nc.handleDeletionOfCollection(note, string, boolean1);
    }

    /**
     * @see MiscCtrl#createPopup(String)
     * @param message Message to popup
     * @return returns the outcome of the choice
     */
    public boolean createPopup(String message){
        return miscCtrl.createPopup(message);
    }

    /**
     * @see MiscCtrl#setMarkDownTutorial(String)
     * @param text Text to set markdown to
     */
    public void setMarkDownTutorial(String text){
        miscCtrl.setMarkDownTutorial(text);
    }

    /**
     * @see NoteCollectionCtrl#generateNewTitle(String)
     * @param prompt prompt to generate from
     * @return return the new title
     */
    public String generateNewTitle(String prompt){
        return nc.generateNewTitle(prompt);
    }

    /**
     * @see EmbedTagsCtrl#loadContentIntoWebView(String)
     * @param content The content to load
     */
    public void loadContentIntoWebView(String content){
        embedctrl.loadContentIntoWebView(content);
    }

    /**
     * @see EmbedTagsCtrl#initializeTags()
     */
    public void initializeTags() {
        embedctrl.initializeTags();
    }

    /**
     * @see SrchColorTranslateCtrl#switchLanguage(Locale)
     * @param locale Locale?
     */
    public void switchLanguage(Locale locale) {
        srchctrl.switchLanguage(locale);
    }

    /**
     * @see SrchColorTranslateCtrl#loadSavedLanguage()
     * @return returns Locale?
     */
    public Locale loadSavedLanguage() {
        return srchctrl.loadSavedLanguage();
    }

    /**
     * @see SrchColorTranslateCtrl#applyLocalizedText()
     */
    public void applyLocalizedText() {
        srchctrl.applyLocalizedText();
    }

    /**
     * @see NoteCollectionCtrl#createDefaultCollection()
     */
    public void createDefaultCollection() {
        nc.createDefaultCollection();
    }

    /**
     * @see NoteCollectionCtrl#setupEditableNotesColumn()
     */
    public void setupEditableNotesColumn() {
        nc.setupEditableNotesColumn();
    }

    /**
     * @see MiscCtrl#setupKeybindListener()
     */
    public void setupKeybindListener() {
        miscCtrl.setupKeybindListener();
    }

    /**
     * @see MiscCtrl#keyBoardShortcuts(KeyEvent)
     * @param event event that is passed
     */
    public void  keyBoardShortcuts(KeyEvent event) {
        miscCtrl.keyBoardShortcuts(event);
    }

    /**
     * @see MiscCtrl#resolve()
     * @return returns boolean
     */
    public boolean resolve() {
        return miscCtrl.resolve();
    }

    /**
     * @see MiscCtrl#markdownToHtml(String)
     * @param markdown Markdown to pass
     * @return returns the Markdown
     */
    public String markdownToHtml(String markdown) {
        return miscCtrl.markdownToHtml(markdown);
    }

    /**
     * @see NoteCollectionCtrl#getNoteById(Long)
     * @param id id of the note
     * @return returns the Note
     */
    public Note getNoteById(long id) {
        return nc.getNoteById(id);
    }

    /**
     * @see NoteCollectionCtrl#updateNoteInDatabase(Note)
     * @param note Note to update
     */
    public void updateNoteInDatabase(Note note){
        nc.updateNoteInDatabase(note);
    }

    /**
     * @see EmbedTagsCtrl#getFileById(Long)
     * @param id Id of the file
     * @return returns the embedded file
     */
    public EmbeddedFile getFileById(long id) {
        return embedctrl.getFileById(id);
    }

    /**
     * @see MiscCtrl#(Variables)
     * @param var Variables controller
     */
    public void setStateFromOldController(IVariables var) {
        miscCtrl.setStateFromOldController(var);
    }

    /**
     * @see MiscCtrl#showNonBlockingWarning(String)
     * @param message Message to show
     */
    public void showNonBlockingWarning(String message){
        miscCtrl.showNonBlockingWarning(message);
    }

    /**
     * @see EmbedTagsCtrl#handleNoteLinkClick(String)
     * @param title title of the note
     */
    public void handleNoteLinkClick(String title) {
        embedctrl.handleNoteLinkClick(title);
    }

    /**
     * @see EmbedTagsCtrl#addTagComboBox()
     */
    public void addTagComboBox(){
        embedctrl.addTagComboBox();
    }

    /**
     * @see EmbedTagsCtrl#handleTagSelection(ComboBox)
     * @param cb Combobox
     */
    public void handleTagSelection(ComboBox<String> cb){
        embedctrl.handleTagSelection(cb);
    }

    /**
     * @see MiscCtrl#setAvailable()
     */
    public void setAvailable(){
        miscCtrl.setAvailable();
    }

    /**
     * @see SrchColorTranslateCtrl#filterNotes()
     */
    public void filterNotes() {
        srchctrl.filterNotes();
    }

    /**
     * @see EmbedTagsCtrl#refreshEmbeddedFiles()
     */
    public void refreshEmbeddedFiles() {
        embedctrl.refreshEmbeddedFiles();
    }

    /**
     * @see SrchColorTranslateCtrl#changeColor(String)
     */
    public void readSavedColor() {
        srchctrl.readSavedColor();
    }

    /**
     * @see SrchColorTranslateCtrl#changeColor(String)
     * @param color Color to be changed to
     */
    public void changeColor(String color) {
        srchctrl.changeColor(color);
    }

    /**
     * Return a CSS from main.css file
     * @return String
     */
    public String readCssForMarkdown(){
        return srchctrl.readCssForMarkdown();
    }

    /**
     * @see EmbedTagsCtrl#renameEntryInDescriptionTextBox(Long, String)
     * @param id Id of the file to be changed
     * @param name name to be changed to
     */
    public void renameEntryInDescriptionTextBox(long id, String name) {
        embedctrl.renameEntryInDescriptionTextBox(id, name);
    }

    /**
     * @see EmbedTagsCtrl#removeEntryFromDescriptionTextBox(Long)
     * @param id Id of the file to be removed
     */
    public void removeEntryFromDescriptionTextBox(long id) {
        embedctrl.removeEntryFromDescriptionTextBox(id);
    }

    public void addEntryToTargetNoteDescription(Note targetNote, EmbeddedFile file) {
        embedctrl.addEntryToTargetNoteDescription(targetNote, file);
    }


    /**
     * @see NoteCollectionCtrl#generateNewName(String)
     * @param curName requested title
     * @return title
     */
    public String generateNewName(String curName) {
        return nc.generateNewName(curName);
    }

    /**
     * @see NoteCollectionCtrl#deleteNote(Note)
     * @param note The note to delete
     */
    public void deleteNote(Note note) {
        nc.deleteNote(note);
    }

    /**
     * @see NoteCollectionCtrl#updatePrivateCollectionname()
     */
    public void updatePrivateCollectionName() {
        nc.updatePrivateCollectionname();
    }

    /**
     * @see NoteCollectionCtrl#setNotes(ObservableList)
     * @param notes - List of notes to set
     */
    public void setNotes(ObservableList<Note> notes) {
        nc.setNotes(notes);
    }

    /**
     * @see NoteCollectionCtrl#updatePrivateCollectionname()
     */
    public void updatePrivateCollectionname(){
        nc.updatePrivateCollectionname();
    }

    /**
     * @see MiscCtrl#createFormattedWarning(String)
     * @param errorMessage the message for the error
     */
    public void createFormattedWarning(String errorMessage) {
        miscCtrl.createFormattedWarning(errorMessage);
    }

    /**
     * @see EmbedTagsCtrl#uploadFile(HttpHeaders, MultiValueMap)
     * @param header Header of the file
     * @param mvm Body
     * @return id of the file
     */
    public long uploadFile(HttpHeaders header, MultiValueMap<String, Object> mvm){
        return embedctrl.uploadFile(header, mvm);
    }

    /**
     * @see EmbedTagsCtrl#replaceNoteReference(String, String) 
     * @param oldReference The old reference
     * @param newReference The new reference
     */
    public void replaceNoteReference(String oldReference, String newReference) {
        embedctrl.replaceNoteReference(oldReference, newReference);
    }
    public Stage currentStage(){
        return (Stage) rootVBox.getScene().getWindow();
    }

    public void deleteUndoItembyId(long id) {
        server.deleteUndoItembyId(currentPortNumber, id);
    }



}
