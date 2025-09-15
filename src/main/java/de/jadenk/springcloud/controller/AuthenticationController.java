package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.service.LogService;
import de.jadenk.springcloud.service.UserService;
import de.jadenk.springcloud.util.MessageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@Controller
public class AuthenticationController {

    @Autowired
    private UserService userService; // Service für Benutzeroperationen

    @Autowired
    private LogService logService; // Service für Logging

    @Autowired
    private MessageService messageService; // Service für Nachrichten / Fehlermeldungen

    /*
     * GET /login
     * Zeigt die Login-Seite an. Optionaler Parameter "error" zeigt Fehlermeldungen an.
     * @param error - Fehlercode (z.B. "locked" oder allgemeiner Fehler)
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        Model model) {
        if ("locked".equals(error)) {
            // Account gesperrt nach mehreren fehlgeschlagenen Login-Versuchen
            model.addAttribute("error", "Account temporarily locked after multiple failed logins.");
        } else if (error != null) {
            // Allgemeiner Login-Fehler (falscher Benutzername oder Passwort)
            model.addAttribute("error", "Invalid Username or Password.");
        }
        return "login"; // Thymeleaf-Template "login.html"
    }

    /*
     * GET /register
     * Zeigt das Registrierungsformular an (nur für Admins)
     * @param user - leeres User-Objekt für Formularbindung
     */
    @GetMapping("/register")
    public String registerForm(Model model, @ModelAttribute User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Prüfen, ob der Benutzer eingeloggt ist
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"))) {
            return "redirect:/login"; // Anonyme Benutzer → Login-Seite
        }

        // Prüfen, ob der Benutzer Admin-Rechte hat
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean hasAdminRole = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().endsWith("ADMIN"));

        if (!hasAdminRole) {
            // Kein Admin → Logging + Weiterleitung zum Dashboard
            String username = authentication.getName();
            logService.log(username, messageService.getLog("register.user.missing.permissions"));
            return "redirect:/dashboard";
        }

        // Admin-Benutzer → Username und Rolle ins Model für die Anzeige
        String username = authentication.getName();
        model.addAttribute("username", username);

        String role = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("UNKNOWN");

        model.addAttribute("role", role);
        return "register"; // Thymeleaf-Template "register.html"
    }

    /*
     * POST /register
     * Registriert einen neuen Benutzer
     * @param user - vom Formular übergebenes User-Objekt
     */
    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute User user, Model model) {
        // Prüfen, ob Username bereits existiert
        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", messageService.getError("register.username.exists"));
            return "register"; // Formular erneut anzeigen mit Fehlermeldung
        }

        // Benutzer registrieren
        userService.register(user);
        return "redirect:/register"; // Weiterleitung zurück zum Registrierungsformular
    }

    /*
     * GET /logout
     * Loggt den aktuellen Benutzer aus und leitet zurück zur Login-Seite
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        request.logout(); // Spring Security Logout
        return "redirect:/login?logout"; // Weiterleitung mit Logout-Parameter
    }
}
