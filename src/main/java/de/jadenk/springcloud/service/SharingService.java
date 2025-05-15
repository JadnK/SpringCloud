package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.SharedLink;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.SharedLinkRepository;
import de.jadenk.springcloud.util.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Service
public class SharingService {

    @Autowired
    private SharedLinkRepository sharedLinkRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private MessageService messageService;

    public String generateSharedLink(HttpServletRequest request, User user, UploadedFile file, Duration linkDuration) {
        String token = UUID.randomUUID().toString();

        LocalDateTime expireDate = LocalDateTime.now().plus(linkDuration);
        SharedLink sharedLink = new SharedLink(file, user, token, expireDate);

        sharedLinkRepository.save(sharedLink);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String fullUrl = baseUrl + "/share/file/" + token;

        String message = messageService.getLog("sharing.file", file.getFileName(), file.getId(), fullUrl);
        logService.log(username, message);

        return fullUrl;
    }
}
