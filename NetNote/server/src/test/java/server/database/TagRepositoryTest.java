package server.database;

import commons.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TagRepositoryTest implements TagRepository {

    private final List<Tag> tags = new ArrayList<>();

    @Override
    public List<Tag> findAll() {
        return tags;
    }

    @Override
    public List<Tag> findAllById(Iterable<Long> longs) {
        List<Tag> result = new ArrayList<>();
        longs.forEach(id -> tags.stream().filter(tag -> tag.getId().equals(id)).forEach(result::add));
        return result;
    }

    @Override
    public long count() {
        return tags.size();
    }

    @Override
    public void deleteById(Long aLong) {
        tags.removeIf(tag -> tag.getId().equals(aLong));
    }

    @Override
    public void delete(Tag entity) {
        tags.remove(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        longs.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(Iterable<? extends Tag> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        tags.clear();
    }

    @Override
    public <S extends Tag> S save(S entity) {
        tags.add(entity);
        return entity;
    }

    @Override
    public <S extends Tag> List<S> saveAll(Iterable<S> entities) {
        entities.forEach(this::save);
        return (List<S>) tags;
    }

    @Override
    public Optional<Tag> findById(Long aLong) {
        return tags.stream().filter(tag -> tag.getId().equals(aLong)).findFirst();
    }

    @Override
    public boolean existsById(Long aLong) {
        return tags.stream().anyMatch(tag -> tag.getId().equals(aLong));
    }

    @Override
    public Optional<Tag> findByName(String name) {
        return tags.stream().filter(tag -> tag.getName().equals(name)).findFirst();
    }

    @Override
    public List<Tag> findByColor(String color) {
        List<Tag> result = new ArrayList<>();
        tags.stream().filter(tag -> tag.getColor().equals(color)).forEach(result::add);
        return result;
    }

    @Override
    public boolean existsByName(String name) {
        return tags.stream().anyMatch(tag -> tag.getName().equals(name));
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Tag> S saveAndFlush(S entity) {
        save(entity);
        flush();
        return entity;
    }

    @Override
    public <S extends Tag> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<Tag> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Tag getOne(Long aLong) {
        return findById(aLong).orElse(null);
    }

    @Override
    public Tag getById(Long aLong) {
        return getOne(aLong);
    }

    @Override
    public Tag getReferenceById(Long aLong) {
        return getOne(aLong);
    }

    @Override
    public <S extends Tag> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Tag> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Tag> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Tag> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Tag> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Tag> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Tag, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public List<Tag> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Tag> findAll(Pageable pageable) {
        return null;
    }

    @Test
    public void testSaveAndFindById() {
        TagRepositoryTest repository = new TagRepositoryTest();
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");
        tag.setColor("Red");

        repository.save(tag);
        Optional<Tag> retrievedTag = repository.findById(1L);

        assertTrue(retrievedTag.isPresent());
        assertEquals("Test Tag", retrievedTag.get().getName());
    }

    @Test
    public void testFindByName() {
        TagRepositoryTest repository = new TagRepositoryTest();
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Sample Tag");
        tag.setColor("Blue");

        repository.save(tag);
        Optional<Tag> retrievedTag = repository.findByName("Sample Tag");

        assertTrue(retrievedTag.isPresent());
        assertEquals("Blue", retrievedTag.get().getColor());
    }

    @Test
    public void testDeleteById() {
        TagRepositoryTest repository = new TagRepositoryTest();
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Delete Tag");
        tag.setColor("Green");

        repository.save(tag);
        repository.deleteById(1L);

        assertFalse(repository.existsById(1L));
    }

    @Test
    public void testFindByColor() {
        TagRepositoryTest repository = new TagRepositoryTest();
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Tag1");
        tag1.setColor("Yellow");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Tag2");
        tag2.setColor("Yellow");

        repository.save(tag1);
        repository.save(tag2);

        List<Tag> yellowTags = repository.findByColor("Yellow");

        assertEquals(2, yellowTags.size());
        assertTrue(yellowTags.stream().allMatch(tag -> tag.getColor().equals("Yellow")));
    }

    @Test
    public void testCountTags() {
        TagRepositoryTest repository = new TagRepositoryTest();
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Tag1");
        tag1.setColor("Red");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Tag2");
        tag2.setColor("Blue");

        repository.save(tag1);
        repository.save(tag2);

        assertEquals(2, repository.count());
    }

    @Test
    public void testExistsByName() {
        TagRepositoryTest repository = new TagRepositoryTest();
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Exists Tag");
        tag.setColor("Purple");

        repository.save(tag);

        assertTrue(repository.existsByName("Exists Tag"));
        assertFalse(repository.existsByName("Nonexistent Tag"));
    }

    @Test
    public void testSaveAll() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Tag1");
        tag1.setColor("Red");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Tag2");
        tag2.setColor("Blue");

        List<Tag> tags = List.of(tag1, tag2);
        repository.saveAll(tags);

        assertEquals(2, repository.count());
        assertTrue(repository.existsById(1L));
        assertTrue(repository.existsById(2L));
    }

    @Test
    public void testDeleteAll() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Tag1");
        tag1.setColor("Red");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Tag2");
        tag2.setColor("Blue");

        repository.save(tag1);
        repository.save(tag2);
        repository.deleteAll();

        assertEquals(0, repository.count());
    }

    @Test
    public void testUpdateTag() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Original Name");
        tag.setColor("Red");

        repository.save(tag);

        Tag updatedTag = new Tag();
        updatedTag.setId(1L);
        updatedTag.setName("Updated Name");
        updatedTag.setColor("Blue");

        repository.save(updatedTag);

        Optional<Tag> retrievedTag = repository.findById(1L);
        assertTrue(retrievedTag.isPresent());
    }

    @Test
    public void testFindNonexistentTag() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Optional<Tag> tag = repository.findById(99L);

        assertFalse(tag.isPresent());
    }

    @Test
    public void testAddDuplicateName() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Duplicate");
        tag1.setColor("Red");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Duplicate"); // Same name as tag1
        tag2.setColor("Blue");

        repository.save(tag1);
        repository.save(tag2);

        List<Tag> duplicates = repository.findAll().stream()
                .filter(tag -> tag.getName().equals("Duplicate"))
                .collect(Collectors.toList());

        assertEquals(2, duplicates.size());
    }

    @Test
    public void testFindWithPagination() {
        TagRepositoryTest repository = new TagRepositoryTest();

        for (int i = 1; i <= 10; i++) {
            Tag tag = new Tag();
            tag.setId((long) i);
            tag.setName("Tag" + i);
            tag.setColor("Color" + i);
            repository.save(tag);
        }

        List<Tag> allTags = repository.findAll();
        int pageSize = 5;
        List<Tag> page1 = allTags.subList(0, pageSize);
        List<Tag> page2 = allTags.subList(pageSize, allTags.size());

        assertEquals(5, page1.size());
        assertEquals(5, page2.size());
        assertEquals("Tag1", page1.get(0).getName());
        assertEquals("Tag6", page2.get(0).getName());
    }

    @Test
    public void testFindByPartialName() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("TagAlpha");
        tag1.setColor("Red");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("TagBeta");
        tag2.setColor("Blue");

        repository.save(tag1);
        repository.save(tag2);

        List<Tag> foundTags = repository.findAll().stream()
                .filter(tag -> tag.getName().contains("Tag"))
                .collect(Collectors.toList());

        assertEquals(2, foundTags.size());
        assertTrue(foundTags.stream().anyMatch(tag -> tag.getName().equals("TagAlpha")));
        assertTrue(foundTags.stream().anyMatch(tag -> tag.getName().equals("TagBeta")));
    }
    @Test
    public void testCountTagsByColor() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("RedTag");
        tag1.setColor("Red");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("AnotherRedTag");
        tag2.setColor("Red");

        Tag tag3 = new Tag();
        tag3.setId(3L);
        tag3.setName("BlueTag");
        tag3.setColor("Blue");

        repository.save(tag1);
        repository.save(tag2);
        repository.save(tag3);

        long redCount = repository.findByColor("Red").size();
        long blueCount = repository.findByColor("Blue").size();

        assertEquals(2, redCount);
        assertEquals(1, blueCount);
    }

    @Test
    public void testFindTagsWithEmptyName() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("");
        tag.setColor("Grey");

        repository.save(tag);

        List<Tag> emptyNameTags = repository.findAll().stream()
                .filter(t -> t.getName().isEmpty())
                .collect(Collectors.toList());

        assertEquals(1, emptyNameTags.size());
        assertEquals("Grey", emptyNameTags.get(0).getColor());
    }

    @Test
    public void testUpdateTagColorOnly() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("ColorChangeTag");
        tag.setColor("Orange");

        repository.save(tag);

        tag.setColor("Purple");
        repository.save(tag);

        Optional<Tag> updatedTag = repository.findById(1L);
        assertTrue(updatedTag.isPresent());
        assertEquals("Purple", updatedTag.get().getColor());
        assertEquals("ColorChangeTag", updatedTag.get().getName());
    }

    @Test
    public void testSaveDuplicateIds() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("FirstTag");
        tag1.setColor("Yellow");

        Tag tag2 = new Tag();
        tag2.setId(1L); // Duplicate ID
        tag2.setName("DuplicateTag");
        tag2.setColor("Green");

        repository.save(tag1);
        repository.save(tag2);

        List<Tag> tags = repository.findAll();
        assertEquals(2, tags.size());
        assertTrue(tags.stream().anyMatch(t -> t.getName().equals("FirstTag")));
        assertTrue(tags.stream().anyMatch(t -> t.getName().equals("DuplicateTag")));
    }

    @Test
    public void testFindAllTagsSortedByName() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Charlie");
        tag1.setColor("Red");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Bravo");
        tag2.setColor("Blue");

        Tag tag3 = new Tag();
        tag3.setId(3L);
        tag3.setName("Alpha");
        tag3.setColor("Green");

        repository.save(tag1);
        repository.save(tag2);
        repository.save(tag3);

        List<Tag> sortedTags = repository.findAll().stream()
                .sorted(Comparator.comparing(Tag::getName))
                .collect(Collectors.toList());

        assertEquals("Alpha", sortedTags.get(0).getName());
        assertEquals("Bravo", sortedTags.get(1).getName());
        assertEquals("Charlie", sortedTags.get(2).getName());
    }

    @Test
    public void testFindByNonexistentColor() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("ExistingTag");
        tag.setColor("Blue");

        repository.save(tag);

        List<Tag> nonexistentColorTags = repository.findByColor("Pink");
        assertTrue(nonexistentColorTags.isEmpty());
    }

    @Test
    public void testFindByPartialColor() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Tag1");
        tag1.setColor("DarkBlue");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Tag2");
        tag2.setColor("LightBlue");

        repository.save(tag1);
        repository.save(tag2);

        List<Tag> blueTags = repository.findAll().stream()
                .filter(tag -> tag.getColor().contains("Blue"))
                .collect(Collectors.toList());

        assertEquals(2, blueTags.size());
    }

    @Test
    public void testSaveNullNameTag() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName(null); // Null name
        tag.setColor("Grey");

        repository.save(tag);

        List<Tag> allTags = repository.findAll();
        assertEquals(1, allTags.size());
        assertNull(allTags.get(0).getName());
    }

    @Test
    public void testDeleteNonexistentTag() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("ToDelete");
        tag.setColor("Red");

        repository.save(tag);

        repository.deleteById(99L); // Nonexistent ID

        assertEquals(1, repository.count());
        assertTrue(repository.existsById(1L));
    }

    @Test
    public void testSaveTagWithoutId() {
        TagRepositoryTest repository = new TagRepositoryTest();

        Tag tag = new Tag();
        tag.setName("NoIdTag");
        tag.setColor("Orange");

        repository.save(tag);

        assertEquals(1, repository.count());
        assertNotNull(repository.findAll().get(0).getName());
        assertEquals("Orange", repository.findAll().get(0).getColor());
    }


}
