package client;

import ch.qos.logback.core.joran.sanity.Pair;
import client.Interfaces.ISrchCTCtrl;
import client.Interfaces.IVariables;
import client.scenes.MainCtrl;
import commons.Note;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.aspectj.weaver.ast.Var;

import java.io.*;
import java.util.*;

import static client.Main.FXML;
import static client.Main.INJECTOR;
import client.Main;

public class SrchColorTranslateCtrl implements ISrchCTCtrl {

    private IVariables var;

    /**
     * Injection-Specific Constructor to {@code SrchCOlorTranslateCtrl} class.
     * @param var Variables interface
     */
    @Inject
    public SrchColorTranslateCtrl(IVariables var) {
        this.var = var;
    }

    /**
     * Apply localized text to all FXML elements dynamically.
     */
    public void applyLocalizedText() {
        // Update all dynamically added ComboBoxes
        for (ComboBox<String> comboBox : var.getTagComboBoxes()) {
            comboBox.setPromptText(var.getBundle().getString("combo.tag.prompt"));
        }
    }

    /**
     * Load FXML with the selected resource bundle for localization.
     * @param locale Takes the current locale
     */
    public void loadFXMLWithBundle(Locale locale) {
        var overview = FXML.load(Variables.class, locale, "client", "scenes", "note.fxml");

        // Set the loaded controller
        Variables oldController = INJECTOR.getInstance(Variables.class);
        Variables newController = overview.getKey();


        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.innitializeNew(overview);

        // Set the new state from the old controller to the new one
        newController.setStateFromOldController(oldController);

        // Save the language preference
        saveLanguagePreference(locale);
    }

    /**
     * @param newLocale The new locale to switch to.
     * Switch language dynamically and apply new localization to UI.
     */
    public void switchLanguage(Locale newLocale) {
        if (var.getCurrentLocale() != null && var.getCurrentLocale().equals(newLocale)) {
            System.out.println("Language is already set to " + newLocale);
            return;
        }

        // Save the currently selected note
        Note previouslySelectedNote = var.getSelectedNote();

        var.setCurrentLocale(newLocale); // Update the current locale
        saveLanguagePreference(newLocale); // Save new locale preference

        // Reload the FXML with the new language
        loadFXMLWithBundle(newLocale);

        // Restore the selected note after reloading the UI
        if (previouslySelectedNote != null) {
            var.setSelectedNote(previouslySelectedNote);

            // Ensure UI elements are updated with the selected note
            var.getNoteContainer().getSelectionModel().select(var.getSelectedNote()); // Re-select the note in the table
            var.getTitleLabel().setText(var.getSelectedNote().getTitle()); // Update the title label
            var.getDescriptionTextBox().setText(var.getSelectedNote().getText()); // Update the description text area

            // Refresh tags and WebView content for the selected note
            var.refreshTagsAndPreview();
        }
    }

