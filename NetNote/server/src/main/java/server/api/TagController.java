package server.api;

import commons.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.TagRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    /**
     * This method is a GET request mapping that retrieves all Tag entities from the repository.
     *
     * The endpoint '/' retrieves all Tag entities stored in the repository.
     * These tag entities are returned in the response enveloped within
     * a ResponseEntity along with an HTTP status code 200 (OK).
     *
     * @return ResponseEntity<List<Tag>> This returns a ResponseEntity containing a List of all
     * Tag entities from the repository and an HTTP status code.
     */
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        return new ResponseEntity<>(tagRepository.findAll(), HttpStatus.OK);
    }

    /**
     * This method is a GET request mapping that retrieves a specific Tag entity from the repository based on its id.
     *
     * It's accessible via the endpoint '/{id}', where 'id' is the id of the desired Tag entity.
     * It attempts to fetch the Tag entity with the specified id from the repository.
     *
     * If a Tag entity with the provided id exists, it's returned
     * in the response along with an HTTP status code 200 (OK).
     * If no Tag entity with the provided id exists, the method returns an HTTP status code 404 (NOT FOUND).
     *
     * @param tagId The id of the Tag entity to be retrieved.
     * @return ResponseEntity<Tag> This returns a ResponseEntity containing
     * the Tag entity (if found) and the corresponding HTTP status code.
     *
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable(value = "id") Long tagId) {
        Optional<Tag> tag = tagRepository.findById(tagId);
        return tag.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * This method is a POST request mapping used to create a new Tag entity in the repository.
     *
     * The endpoint '/' accepts a Tag object in the request body. It then saves this object to the repository.
     * The newly created tag entity is returned in the response enveloped within a
     * ResponseEntity along with an HTTP status code 201 (Created).
     *
     * @param tag The Tag object to be saved in the repository.
     * @return ResponseEntity<Tag> This returns a ResponseEntity containing
     * the newly created Tag entity and an HTTP status code.
     */
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        return new ResponseEntity<>(tagRepository.save(tag), HttpStatus.CREATED);
    }

    /**
     * This method is a PUT request mapping that updates an existing Tag entity in the repository.
     *
     * The endpoint is '/{id}', where 'id' is the id of the Tag entity to update.
     * It expects a Tag object in the request body containing the updated details,
     * and the corresponding Tag entity's identifier as a path variable.
     *
     * If a Tag entity with the specified id exists,
     * it updates the entity with the new details and saves it back to the repository.
     * If successful, the updated Tag entity is returned in the response with an HTTP status code 200 (OK).
     *
     * If the specified Tag entity does not exist, returns an HTTP status code 404 (NOT FOUND).
     *
     * @param tagId The id of the Tag entity to be updated.
     * @param tagDetails A Tag object containing the updated details for the Tag entity.
     * @return ResponseEntity<Tag> This returns a ResponseEntity containing the updated Tag entity,
     * if found, and the corresponding HTTP status code.
     *
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(@PathVariable(value = "id") Long tagId, @RequestBody Tag tagDetails) {
        Optional<Tag> tag = tagRepository.findById(tagId);
        if (tag.isPresent()) {
            Tag updatedTag = tag.get();
            updatedTag.setName(tagDetails.getName());
            updatedTag.setColor(tagDetails.getColor());
            tagRepository.save(updatedTag);
            return new ResponseEntity<>(updatedTag, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * This method is a DELETE request mapping that removes a specific Tag entity from the repository based on its id.
     *
     * It's accessible via the endpoint '/{id}', where 'id' is the id of the Tag entity to be deleted.
     * It tries to fetch the Tag entity with the given id from the repository.
     *
     * If a Tag entity with the provided id is present, it's deleted from the repository.
     * After successful deletion, an HTTP status code 204 (No Content) is returned.
     *
     * If no Tag entity with the provided id exists, the method returns an HTTP status code 404 (NOT FOUND).
     *
     * @param tagId The id of the Tag entity to be deleted.
     * @return ResponseEntity<Void> This returns a ResponseEntity with the corresponding HTTP status code.
     *
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable(value = "id") Long tagId) {
        Optional<Tag> tag = tagRepository.findById(tagId);
        if (tag.isPresent()) {
            tagRepository.delete(tag.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}