package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.exception.ResourceNotFoundException;
import de.jadenk.springcloud.model.Log;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.repository.UploadedFileRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.FileUploadService;
import de.jadenk.springcloud.service.LogService;
import de.jadenk.springcloud.service.UserService;
import de.jadenk.springcloud.service.WebhookService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private LogService logService;

    @Autowired
    private MessageService messageService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            model.addAttribute("username", username);

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String role = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("UNKNOWN");

            model.addAttribute("role", role);
        }

        List<UploadedFile> files = uploadedFileRepository.findAll();
        model.addAttribute("files", files);

        return "dashboard";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                fileUploadService.uploadFile(file);
                return "redirect:/dashboard";
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/dashboard?uploadError";
            }
        } else {
            return "redirect:/dashboard?fileEmpty";
        }
    }

    @Autowired
    private WebhookService webhookService;

    @GetMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UploadedFile file = uploadedFileRepository.findById(id).orElseThrow();
        String filename = file.getFileName();

        uploadedFileRepository.deleteById(id);
        String message = messageService.getLog("dashboard.file.deleted", filename);
        Log log = logService.log(currentUser.getUsername(), message);

        webhookService.triggerWebhookEvent(WebhookEvent.USER_UPDATED, "User " + currentUser.getUsername() + " deleted an File.", log.getId());
        return "redirect:/dashboard";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UploadedFile file = uploadedFileRepository.findById(id).orElseThrow();
        String filename = file.getFileName();

        String message = messageService.getLog("dashboard.file.downloaded", filename);
        logService.log(currentUser.getUsername(), message);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, file.getFileType())
                .body(new ByteArrayResource(file.getFileData()));
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<Resource> getFile(@PathVariable Long fileId) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        String fileType = file.getFileType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileType))
                .body(new ByteArrayResource(file.getFileData()));
    }

}
