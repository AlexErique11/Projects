package server.api;

import commons.EmbeddedFile;
import commons.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import server.database.FileRepositoryTest;
import server.database.NoteRepositoryTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FileControllerTest {
    private FileRepositoryTest fileRepositoryTest;
    private NoteRepositoryTest noteRepositoryTest;
    private FileController fileController;
    private EmbeddedFile file;
    private Note note;

    @BeforeEach
    void setUp() {
        fileRepositoryTest = new FileRepositoryTest();
        noteRepositoryTest = new NoteRepositoryTest();
        fileController = new FileController(fileRepositoryTest, noteRepositoryTest);

        file = new EmbeddedFile();
        file.setId(1L);
        file.setName("test.txt");
        file.setType("text/plain");

        note = new Note();
        note.setId(1L);
    }

    @Test
    void uploadFileNoteNotFound() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        ResponseEntity<EmbeddedFile> response = fileController.uploadFile(999L, mockFile);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void deleteFileSuccess() {
        fileRepositoryTest.save(file);
        ResponseEntity<Void> response = fileController.deleteFile(file.getId());
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(fileRepositoryTest.findAll().isEmpty());
    }

    @Test
    void deleteFileNotFound() {
        ResponseEntity<Void> response = fileController.deleteFile(999L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void renameFileSuccess() {
        fileRepositoryTest.save(file);
        ResponseEntity<EmbeddedFile> response = fileController.renameFile(file.getId(), "renamed.txt");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("renamed.txt", response.getBody().getName());
    }

    @Test
    void renameFileNotFound() {
        ResponseEntity<EmbeddedFile> response = fileController.renameFile(999L, "renamed.txt");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void downloadFileSuccess() {
        file.setType("text/plain");
        file.setContent("Sample file content".getBytes()); // Set some content for the file
        fileRepositoryTest.save(file);

        Optional<EmbeddedFile> savedFile = fileRepositoryTest.findById(file.getId());
        System.out.println("Saved file: " + savedFile.orElse(null));

        ResponseEntity<byte[]> response = fileController.downloadFile(file.getId());
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body length: " + (response.getBody() != null ? response.getBody().length : "null"));

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void downloadFileNotFound() {
        ResponseEntity<byte[]> response = fileController.downloadFile(999L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void previewFileSuccess() {
        file.setType("image/png");
        file.setContent("Sample image content".getBytes());  // Ensure some content is set
        fileRepositoryTest.save(file);

        Optional<EmbeddedFile> savedFile = fileRepositoryTest.findById(file.getId());
        System.out.println("Saved file: " + savedFile.orElse(null));

        ResponseEntity<byte[]> response = fileController.previewFile(file.getId());
        System.out.println("Response status: " + response.getStatusCode());

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void previewFileNotImage() {
        file.setType("text/plain");
        fileRepositoryTest.save(file);
        ResponseEntity<byte[]> response = fileController.previewFile(file.getId());
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void getFileMetadataSuccess() {
        fileRepositoryTest.save(file);
        ResponseEntity<EmbeddedFile> response = fileController.getFileMetadata(file.getId());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("test.txt", response.getBody().getName());
    }

    @Test
    void getFileMetadataNotFound() {
        ResponseEntity<EmbeddedFile> response = fileController.getFileMetadata(999L);
        assertEquals(404, response.getStatusCodeValue());
    }
}