    /**
     * Save the selected language in a config file.
     * @param locale takes the current locale
     */
    public void saveLanguagePreference(Locale locale) {
        try (FileWriter writer = new FileWriter("config.properties")) {
            Properties props = new Properties();
            props.setProperty("language", locale.toString());
            props.store(writer, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Load the previously selected language from the config file.
     * @return Locale return 
     */
    public Locale loadSavedLanguage() {
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
     * Function that acts upon the search bar being empty (without any input)
     * @return returns {@code TRUE} when the searchfield is empty, and {@code FALSE} when it contains a search query.
     */
    public boolean checkIfEmpty(){
        if(var.getSearchField().getText().isEmpty()){
            var.getSearchInAllCheckbox().setSelected(false);
        }
        if(var.getSearchInAllCheckbox().isSelected()) {
            var.setAllNotes(FXCollections.observableArrayList(var.getNotesDB()));
            // sets visibility of collectionsColumn based on if the user is typing
/*            if (searchField.getText().isEmpty()) {
                collectionColumn.setVisible(false);
            }
            else {
                collectionColumn.setVisible(true);
            }*/
        }
        else {
            var.setAllNotes(FXCollections.observableArrayList(var.getNotesCollection()));
            //collectionColumn.setVisible(false);
        }
        var.setSearching(true);
        String searchQuery = var.getSearchField().getText();
        if(searchQuery.isEmpty()) {
            // below code might be obsolete, please check if it is truly
            var.getSearchInAllCheckbox().setVisible(false);
            var.setSearching(false);
            var.sortAllNotes(Comparator.comparingInt(Note::getPosition));
            var.getNoteContainer().setItems(var.getAllNotes());
            var.refreshCollections(); // refreshing notes

            if(var.getSelectedNote() == null) {
                return true;
            }
            else {
                var.getDescriptionTextBox().setText(var.getSelectedNote().getText() + " ");
                var.getDescriptionTextBox().setText(var.getSelectedNote().getText());
            }

            return true;
        }
        return false;
    }

    /**
     * This method is called everytime a person types something in the searchbar
     * First, it checks if we didn't delete everything from the search bar - if so
     * it updates all notes to show
     * If we typed something new, it sets the noteContainer to the filtered list
     * it selects the note again and refreshes the description box so the html refreshes
     */
    public void filterNotes() {
        String searchQuery = var.getSearchField().getText().toLowerCase().trim();

        // Reset to all notes if the search bar is cleared
        if (searchQuery.isEmpty()) {
            checkIfEmpty();
            var.sortAllNotes(Comparator.comparingInt(Note::getPosition));
            var.getNoteContainer().setItems(var.getAllNotes());
            return;
        }

        // Fetch notes from the current collection only
//        var.setAllNotes(FXCollections.observableArrayList(var.getNoteContainer().getItems()));

        var.sortAllNotes(Comparator.comparingInt(Note::getPosition));

        // Start with the currently filtered list
        ObservableList<Note> searchResults = FXCollections.observableArrayList(var.getAllNotes());

        // Apply tag filter if tags are selected
        if (!var.getSelectedTags().isEmpty()) {
            searchResults = searchResults.filtered(note ->
                    var.getSelectedTags().stream().allMatch(tag -> note.getText().contains("#" + tag))
            );
        }

        // Apply search filter to the already filtered list
        if (!searchQuery.isEmpty()) {
            searchResults = searchResults.filtered(note ->
                    note.getTitle().toLowerCase().contains(searchQuery) ||
                            note.getText().toLowerCase().contains(searchQuery)
            );
        }

        // Update the table with filtered results
        var.getNoteContainer().setItems(searchResults);

        // Ensure markdown updates after filtering
        if (searchResults.isEmpty()) {
        //    var.createWarning("no_matching_notes");
            var.loadContentIntoWebView("");  // Clear the WebView if nothing matches
        } else {
            Note selected = var.getNoteContainer().getSelectionModel().getSelectedItem();
            if (selected == null && !searchResults.isEmpty()) {
                selected = searchResults.get(0);  // Select the first note by default
                var.getNoteContainer().getSelectionModel().select(0);
            }
            if (selected != null) {
                var.refreshTagsAndPreview();
            }
        }
    }

    /**
     * This is the method for changing color on the whole scene
     * you have to specify the color assocatied with one of the Modes
     * @param s - the color to which the scene will switch
     */
    public void changeColor(String s){

        if(s.equals("light"))
        {
            var.setCurrentMode(var.getLightMode());

            var.getNoteContainer().getScene().getStylesheets().clear();
            //var.getNoteContainer().getScene().getStylesheets().add(var.getCurrentMode());

            var.setCssForMarkdown("background-color: #ffffff;" +
                    "color: #000000;");

        }
        if(s.equals("dark"))
        {
            var.setCurrentMode(var.getDarkMode());

            var.getNoteContainer().getScene().getStylesheets().clear();
            var.getNoteContainer().getScene().getStylesheets().add(var.getCurrentMode());

            var.setCssForMarkdown("background-color: #2b2b2b;" +
                    "color: #e8e6e3;");
        }
        if(s.equals("blue"))
        {
            var.setCurrentMode(var.getBlueMode());
            var.getNoteContainer().getScene().getStylesheets().clear();
            var.getNoteContainer().getScene().getStylesheets().add(var.getCurrentMode());

            var.setCssForMarkdown("background-color: #3b87e3;" +
                    "color: #151313;");
        }
        if(s.equals("green"))
        {
            var.setCurrentMode(var.getGreenDarkMode());
            var.getNoteContainer().getScene().getStylesheets().clear();
            var.getNoteContainer().getScene().getStylesheets().add(var.getCurrentMode());

            var.setCssForMarkdown("background-color: #151414;" +
                    "color: #51ff43;");
        }
        if(s.equals("red"))
        {
            var.setCurrentMode(var.getRedMode());
            var.getNoteContainer().getScene().getStylesheets().clear();
            var.getNoteContainer().getScene().getStylesheets().add(var.getCurrentMode());

            var.setCssForMarkdown("background-color: #ef0606;" +
                    "color: #c19c00;");
        }
        saveCssForMarkdown(var.getCssForMarkdown());
        System.out.println(var.getCssForMarkdown());
        saveColorToFile(var.getCurrentMode());
        System.out.println(var.getCurrentMode());
        var.refreshNotes();
        if(var.getTitleLabel().getText().equals("Select a note") ||
                var.getTitleLabel().getText().equals("Eine Notiz auswählen")||
                var.getTitleLabel().getText().equals("Selecteer een notitie")){
            var.refreshTagsAndPreview();
        }
    }

    /**
     * This method will save very simple css to the main.css
     * for the use of webview
     * @param css
     */
    public void saveCssForMarkdown(String css){
        try {
            PrintWriter writer = new PrintWriter("client/main.css");
            css = ".highlight {\n" +
                    "        background-color: yellow !important;\n" +
                    "        color: #000000;\n" +
                    "    }\nbody { " + css + " }";
            writer.println(css);
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error occurred");
        }
    }


    /**
     * Method for reading CSS for markdown from the file
     * @return String
     */
    public String readCssForMarkdown(){
        String css = "";
        try {
            Scanner scanner = new Scanner(new FileReader("client/main.css"));
            while(scanner.hasNextLine()){
                css = scanner.nextLine();
            }

        } catch (IOException e) {
            System.out.println("Error occurred");
        }
        if(var.getUsersCSS() != ""){
            css = var.getUsersCSS();
        }
        return css;
    }

    /**
     * Reads the user-specified CSS from file
     * @return Returns the CSS String
     */
    public String readUserCss(){
        String css = "";
        try {
            Scanner scanner = new Scanner(new FileReader("client/user.css"));
            while(scanner.hasNextLine()){
                css = css + scanner.nextLine();
            }

        } catch (IOException e) {
            System.out.println("Error occurred");
        }
        return css;
    }


    /**
     * Method for saving the latest chosen color to the file,
     * it can be then read and used at the initialization
     * @param s
     */
    public void saveColorToFile(String s){
        try {
            PrintWriter writer = new PrintWriter("client/color.txt");
            writer.println(s);
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error occurred");
        }
    }

    /**
     * Method for getting the latest choosen color to the txt file,
     * part of the user preferences.
     */
    public void readSavedColor(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader("client/color.txt"));
            String line = reader.readLine();
            if(line != null)
                if(line.equals(var.getLightMode()) || line.equals(var.getDarkMode()) || line.equals(var.getBlueMode())
                        || line.equals(var.getGreenDarkMode()) || line.equals(var.getRedMode())){
                    var.setCurrentMode(line);
                }
                else {
                    var.setCurrentMode(var.getLightMode());
                }
        } catch (IOException e) {
            System.out.println("Error occurred");
        }
    }
}
