package commons;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;

import java.util.Locale;

public class UndoRenameFile {

    private UndoItem undo;


    /**
     * Undo rename file constructor
     *
     * @param file  renamed
     * @param value stores the previous name of the file
     * @param id
     */
    public UndoRenameFile(EmbeddedFile file, String value, long id) {
        this.undo = new UndoItem();
        Locale locale = loadSavedLanguage();

        ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);
        String descriptionTemplate = bundle.getString("undoRenameFile.description");
        // Format the description with dynamic values
        String description = MessageFormat.format(descriptionTemplate, file.getId(), value);

        this.undo.description = description;
        this.undo.fileId = file.getId();
        this.undo.oldName = value;
        this.undo.note = id;
        this.undo.setType("renameFile");
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
