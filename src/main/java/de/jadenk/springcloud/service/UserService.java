package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.Ban;
import de.jadenk.springcloud.model.Log;
import de.jadenk.springcloud.model.Role;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.RoleRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.util.MessageService;
import de.jadenk.springcloud.util.WebhookEvent;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private LogService logService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageService messageService;
    @Autowired
    private WebhookService webhookService;

    public boolean deleteUserById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        Role userRole = roleRepo.findById(1L).orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(userRole);
        userRepository.save(user);

        Log log = logService.log(user.getUsername(), messageService.getLog("register.success"));

        if (log == null) {
//            throw new RuntimeException("Log entry konnte nicht erstellt werden");
            log = new Log();
            log.setId(0L);
        }

        webhookService.triggerWebhookEvent(WebhookEvent.USER_UPDATED, "User " + user.getUsername() + "registerd someone.", log.getId());
    }

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public User getUserByName(String name) {
        return userRepository.findByUsername(name)
                .orElseThrow(() -> new EntityNotFoundException("User not found with name: " + name));
    }

    public void banUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanned(!user.isBanned());
        userRepository.save(user);
    }
}
