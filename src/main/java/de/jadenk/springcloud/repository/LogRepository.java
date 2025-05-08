package de.jadenk.springcloud.repository;

import de.jadenk.springcloud.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Long> {
}