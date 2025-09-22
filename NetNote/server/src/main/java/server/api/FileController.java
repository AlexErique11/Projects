package server.api;

import commons.EmbeddedFile;
import commons.Note;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.database.FileRepository;
import server.database.NoteRepository;

import java.util.Optional;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileRepository fileRepository;
    private final NoteRepository noteRepository;

    /**
     * Constructs a new FileController with the specified repositories.
     * @param fileRepository the repository for managing files
     * @param noteRepository the repository for managing notes
     */
    public FileController(FileRepository fileRepository, NoteRepository noteRepository) {
        this.fileRepository = fileRepository;
        this.noteRepository = noteRepository;
    }

    /**
     * Handles file upload and associates the uploaded file with a specified note.
     * @param noteId the ID of the note to associate the file with
     * @param file   the file to upload
     * @return a ResponseEntity containing the saved EmbeddedFile or an appropriate error response
     */
    @PostMapping("/{noteId}")
    public ResponseEntity<EmbeddedFile> uploadFile
    (@PathVariable Long noteId, @RequestParam("file") MultipartFile file) {
        Optional<Note> noteOptional = noteRepository.findById(noteId);
        if (noteOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            EmbeddedFile embeddedFile = new EmbeddedFile();
            embeddedFile.setName(file.getOriginalFilename());
            embeddedFile.setType(file.getContentType());
            embeddedFile.setContent(file.getBytes());

            EmbeddedFile savedFile = fileRepository.save(embeddedFile);

            Note note = noteOptional.get();
            note.getEmbeddedFiles().add(savedFile);
            noteRepository.save(note);

            return ResponseEntity.ok(savedFile);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Deletes a file by its ID and removes its association with any notes.
     * @param fileId the ID of the file to delete
     * @return a ResponseEntity indicating the result of the operation
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        try {
            Optional<EmbeddedFile> fileOptional = fileRepository.findById(fileId);
            if (fileOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Remove file from associated notes, if any
            EmbeddedFile file = fileOptional.get();
            // Find the associated note, if any
            Optional<Note> noteOptional = noteRepository.findAll().stream()
                    .filter(note -> note.getEmbeddedFiles().contains(file))
                    .findFirst();

            if (noteOptional.isPresent()) {
                // Remove the file from the note
                Note note = noteOptional.get();
                note.getEmbeddedFiles().remove(file);
                noteRepository.save(note);
            }

            // Delete the file from the repository
            fileRepository.delete(file);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Renames a file by its ID.
     *
     * @param fileId the ID of the file to rename
     * @param newName the new name for the file
     * @return a ResponseEntity containing the updated file, or a not found response if the file doesn't exist
     */
    @PutMapping("/{fileId}/rename")
    public ResponseEntity<EmbeddedFile> renameFile(@PathVariable Long fileId, @RequestBody String newName) {
        Optional<EmbeddedFile> fileOptional = fileRepository.findById(fileId);
        if (fileOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmbeddedFile file = fileOptional.get();
        file.setName(newName);
        fileRepository.save(file);

        return ResponseEntity.ok(file);
    }

    /**
     * Downloads a file by its ID.
     * @param fileId the ID of the file to download
     * @return a ResponseEntity containing the file content, or a not found response if the file doesn't exist
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        Optional<EmbeddedFile> fileOptional = fileRepository.findById(fileId);
        if (fileOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmbeddedFile file = fileOptional.get();

        String fileName = file.getName();
        String mimeType = file.getType(); // The stored MIME type, e.g., "image/jpeg"
        System.out.println(mimeType);

        // Derive the correct extension from the MIME type
        String extension = mimeType.substring(mimeType.lastIndexOf("/") + 1);
        if (!fileName.endsWith("." + extension)) {
            fileName = fileName + "." + extension; // Append the correct extension
        }

        // Return the file content with proper headers
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(mimeType)) // Set the MIME type explicitly
                .body(file.getContent());
    }

    /**
     * Previews a file by its ID, but only if the file is an image.
     *
     * @param fileId the ID of the file to preview
     * @return a ResponseEntity containing the file content if it's an image,
     * or a bad request response if the file type is not an image
     */
    @GetMapping("/{fileId}/preview")
    public ResponseEntity<byte[]> previewFile(@PathVariable Long fileId) {
        Optional<EmbeddedFile> fileOptional = fileRepository.findById(fileId);
        if (fileOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmbeddedFile file = fileOptional.get();
        if (!file.getType().startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getType()))
                .body(file.getContent());
    }

    /**
     * Retrieves the metadata of a file by its ID.
     * @param fileId the ID of the file to retrieve metadata for
     * @return a ResponseEntity containing the file metadata, or a not found response if the file doesn't exist
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<EmbeddedFile> getFileMetadata(@PathVariable Long fileId) {
        Optional<EmbeddedFile> fileOptional = fileRepository.findById(fileId);
        if (fileOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmbeddedFile file = fileOptional.get();
        return ResponseEntity.ok(file); // Return the file metadata
    }

    /**
     * Moves a file to another note.
     *
     * @param fileId the ID of the file to move.
     * @param targetNoteId the ID of the target note.
     * @return a ResponseEntity indicating the result of the operation.
     */
    @PutMapping("/{fileId}/move/{targetNoteId}")
    public ResponseEntity<Void> moveFile(@PathVariable Long fileId, @PathVariable Long targetNoteId) {
        Optional<EmbeddedFile> fileOptional = fileRepository.findById(fileId);
        if (fileOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Note> targetNoteOptional = noteRepository.findById(targetNoteId);
        if (targetNoteOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmbeddedFile file = fileOptional.get();
        Optional<Note> currentNoteOptional = noteRepository.findAll().stream()
                .filter(note -> note.getEmbeddedFiles().contains(file))
                .findFirst();

        // Remove file from current note if it exists
        currentNoteOptional.ifPresent(note -> {
            note.getEmbeddedFiles().remove(file);
            noteRepository.save(note);
        });

        // Add file to target note
        Note targetNote = targetNoteOptional.get();
        targetNote.getEmbeddedFiles().add(file);
        noteRepository.save(targetNote);

        return ResponseEntity.ok().build();
    }
}