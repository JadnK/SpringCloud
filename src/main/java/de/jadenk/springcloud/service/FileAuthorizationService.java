package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.FileAuthorization;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.FileAuthorizationRepository;
import de.jadenk.springcloud.repository.UploadedFileRepository;
import de.jadenk.springcloud.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileAuthorizationService {

    @Autowired
    private FileAuthorizationRepository fileAuthorizationRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private UserRepository userRepository;

    public void authorizeUser(Long fileId, Long userId) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Datei nicht gefunden"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));

        boolean alreadyAuthorized = fileAuthorizationRepository
                .findByFileId(fileId)
                .stream()
                .anyMatch(fa -> fa.getUser().getId().equals(userId));

        if (!alreadyAuthorized) {
            FileAuthorization authorization = new FileAuthorization();
            authorization.setFile(file);
            authorization.setUser(user);
            fileAuthorizationRepository.save(authorization);
        }
    }

    public void revokeAuthorization(Long fileId, Long userId) {
        List<FileAuthorization> authorizations = fileAuthorizationRepository.findByFileId(fileId)
                .stream()
                .filter(fa -> fa.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        if (!authorizations.isEmpty()) {
            fileAuthorizationRepository.deleteAll(authorizations);
        }
    }

    public boolean isUserAuthorized(Long fileId, Long userId) {
        return fileAuthorizationRepository.findByFileId(fileId)
                .stream()
                .anyMatch(fa -> fa.getUser().getId().equals(userId));
    }

    @Transactional
    public void setAuthorizedUsers(Long fileId, List<Long> userIds) {
        fileAuthorizationRepository.deleteByFileId(fileId);

        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Datei nicht gefunden"));

        if (userIds != null) {
            for (Long userId : userIds) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));
                FileAuthorization authorization = new FileAuthorization();
                authorization.setFile(file);
                authorization.setUser(user);
                fileAuthorizationRepository.save(authorization);
            }
        }
    }
}
