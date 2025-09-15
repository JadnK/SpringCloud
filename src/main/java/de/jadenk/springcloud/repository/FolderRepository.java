package de.jadenk.springcloud.repository;

import de.jadenk.springcloud.model.Folder;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByOwner(User owner);
}
