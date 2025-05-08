package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.UploadedFileRepository;
import de.jadenk.springcloud.service.FileUploadService;
import de.jadenk.springcloud.service.LogService;
import de.jadenk.springcloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute User user) {
        userService.register(user);
        return "redirect:/login";
    }

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            model.addAttribute("username", username);
        }

        List<UploadedFile> files = uploadedFileRepository.findAll(); // alternativ: nur vom eingeloggten User
        model.addAttribute("files", files);

        return "dashboard";
    }

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                fileUploadService.uploadFile(file);
                return "redirect:/dashboard?uploadSuccess";
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/dashboard?uploadError";
            }
        } else {
            return "redirect:/dashboard?fileEmpty";
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id) {
        UploadedFile file = uploadedFileRepository.findById(id).orElseThrow();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, file.getFileType())
                .body(new ByteArrayResource(file.getFileData()));
    }

    @GetMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id) {
        uploadedFileRepository.deleteById(id);
        return "redirect:/dashboard";
    }

}
