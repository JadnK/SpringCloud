package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.config.SecurityConfig;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.util.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@Controller
public class ChangePasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private MessageService messageService;

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String username = authentication.getName();
        model.addAttribute("username", username);

        String role = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("UNKNOWN");

        model.addAttribute("role", role);
        return "change-password";
    }

    @PostMapping("/change-password")
    public String handlePasswordChange(@RequestParam("currentPassword") String currentPassword,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!securityConfig.passwordEncoder().matches(currentPassword, user.getPassword())) {
            model.addAttribute("error", messageService.getError("changepassword.current.invalid"));
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", messageService.getError("changepassword.different.passwords"));
            return "change-password";
        }

        if (securityConfig.passwordEncoder().matches(newPassword, user.getPassword())) {
            model.addAttribute("error", messageService.getError("changepassword.same.password"));
            return "change-password";
        }

        user.setPassword(securityConfig.passwordEncoder().encode(newPassword));
        userRepository.save(user);

        return "redirect:/dashboard";
    }


}
