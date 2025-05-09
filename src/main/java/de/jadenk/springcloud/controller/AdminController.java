package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.model.Log;
import de.jadenk.springcloud.model.Role;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.LogRepository;
import de.jadenk.springcloud.repository.RoleRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Controller
public class AdminController {

    @Autowired
    private LogService logService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/admin")
    public String adminDashboard(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean hasAdminRole = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!hasAdminRole) {
            String username = authentication.getName();
            logService.log(username, "Access attempt to /admin denied due to lack of ADMIN role");
            return "redirect:/dashboard";
        }

        List<User> users = userRepository.findAll();
        List<Log> logs = logRepository.findAllLogsSortedByTimestamp();

        model.addAttribute("users", users);
        model.addAttribute("logs", logs);

        List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles);

        String username = authentication.getName();
        logService.log(username, "Access to /admin page");
        return "admin";
    }

    @PostMapping("/admin/user/update")
    public String updateUser(@RequestParam Long id,
                             @RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String role) {

        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        logService.log(currentUser.getUsername(), "Editing User: " + user.getUsername());

        if (!user.getUsername().equals(username)) {
            logService.log(currentUser.getUsername(), "Username changed for USER: '" + user.getUsername() + "' -> " + username);
            user.setUsername(username);
        }

        if (!user.getEmail().equals(email)) {
            logService.log(currentUser.getUsername(), "Email changed for USER: '" + user.getUsername() + "' -> " + email);
            user.setEmail(email);
        }

        Role foundRole = roleRepository.findByName(role).orElse(null);
        if (foundRole == null) {
            throw new IllegalArgumentException("Role not found: " + role);
        }

        Role currentRole = user.getRole().iterator().next();
        if (!currentRole.equals(foundRole)) {
            logService.log(currentUser.getUsername(), "Role Change for USER: '" + user.getUsername() + "' Role: " + currentRole.getName() + " -> " + foundRole.getName());
            user.getRole().clear();
            user.getRole().add(foundRole);
        }

        return "redirect:/admin";
    }


    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "user-list";
    }

}
