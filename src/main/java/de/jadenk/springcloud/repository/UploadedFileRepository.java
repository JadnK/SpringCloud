package de.jadenk.springcloud.repository;

import de.jadenk.springcloud.model.Folder;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    List<UploadedFile> findByFileOwner(User fileOwner);
    List<UploadedFile> findByFileOwnerAndFolder(User fileOwner, Folder folder);
}
