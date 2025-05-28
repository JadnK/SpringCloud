package de.jadenk.springcloud.service;

import de.jadenk.springcloud.exception.CustomRuntimeException;
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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private byte[] getDefaultProfileImage() {
        try (InputStream is = getClass().getResourceAsStream("/static/images/template.png")) {
            if (is == null) {
                throw new CustomRuntimeException("[User Service] Default profile image not found.");
            }

            BufferedImage originalImage = ImageIO.read(is);

            int targetWidth = 64;
            int targetHeight = 64;

            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new CustomRuntimeException("[User Service] Failed to load default profile image.");
        }
    }

    public void register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        Role userRole = roleRepo.findById(1L).orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(userRole);
        user.setProfileImageData(getDefaultProfileImage());
        userRepository.save(user);

        Log log = logService.log(user.getUsername(), messageService.getLog("register.success"));

        if (log == null) {
//            throw new RuntimeException("Log entry konnte nicht erstellt werden");
            log = new Log();
            log.setId(0L);
        }

        webhookService.triggerWebhookEvent(WebhookEvent.USER_REGISTERED, "User " + user.getUsername() + " registerd someone.", log.getId());
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
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomRuntimeException("[User Service] User not found with id " + userId));
        user.setBanned(!user.isBanned());
        userRepository.save(user);
    }
}
