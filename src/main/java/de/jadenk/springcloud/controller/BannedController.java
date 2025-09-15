package de.jadenk.springcloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BannedController {

    /*
     * GET /banned
     * Zeigt die "banned"-Seite an, wenn ein Benutzer gesperrt wurde.
     * Einfaches Thymeleaf-Template "banned.html" wird zurückgegeben.
     */
    @GetMapping("/banned")
    public String banned() {
        return "banned";
    }

}
