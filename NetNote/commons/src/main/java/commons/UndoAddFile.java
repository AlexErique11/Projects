package commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;


public class UndoAddFile {

    private UndoItem undo;

    /**
     * Undo add file constructor
     * @param file added
     * @param id of the note
     */
    public UndoAddFile(EmbeddedFile file, long id) {
        this.undo = new UndoItem();

        Locale locale = loadSavedLanguage();

        ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);

        String descriptionTemplate = bundle.getString("undoAddFile.description");

        String description = MessageFormat.format(descriptionTemplate, file.getName());


        this.undo.description = description;
        this.undo.fileId = file.getId();
        this.undo.fileName = file.getName();
        this.undo.note = id;
        this.undo.setType("addFile");
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
