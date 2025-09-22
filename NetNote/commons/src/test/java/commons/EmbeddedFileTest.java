package commons;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmbeddedFileTest {

    @Test
    void testDefaultConstructor() {
        EmbeddedFile file = new EmbeddedFile();
        assertNull(file.getId(), "Default ID should be null");
        assertNull(file.getName(), "Default name should be null");
        assertNull(file.getType(), "Default type should be null");
        assertNull(file.getContent(), "Default content should be null");
    }

    @Test
    void testGetAndSetId() {
        EmbeddedFile file = new EmbeddedFile();
        file.setId(123L);
        assertEquals(123L, file.getId(), "ID should be set to 123");
    }

    @Test
    void testGetAndSetName() {
        EmbeddedFile file = new EmbeddedFile();
        file.setName("example.txt");
        assertEquals("example.txt", file.getName(), "Name should be set to 'example.txt'");
    }

    @Test
    void testSetNameWithNull() {
        EmbeddedFile file = new EmbeddedFile();
        file.setName(null);
        assertNull(file.getName(), "Name should be null when set to null");
    }

    @Test
    void testGetAndSetType() {
        EmbeddedFile file = new EmbeddedFile();
        file.setType("text/plain");
        assertEquals("text/plain", file.getType(), "Type should be set to 'text/plain'");
    }

    @Test
    void testSetTypeWithNull() {
        EmbeddedFile file = new EmbeddedFile();
        file.setType(null);
        assertNull(file.getType(), "Type should be null when set to null");
    }

    @Test
    void testGetAndSetContent() {
        EmbeddedFile file = new EmbeddedFile();
        byte[] content = {1, 2, 3, 4, 5};
        file.setContent(content);
        assertArrayEquals(content, file.getContent(), "Content should match the provided byte array");
    }

    @Test
    void testSetContentWithNull() {
        EmbeddedFile file = new EmbeddedFile();
        file.setContent(null);
        assertNull(file.getContent(), "Content should be null when set to null");
    }

    @Test
    void testSetAndGetEmptyContent() {
        EmbeddedFile file = new EmbeddedFile();
        byte[] emptyContent = new byte[0];
        file.setContent(emptyContent);
        assertArrayEquals(emptyContent, file.getContent(), "Content should match the empty byte array");
    }

    @Test
    void testSetAndGetLargeContent() {
        EmbeddedFile file = new EmbeddedFile();
        byte[] largeContent = new byte[1024 * 1024]; // 1 MB of data
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        file.setContent(largeContent);
        assertArrayEquals(largeContent, file.getContent(), "Large content should match the provided byte array");
    }

    @Test
    void testToStringDefaultValues() {
        EmbeddedFile file = new EmbeddedFile();
        String expected = "EmbeddedFile{id=null, name='null', type='null'}";
        assertEquals(expected, file.toString(), "toString should match expected output for default values");
    }

    @Test
    void testToStringWithValues() {
        EmbeddedFile file = new EmbeddedFile();
        file.setId(456L);
        file.setName("test.png");
        file.setType("image/png");
        String expected = "EmbeddedFile{id=456, name='test.png', type='image/png'}";
        assertEquals(expected, file.toString(), "toString should match expected output for populated values");
    }

    @Test
    void testToStringWithSpecialCharacters() {
        EmbeddedFile file = new EmbeddedFile();
        file.setId(789L);
        file.setName("special!@#$%^&*.txt");
        file.setType("application/octet-stream");
        String expected = "EmbeddedFile{id=789, name='special!@#$%^&*.txt', type='application/octet-stream'}";
        assertEquals(expected, file.toString(), "toString should handle special characters in file name");
    }

    @Test
    void testSettersAndToStringIntegration() {
        EmbeddedFile file = new EmbeddedFile();
        file.setId(789L);
        file.setName("document.pdf");
        file.setType("application/pdf");
        file.setContent(new byte[]{10, 20, 30});

        assertEquals(789L, file.getId(), "ID should be 789");
        assertEquals("document.pdf", file.getName(), "Name should be 'document.pdf'");
        assertEquals("application/pdf", file.getType(), "Type should be 'application/pdf'");
        assertArrayEquals(new byte[]{10, 20, 30}, file.getContent(), "Content should match the provided byte array");

        String expected = "EmbeddedFile{id=789, name='document.pdf', type='application/pdf'}";
        assertEquals(expected, file.toString(), "toString should match expected output for populated values");
    }


    @Test
    void testToStringWithLargeContent() {
        EmbeddedFile file = new EmbeddedFile();
        file.setId(123L);
        file.setName("largeFile.bin");
        file.setType("application/octet-stream");

        byte[] largeContent = new byte[100];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        file.setContent(largeContent);

        String expected = "EmbeddedFile{id=123, name='largeFile.bin', type='application/octet-stream'}";
        assertEquals(expected, file.toString(), "toString should not include content data for large files");
    }
}
