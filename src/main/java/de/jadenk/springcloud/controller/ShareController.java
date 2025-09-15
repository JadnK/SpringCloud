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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Duration;
import java.time.LocalDateTime;

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

    // =========================
    // Datei-Sharing
    // =========================
    /*
     * GET /share/{fileId}?duration=hours
     *
     * Erstellt einen temporären Link zum Teilen einer Datei.
     * - Prüft, ob Sharing aktiviert ist (Cloud-Einstellung ALLOW_SHARING)
     * - Holt aktuell eingeloggten Benutzer
     * - Holt Datei anhand der ID
     * - Generiert temporären Link für angegebene Dauer (in Stunden)
     * - Redirect zum generierten Link
     */
    @GetMapping("/share/{fileId}")
    public RedirectView shareFile(HttpServletRequest request, @PathVariable Long fileId, @RequestParam int duration) {

        // Prüfen, ob Sharing erlaubt ist
        if (!cloudSettingService.getBooleanSetting("ALLOW_SHARING", true)) {
            RedirectView redirectView = new RedirectView("/dashboard");
            redirectView.setExposeModelAttributes(false);
            return redirectView;
        }

        // Aktuellen Benutzer abrufen
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserByName(username);

        // Datei abrufen
        UploadedFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Dauer des Links
        Duration linkDuration = Duration.ofHours(duration);

        // Link generieren
        String link = sharingService.generateSharedLink(request, user, file, linkDuration);

        return new RedirectView(link);
    }

    // =========================
    // Datei-Download / Preview
    // =========================
    /*
     * Hilfsmethode: Gibt die Datei als Resource zurück
     * - preview=true → inline (z.B. PDF, Image)
     * - preview=false → als Download
     */
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

    // =========================
    // Shared Link prüfen
    // =========================
    /*
     * GET /share/file/{token}
     * Prüft, ob der geteilte Link existiert und noch gültig ist.
     * - Abgelaufene Links → link-expired.html
     * - Gültige Links → Weiterleitung zur Anzeige-Seite
     */
    @GetMapping("/share/file/{token}")
    public String checkLinkAndRedirect(@PathVariable String token) {
        SharedLink link = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (LocalDateTime.now().isAfter(link.getExpireDate())) {
            return "link-expired";
        }

        return "redirect:/share/file/view/" + token;
    }

    // =========================
    // Shared File anzeigen
    // =========================
    /*
     * GET /share/file/view/{token}
     * Zeigt das geteilte File im Browser an (Preview)
     * - Prüft, ob der Link abgelaufen ist
     * - Prüft, ob die Datei previewbar ist (Image, PDF, Text)
     */
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

        return "shared_file"; // Thymeleaf Template
    }

    // =========================
    // Shared File Download
    // =========================
    /*
     * GET /share/file/download/{token}?preview=false
     * Liefert die Datei als Download oder inline (Preview)
     */
    @GetMapping("/share/file/download/{token}")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String token,
                                                       @RequestParam(name = "preview", defaultValue = "false") boolean preview) {
        SharedLink link = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UploadedFile file = link.getFile();
        return serveFile(file, preview);
    }

}
