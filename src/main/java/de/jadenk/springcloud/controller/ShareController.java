package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.model.SharedLink;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.SharedLinkRepository;
import de.jadenk.springcloud.repository.UploadedFileRepository;
import de.jadenk.springcloud.service.CloudSettingService;
import de.jadenk.springcloud.service.FileUploadService;
import de.jadenk.springcloud.service.SharingService;
import de.jadenk.springcloud.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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

    @Autowired
    private CloudSettingService cloudSettingService;


    @GetMapping("/share/{fileId}")
    public RedirectView shareFile(HttpServletRequest request, @PathVariable Long fileId, @RequestParam int duration) {

        if (!cloudSettingService.getBooleanSetting("ALLOW_SHARING", true)) {
            RedirectView redirectView = new RedirectView("/admin");
            redirectView.setExposeModelAttributes(false);
            redirectView.setUrl("/dashboard");
            return redirectView;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.getUserByName(username);

        UploadedFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Duration linkDuration = Duration.ofHours(duration);
        String link = sharingService.generateSharedLink(request, user, file, linkDuration);

        return new RedirectView(link);
    }



    private ResponseEntity<Resource> serveFile(UploadedFile file, boolean preview) {
        ByteArrayResource resource = new ByteArrayResource(file.getFileData());

        HttpHeaders headers = new HttpHeaders();
        if (preview) {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"");
        } else {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(file.getFileType()))
                .body(resource);
    }

    @GetMapping("/share/file/{token}")
    public String checkLinkAndRedirect(@PathVariable String token) {
        SharedLink link = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (LocalDateTime.now().isAfter(link.getExpireDate())) {
            return "link-expired";
        }

        return "redirect:/share/file/view/" + token;
    }

    @GetMapping("/share/file/view/{token}")
    public String viewSharedFile(@PathVariable String token, Model model) {
        SharedLink link = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (LocalDateTime.now().isAfter(link.getExpireDate())) {
            return "link-expired";
        }

        UploadedFile file = link.getFile();
        String fileType = file.getFileType();

        boolean isPreviewable = fileType != null && (
                fileType.startsWith("image/") ||
                        fileType.equals("application/pdf") ||
                        fileType.startsWith("text/")
        );

        model.addAttribute("fileName", file.getFileName());
        model.addAttribute("fileType", fileType);
        model.addAttribute("token", token);
        model.addAttribute("expireDate", link.getExpireDate());
        model.addAttribute("isPreviewable", isPreviewable);

        return "shared_file";
    }



    @GetMapping("/share/file/download/{token}")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String token,
                                                       @RequestParam(name = "preview", defaultValue = "false") boolean preview) {
        SharedLink link = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UploadedFile file = link.getFile();
        return serveFile(file, preview);
    }

}
