package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.config.SecurityConfig;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChangePasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfig securityConfig;

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
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
            model.addAttribute("error", "Current Password is wrong!");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "There are 2 different Passwords!");
            return "change-password";
        }

        if (securityConfig.passwordEncoder().matches(newPassword, user.getPassword())) {
            model.addAttribute("error", "You cant Use the same Password as before!");
            return "change-password";
        }

        user.setPassword(securityConfig.passwordEncoder().encode(newPassword));
        userRepository.save(user);

        return "redirect:/dashboard";
    }


}
