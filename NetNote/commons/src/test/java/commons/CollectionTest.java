package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionTest {

    @Test
    public void testGetId() {
        Collection collection = new Collection();
        assertEquals(0, collection.getId());
    }

    @Test
    public void testNameMethods(){
        Collection collection = new Collection();
        collection.setName("test");
        assertEquals("test", collection.getName());
    }

    @Test
    public void testNameMethods2(){
        Collection collection = new Collection();
        collection.setName("test");
        assertNotEquals("test2", collection.getName());
    }

    @Test
    public void testNoteMethods(){
        Collection collection = new Collection();
        collection.setName("test2");
        assertNotEquals("test", collection.getName());
    }

    @Test
    public void testAddNotes(){
        Collection collection = new Collection();
        Note note = new Note();
        note.setTitle("title");
        note.setText("text");
        collection.addNote(note);
        assertEquals(1, collection.getNotes().size());
        assertEquals("title", collection.getNotes().get(0).getTitle());
        assertEquals(note, collection.getNotes().get(0));
    }

    @Test
    public void testRemoveNotes(){
        Collection collection = new Collection();
        Note note = new Note();
        note.setTitle("title");
        note.setText("text");
        collection.addNote(note);
        assertEquals(1, collection.getNotes().size());
        collection.removeNote(note);
        assertEquals(0, collection.getNotes().size());
    }

    @Test
    public void testCollectionEquals(){
        Collection collection = new Collection();
        Collection collection2 = new Collection();
        collection.setName("test");
        collection2.setName("test2");
        assertEquals(collection, collection2);
    }

    @Test
    public void testHashCode(){
        Collection collection = new Collection();
        Collection collection2 = new Collection();
        collection.setName("test");
        collection2.setName("test2");
        assertEquals(collection.hashCode(), collection2.hashCode());
    }

    @Test
    public void testToString(){
        Collection collection = new Collection();
        collection.setName("test");
        assertEquals("Collection: test\nContains following notes:\n", collection.toString());
    }

    @Test
    public void testToString2(){
        Collection collection = new Collection();
        collection.setName("test");
        Note note = new Note();
        note.setTitle("title");
        note.setText("text");
        collection.addNote(note);
        Collection collection2 = new Collection();
        collection2.setName("test");
        collection2.addNote(note);
        assertEquals(collection.toString(), collection2.toString());
    }

    @Test
    public void testSetId(){
        Collection collection = new Collection();
        collection.setName("test");
        Long a = 10L;
        collection.setId(a);
        assertEquals(a, collection.getId());
        assertEquals(10, a);
    }

}
