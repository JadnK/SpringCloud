package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.config.SecurityConfig;
import de.jadenk.springcloud.model.Log;
import de.jadenk.springcloud.model.Role;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.LogRepository;
import de.jadenk.springcloud.repository.RoleRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.LogService;
import de.jadenk.springcloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
    private SecurityConfig securityConfig;

    @Autowired
    private UserService userService;

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
    public String updateUser(@RequestParam("id") Long id,
                             @RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("role") String role,
                             @RequestParam(value = "password", required = false) String password) {

        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

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

        if (password != null && !password.trim().isEmpty()) {
            String encodedPassword = securityConfig.passwordEncoder().encode(password);
            user.setPassword(encodedPassword);
            logService.log(currentUser.getUsername(), "Password changed for USER: '" + user.getUsername() + "'");
        }

        userRepository.save(user);

        return "redirect:/admin";
    }

    @GetMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User userToDelete = userService.getUserById(id);
        boolean deleted = userService.deleteUserById(id);
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (deleted) {
            logService.log(currentUser.getUsername(), "Deleted User: '" + userToDelete.getUsername() + "'");
            redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found or could not be deleted.");
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/user/ban/{id}")
    public String banUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            userService.banUser(id);
            User user = userService.getUserById(id);
            String status = user.isBanned() ? "banned" : "unbanned";
            redirectAttributes.addFlashAttribute("success", "User wurde " + status + ".");
            logService.log(currentUser.getUsername(), "User: " + user.getUsername() + " was " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "User nicht gefunden.");
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
