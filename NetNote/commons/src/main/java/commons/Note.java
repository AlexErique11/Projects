package commons ;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;


import java.io.Serializable;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
public class Note implements Serializable {
    //The generated ID will be set as a unique number after posting on DB
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    @JsonView(Views.Public.class)
    private long id;

    @JsonView(Views.Public.class)
    private String title;

    @Lob
    @Column(length = 15000)
    @JsonView(Views.Public.class)
    private String text;

    @JsonView(Views.Public.class)
    private int position;

    @ManyToMany
    @JsonView(Views.Public.class)
    private Set<Tag> tags = new HashSet<>();

    @ManyToOne
    @JsonBackReference
    //@JoinColumn(name = "collection_id")
    @JsonView(Views.Public.class)
    private Collection collection;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonView(Views.Public.class)
    private List<EmbeddedFile> embeddedFiles = new ArrayList<>();

    /**
     * A default constructor - Make sure that the code compiles
     * after creating any other constructors for this entity!!!
     */
    public Note() {
    }

    /**
     * A regular getter
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * A regular getter
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * A regular setter for title
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * A regular setter for text
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }


    /**
     * A setter for id. Only use for testing!
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * A regular getter
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * A setter for position
     * @param position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * A regular getter
     *
     * @return position
     */
    public int getPosition() {
        return position;
    }

    /**
     * A setter for setting the collection
     *
     * @param collection collection
     */
    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    /**
     * A getter for getting the collection
     *
     * @return collection
     */
    public Collection getCollection() {
        return this.collection;
    }

    /**
     * A regular equals method based on ID of the note
     * @return A boolean representing whether the object are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return id == note.id;
    }

    /**
     * Hashcode
     * @return hashcode for the Note
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /**
     * A to String method
     * @return a readable String representing the Note
     */
    @Override
    public String toString() {
        return "Note:" +
                "\ntitle: " + title +
                "\ntext: " + text +
                '\n';
    }

    /**
     * This method retrieves all the tags associated with this entity.
     *
     * @return Set<Tag> This returns a Set containing all the
     * Tag entities associated with this entity.
     */
    public Set<Tag> getTags() {
        return tags;
    }


    /**
     * This method adds a Tag entity to the set of tags associated with this entity.
     *
     * It also ensures consistency by adding this entity to the list of
     * Notes in the provided Tag entity.
     *
     * @param tag The Tag entity to be associated with this entity.
     */
    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getNotes().add(this);
    }


    /**
     * This method removes a Tag entity from the set of tags associated with this entity.
     *
     * It also ensures consistency by removing this entity from the list of
     * Notes in the removed Tag entity.
     *
     * @param tag The Tag entity to be dissociated from this entity.
     */

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getNotes().remove(this);
    }

    /**
     * This method sets (replaces) the set of Tag entities associated with this entity.
     *
     * It does not ensure the consistency on the side of the individual Tag entities,
     * i.e., this entity is not added to the list of Notes in the provided Tag entities.
     *
     * @param tags The new Set of Tag entities to be associated with this entity.
     */
    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
    /**
     * Gets the list of embedded files associated with the note.
     * @return A {@link List} of {@link EmbeddedFile} objects.
     */
    public List<EmbeddedFile> getEmbeddedFiles() {
        return embeddedFiles;
    }

    /**
     * Sets the list of embedded files associated with the note.
     * @param embeddedFiles A {@link List} of {@link EmbeddedFile}
     *                      objects to associate with the note.
     */
    public void setEmbeddedFiles(List<EmbeddedFile> embeddedFiles) {
        this.embeddedFiles = embeddedFiles;
    }

}