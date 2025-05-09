package de.jadenk.springcloud.repository;

import de.jadenk.springcloud.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    @Query("SELECT l FROM Log l ORDER BY l.timestamp DESC")
    List<Log> findAllLogsSortedByTimestamp();
}
