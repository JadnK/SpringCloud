package de.jadenk.springcloud.repository;

import de.jadenk.springcloud.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    @Query("SELECT l FROM Log l ORDER BY l.timestamp DESC")
    List<Log> findAllLogsSortedByTimestamp();

    @Query(value = "SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Log> findLogsPaged(@Param("offset") int offset, @Param("limit") int limit);
}
