package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.SharedLink;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.SharedLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SharingService {

    @Autowired
    private SharedLinkRepository sharedLinkRepository;

    public String generateSharedLink(User user, UploadedFile file, Duration linkDuration) {
        String token = UUID.randomUUID().toString(); // Token erstellen
        System.out.println("Reached the generateSharedLink method!");
        LocalDateTime expireDate = LocalDateTime.now().plus(linkDuration);
        SharedLink sharedLink = new SharedLink(file, user, token, expireDate);

        sharedLinkRepository.save(sharedLink); // Speichern des Links in der DB

        return "http://localhost:8080/share/" + token; // Gebe den Link zurück
    }

}
