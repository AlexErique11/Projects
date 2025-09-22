package server.database;

import commons.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * This method retrieves a Tag entity by its name from the repository.
     *
     * The underlying Spring Data JPA implementation will create and execute the equivalent SQL query to fetch the Tag.
     * It returns an Optional<Tag> which may be empty if a Tag with
     * the specified name was not found; if such Tag is found,
     * it is included in the Optional.
     *
     * @param name The name of the Tag entity to be fetched from the repository.
     * @return Optional<Tag> This returns an Optional that contains the found Tag entity if any;
     * if no such Tag is found, the Optional is empty.
     */
    Optional<Tag> findByName(String name);

    /**
     * This method retrieves all Tag entities with a specific color from the repository.
     *
     * The underlying Spring Data JPA implementation will create and execute the equivalent SQL query to fetch the Tags.
     * It returns a List<Tag> containing all Tag entities with the given color;
     * the returned list may be empty if no such Tags exist.
     *
     * @param color The color of the Tag entities to be fetched from the repository.
     * @return List<Tag> This returns a List of Tag entities with the specified color.
     * Empty List if there is no Tag entity with the specified color.
     */
    List<Tag> findByColor(String color);

    /**
     * This method checks if a Tag entity with a specific name exists in the repository.
     *
     * The underlying Spring Data JPA implementation will create and execute the
     * equivalent SQL query to check the existence.
     *
     * @param name The name of the Tag entity to be checked for existence in the repository.
     * @return boolean This returns true if a Tag with the specified name exists in the repository, and false otherwise.
     */
    boolean existsByName(String name);



}
