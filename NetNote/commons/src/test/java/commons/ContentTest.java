package commons;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ContentTest {

    @Test
    void getContentID() {
        Content content = new Content();
        content.setContentID(1L);
        assertEquals(1L, content.getContentID());
    }

    @Test
    void setContentID() {
        Content content = new Content();
        content.setContentID(2L);
        assertEquals(2L, content.getContentID());
    }

    @Test
    void getText() {
        Content content = new Content();
        content.setText("Sample Text");
        assertEquals("Sample Text", content.getText());
    }
    @Test
    void setText() {
        Content content = new Content();
        content.setText("New Text");
        assertEquals("New Text", content.getText());
    }

    @Test
    void getFilePath() {
        Content content = new Content();
        content.setFilePath("/path/to/file");
        assertEquals("/path/to/file", content.getFilePath());
    }

    @Test
    void setFilePath() {
        Content content = new Content();
        content.setFilePath("/new/path/to/file");
        assertEquals("/new/path/to/file", content.getFilePath());
    }

    @Test
    void getImagePath() {
        Content content = new Content();
        content.setImagePath("/path/to/image");
        assertEquals("/path/to/image", content.getImagePath());
    }

    @Test
    void setImagePath() {
        Content content = new Content();
        content.setImagePath("/new/path/to/image");
        assertEquals("/new/path/to/image", content.getImagePath());
    }

    @Test
    void testEquals() {
        Content content1 = new Content();
        content1.setContentID(1L);

        Content content2 = new Content();
        content2.setContentID(1L);

        Content content3 = new Content();
        content3.setContentID(2L);

        assertEquals(content1, content2);
        assertNotEquals(content1, content3); 
    }

    @Test
    void testHashCode() {
        Content content1 = new Content();
        content1.setContentID(1L);

        Content content2 = new Content();
        content2.setContentID(1L);

        assertEquals(content1.hashCode(), content2.hashCode());
    }

    @Test
    void testToString() {
        Content content = new Content();
        content.setContentID(1L);
        content.setText("Sample Text");
        content.setFilePath("/path/to/file");
        content.setImagePath("/path/to/image");

        String expected = "Content{contentID=1, text='Sample Text', filePath='/path/to/file', imagePath='/path/to/image'}";
        assertEquals(expected, content.toString());
    }

    @Test
    void testTagsRelationship() {
        Content content = new Content();
        Tag tag1 = new Tag("Tag1", "#FF0000");
        Tag tag2 = new Tag("Tag2", "#00FF00");

        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);
        tags.add(tag2);

        content.setTags(tags);

        assertEquals(2, content.getTags().size());
        assertTrue(content.getTags().contains(tag1));
        assertTrue(content.getTags().contains(tag2));
    }


}
