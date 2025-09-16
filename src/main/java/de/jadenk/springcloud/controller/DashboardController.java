package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.dto.UserDTO;
import de.jadenk.springcloud.exception.ResourceNotFoundException;
import de.jadenk.springcloud.model.*;
import de.jadenk.springcloud.repository.FileAuthorizationRepository;
import de.jadenk.springcloud.repository.FolderRepository;
import de.jadenk.springcloud.repository.UploadedFileRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.*;
import de.jadenk.springcloud.util.MessageService;
import de.jadenk.springcloud.util.WebhookEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    // =========================
    // Repositories und Services
    // =========================
    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private LogService logService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private FileAuthorizationRepository fileAuthorizationRepository;

    @Autowired
    private UpdateService updateService;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FileAuthorizationService fileAuthorizationService;

    // =========================
    // Dashboard-Anzeige
    // =========================
    /*
     * GET /dashboard
     * Zeigt die Dashboard-Seite an, inkl.:
     * - aktuell eingeloggter Benutzer und Rolle
     * - zugängliche Dateien
     * - alle Benutzer als DTOs
     * - Hinweis auf verfügbare Updates (nur für Admin)
     */
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "error", required = false) String error,
                            Model model,
                            Authentication authentication,
                            @RequestParam(required = false) Long folderId) {

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            model.addAttribute("username", username);

            // Rolle bestimmen
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("UNKNOWN");
            model.addAttribute("role", role);

            // Aktueller Benutzer
            User currentUser = userRepository.findByUsername(username).orElseThrow();
            Long userId = currentUser.getId();

            Folder currentFolder;
            if (folderId != null) {
                currentFolder = folderRepository.findById(folderId).orElseThrow();
            } else {
                currentFolder = null;
            }

            // Unterordner des aktuellen Benutzers
            List<Folder> subFolders = folderRepository.findByOwner(currentUser);

            List<UploadedFile> accessibleFiles = uploadedFileRepository.findAll().stream()
                    .filter(file -> file.getFileOwner().getId().equals(userId)
                            || fileAuthorizationService.isUserAuthorized(file.getId(), userId))
                    .peek(file -> {
                        List<User> allowedUsers = fileAuthorizationRepository.findByFileId(file.getId())
                                .stream()
                                .map(FileAuthorization::getUser)
                                .collect(Collectors.toList());
                        file.setAuthorizedUsers(allowedUsers);
                    })
                    .toList();


            // Alle Benutzer für Admin/Anzeigezwecke
            List<UserDTO> userDTOs = userRepository.findAll().stream()
                    .map(UserDTO::new)
                    .toList();

            // Model Attributes
            model.addAttribute("currentFolder", currentFolder);
            model.addAttribute("subFolders", subFolders);
            model.addAttribute("files", accessibleFiles);
            model.addAttribute("allUsers", userDTOs);

            // Hinweis auf verfügbares Update für Admin
            if ("ROLE_ADMIN".equalsIgnoreCase(role) && updateService.isUpdateAvailable()) {
                model.addAttribute("error", "Please update SpringCloud, use the Admin Panel.");
            }
        }

        // Fehlermeldungen beim Upload
        if ("uploadError".equals(error)) {
            model.addAttribute("error", "There was an Error while Uploading. Try again later.");
        } else if ("uploadInProgress".equals(error)) {
            model.addAttribute("error", "There is an current Upload in Progress.");
        } else if (error != null) {
            model.addAttribute("error", "An Error occurred.");
        }

        return "dashboard";
    }


    // =========================
    // Ordner erstellung
    // =========================
    /*
     * POST /createFolder
     * Erstellung eines ordners
     */

    @PostMapping("/createFolder")
    public String createFolder(@RequestParam String folderName,
                               Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Folder folder = new Folder();
        folder.setFolderName(folderName);
        folder.setOwner(currentUser);

        folderRepository.save(folder);
        return "redirect:/dashboard";
    }
    // =========================
    // Ordner löschung
    // =========================
    /*
     * POST /deleteFolder
     * Löschung eines Ordners
     */
    @PostMapping("/deleteFolder")
    public String deleteFolder(@RequestParam Long folderId, Principal principal) {
        if (folderId == null || folderId == 0) {
            return "redirect:/dashboard?error=cannotDeleteRoot";
        }

        Folder folder = folderRepository.findById(folderId).orElseThrow();

        if (!folder.getOwner().getUsername().equals(principal.getName())) {
            return "redirect:/dashboard?error=notAuthorized";
        }

        folderRepository.delete(folder);

        return "redirect:/dashboard?success=folderDeleted";
    }


    // =========================
    // Datei-Upload
    // =========================
    /*
     * POST /upload
     * Upload von einem oder mehreren Dateien
     */
    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile[] files,
                               @RequestParam(required = false) Long folderId,
                               Principal principal) {

        String username = principal.getName();

        try {
//            System.out.println("DashboardController >> L_214");
            User currentUser = userRepository.findByUsername(username).orElseThrow();
            Folder folder = null;
            if (folderId != null) {
                folder = folderRepository.findById(folderId).orElseThrow();
            }

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    fileUploadService.uploadFile(file, currentUser, folder);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dashboard?error=uploadError";
        }

        return "redirect:/dashboard";
        //return "redirect:/dashboard" + (folderId != null ? "?folderId=" + folderId : "");
    }

    // =========================
    // Datei-Löschen
    // =========================
    /*
     * GET /delete/{id}
     * Löscht eine Datei
     * Loggt die Aktion und triggert Webhook
     */
    @GetMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UploadedFile file = uploadedFileRepository.findById(id).orElseThrow();
        String filename = file.getFileName();

        uploadedFileRepository.deleteById(id);

        String message = messageService.getLog("dashboard.file.deleted", filename);
        Log log = logService.log(currentUser.getUsername(), message);

        webhookService.triggerWebhookEvent(WebhookEvent.USER_UPDATED,
                "User " + currentUser.getUsername() + " deleted a file.", log.getId());

        return "redirect:/dashboard";
    }

    // =========================
    // Datei-Download
    // =========================
    /*
     * GET /download/{id}
     * Lädt eine Datei herunter
     * Loggt die Aktion
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UploadedFile file = uploadedFileRepository.findById(id).orElseThrow();
        String filename = file.getFileName();

        String message = messageService.getLog("dashboard.file.downloaded", filename);
        logService.log(currentUser.getUsername(), message);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, file.getFileType())
                .body(new ByteArrayResource(file.getFileData()));
    }

    /*
     * GET /file/{fileId}
     * Liefert eine Datei als Resource (z.B. für Preview)
     */
    @GetMapping("/file/{fileId}")
    public ResponseEntity<Resource> getFile(@PathVariable Long fileId) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getFileType()))
                .body(new ByteArrayResource(file.getFileData()));
    }

    // =========================
    // Datei-Berechtigungen verwalten
    // =========================
    /*
     * POST /edit/file
     * Aktualisiert die Liste der autorisierten Benutzer für eine Datei
     */
    @PostMapping("/edit/file")
    public String editFile(@RequestParam Long fileId,
                           @RequestParam(name = "authorizedUsers", required = false) List<Long> authorizedUserIds) {
        fileAuthorizationService.setAuthorizedUsers(fileId, authorizedUserIds);
        return "redirect:/dashboard";
    }

    /*
     * GET /api/file/{fileId}/authorizedUsers
     * Liefert die IDs der Benutzer, die Zugriff auf eine Datei haben
     */
    @GetMapping("/api/file/{fileId}/authorizedUsers")
    @ResponseBody
    public List<Long> getAuthorizedUserIds(@PathVariable Long fileId) {
        UploadedFile file = uploadedFileRepository.findById(fileId).orElseThrow();
        return file.getAuthorizedUsers().stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }
}
