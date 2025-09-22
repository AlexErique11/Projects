package server.database;

import commons.EmbeddedFile;
import commons.Note;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class NoteRepositoryTest implements NoteRepository {
    public final List<Note> notes = new ArrayList<>();

    @Override
    public void flush() {

    }

    @Override
    public <S extends Note> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Note> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<Note> entities) {
        deleteAll();
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Note getOne(Long aLong) {
        return null;
    }

    @Override
    public Note getById(Long aLong) {
        return notes.stream().filter(n -> n.getId() == aLong).findFirst().orElse(null);
    }

    @Override
    public Note getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends Note> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Note> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends Note> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends Note> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Note> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Note> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Note, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends Note> S save(S entity) {
        entity.setId((long) (notes.size() + 1)); // Auto-generate ID
        notes.removeIf(note -> Objects.equals(note.getId(), entity.getId())); // Safely compare IDs
        notes.add(entity);
        return entity;
    }


    @Override
    public <S extends Note> List<S> saveAll(Iterable<S> entities) {
        List<S> savedNotes = new ArrayList<>();
        for (S entity : entities) {
            savedNotes.add(save(entity));
        }
        return savedNotes;
    }

    @Override
    public Optional<Note> findById(Long id) {
        return notes.stream().filter(note -> note.getId() == id).findFirst();
    }

    @Override
    public boolean existsById(Long id) {
        return notes.stream().anyMatch(note -> note.getId() == id);
    }

    @Override
    public List<Note> findAll() {
        return notes;
    }

    @Override
    public List<Note> findAllById(Iterable<Long> longs) {
        return List.of();
    }

    @Override
    public long count() {
        return notes.size();
    }

    @Override
    public void deleteById(Long aLong) {
        notes.removeIf(note -> Long.valueOf(note.getId()).equals(aLong));
    }

    @Override
    public void delete(Note entity) {
        notes.remove(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        longs.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(Iterable<? extends Note> entities) {

    }

    @Override
    public void deleteAll() {
        notes.removeAll(notes);
    }

    @Override
    public List<Note> findAll(Sort sort) {
        return findAll();
    }

    @Override
    public Page<Note> findAll(Pageable pageable) {
        List<Note> allNotes = findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allNotes.size());
        return new PageImpl<>(allNotes.subList(start, end), pageable, allNotes.size());
    }

    @Override
    public Optional<Note> findByTitle(String title) {
        return notes.stream().filter(note -> note.getTitle().equals(title)).findFirst();
    }

    @Override
    public List<Note> findDistinctByTagsNameIn(List<String> tagNames) {
        Note note = new Note();
        note.setId(1L);
        note.setText("This is a note");
        note.setTitle("title");

        // Returning a list with note for the given tag names
        return List.of(note);
    }

}