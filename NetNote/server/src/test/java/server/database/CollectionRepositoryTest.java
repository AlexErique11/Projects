package server.database;

import commons.Collection;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CollectionRepositoryTest implements CollectionRepository {

    public final List<Collection> collections = new ArrayList<>();

    @Override
    public void flush() {

    }

    @Override
    public <S extends Collection> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public <S extends Collection> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<Collection> entities) {
        deleteAll(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
        for (Long id : longs) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAllInBatch() {
        collections.clear();
    }

    @Override
    public Collection getOne(Long aLong) {
        return getById(aLong);
    }

    @Override
    public Collection getById(Long aLong) {
        return collections.stream().filter(collection -> collection.getId().equals(aLong)).findFirst().orElse(null);
    }

    @Override
    public Collection getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends Collection> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Collection> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends Collection> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends Collection> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Collection> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Collection> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Collection, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends Collection> S save(S entity) {

        if(entity instanceof Collection) {
            collections.add(entity);
        }
        return null;
    }

    @Override
    public <S extends Collection> List<S> saveAll(Iterable<S> entities) {
        List<S> list = new ArrayList<>();
        for (S entity : entities) {
            list.add(save(entity));
        }
        return list;
    }

    @Override
    public Optional<Collection> findById(Long aLong) {
        for (Collection collection : collections) {
            if (collection.getId().equals(aLong)) {
                return Optional.of(collection);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        for (Collection collection : collections) {
            if (collection.getId().equals(aLong)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Collection> findAll() {
        return collections;
    }

    @Override
    public List<Collection> findAllById(Iterable<Long> longs) {
        List<Collection> list = new ArrayList<>();
        for(long id : longs) {
            Optional<Collection>  col =  findById(id);
            if(col.isPresent()) {
                list.add(col.get());
            }
        }
        return list;
    }

    @Override
    public long count() {
        return collections.size();
    }

    @Override
    public void deleteById(Long aLong) {
        collections.removeIf(collection -> collection.getId().equals(aLong));
    }

    @Override
    public void delete(Collection entity) {
        collections.remove(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        for (Long id : longs) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Collection> entities) {
        for (Collection collection : entities) {
            delete(collection);
        }
    }

    @Override
    public void deleteAll() {
        collections.clear();
    }

    @Override
    public List<Collection> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<Collection> findAll(Pageable pageable) {
        return null;
    }
}
