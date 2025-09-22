package server.database;
import commons.EmbeddedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import commons.Note;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository <Note, Long> {

    /**
     * This method searches for a Note entity with the given title in the repository.
     *
     * It calls the underlying JPA framework's `findByTitle` method with the provided title parameter.
     * The method returns an Optional<Note> which may be empty if a Note with the specified title was not found,
     * else it contains the found Note.
     *
     * @param title The title of the Note entity to be searched in the repository.
     * @return Optional<Note> This returns an Optional that contains the found Note entity if any;
     * is no such Note is found, the Optional is empty.
     */
    Optional<Note> findByTitle(String title);

    /**
     * This method retrieves all distinct Note entities that have any of the tags whose names are in the provided list.
     *
     * It calls the underlying JPA framework's `findDistinctByTagsNameIn` method with the provided list of tag names.
     * It returns a list of distinct Notes that are associated with any of the tags in the provided list;
     * the list may be empty if no such Notes exist.
     *
     * @param tagNames The list of tag names to match against the Notes' associated tags in the repository.
     * @return List<Note> This returns a list of distinct Note entities that possess any of the specified tags.
     * Empty List if there is no Note entity associated with any of the provided tags.
     */
    List<Note> findDistinctByTagsNameIn(List<String> tagNames);

}