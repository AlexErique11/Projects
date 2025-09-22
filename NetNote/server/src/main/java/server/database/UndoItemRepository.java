package server.database;

import commons.Collection;
import commons.UndoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UndoItemRepository extends JpaRepository<UndoItem, Long> {
}

