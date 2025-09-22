package server;

import com.fasterxml.jackson.annotation.JsonView;
import commons.Collection;
import commons.Note;
import commons.Views;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import server.database.CollectionRepository;
import server.database.NoteRepository;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/collections")
public class CollectionController {

    private final NoteRepository noteRepository;
    private CollectionRepository cr;

    /**
     * Constructor
     * @param cr - CollectionRepository
     * @param noteRepository -NoteRepository
     */
    public CollectionController(CollectionRepository cr, NoteRepository noteRepository) {
        this.cr = cr;
        this.noteRepository = noteRepository;
    }

    /**
     * This method is used to get ONE collection from database
     * @param id id of collection
     * @return a single collection
     */
    @GetMapping("/{id}")
    @ResponseBody
    @JsonView(Views.Public.class)
    public Collection getCollection(@PathVariable Long id) { return cr.findById(id).orElse(null); }


    /**
     * This method is used to get ONE id
     * @param id id of collection
     * @return a single collection
     */
    @GetMapping("/Note/{id}")
    @ResponseBody
    @JsonView(Views.Public.class)
    public Long getIdInefficient(@PathVariable Long id) {
        List<Collection> allCollections = cr.findAll();
        for(Collection col : allCollections){
            for(Note note : col.getNotes()){
                if(note.getId() == id){
                    return col.getId();
                }
            }
        }
        return null;
    }

    /**
     * This method is used to get all collections form database
     * @return a list of collections
     */
    @GetMapping
    @ResponseBody
    @JsonView(Views.Public.class)
    public List<Collection> getAllCollections(){
        return cr.findAll();
    }

    /**
     * This method post a new Collection to the database
     * @param c
     * @return collection
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection addCollection(@RequestBody Collection c){
        return cr.save(c);
    }


    /**
     * This mapping is used to delete a collection with
     * a given ID from database
     * @param id
     * @return Returns ResponseEntity status of what happened when trying to delete a Collection
     */
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id){
        Optional<Collection> collectionOpt = cr.findById(id);
        if (collectionOpt.isPresent()) { // there is a collection
            Collection collection = collectionOpt.get();
            cr.delete(collection);
            return ResponseEntity.noContent().build();
        } else { // not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * This method returns a
     * @param id
     * @return List of notes
     */
    @GetMapping("/{id}/notes")
    @ResponseBody
    public List<Note> getNotes(@PathVariable Long id){
        Collection c = cr.findById(id).get();
        return c.getNotes();
    }

    /**
     * This method is used to connect a note to a collection
     * @param id
     * @param noteId
     * @return note
     */
    @PostMapping(value = "/{id}/notes/{noteId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Note addNote(@PathVariable Long id, @PathVariable Long noteId){
        Collection c = cr.findById(id).orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        Note n = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("Note not found"));

        n.setCollection(c);
        c.addNote(n);
        cr.save(c);
        return noteRepository.save(n);
    }


    /**
     * This method is used to delete a note from specific collection
     * @param id _> the ID of collection
     * @param noteId -> the ID of the note
     */
    @DeleteMapping(value ="/{id}/notes/{noteId}")
    @ResponseBody
    public void deleteNote(@PathVariable Long id, @PathVariable Long noteId){
        Collection c = cr.findById(id).get();
        Note n = noteRepository.findById(noteId).get();
        c.removeNote(n);
        cr.save(c);
        //noteRepository.delete(n);
    }


}


