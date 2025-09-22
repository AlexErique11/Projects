package server;

import com.fasterxml.jackson.annotation.JsonView;
import commons.EmbeddedFile;
import commons.Note;
import commons.Collection;
import commons.Views;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.database.NoteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/notes")
public class SomeController {

    private NoteRepository db ;

    /**
     * This method is used to post a note on DB
     * @param n
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void fileDB(@RequestBody Note n) {
        db.save(n);
    }

    /**
     * This method is used to retrieve all entities of type Note from the db
     * @return returns a list of Notes
     */
    @GetMapping
    @ResponseBody
    @JsonView(Views.Public.class)
    public List<Note> getNotes() {
        return db.findAll();
    }

    /**
     * constructor
     * @param db
     */
    public SomeController (NoteRepository db) {
        this.db = db;
    }


    /**
     * Deletes all notes from the database.
     */
    @DeleteMapping
    @ResponseBody
    public void deleteAllNotes() {
        db.deleteAll(); // Deletes all notes
        ResponseEntity.ok().build();
    }

    /**
     * Deletes a note from the database.
     * @param id - id of the note
     */
    @ResponseBody
    @DeleteMapping("/{id}")
    public void deleteNote(@PathVariable Long id) {
        if (db.existsById(id)) {
            db.deleteById(id);
        }
    }

    /**
     * Gets a specific note by id
     * @param id - id of the note
     * @return - returns the Note object
     */
    @GetMapping("/{id}")
    @ResponseBody
   // @JsonView(Views.Public.class)
    public Note getNote(@PathVariable Long id) {
        return db.findById(id).orElse(null);
    }

    /**
     * Gets a specific collection by note ID (depreciated)
     * @param id id of the note whose parent Collection should be returned
     * @return returns collection 
     */
    @GetMapping("/c/{id}")
    @ResponseBody
    public Collection getCollection(@PathVariable Long id) {
        Note note = db.findById(id).orElse(null);
        return note.getCollection();
    }


    /**
     * Updates a position of the note
     * @param id
     * @param updatedNote
     * @return
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateNote(@PathVariable Long id, @RequestBody Note updatedNote) {
        Optional<Note> noteToUpdate = db.findById(id);
        if(noteToUpdate.isEmpty()){return;}
        Note updated = noteToUpdate.get();
        updated.setPosition(updatedNote.getPosition());
        updated.setText(updatedNote.getText());
        db.save(updated);
    }

    /**
     * This method is used to check if the server is responding
     * @return Boolean -> true
     */
    @GetMapping("/healthCheck")
    @ResponseBody
    public Boolean isHealthCHeck() {
        return true;
    }


}