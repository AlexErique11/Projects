package server.api;

import commons.Note;
import commons.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import server.database.NoteRepositoryTest;
import server.service.NoteSrv;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NoteCtrlTest {
    private NoteSrv noteService;
    private NoteCtrl noteCtrl;

    @BeforeEach
    void setUp() {
        noteService = mock(NoteSrv.class);
        noteCtrl = new NoteCtrl(noteService);
    }

    /**
     * test if the function can successfully get the correct note by tags
     */
    @Test
    void getNotesByTagsSuccess() {
        List<String> tags = List.of("tag1", "tag2");
        Note note1 = new Note(), note2 = new Note();
        note1.setId(1L);
        note1.setTitle("Note 1");
        note2.setId(2L);
        note2.setTitle("Note 2");

        List<Note> mockNotes = List.of(note1, note2);

        when(noteService.findNotesByTags(tags)).thenReturn(mockNotes);

        ResponseEntity<List<Note>> response = noteCtrl.getNotesByTags(tags);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Note 1", response.getBody().get(0).getTitle());
    }

    /**
     * test if an empty result is given by notesbytag
     */
    @Test
    void getNotesByTagsEmptyResult() {
        List<String> tags = List.of("deliriousTag");
        List<Note> mockNotes = List.of();

        when(noteService.findNotesByTags(tags)).thenReturn(mockNotes);
        ResponseEntity<List<Note>> response = noteCtrl.getNotesByTags(tags);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    /**
     * test for checking of the note can still be retrieved from the tag if the input is invalid.
     */
    @Test
    void getNotesByTagsInvalidInput() {
        ResponseEntity<List<Note>> response = noteCtrl.getNotesByTags(null);
        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * test if using an empty value the response is not null, is empty, and has status code 200: "ok"
     */
    @Test
    void getNotesByTagsEmpty() {
        List<String> tags = List.of("nonexistent");

        when(noteService.findNotesByTags(tags)).thenReturn(new ArrayList<>());
        ResponseEntity<List<Note>> response = noteCtrl.getNotesByTags(tags);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    /**
     * Test if we can save a note with tags successfully
     */
    @Test
    void createNoteSuccess() {
        Note newNote = new Note();
        newNote.setId(1L);
        newNote.setTitle("New Note");

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setTitle("New Note");

        when(noteService.saveNoteWithTags(newNote)).thenReturn(savedNote);
        Note response = noteCtrl.createNote(newNote);

        assertNotNull(response);
        assertEquals("New Note", response.getTitle());
        assertEquals(1L, response.getId());
    }

    /**
     * Test if single tag filter works
     */
    @Test
    void filterBySingleTagSuccess() {
        List<String> tags = List.of("tag1");
        Tag tag1 = new Tag("tag1");

        Note note1 = new Note();
        note1.setId(1L);
        note1.setTitle("Note 1");
        note1.setTags(Set.of(tag1));

        List<Note> mockNotes = List.of(note1);
        when(noteService.findNotesByTags(tags)).thenReturn(mockNotes);
        List<Note> response = noteCtrl.filterByTags(tags);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Note 1", response.get(0).getTitle());
    }


    /**
     * Test if multiple tag filter works
     */
    @Test
    void filterByMultipleTagsSuccess() {
        List<String> tags = List.of("tag1", "tag2");

        Tag tag1 = new Tag("tag1");
        Tag tag2 = new Tag("tag2");
        Tag tag3 = new Tag("tag3");

        Note note1 = new Note();
        note1.setId(1L);
        note1.setTitle("Note 1");
        note1.setTags(Set.of(tag1, tag2));

        Note note2 = new Note();
        note2.setId(2L);
        note2.setTitle("Note 2");
        note2.setTags(Set.of(tag2, tag3));

        List<Note> mockNotes = List.of(note1, note2);

        when(noteService.findNotesByTags(tags)).thenReturn(mockNotes);
        List<Note> response = noteCtrl.filterByTags(tags);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Note 1", response.get(0).getTitle());
        assertEquals("Note 2", response.get(1).getTitle());
    }

    /**
     * Test if no tags filter works
     */
    @Test
    void filterByNoTags() {
        List<String> tags = new ArrayList<>();
        when(noteService.findNotesByTags(tags)).thenReturn(new ArrayList<>());

        List<Note> response = noteCtrl.filterByTags(tags);
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    /**
     * test if "null" tag filter works
     */
    @Test
    void filterByTagsNull() {
        assertThrows(IllegalArgumentException.class, () -> noteCtrl.filterByTags(null));
    }
}
