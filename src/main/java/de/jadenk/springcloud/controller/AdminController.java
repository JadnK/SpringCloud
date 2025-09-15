package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.config.SecurityConfig;
import de.jadenk.springcloud.model.*;
import de.jadenk.springcloud.repository.*;
import de.jadenk.springcloud.service.*;
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

    // Services & Repositories, die für Admin-Operationen benötigt werden
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

    @Autowired
    private ApiTokenRepository apiTokenRepository;

    @Autowired
    private ApiTokenService apiTokenService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private UpdateService updateService;


    /*
     * GET /admin
     * Lädt das Admin-Dashboard mit allen nötigen Daten wie Benutzer, Logs, Rollen, Einstellungen etc.
     * @param page - Paginierung für Logs (default 1)
     */
    @GetMapping("/admin")
    public String adminDashboard(@RequestParam(value = "page", defaultValue = "1") int page,
                                 Model model) {
        // Aktuellen authentifizierten Benutzer abrufen
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Prüfen, ob der Benutzer die ADMIN-Rolle hat
        boolean hasAdminRole = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().endsWith("ADMIN"));

        // Wenn der Benutzer keine Admin-Rechte hat, Logging und Weiterleitung zum normalen Dashboard
        if (!hasAdminRole) {
            String username = authentication.getName();
            logService.log(username, messageService.getLog("admin.user.missing.permissions"));
            return "redirect:/dashboard";
        }

        // Daten abrufen: alle Benutzer, Rollen, Logs und Einstellungen
        List<User> users = userRepository.findAll();
        List<Role> roles = roleRepository.findAll();

        // Logs mit Paginierung
        int pageSize = 10;
        long totalLogs = logRepository.count();
        int totalPages = (int) Math.ceil((double) totalLogs / pageSize);
        int offset = (page - 1) * pageSize;
        List<Log> logs = logRepository.findLogsPaged(offset, pageSize);

        Webhook webhook = webhookService.getFirst().orElse(new Webhook());
        List<CloudSetting> settings = cloudSettingRepository.getAllSettings();
        ApiToken apiToken = apiTokenService.getFirst().orElse(new ApiToken());

        // Attribute für Thymeleaf-Template setzen
        model.addAttribute("settings", settings);
        model.addAttribute("api", apiToken);
        model.addAttribute("apis", apiTokenService.getAll());
        model.addAttribute("webhook", webhook);
        model.addAttribute("webhooks", webhookService.getAll());
        model.addAttribute("users", users);
        model.addAttribute("logs", logs);
        model.addAttribute("roles", roles);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", authorities.stream().findFirst().map(GrantedAuthority::getAuthority).orElse("UNKNOWN"));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentVersion", versionService.getCurrentVersion());
        model.addAttribute("latestVersion", updateService.fetchLatestVersion());

        return "admin"; // Thymeleaf-Template "admin.html"
    }

    /*
     * POST /admin/settings/update
     * Aktualisiert eine Cloud-Einstellung
     * @param key - Name der Einstellung
     * @param value - Neuer Wert
     * @param type - Typ der Einstellung (z.B. CHECKBOX)
     */
    @PostMapping("/admin/settings/update")
    public String updateSetting(@RequestParam String key,
                                @RequestParam(required = false) String value,
                                @RequestParam String type) {
        if ("CHECKBOX".equals(type) && value == null) {
            value = "false"; // Checkbox nicht angehakt → false
        }
        cloudSettingService.updateSetting(key, value, type);
        return "redirect:/admin";
    }

    /*
     * POST /admin/system/update
     * Prüft, ob ein Systemupdate verfügbar ist und führt es ggf. aus
     */
    @PostMapping("/admin/system/update")
    public String updateSystem(RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            String updateResult = updateService.checkForUpdateAndUpdateIfNeeded();

            if ("SUCCESS".equalsIgnoreCase(updateResult)) {
                logService.log(username, messageService.getLog("admin.system.update.success"));
            } else if ("NO_UPDATE".equalsIgnoreCase(updateResult)) {
                logService.log(username, messageService.getLog("admin.system.update.noUpdate"));
            } else {
                logService.log(username, messageService.getLog("admin.system.update.failed", updateResult));
            }
        } catch (Exception e) {
            String message = messageService.getLog("admin.system.update.exception", e.getMessage());
            logService.log(username, message);
            redirectAttributes.addFlashAttribute("errorMessage", message);
            e.printStackTrace();
        }

        return "redirect:/admin";
    }

    /*
     * POST /admin/user/update
     * Aktualisiert Benutzerinformationen: Username, E-Mail, Rolle, Passwort
     */
    @PostMapping("/admin/user/update")
    public String updateUser(@RequestParam("id") Long id,
                             @RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("role") String role,
                             @RequestParam(value = "password", required = false) String password) {

        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        // Aktive Sessions des Users ablaufen lassen
        expireUserSessions(user.getUsername());

        Log log = null;

        // Username aktualisieren
        if (!user.getUsername().equals(username)) {
            String message = messageService.getLog("admin.user.username.changed", user.getUsername(), username);
            log = logService.log(currentUser.getUsername(), message);
            user.setUsername(username);
        }

        // Email aktualisieren
        if (!user.getEmail().equals(email)) {
            String message = messageService.getLog("admin.user.email.changed", user.getUsername(), email);
            log = logService.log(currentUser.getUsername(), message);
            user.setEmail(email);
        }

        // Rolle aktualisieren
        Role foundRole = roleRepository.findByName(role).orElse(null);
        if (foundRole == null) {
            throw new IllegalArgumentException("Role not found: " + role);
        }
        if (!user.getRole().equals(foundRole)) {
            String message = messageService.getLog("admin.user.role.changed", user.getUsername(), user.getRole().getName(), foundRole.getName());
            log = logService.log(currentUser.getUsername(), message);
            user.setRole(foundRole);
        }

        // Passwort aktualisieren
        if (password != null && !password.trim().isEmpty()) {
            String encodedPassword = securityConfig.passwordEncoder().encode(password);
            user.setPassword(encodedPassword);
            String message = messageService.getLog("admin.user.password.changed", user.getUsername());
            log = logService.log(currentUser.getUsername(), message);
        }

        // Webhook auslösen
        webhookService.triggerWebhookEvent(WebhookEvent.USER_UPDATED, "User " + user.getUsername() + " was updated", log.getId());
        userRepository.save(user);

        return "redirect:/admin";
    }

    /*
     * GET /admin/user/delete/{id}
     * Löscht einen Benutzer
     */
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

    /*
     * GET /admin/user/ban/{id}
     * Banned oder entbannt einen Benutzer
     */
    @GetMapping("/admin/user/ban/{id}")
    public String banUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            userService.banUser(id); // Toggle ban/unban
            User user = userService.getUserById(id);

            // Aktive Sessions ablaufen lassen
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

    /*
     * GET /users
     * Liefert alle Benutzer für eine Benutzerliste (Frontend)
     */
    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "user-list";
    }

    /*
     * Hilfsmethode: Beendet alle Sessions eines Benutzers (z.B. nach Ban oder Rollenänderung)
     */
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
