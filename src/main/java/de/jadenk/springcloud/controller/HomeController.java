package de.jadenk.springcloud.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /*
     * GET /
     * Root-Pfad der Anwendung.
     *
     * Funktion:
     * - Wenn der Benutzer eingeloggt ist, wird er automatisch zum Dashboard weitergeleitet.
     * - Wenn der Benutzer nicht eingeloggt ist, erfolgt eine Weiterleitung zur Login-Seite.
     */
    @GetMapping("/")
    public String rootRedirect(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard"; // eingeloggte Benutzer → Dashboard
        }
        return "redirect:/login"; // nicht eingeloggte Benutzer → Login
    }
}
