package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.model.SharedLink;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.SharedLinkRepository;
import de.jadenk.springcloud.repository.UploadedFileRepository;
import de.jadenk.springcloud.service.FileUploadService;
import de.jadenk.springcloud.service.SharingService;
import de.jadenk.springcloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Controller
public class ShareController {

    @Autowired
    private UserService userService;

    @Autowired
    private UploadedFileRepository fileRepository;

    @Autowired
    private SharingService sharingService;

    @Autowired
    private SharedLinkRepository sharedLinkRepository;

    @Autowired
    private FileUploadService fileUploadService;


    @GetMapping("/share/{fileId}")
    public ResponseEntity<String> shareFile(@PathVariable Long fileId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.getUserByName(username);

        UploadedFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Duration linkDuration = Duration.ofDays(7);

        String link = sharingService.generateSharedLink(user, file, linkDuration);

        return ResponseEntity.ok(link);
    }



    private ResponseEntity<Resource> serveFile(UploadedFile file) {
        ByteArrayResource resource = new ByteArrayResource(file.getFileData());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getFileType()))
                .body(resource);
    }

    @GetMapping("/share/file/{token}")
    public String checkLinkAndRedirect(@PathVariable String token, RedirectAttributes redirectAttributes) {
        SharedLink link = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (LocalDateTime.now().isAfter(link.getExpireDate())) {
            return "link-expired";
        }

        return "redirect:/share/file/download/" + token;
    }


    @GetMapping("/share/file/download/{token}")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String token) {
        System.out.println("Requested token: " + token);
        SharedLink link = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UploadedFile file = link.getFile();
        return serveFile(file);
    }




}
