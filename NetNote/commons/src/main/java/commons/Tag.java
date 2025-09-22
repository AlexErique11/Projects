package commons;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@Entity
@Table(name = "Tag")
public class Tag implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "tag_id")
    private Long id;

    private String name;
    private String color;
    @ManyToMany(mappedBy = "tags")
    private Set<Content> contents = new HashSet<>();

    @ManyToMany(mappedBy = "tags")
    private Set<Note> notes = new HashSet<>();

    /**
     * Parameterless constructor for Tag
     */
    public Tag() {
    }

    /**
     * Constructor with name for a tag
     * @param name -> String
     */
    public Tag(String name) {
        this.name = name;
    }

    /**
     * Constructor with name and color for Tag
     * @param name _> String
     * @param color -> String
     */
    public Tag(String name, String color) {
        this.name = name;
        this.color = color;
    }

    /**
     * Constructor with tag for Tag
     * @param t -> different tag
     */
    public Tag(Tag t) {
        this.id = t.getId();
        this.name = t.getName();
        this.color = t.getColor();
    }

    /**
     * Constructor for tag
     * @param name -> String
     * @param color -> String
     * @param contents -> Set with Content items
     */
    public Tag(String name, String color, Set<Content> contents) {
        this.name = name;
        this.color = color;
        this.contents = contents;
    }

    /**
     * Constructor for Tag
     * @param name -> String
     * @param color -> String
     * @param contents -> Set with Content items
     * @param notes -> Set with Note items
     */
    public Tag(String name, String color, Set<Content> contents, Set<Note> notes) {
        this.name = name;
        this.color = color;
        this.contents = contents;
        this.notes = notes;
    }


    /**
     * Getter for Tag's id
     * @return Long ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Setter for ID
     * @param id -> Long
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Getter for Tag's name
     * @return a name of a tag
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for Tag's name
     * @param name -> String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for tag's color
     * @return A string(color)
     */
    public String getColor() {
        return color;
    }

    /**
     * Setter for tag's color
     * @param color ->String
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Getter for tag's Contents
     * @return A set of Content items
     */
    public Set<Content> getContents() {
        return contents;
    }

    /**
     * Setter for tag's Contents
     * @param contents -> A set of Content items
     */
    public void setContents(Set<Content> contents) {
        this.contents = contents;
    }

    /**
     * Getter for the set of Notes
     * @return A set of Note items
     */
    public Set<Note> getNotes() {
        return notes;
    }

    /**
     * A setter for tag's note elements
     * @param notes
     */
    public void setNotes(Set<Note> notes) {
        this.notes = notes;
    }

    /**
     * Equals method for tag element. Chechls whether 2 tags are equal
     * @param o -> An object
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(getId(), tag.getId())
                && Objects.equals(getName(), tag.getName())
                && Objects.equals(getColor(), tag.getColor());
    }

    /**
     * Generates hashcode for a tag
     * @return An int being a hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getColor());
    }

    /**
     * To string method for tag
     * @return returns a String representation of tag element
     */
    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
