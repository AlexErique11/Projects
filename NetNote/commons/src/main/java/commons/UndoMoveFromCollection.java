package commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.text.MessageFormat;


public class UndoMoveFromCollection {

    private UndoItem undo;

    /**
     * undo Move from Collection constructor
     * @param collection that the note was moved from
     * @param id of the corresponding note
     */
    public UndoMoveFromCollection(Collection collection, long id) {
        this.undo = new UndoItem();

        Locale locale = loadSavedLanguage();

        ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);

        String descriptionTemplate = bundle.getString("undoMoveFromCollection.description");

        String description = MessageFormat.format(descriptionTemplate, collection.getName());


        this.undo.description = description;
        this.undo.collectionId = collection.getId();
        this.undo.collectionName = collection.getName();
        this.undo.note = id;
        this.undo.setType("moveCol");
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
