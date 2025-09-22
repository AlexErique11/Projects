package server.database;

import commons.EmbeddedFile;
import commons.Note;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class FileRepositoryTest implements FileRepository{
    private final List<EmbeddedFile> files = new ArrayList<>();

    @Override
    public void flush() {

    }

    @Override
    public <S extends EmbeddedFile> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends EmbeddedFile> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<EmbeddedFile> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public EmbeddedFile getOne(Long aLong) {
        return null;
    }

    @Override
    public EmbeddedFile getById(Long aLong) {
        return null;
    }

    @Override
    public EmbeddedFile getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends EmbeddedFile> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends EmbeddedFile> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends EmbeddedFile> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends EmbeddedFile> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends EmbeddedFile> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends EmbeddedFile> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends EmbeddedFile, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public EmbeddedFile save(EmbeddedFile file) {
        files.removeIf(f -> f.getId().equals(file.getId()));
        files.add(file);
        return file;
    }

    @Override
    public <S extends EmbeddedFile> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<EmbeddedFile> findById(Long id) {
        return files.stream().filter(file -> file.getId().equals(id)).findFirst();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public List<EmbeddedFile> findAll() {
        return files;
    }

    @Override
    public List<EmbeddedFile> findAllById(Iterable<Long> longs) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(EmbeddedFile file) {
        files.remove(file);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends EmbeddedFile> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<EmbeddedFile> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<EmbeddedFile> findAll(Pageable pageable) {
        return null;
    }
}
