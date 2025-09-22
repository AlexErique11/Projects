package server.api;

import commons.Note;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.service.NoteSrv;

import java.util.List;
@RestController
public class NoteCtrl {
    private final NoteSrv noteService;

    /**
     * This constructor injects the NoteService dependency into the NoteCtrl class.
     *
     * @param noteService The NoteService instance to be injected into the NoteCtrl class.
     */
    public NoteCtrl(NoteSrv noteService) {
        this.noteService = noteService;
    }


    /**
     * This method is a GET request mapping that returns a list of Notes associated with the specified tags.
     *
     * It's accessible via the '/filter' endpoint and expects a list of tags as request parameters.
     * It fetches the corresponding notes using the findNotesByTags method of the noteService,
     * wraps them in a ResponseEntity with an HTTP status code of 200 (OK) and returns the result.
     *
     * @param tags A list of tags for which associated Note entities are to be fetched.
     * @return ResponseEntity<List<Note>> This returns a ResponseEntity containing a list of
     * Notes associated with the input tags and an HTTP status code.
     *
     */
    @RequestMapping(value = "/filter", method = RequestMethod.GET)
    public ResponseEntity<List<Note>> getNotesByTags(@RequestParam List<String> tags) {
        if (tags == null || tags.isEmpty()) {return new ResponseEntity<>(HttpStatus.BAD_REQUEST);} // a quick check
        List<Note> notes = noteService.findNotesByTags(tags);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    /**
     * This emthod is used to save a note with tags
     * @param note -> the note
     * @return save note
     */
    @PostMapping
    public Note createNote(@RequestBody Note note) {
        return noteService.saveNoteWithTags(note);
    }

    /**
     * This method is used to set all notes with a specific tags
     * @param tagNames -> the tags in the format of Strings
     * @return -> The list of notes with those tags
     */
    @PostMapping("/filterByTags")
    public List<Note> filterByTags(@RequestBody List<String> tagNames) {
        if (tagNames == null) {
            throw new IllegalArgumentException("Tags cannot be null");
        }
        return noteService.findNotesByTags(tagNames);
    }
}
