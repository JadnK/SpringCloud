package de.jadenk.springcloud.repository;

import de.jadenk.springcloud.model.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiTokenRepository extends JpaRepository<ApiToken, Long> {
    Optional<ApiToken> findByTokenAndActiveTrue(String token);
}
