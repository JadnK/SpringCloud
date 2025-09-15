package de.jadenk.springcloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LinkExpiredController {

    /*
     * GET /link-expired
     *
     * Zeigt die Seite an, die erscheint, wenn ein Link (z.B. Passwort-Reset oder Einladung) abgelaufen ist.
     *
     * Funktion:
     * - Lädt einfach das Thymeleaf-Template "link-expire.html".
     * - Keine Authentifizierung oder Logik notwendig, da es sich um eine informative Seite handelt.
     */
    @GetMapping("/link-expired")
    public String linkExpiredDashboard(Model model) {
        return "link-expire"; // Thymeleaf Template für abgelaufene Links
    }

}
