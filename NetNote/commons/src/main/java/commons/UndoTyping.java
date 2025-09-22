package commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;


public class UndoTyping {

    private UndoItem undo;

    /**
     * Undo rename file constructor
     * @param value stores the previous name of the file
     * @param id of the note
     * @param textChanges the number of changes that this undoItem will revert
     */
    public UndoTyping(String value, long id, int textChanges) {
        this.undo = new UndoItem();

        Locale locale = loadSavedLanguage();

        ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);

        String descriptionTemplate = bundle.getString("undoTyping.description");

        String description = MessageFormat.format(descriptionTemplate, textChanges);


        this.undo.description = description;
        this.undo.oldName = value;
        this.undo.textChanges = textChanges;
        this.undo.note = id;
        this.undo.setType("typing");
    }


    /**
     * A getter method
     * @return undoItem
     */
    public UndoItem getUndo() {
        return undo;
    }

    /**
     * A setter method
     * @param undo
     */
    public void setUndo(UndoItem undo) {
        this.undo = undo;
    }

    private Locale loadSavedLanguage() {
        try (InputStream input = new FileInputStream("config.properties")) {
            Properties props = new Properties();
            props.load(input);
            String lang = props.getProperty("language");
            return Locale.forLanguageTag(lang.replace('_', '-'));
        } catch (IOException e) {
            return Locale.ENGLISH; // Fallback to English
        }
    }
}
