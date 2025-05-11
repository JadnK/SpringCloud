package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.service.LogService;
import de.jadenk.springcloud.service.UserService;
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

import java.util.Collection;

@Controller
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
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
            logService.log(username, "Access attempt to /register denied due to lack of ADMIN role");
            return "redirect:/dashboard";
        }

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

}
