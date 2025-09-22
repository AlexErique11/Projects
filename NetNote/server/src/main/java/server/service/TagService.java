package server.service;

import commons.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.TagRepository;

import java.util.List;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    /**
     * This method searches for a Tag entity in the repository based on the provided name.
     * If found, it returns the existing Tag;
     * otherwise, a new Tag is created with the provided name and color and saved in the repository.
     *
     * @param name The name of the Tag entity to be found or created.
     * @param color The color to be assigned to the Tag entity if it needs to be created.
     * @return Tag This returns the found or newly created Tag entity.
     */
    public Tag getOrCreateTag(String name, String color) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(new Tag(name, color)));
    }

    /**
     * This method retrieves Tag entities from the repository that match the provided color value.
     *
     * It calls the `findByColor` method of the `TagRepository` with the provided color parameter.
     * All Tag entities with a matching color are then returned as a List.
     *
     * @param color The color attribute that the Tag entities should possess.
     * @return List<Tag> This returns a list of Tag entities that have the specified color attribute.
     */
    public List<Tag> getTagsByColor(String color) {
        return tagRepository.findByColor(color);
    }

    /**
     * This method checks if a Tag entity with the specified name exists in the repository.
     *
     * It calls the `existsByName` method of the `TagRepository` with the provided name parameter.
     * The method returns true if a Tag with the specified name exists in the repository, and false otherwise.
     *
     * @param name The name of the Tag entity to check for existence.
     * @return boolean This returns a boolean indicating whether a Tag entity
     * with the specified name exists in the repository.
     */
    public boolean doesTagExist(String name) {
        return tagRepository.existsByName(name);
    }

}

