package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.model.Log;
import de.jadenk.springcloud.model.Role;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.LogRepository;
import de.jadenk.springcloud.repository.RoleRepository;
import de.jadenk.springcloud.repository.UploadedFileRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.FileUploadService;
import de.jadenk.springcloud.service.LogService;
import de.jadenk.springcloud.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;

    @Autowired
    private UserRepository userRepository;

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

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        request.logout();
        return "redirect:/login?logout";
    }

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

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

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "user-list";
    }

    @Autowired
    private LogRepository logRepository;

    @GetMapping("/admin")
    public String adminDashboard(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean hasAdminRole = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!hasAdminRole) {
            String username = authentication.getName();
            logService.log(username, "Access attempt to /admin denied due to lack of ADMIN role");
            return "redirect:/dashboard";
        }

        List<User> users = userRepository.findAll();
        List<Log> logs = logRepository.findAllLogsSortedByTimestamp();

        model.addAttribute("users", users);
        model.addAttribute("logs", logs);

        String username = authentication.getName();
        logService.log(username, "Access to /admin page");
        return "admin";
    }

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/admin/changeRole")
    public String changeUserRole(@RequestParam Long userId, @RequestParam String newRoleName) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Role newRole = roleRepository.findByName(newRoleName).orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRole().clear();
        user.getRole().add(newRole);

        userRepository.save(user);
        return "redirect:/admin";
    }

    @GetMapping("/admin/deleteFile")
    public String deleteUserFile(@RequestParam Long fileId) {
        UploadedFile file = uploadedFileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
        uploadedFileRepository.delete(file);
        return "redirect:/admin";
    }

    @GetMapping("/user/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
        model.addAttribute("user", user);
        return "admin/edit-user";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UploadedFile file = uploadedFileRepository.findById(id).orElseThrow();
        String filename = file.getFileName();

        logService.log(currentUser.getUsername(), "File downloaded: " + filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, file.getFileType())
                .body(new ByteArrayResource(file.getFileData()));
    }

    @GetMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UploadedFile file = uploadedFileRepository.findById(id).orElseThrow();
        String filename = file.getFileName();

        uploadedFileRepository.deleteById(id);
        logService.log(currentUser.getUsername(), "File deleted: " + filename);
        return "redirect:/dashboard";
    }

}
