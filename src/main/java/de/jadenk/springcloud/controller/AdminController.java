package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.config.SecurityConfig;
import de.jadenk.springcloud.model.*;
import de.jadenk.springcloud.repository.CloudSettingRepository;
import de.jadenk.springcloud.repository.LogRepository;
import de.jadenk.springcloud.repository.RoleRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.CloudSettingService;
import de.jadenk.springcloud.service.LogService;
import de.jadenk.springcloud.service.UserService;
import de.jadenk.springcloud.service.WebhookService;
import de.jadenk.springcloud.util.MessageService;
import de.jadenk.springcloud.util.WebhookEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class AdminController {

    @Autowired
    private LogService logService;

    @Autowired
    private SessionRegistry sessionRegistry;


    @Autowired
    private WebhookService webhookService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CloudSettingRepository cloudSettingRepository;

    @Autowired
    private CloudSettingService cloudSettingService;


    @GetMapping("/admin")
    public String adminDashboard(@RequestParam(value = "page", defaultValue = "1") int page,
                                 Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean hasAdminRole = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().endsWith("ADMIN"));

        if (!hasAdminRole) {
            String username = authentication.getName();
            logService.log(username, messageService.getLog("admin.user.missing.permissions"));
            return "redirect:/dashboard";
        }

        List<User> users = userRepository.findAll();
        List<Role> roles = roleRepository.findAll();

        int pageSize = 10;
        long totalLogs = logRepository.count();
        int totalPages = (int) Math.ceil((double) totalLogs / pageSize);

        int offset = (page - 1) * pageSize;
        List<Log> logs = logRepository.findLogsPaged(offset, pageSize);


        Webhook webhook = webhookService.getFirst().orElse(new Webhook());
        List<CloudSetting> settings = cloudSettingRepository.getAllSettings();
        model.addAttribute("settings", settings);
        model.addAttribute("webhook", webhook);
        model.addAttribute("webhooks", webhookService.getAll());
        model.addAttribute("users", users);
        model.addAttribute("logs", logs);
        model.addAttribute("roles", roles);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", authorities.stream().findFirst().map(GrantedAuthority::getAuthority).orElse("UNKNOWN"));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin";
    }

    @PostMapping("/admin/settings/update")
    public String updateSetting(@RequestParam String key,
                                @RequestParam(required = false) String value,
                                @RequestParam String type) {
        if ("CHECKBOX".equals(type) && value == null) {
            value = "false";
        } else {

        }
        cloudSettingService.updateSetting(key, value, type);
        return "redirect:/admin";
    }



    @PostMapping("/admin/user/update")
    public String updateUser(@RequestParam("id") Long id,
                             @RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("role") String role,
                             @RequestParam(value = "password", required = false) String password) {

        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        expireUserSessions(user.getUsername());

        Log log = null;

        if (!user.getUsername().equals(username)) {
            String message = messageService.getLog("admin.user.username.changed", user.getUsername(), username);
            log = logService.log(currentUser.getUsername(), message);
            user.setUsername(username);
        }

        if (!user.getEmail().equals(email)) {
            String message = messageService.getLog("admin.user.email.changed", user.getUsername(), email);
            log = logService.log(currentUser.getUsername(), message);
            user.setEmail(email);
        }

        Role foundRole = roleRepository.findByName(role).orElse(null);
        if (foundRole == null) {
            throw new IllegalArgumentException("Role not found: " + role);
        }

        Role currentRole = user.getRole();
        if (!currentRole.equals(foundRole)) {
            String message = messageService.getLog("admin.user.role.changed", user.getUsername(), currentRole.getName(), foundRole.getName());
            log = logService.log(currentUser.getUsername(), message);
            user.setRole(foundRole);
        }

        if (password != null && !password.trim().isEmpty()) {
            String encodedPassword = securityConfig.passwordEncoder().encode(password);
            user.setPassword(encodedPassword);
            String message = messageService.getLog("admin.user.password.changed", user.getUsername());
            log = logService.log(currentUser.getUsername(), message);
        }

        webhookService.triggerWebhookEvent(WebhookEvent.USER_UPDATED, "User " + user.getUsername() + " was updated", log.getId());

        userRepository.save(user);

        return "redirect:/admin";
    }

    @GetMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User userToDelete = userService.getUserById(id);
        boolean deleted = userService.deleteUserById(id);
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (deleted) {
            String message = messageService.getLog("admin.user.deleted", userToDelete.getUsername());
            logService.log(currentUser.getUsername(), message);
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/user/ban/{id}")
    public String banUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            userService.banUser(id);
            User user = userService.getUserById(id);
            expireUserSessions(user.getUsername());
            String status = user.isBanned() ? "banned" : "unbanned";
            String message = messageService.getLog("admin.user.ban.status", user.getUsername(), status);
            Log log = logService.log(currentUser.getUsername(), message);
            webhookService.triggerWebhookEvent(WebhookEvent.USER_BANNED, "User " + user.getUsername() + " was " + status, log.getId());
        } catch (Exception e) {
            System.out.println("Error while banning user.");
        }
        return "redirect:/admin";
    }

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "user-list";
    }

    private void expireUserSessions(String username) {
        List<Object> principals = sessionRegistry.getAllPrincipals();
        for (Object principal : principals) {
            if (principal instanceof UserDetails userDetails &&
                    userDetails.getUsername().equals(username)) {
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(userDetails, false);
                for (SessionInformation session : sessions) {
                    session.expireNow();
                }
            }
        }
    }


}
