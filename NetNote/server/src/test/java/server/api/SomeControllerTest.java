package server.api;

import commons.Collection;
import commons.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.SomeController;
import server.database.NoteRepositoryTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SomeControllerTest {
    private NoteRepositoryTest noteRepositoryTest;
    private SomeController someController;

    @BeforeEach
    void setUp() {
        noteRepositoryTest = new NoteRepositoryTest();
        someController = new SomeController(noteRepositoryTest);
    }

    @Test
    void fileDBSuccess() {
        Note note = new Note();
        note.setTitle("Test Note");
        someController.fileDB(note);

        assertEquals(1, noteRepositoryTest.findAll().size());
        assertEquals(note, noteRepositoryTest.findAll().get(0));
    }

    @Test
    void fileDBFailure() {
        Note note = null;
        assertThrows(NullPointerException.class, () -> someController.fileDB(note));
    }

    @Test
    void getNotesSuccess() {
        Note note = new Note();
        note.setTitle("Test Note");
        someController.fileDB(note);

        List<Note> notes = someController.getNotes();
        assertEquals(1, notes.size());
        assertEquals(note, notes.get(0));
    }

    @Test
    void getNotesFailure() {
        List<Note> notes = someController.getNotes();
        assertTrue(notes.isEmpty());
    }

    @Test
    void deleteAllNotesSuccess() {
        Note note = new Note();
        someController.fileDB(note);

        someController.deleteAllNotes();
        assertTrue(noteRepositoryTest.findAll().isEmpty());
    }

    @Test
    void deleteAllNotesFailure() {
        someController.deleteAllNotes(); // No notes in the repository
        assertTrue(noteRepositoryTest.findAll().isEmpty()); // Still empty
    }

    @Test
    void deleteNoteSuccess() {
        Note note = new Note();
        someController.fileDB(note);
        Long id = note.getId();

        someController.deleteNote(id);
        assertFalse(noteRepositoryTest.existsById(id));
    }

    @Test
    void deleteNoteFailure() {
        Long nonExistentId = 999L;
        someController.deleteNote(nonExistentId);

        // No exception, but the list remains unaffected
        assertTrue(noteRepositoryTest.findAll().isEmpty());
    }

    @Test
    void getNoteSuccess() {
        Note note = new Note();
        someController.fileDB(note);
        Long id = note.getId();

        Note retrievedNote = someController.getNote(id);
        assertEquals(note, retrievedNote);
    }

    @Test
    void getNoteFailure() {
        Long nonExistentId = 999L;

        Note retrievedNote = someController.getNote(nonExistentId);
        assertNull(retrievedNote);
    }

    @Test
    void updateNoteFailure() {
        Long nonExistentId = 999L;

        Note updatedNote = new Note();
        updatedNote.setPosition(10);

        someController.updateNote(nonExistentId, updatedNote);
        assertTrue(noteRepositoryTest.findAll().isEmpty());
    }

    @Test
    void getCollectionSuccess() {
        Note note = new Note();
        Collection collection = new Collection();
        note.setCollection(collection);
        someController.fileDB(note);

        Collection retrievedCollection = someController.getCollection(note.getId());
        assertEquals(collection, retrievedCollection);
    }
    @Test
    void getNotesReturnsAllNotes() {
        Note note1 = new Note();
        note1.setText("Note 1");
        someController.fileDB(note1);

        Note note2 = new Note();
        note2.setText("Note 2");
        someController.fileDB(note2);

        // Retrieve notes
        List<Note> notes = someController.getNotes();

        // Verify both notes are retrieved
        assertEquals(2, notes.size());
        assertTrue(notes.contains(note1));
        assertTrue(notes.contains(note2));
    }

    @Test
    void deleteAllNotesClearsRepository() {
        Note note = new Note();
        someController.fileDB(note);

        // Delete all notes
        someController.deleteAllNotes();

        // Verify repository is empty
        assertTrue(noteRepositoryTest.findAll().isEmpty());
    }

    @Test
    void isHealthCheckSuccess() {
        Boolean healthStatus = someController.isHealthCHeck();
        assertTrue(healthStatus);
    }

    @Test
    void deleteNoteRemovesSpecificNote() {
        Note note = new Note();
        someController.fileDB(note);
        Long id = note.getId();
        someController.deleteNote(id);
        assertFalse(noteRepositoryTest.existsById(id));
    }

}