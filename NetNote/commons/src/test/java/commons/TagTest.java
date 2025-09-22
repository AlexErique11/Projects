package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    private static Tag tag1;
    private static Tag tag2;
    private static Tag tag3;

    private static Tag tag4;

    @BeforeEach
    void setUp() {
        tag1 = new Tag();
        tag2 = new Tag("Note", "Red");
        tag3 = new Tag("Note-3", "Yellow");
        tag4 = new Tag(tag2); // Copy constructor

    }
    @Test
    void testIDGenerator() {
        assertNull(tag1.getId());
    }

    @Test
    void testConstructorAndGetName() {
        assertEquals(tag2.getName(), "Note");
    }

    @Test
    void testSettersAndGetters() {
        tag1.setName("Note-2");
        tag1.setColor("Blue");
        assertEquals(tag1.getColor() + " " + tag1.getName(), "Blue Note-2");
    }

    @Test
    void testEquals() {
        tag2.setName("Note-3");
        tag2.setColor("Yellow");
        assertEquals(tag2, tag3);
    }

    @Test
    void testNotEquals() {
        tag2.setColor("Green");
        assertNotEquals(tag2, tag3);
    }

    @Test
    void testHashCode() {
        assertEquals(tag2.hashCode(), tag4.hashCode());
    }

    @Test
    void testHashCodeV2() {
        tag2.setColor("Blue");
        assertNotEquals(tag2.hashCode(), tag3.hashCode());
    }

    @Test
    void testToString() {
        String expected = "Tag{id=null, name='Note', color='Red'}";
        assertEquals(expected, tag2.toString());
    }

    @Test
    void testCopyConstructor() {
        assertEquals(tag2.getName(), tag4.getName());
        assertEquals(tag2.getColor(), tag4.getColor());
    }

    @Test
    void testSetAndGetId() {
        tag1.setId(100L);
        assertEquals(100L, tag1.getId());
    }

    @Test
    void testEqualsWithNull() {
        Tag tagWithNulls = new Tag();
        assertNotEquals(tagWithNulls, tag2);
    }

    @Test
    void testHashCodeWithNullValues() {
        Tag tagWithNulls = new Tag();
        assertEquals(Objects.hash(null, null, null), tagWithNulls.hashCode());
    }

    @Test
    void testAddContentToTag() {
        Content content = new Content();
        content.setText("Sample Content");

        Set<Content> contents = new HashSet<>();
        contents.add(content);

        tag1.setContents(contents);

        assertTrue(tag1.getContents().contains(content));
        assertEquals(1, tag1.getContents().size());
    }

    @Test
    void testAddMultipleContentsToTag() {
        Content content1 = new Content();
        content1.setText("Content 1");

        Content content2 = new Content();
        content2.setText("Content 2");

        Set<Content> contents = new HashSet<>();
        contents.add(content1);
        contents.add(content2);

        tag1.setContents(contents);

        assertTrue(tag1.getContents().contains(content1));
        assertTrue(tag1.getContents().contains(content2));
    }

    @Test
    void testRemoveContentFromTag() {
        Content content = new Content();
        content.setText("Sample Content");

        tag1.getContents().add(content);
        assertTrue(tag1.getContents().contains(content));

        tag1.getContents().remove(content);
        assertFalse(tag1.getContents().contains(content));
    }

    @Test
    void testContents() {
        Content content = new Content();
        content.setText("Sample Content");

        tag1.getContents().add(content);
        content.getTags().add(tag1);

        assertTrue(tag1.getContents().contains(content));
        assertTrue(content.getTags().contains(tag1));
    }

    @Test
    void testNullNameOrColor() {
        tag1.setName(null);
        tag1.setColor(null);
        assertNull(tag1.getName());
        assertNull(tag1.getColor());
    }


    @Test
    void testAddNoteToTag() {
        Note note = new Note();
        note.setTitle("Sample Note");

        Set<Note> notes = new HashSet<>();
        notes.add(note);

        tag1.setNotes(notes);

        assertTrue(tag1.getNotes().contains(note));
        assertEquals(1, tag1.getNotes().size());
    }

    @Test
    void testAddMultipleNotesToTag() {
        Note note1 = new Note();
        note1.setTitle("Note 1");

        Note note2 = new Note();
        note2.setTitle("Note 2");

        Set<Note> notes = new HashSet<>();
        notes.add(note1);
        notes.add(note2);

        tag1.setNotes(notes);

        assertTrue(tag1.getNotes().contains(note1));
        assertTrue(tag1.getNotes().contains(note2));
    }

    @Test
    void testRemoveNoteFromTag() {
        Note note = new Note();
        note.setTitle("Sample Note");

        tag1.getNotes().add(note);
        assertTrue(tag1.getNotes().contains(note));

        tag1.getNotes().remove(note);
        assertFalse(tag1.getNotes().contains(note));
    }

    @Test
    void testRelateNoteAndTagBidirectionally() {
        Note note = new Note();
        note.setTitle("Bidirectional Note");

        tag1.getNotes().add(note);
        note.getTags().add(tag1);

        assertTrue(tag1.getNotes().contains(note));
        assertTrue(note.getTags().contains(tag1));
    }

    @Test
    void testNullNotesInTag() {
        tag1.setNotes(null);
        assertNull(tag1.getNotes());
    }

    @Test
    void testEmptyNotesInTag() {
        tag1.setNotes(new HashSet<>());
        assertTrue(tag1.getNotes().isEmpty());
    }

    @Test
    void testEqualityWithDifferentIds() {
        tag1.setId(1L);
        tag2.setId(2L);
        assertNotEquals(tag1, tag2, "Tags with different IDs should not be equal");
    }

    @Test
    void testSetNullContents() {
        tag1.setContents(null);
        assertNull(tag1.getContents(), "Contents should be null after setting null");
    }

    @Test
    void testSetNullNotes() {
        tag1.setNotes(null);
        assertNull(tag1.getNotes(), "Notes should be null after setting null");
    }

    @Test
    void testEmptyContentsByDefault() {
        assertTrue(tag1.getContents().isEmpty(), "Contents should be empty by default");
    }

    @Test
    void testEmptyNotesByDefault() {
        assertTrue(tag1.getNotes().isEmpty(), "Notes should be empty by default");
    }

    @Test
    void testAddDuplicateContent() {
        Content content = new Content();
        content.setText("Duplicate Content");

        tag1.getContents().add(content);
        tag1.getContents().add(content);

        assertEquals(1, tag1.getContents().size(), "Duplicate content should not increase the size of contents");
    }

    @Test
    void testAddDuplicateNote() {
        Note note = new Note();
        note.setTitle("Duplicate Note");

        tag1.getNotes().add(note);
        tag1.getNotes().add(note);

        assertEquals(1, tag1.getNotes().size(), "Duplicate note should not increase the size of notes");
    }

    @Test
    void testContentsAndNotesAreIndependent() {
        Content content = new Content();
        Note note = new Note();

        tag1.getContents().add(content);
        tag1.getNotes().add(note);

        assertEquals(1, tag1.getContents().size(), "Adding a note should not affect contents");
        assertEquals(1, tag1.getNotes().size(), "Adding a content should not affect notes");
    }

    @Test
    void testToStringWithNullFields() {
        Tag tagWithNullFields = new Tag();
        tagWithNullFields.setName(null);
        tagWithNullFields.setColor(null);
        String expected = "Tag{id=null, name='null', color='null'}";
        assertEquals(expected, tagWithNullFields.toString(), "toString should handle null fields correctly");
    }

    @Test
    void testAddContentWithNullTag() {
        Content content = new Content();
        content.setText("Null Tag Test");

        tag1.getContents().add(content);
        content.setTags(null);

        assertTrue(tag1.getContents().contains(content), "Content should still be associated with the tag even if the tag list in content is null");
    }

    @Test
    void testAddNoteWithNullTag() {
        Note note = new Note();
        note.setTitle("Null Tag Test");

        tag1.getNotes().add(note);
        note.setTags(null);

        assertTrue(tag1.getNotes().contains(note), "Note should still be associated with the tag even if the tag list in note is null");
    }

    @Test
    void testAddNullContent() {
        tag1.getContents().add(null);
        assertTrue(tag1.getContents().contains(null), "Null content should be allowed in the contents set");
    }

    @Test
    void testAddNullNote() {
        tag1.getNotes().add(null);
        assertTrue(tag1.getNotes().contains(null), "Null note should be allowed in the notes set");
    }

    @Test
    void testEqualsWithSameReference() {
        assertEquals(tag1, tag1, "A tag should be equal to itself");
    }

    @Test
    void testEqualsWithDifferentObjectTypes() {
        String otherObject = "Not a Tag";
        assertNotEquals(tag1, otherObject, "A tag should not be equal to an object of a different type");
    }

    @Test
    void testHashCodeConsistency() {
        tag1.setId(123L);
        tag1.setName("Consistent Tag");
        tag1.setColor("Consistent Color");

        int hashCode1 = tag1.hashCode();
        int hashCode2 = tag1.hashCode();

        assertEquals(hashCode1, hashCode2, "Hash code should be consistent across multiple invocations");
    }

    @Test
    void testContentsAndNotesDoNotAffectHashCode() {
        tag1.setId(123L);
        tag1.setName("Test Tag");
        tag1.setColor("Test Color");

        int hashCodeBefore = tag1.hashCode();

        Content content = new Content();
        Note note = new Note();
        tag1.getContents().add(content);
        tag1.getNotes().add(note);

        int hashCodeAfter = tag1.hashCode();
        assertEquals(hashCodeBefore, hashCodeAfter, "Adding contents or notes should not affect the hash code");
    }



}