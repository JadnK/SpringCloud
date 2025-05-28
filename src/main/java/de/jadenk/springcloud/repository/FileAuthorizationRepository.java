package de.jadenk.springcloud.repository;


import de.jadenk.springcloud.model.FileAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileAuthorizationRepository extends JpaRepository<FileAuthorization, Long> {
    List<FileAuthorization> findByFileId(Long fileId);
    void deleteByFileId(Long fileId);
}
