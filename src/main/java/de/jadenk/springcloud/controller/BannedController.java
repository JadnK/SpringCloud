package de.jadenk.springcloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BannedController {

    @GetMapping("/banned")
    public String banned() {
        return "banned";
    }

}
