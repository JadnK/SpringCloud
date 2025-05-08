package de.jadenk.springcloud.repository;

import de.jadenk.springcloud.model.Ban;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BanRepository extends JpaRepository<Ban, Long> {
}