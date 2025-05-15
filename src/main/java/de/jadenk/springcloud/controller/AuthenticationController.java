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
    private UserService userService;

    @Autowired
    private LogService logService;

    @Autowired
    private MessageService messageService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", messageService.getError("auth.invalid"));
        }
        return "login";
    }


    @GetMapping("/register")
    public String registerForm(Model model, @ModelAttribute User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"))) {
            return "redirect:/login";
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean hasAdminRole = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!hasAdminRole) {
            String username = authentication.getName();
            logService.log(username, messageService.getLog("register.user.missing.permissions"));
            return "redirect:/dashboard";
        }

        String username = authentication.getName();
        model.addAttribute("username", username);

        String role = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("UNKNOWN");

        model.addAttribute("role", role);
        return "register";
    }


    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute User user, Model model) {
        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", messageService.getError("register.username.exists"));
            return "register";
        }

        userService.register(user);
        return "redirect:/login";
    }


    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        request.logout();
        return "redirect:/login?logout";
    }

}
