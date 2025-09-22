package server.api;

import commons.Collection;
import commons.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import server.CollectionController;
import server.database.CollectionRepositoryTest;
import server.database.NoteRepositoryTest;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class CollectionControllerTest {

    private CollectionRepositoryTest collectionRepositoryTest;
    private NoteRepositoryTest noteRepositoryTest;
    private CollectionController collectionController;

    @BeforeEach
    void setUp() {
        collectionRepositoryTest = new CollectionRepositoryTest();
        noteRepositoryTest = new NoteRepositoryTest();
        collectionController = new CollectionController(collectionRepositoryTest, noteRepositoryTest);
    }

    @Test
    void addNoteTest() {
        Collection collection = new Collection();
        collectionRepositoryTest.save(collection);

        Note note = new Note();
        note.setTitle("Test Note Title");
        note.setText("Test Note Text");
        noteRepositoryTest.save(note);

        Note result = collectionController.addNote(collection.getId(), note.getId());

        assertNotNull(result);
        assertEquals(note.getId(), result.getId());
        assertEquals(collection, result.getCollection());
        assertTrue(collection.getNotes().contains(result));
        assertEquals(1, collectionRepositoryTest.findById(collection.getId()).get().getNotes().size());
    }

    @Test
    void addNoteCollectionButNoCollectionFoundTest() {
        Note note = new Note();
        note.setId(2L);
        noteRepositoryTest.save(note);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> collectionController.addNote(999L, 2L));
        assertEquals("Collection not found", exception.getMessage());
    }

    @Test
    void addNoteNoteNotFoundTest() {
        Collection collection = new Collection();
        collection.setId(1L);
        collectionRepositoryTest.save(collection);
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> collectionController.addNote(1L, 999L));
        assertEquals("Note not found", exception.getMessage());
    }

    @Test
    void getCollectionTest() {
        Collection collection = new Collection();
        collection.setId(1L);
        collectionRepositoryTest.save(collection);

        Collection result = collectionController.getCollection(1L);

        assertNotNull(result);
        assertEquals(collection, result);
    }

    @Test
    void getCollectionButNoCollectionFoundTest() {
        Collection result = collectionController.getCollection(999L);

        assertNull(result);
    }

    @Test
    void getAllCollectionsTest() {
        Collection collection1 = new Collection();
        collection1.setId(1L);

        Collection collection2 = new Collection();
        collection2.setId(2L);

        collectionRepositoryTest.save(collection1);
        collectionRepositoryTest.save(collection2);

        List<Collection> result = collectionController.getAllCollections();

        assertEquals(2, result.size());
        assertTrue(result.contains(collection1));
        assertTrue(result.contains(collection2));
    }

    @Test
    void deleteCollectionTest() {
        Collection collection = new Collection();
        collection.setId(1L);
        collectionRepositoryTest.save(collection);
        assertEquals(1, collectionRepositoryTest.findAll().size());

        collectionController.deleteCollection(1L);
        assertTrue(collectionRepositoryTest.findAll().isEmpty());
    }

    @Test
    void deleteCollectionButNoCollectionFoundTest() {
        ResponseEntity<Void> response = collectionController.deleteCollection(999L);

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(collectionRepositoryTest.findAll().isEmpty());
    }

    @Test
    void getNotesFromCollectionTest() {
        Collection collection = new Collection();
        collection.setId(1L);

        Note note1 = new Note();
        note1.setId(2L);
        Note note2 = new Note();
        note2.setId(3L);
        collection.addNote(note1);
        collection.addNote(note2);
        collectionRepositoryTest.save(collection);

        List<Note> notes = collectionController.getNotes(1L);
        assertEquals(2, notes.size());
        assertTrue(notes.contains(note1));
        assertTrue(notes.contains(note2));
    }

    @Test
    void getNotesFromCollectionButCollectionNotFoundTest() {
        assertThrows(NoSuchElementException.class, () -> collectionController.getNotes(999L));
    }

    @Test
    void deleteNoteFromCollectionTest() {
        Collection collection = new Collection();
        collection.setId(1L);
        Note note1 = new Note();
        note1.setId(2L);
        Note note2 = new Note();
        note2.setId(3L);
        collection.addNote(note1);
        collection.addNote(note2);
        collectionRepositoryTest.save(collection);

        assertEquals(1, collectionRepositoryTest.findAll().size());
        assertEquals(2, collectionRepositoryTest.findById(collection.getId()).get().getNotes().size());

        collection.removeNote(note1);
        collectionRepositoryTest.save(collection);
        assertEquals(1,collectionRepositoryTest.findById(collection.getId()).get().getNotes().size() );
        assertEquals(note2,collectionRepositoryTest.findById(collection.getId()).get().getNotes().get(0));
        collection.removeNote(note2);
        collectionRepositoryTest.save(collection);
        assertEquals(0,collectionRepositoryTest.findById(collection.getId()).get().getNotes().size());
    }

    @Test
    void equalsMethodTest() {
        Collection collection1 = new Collection();
        collection1.setId(1L);
        Collection collection2 = new Collection();
        collection2.setId(2L);
        Note note1 = new Note();
        note1.setId(1L);
        Note note2 = new Note();
        note2.setId(2L);
        collection1.addNote(note1);
        collection1.addNote(note2);
        collection2.addNote(note1);
        collectionRepositoryTest.save(collection1);
        collectionRepositoryTest.save(collection2);

        assertNotEquals(collection1, collection2);

        collection2.setId(1L);
        collection2.addNote(note2);
        collectionRepositoryTest.save(collection2);
        assertEquals(collection1, collection2);
    }
}
