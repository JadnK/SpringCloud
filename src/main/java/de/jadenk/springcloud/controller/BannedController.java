package de.jadenk.springcloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/banned")
public class BannedController {

    /*
     * GET /banned
     * Zeigt die "banned"-Seite an, wenn ein Benutzer gesperrt wurde.
     * Einfaches Thymeleaf-Template "banned.html" wird zurückgegeben.
     */
    @GetMapping
    public String banned() {
        return "banned";
    }

}
