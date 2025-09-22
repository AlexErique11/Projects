package server.database;

import commons.EmbeddedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<EmbeddedFile, Long> {
    // Additional query methods if needed
}