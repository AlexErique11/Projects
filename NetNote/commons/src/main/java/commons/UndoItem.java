package commons;


import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;


@Entity
public class UndoItem implements Serializable {


    @Id
    @GeneratedValue
    @JsonView(Views.Public.class)
    protected long id;
    protected String description = "";

    @Lob
    @Column(length = 15000)
    protected String oldName = null;

    protected long fileId = -1;
    protected String fileName = "";
    protected long collectionId = -1;
    protected String collectionName = "";
    protected int position = 0;
    protected long note = 0;
    private String type;
    protected int textChanges = -1;


    /**
     * Constructor
     */
    public UndoItem() {

    }

    /**
     * Constructor with id
     * @param id of the Item
     */
    public UndoItem(long id) {
        this.id = id;
    }

    /**
     * A getter method
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * A setter method
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * A getter method
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * A getter method
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * A getter method
     * @return the old name / note value
     */
    public String getOldName() {
        return oldName;
    }

    /**
     * A getter method
     * @return file Id
     */
    public long getFileId() {
        return fileId;
    }

    /**
     * A getter method
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * A getter method
     * @return collection Id
     */
    public long getCollectionId() {
        return collectionId;
    }

    /**
     * A getter method
     * @return collection Name
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * A getter method
     * @return Position
     */
    public int getPosition() {
        return position;
    }

    /**
     * A getter method
     * @return note Id
     */
    public long getNote() {
        return note;
    }

    /**
     * A getter method
     * @return textChanges
     */
    public int getTextChanges() {
        return textChanges;
    }

    /**
     * A setter method
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * A setter method
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * A setter method
     * @param oldName
     */
    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    /**
     * A setter method
     * @param fileId
     */
    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    /**
     * A setter method
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * A setter method
     * @param collectionId
     */
    public void setCollectionId(long collectionId) {
        this.collectionId = collectionId;
    }

    /**
     * A setter method
     * @param collectionName
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * A setter method
     * @param position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * A setter method
     * @param note
     */
    public void setNote(long note) {
        this.note = note;
    }

    /**
     * A setter method
     * @param textChanges
     */
    public void setTextChanges(int textChanges) {
        this.textChanges = textChanges;
    }

    /**
     * An equals method
     * @param o
     * @return object equals object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UndoItem undoItem = (UndoItem) o;
        return id == undoItem.id;
    }

    /**
     * A hashCode method
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}



