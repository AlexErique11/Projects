package commons;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Collection implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(Views.Public.class)
    private long id;

    @JsonView(Views.Public.class)
    private String name;

    @JsonManagedReference
    @OneToMany
    @JsonView(Views.Public.class)
    private List<Note> notes = new ArrayList<>();


    /**
     * A default constructor for Collection.
     */
    public Collection() {}


    /**
     * A default getter for ID
     * @return id
     */
    public Long getId(){
        return id;
    }


    /**
     * A default setter for id
     * @param id
     */
    public void setId(Long id){
        this.id = id;
    }

    /**
     * Adefault getter for name
     * @return name
     */
    public String getName(){
        return name;
    }

    /**
     * A regular setter for name
     * @param name
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * A getter for the notes in a collection
     * @return notes
     */
    public List<Note> getNotes(){
        return notes;
    }

    /**
     * The regular setter for notes in collection
     * @param notes
     */
    public void setNotes(List<Note> notes){
        if (notes != null) {
            this.notes = notes;
        }
    }

    /**
     * This method adds a note to the collection
     * @param note
     */
    public void addNote(Note note){
        this.notes.add(note);
        note.setCollection(this);
    }

    /**
     * Checker for checking if a collection contains a note by ID
     * @param id the ID of the note that is to be checked
     * @return returns Boolean, true if the collection contains the note with ID, false if it doesn't.
     */
    public Boolean contains(int id){
        for (Note k : this.notes) {
            if (k.getId() == id) return true;
        }
        return false;
    }

    /**
     * This method removes a note from the collection
     * @param note
     */
    public void removeNote(Note note){
        notes.remove(note);
        note.setCollection(null);
    }

    /**
     * Equals method that uses id for checking whethe 2 collections are equal
     * @param o
     * @return A boolean representing whether 2 objects are equal
     */
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collection that = (Collection) o;
        return id == that.id;
    }


    /**
     * A hashcode method for collections
     * @return hashcode
     */
    @Override
    public int hashCode(){
        return Objects.hashCode(id);
    }


    /**
     *A to String method or collection
     * @return The string representing a collection
     */
    @Override
    public String toString(){
        String result = "Collection: " + name + "\nContains following notes:\n";
        for (Note note : notes){
            result += note.toString() + "\n";
        }
        return result;
    }

}
